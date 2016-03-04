package fr.inria.jessy.transaction;

import fr.inria.jessy.store.ReadRequest;
import java.io.FileInputStream;
import java.util.*;
import java.util.concurrent.Callable;

import net.sourceforge.fractal.utils.PerformanceProbe.SimpleCounter;
import net.sourceforge.fractal.utils.PerformanceProbe.ValueRecorder;

import org.apache.log4j.Logger;

import fr.inria.jessy.ConstantPool;
import fr.inria.jessy.DebuggingFlag;
import fr.inria.jessy.Jessy;
import fr.inria.jessy.store.JessyEntity;
import fr.inria.jessy.store.ReadRequestKey;

/**
 * This class is the interface for transactional execution on top of Jessy.
 */
public abstract class Transaction implements Callable<ExecutionHistory> {
    private static Logger logger = Logger.getLogger(Transaction.class);
    private static boolean retryCommitOnAbort;
    public static SimpleCounter totalCount = new SimpleCounter(
            "Transaction#TotalTransactions");
    private static ValueRecorder transactionExecutionTime_ReadOlny;
    private static ValueRecorder transactionExecutionTime_Update;
    private static ValueRecorder transactionReadOperatinTime;
    private static ValueRecorder transactionTerminationTime_ReadOnly;
    private static ValueRecorder transactionTerminationTime_Update;

    static {
        // Performance measuring facilities

        transactionExecutionTime_ReadOlny = new ValueRecorder(
                "Transaction#transactionExecutionTime_ReadOlny(ms)");
        transactionExecutionTime_ReadOlny.setFormat("%a");

        transactionExecutionTime_Update = new ValueRecorder(
                "Transaction#transactionExecutionTime_Update(ms)");
        transactionExecutionTime_Update.setFormat("%a");

        transactionTerminationTime_ReadOnly = new ValueRecorder(
                "Transaction#transactionTerminationTime_ReadOnly(ms)");
        transactionTerminationTime_ReadOnly.setFormat("%a");

        transactionTerminationTime_Update = new ValueRecorder(
                "Transaction#transactionTerminationTime_Update(ms)");
        transactionTerminationTime_Update.setFormat("%a");

        transactionReadOperatinTime = new ValueRecorder(
                "Transaction#transactionReadOperatinTime(ms)");
        transactionReadOperatinTime.setFormat("%a");

        retryCommitOnAbort = readConfig();
    }

    /**
     * Start time of the execution phase in nanoseconds.
     * <p>
     * If the transaction aborts, the resulting transaction will again calculate the execution phase.
     * This is for the sake of simplicity and should not change the result.
     */
    private long executionStartTime;
    private boolean isQuery;
    private Jessy jessy;
    /**
     * If zero, it means the commit is for the main transaction.
     * If greater than zero, it means the main transaction aborts, and this commit is for the resulting transaction.
     * This variable is crucial to compute the correct termination latency. In other words, termination latency is
     * only computed for
     * the main transaction inside the commit method.
     */
    private int mainTransactionCommit = 0;
    /**
     * Start time of the termination phase in nanoseconds.
     * <p>
     * If the transaction aborts, the resulting transaction will NOT calculate the termination phase of the resulting
     * transaction.
     * The termination latency is only calculated for the main transaction from the starting of its termination until
     * it commits when
     * {@code Transaction#mainTransactionCommit} is zero.
     */
    private long terminationStartTime;
    private TransactionHandler transactionHandler;

    public Transaction(Jessy jessy) throws Exception {
        this.jessy = jessy;
        this.transactionHandler = jessy.startTransaction();
        this.isQuery = true;
        executionStartTime = System.currentTimeMillis();
        totalCount.incr();
    }

    public Transaction(Jessy jessy, int readOperations, int updateOperations, int createOperations) throws Exception {
        this.jessy = jessy;
        this.transactionHandler = jessy.startTransaction(readOperations, updateOperations, createOperations);
        this.isQuery = true;
        executionStartTime = System.currentTimeMillis();
        totalCount.incr();
    }

    public ExecutionHistory abortTransaction() {
        return jessy.abortTransaction(transactionHandler);
    }

    public ExecutionHistory call() {
        return execute();
    }

