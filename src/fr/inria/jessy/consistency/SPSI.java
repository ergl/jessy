package fr.inria.jessy.consistency;

import static fr.inria.jessy.transaction.ExecutionHistory.TransactionType.READONLY_TRANSACTION;

import fr.inria.jessy.communication.JessyGroupManager;
import fr.inria.jessy.store.DataStore;
import fr.inria.jessy.transaction.ExecutionHistory;
import fr.inria.jessy.transaction.TransactionTouchedKeys;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.fractal.utils.CollectionUtils;

import org.apache.log4j.Logger;

public abstract class SPSI extends Consistency {
    /**
     * This constant should be used as the key to mark a transaction as SER or PSI.
     * <p>
     * Every SPSI transaction should be marked as SER or PSI. A transaction marked as SER behaves like a
     * transaction executed under serializability. On the other hand a transaction marked as PSI behaves like a
     * transaction executed under parallel snapshot isolation.
     */
    public static final String LEVEL = SPSI.class.getName() + "::LEVEL";

    /**
     * This constant should be used to mark a transaction as PSI.
     * <p>
     * Every SPSI transaction should be marked as SER or PSI. A transaction marked as SER behaves like a
     * transaction executed under serializability. On the other hand a transaction marked as PSI behaves like a
     * transaction executed under parallel snapshot isolation.
     */
    public static final Integer PSI = 0;

    /**
     * This constant should be used to mark a transaction as SER.
     * <p>
     * Every SPSI transaction should be marked as SER or PSI. A transaction marked as SER behaves like a
     * transaction executed under serializability. On the other hand a transaction marked as PSI behaves like a
     * transaction executed under parallel snapshot isolation.
     */
    public static final Integer SER = 1;

    /**
     * Log information.
     */
    protected static Logger logger = Logger.getLogger(SPSI.class);

    /**
     * Create a new instance of SPSI consistency level.
     *
     * @param m The group manager.
     * @param s The data store instance.
     */
    public SPSI(JessyGroupManager m, DataStore s) {
        super(m, s);
    }

    /**
     * When a transaction ends it's submitted for certification. Submitted transactions are put in a queue and then
     * certified one at a time. This way transaction Ti has to wait transactions T0, T1, ..., Ti-1 to be certified in
     * order to start its certification. To minimize this convoy effect we define the concept of commutation. Two
     * transactions commute if they can be certified in parallel. In general this is up to the specific consistency
     * model. SPSI merge together the commutation algorithms of NMSI and SER.
     *
     * @param h1 The first history to check.
     * @param h2 The second history to check.
     * @return true if the two transactions commute.
     */
    @Override
    public boolean certificationCommute(ExecutionHistory h1, ExecutionHistory h2) {
        // Check h1's write set against h2's read set.
        if (h1.getWriteSet() != null && h2.getReadSet() != null) {
            // Usually the read set of a transaction is far bigger than its write set. Passing the write set as first
            // argument to the method isIntersectingWith() gives a huge boost in performances: the implementation
            // checks if every element of the first set is contained in the second.
            if (CollectionUtils.isIntersectingWith(h1.getWriteSet().getKeys(), h2.getReadSet().getKeys()))
                return false;
        }

        // Then, do the opposite: check h2's write set against h1's read set.
        if (h1.getReadSet() != null && h2.getWriteSet() != null) {
            if (CollectionUtils.isIntersectingWith(h2.getWriteSet().getKeys(), h1.getReadSet().getKeys()))
                return false;
        }

        // No need to check ww conflicts because rs is a superset of ws.
        return true;
    }

    /**
     * When a transaction ends it's submitted for certification. Submitted transactions are put in a queue and then
     * certified one at a time. This way transaction Ti has to wait transactions T0, T1, ..., Ti-1 to be certified in
     * order to start its certification. To minimize this convoy effect we define the concept of commutation. Two
     * transactions commute if they can be certified in parallel. In general this is up to the specific consistency
     * model. SPSI merge together the commutation algorithms of NMSI and SER.
     *
     * @param k1 The first set of keys to check.
     * @param k2 The second set of keys to check.
     * @return true if the two transactions commute.
     */
    @Override
    public boolean certificationCommute(TransactionTouchedKeys k1, TransactionTouchedKeys k2) {
//        // Check k1's write set against k2's read set.
//        // Usually the read set of a transaction is far bigger than its write set. Passing the write set as first
//        // argument to the method isIntersectingWith() gives a huge boost in performances: the implementation
//        // checks if every element of the first set is contained in the second.
//        if (CollectionUtils.isIntersectingWith(k1.writeKeys, k2.readKeys))
//            return false;
//
//        // Then, do the opposite: check k2's write set against k1's read set.
//        if (CollectionUtils.isIntersectingWith(k2.writeKeys, k1.readKeys))
//            return false;
//
//        // No need to check ww conflicts because rs is a superset of ws.
//        return true;
        return false;
    }

    /**
     * Concerning keys is a broad concept. The {@code target} parameter determines the meaning of this term. There are
     * three possible values for the {@code target} parameter: {@link ConcernedKeysTarget#TERMINATION_CAST},
     * {@link ConcernedKeysTarget#SEND_VOTES} and {@link ConcernedKeysTarget#RECEIVE_VOTES}. If {@code target}'s value
     * is {@code TERMINATION_CAST} what the caller is really asking is what are the replicas that should be warned of
     * the termination of the transaction. If {@code target}'s value is {@code SEND_VOTES} the caller is asking for
     * the replicas that should send a certification vote. If {@code target}'s value is {@code RECEIVE_VOTES} the caller
     * is interested in what are the replicas that should receive a certification vote. In every case we return a set
     * of keys and the caller will get the set of Jessy instances replicating the keys.
     * <p>
     * In the case of SPSI concistency model we should only distinguish between PSI transaction and SER transactions.
     *
     * @param h      The execution history.
     * @param target The target.
     * @return The set of concerning keys for that target.
     */
    @Override
    public Set<String> getConcerningKeys(ExecutionHistory h, ConcernedKeysTarget target) {
        Set<String> concerningKeys = new HashSet<>(getConcerningKeysSize(h, target));

        if (isMarkedSerializable(h)) {
            // If a transaction is marked as serializable, its concerning keys set is the union of keys of read set,
            // write set, and create set.

            if (target != ConcernedKeysTarget.RECEIVE_VOTES) {
                if (h.getReadSet() != null)
                    concerningKeys.addAll(h.getReadSet().getKeys());
            }

            if (h.getWriteSet() != null)
                concerningKeys.addAll(h.getWriteSet().getKeys());

            if (h.getCreateSet() != null)
                concerningKeys.addAll(h.getCreateSet().getKeys());

            Set<String> destGroups = manager.getPartitioner().resolveNames(concerningKeys);

            if (h.getTransactionType() == READONLY_TRANSACTION && destGroups.size() == 1)
                concerningKeys.clear();
        } else {
            // If a transaction is not marked as serializable (and then it executes under PSI), its concerning keys
            // set is the union of write and create sets only.
            if (h.getWriteSet() != null)
                concerningKeys.addAll(h.getWriteSet().getKeys());

            if (h.getCreateSet() != null)
                concerningKeys.addAll(h.getCreateSet().getKeys());
        }

        return concerningKeys;
    }

    private int getConcerningKeysSize(ExecutionHistory h, ConcernedKeysTarget target) {
        int size = 0;

        if (isMarkedSerializable(h)) {
            if (target != ConcernedKeysTarget.RECEIVE_VOTES) {
                if (h.getReadSet() != null)
                    size += h.getReadSet().getKeys().size();
            }

            if (h.getWriteSet() != null)
                size += h.getWriteSet().getKeys().size();

            if (h.getCreateSet() != null)
                size += h.getCreateSet().getKeys().size();

        } else {
            if (h.getWriteSet() != null)
                size += h.getWriteSet().getKeys().size();

            if (h.getCreateSet() != null)
               size += h.getCreateSet().getKeys().size();
        }

        return size;
    }

    /**
     * Return true if the transaction this execution history belongs to has been marked as SER.
     *
     * @param history The execution history.
     * @return true if the transaction's level is SER.
     */
    public boolean isMarkedSerializable(ExecutionHistory history) {
        Integer level = (Integer) history.getExtra(LEVEL);
        return level != null && level.equals(SER);
    }
}