    /*
     * Tries to commit a transaction. If commit is not successful, and it is
     * defined to retryCommitOnAbort, it will do so, until it commits the
     * transaction.
     *
     */
    public ExecutionHistory commitTransaction() {
        if (mainTransactionCommit == 0) {
            if (DebuggingFlag.TRANSACTION)
                logger.debug("Start committing " + this.getTransactionHandler().toString());
            terminationStartTime = System.currentTimeMillis();
        }

        mainTransactionCommit++;

        if (isQuery)
            transactionExecutionTime_ReadOlny.add(System.currentTimeMillis()
                    - executionStartTime);
        else {
            transactionExecutionTime_Update.add(System.currentTimeMillis()
                    - executionStartTime);
        }

        ExecutionHistory executionHistory = jessy
                .commitTransaction(transactionHandler);

        if (executionHistory.getTransactionState() != TransactionState.COMMITTED
                && retryCommitOnAbort) {

            try {

                if (executionHistory.getTransactionState() == TransactionState.ABORTED_BY_TIMEOUT && DebuggingFlag
                        .TRANSACTION)
                    logger.warn("Re-executing aborted "
                            + (isQuery ? "(query)" : "") + " transaction "
                            + executionHistory.getTransactionHandler() + " . Reason : " + executionHistory
                            .getTransactionState());

				/*
                 * Garbage collect the older execution. We do not need it
				 * anymore.
				 */
                jessy.garbageCollectTransaction(transactionHandler);

				/*
				 * must have a new handler.
				 */
                TransactionHandler oldHanlder = this.transactionHandler.clone();
                this.transactionHandler = jessy.startTransaction();
                if (executionHistory.getTransactionState() == TransactionState.ABORTED_BY_TIMEOUT)
                    this.transactionHandler.setPreviousTimedoutTransactionHandler(oldHanlder);
                reInitProbes();
                mainTransactionCommit++;
                executionHistory = execute();
                mainTransactionCommit--;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        jessy.garbageCollectTransaction(transactionHandler);
        mainTransactionCommit--;
        if (mainTransactionCommit == 0) {
            if (DebuggingFlag.TRANSACTION)
                logger.debug("Finished committing " + this.getTransactionHandler().toString());
            if (isQuery)
                transactionTerminationTime_ReadOnly.add(System.currentTimeMillis()
                        - terminationStartTime);
            else
                transactionTerminationTime_Update.add(System.currentTimeMillis()
                        - terminationStartTime);
        }

        return executionHistory;
    }

    public <E extends JessyEntity> void create(E entity) {
        entity.setPrimaryKey(null);

        jessy.create(transactionHandler, entity);
        logger.info("entity " + entity.getKey() + " is created");
    }

    /**
     * Execute the transaction logic.
     */
    public abstract ExecutionHistory execute();

    /**
     * Get extra information set for this transaction.
     * <p>
     * Extra information is put like in a key-value store. To retrieve an information you put before you need its key.
     *
     * @param key The key assigned to the extra.
     * @return The extra.
     */
    public Object getExtra(String key) {
        return transactionHandler.getExtra(key);
    }

    public static boolean getRetryCommitOnAbort() {
        return Transaction.retryCommitOnAbort;
    }

    public TransactionHandler getTransactionHandler() {
        return transactionHandler;
    }

    public boolean isRetryCommitOnAbort() {
        return retryCommitOnAbort;
    }

    /**
     * Put extra information in this transaction.
     * <p>
     * Information is put in a key-value store. You can later get the information you put using
     * {@link Transaction#getExtra(String)}.
     *
     * @param extras A map containing a set of information.
     */
    public void putAllExtras(Map<String, Object> extras) {
        transactionHandler.putAllExtras(extras);
    }

    /**
     * Put one extra information in this transaction.
     * <p>
     * Information is put in a key-value store. You can later get the information you put using
     * {@link Transaction#getExtra(String)}.
     *
     * @param key   The key assigned to the extra.
     * @param value The value of the extra.
     */
    public void putExtra(String key, Object value) {
        transactionHandler.putExtra(key, value);
    }

    /**
     * Since the transaction can abort, and re-executing the aborted transaction is performed inside the {@code
     * Transaction#commitTransaction()}
     * we need to re set all the probes. Otherwise, the execution latency is not accurate since the start time is the
     * very beginning of the transaction.
     */
    private void reInitProbes() {
        executionStartTime = System.currentTimeMillis();
    }

    /**
     * Performs a transactional read on top of Jessy.
     *
     * @param <E>         The type of the entity needed to be read.
     * @param entityClass The class of the entity needed to be read.
     * @param keyValue    The key of the entity needed to be read.
     * @return The read entity from jessy.
     * @throws Exception
     */
    public <E extends JessyEntity> E checkRead(Class<E> entityClass, String keyValue, boolean fail)
            throws Exception {
        long start = System.currentTimeMillis();

        E entity = jessy.checkRead(transactionHandler, entityClass, keyValue, fail);

        transactionReadOperatinTime.add(System.currentTimeMillis() - start);
        return entity;
    }

    public <E extends JessyEntity> E read(Class<E> entityClass, String keyValue)
            throws Exception {
        return checkRead(entityClass, keyValue, true);
    }

    public <E extends JessyEntity> Collection<E> checkRead(Class<E> entityClass, List<ReadRequestKey<?>> keys,
                                                           boolean fail) throws Exception {
        return jessy.checkRead(transactionHandler, entityClass, keys, fail);
    }

    public <E extends JessyEntity> Collection<E> read(Class<E> entityClass, List<ReadRequestKey<?>> keys)
            throws Exception {
        return checkRead(entityClass, keys, true);
    }

    public <E extends JessyEntity, V> Collection<E> readBySecondary(Class<E> clazz, String key, V value)
        throws Exception {
        return readBySecondary(clazz, new ReadRequestKey<V>(key, value));
    }

    public <E extends JessyEntity> Collection<E> readBySecondary(Class<E> clazz, ReadRequestKey<?> key)
            throws Exception {
        return jessy.readBySecondary(transactionHandler, clazz, key);
    }

    private static boolean readConfig() {
        try {
            Properties myProps = new Properties();
            FileInputStream MyInputStream = new FileInputStream(
                    ConstantPool.CONFIG_PROPERTY);
            myProps.load(MyInputStream);
            return myProps.getProperty(ConstantPool.RETRY_COMMIT)
                    .equals("true") ? true : false;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return true;
    }

    public static void setRetryCommitOnAbort(boolean retryCommitOnAbort) {
        Transaction.retryCommitOnAbort = retryCommitOnAbort;
    }

    public <E extends JessyEntity> void write(E entity)
            throws NullPointerException {
        entity.setPrimaryKey(null);

        try {
            jessy.write(transactionHandler, entity);
            isQuery = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
