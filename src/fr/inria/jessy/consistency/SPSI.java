package fr.inria.jessy.consistency;

import fr.inria.jessy.communication.JessyGroupManager;
import fr.inria.jessy.store.DataStore;
import fr.inria.jessy.transaction.ExecutionHistory;
import fr.inria.jessy.transaction.TransactionTouchedKeys;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.fractal.utils.CollectionUtils;

import org.apache.log4j.Logger;

import static fr.inria.jessy.transaction.ExecutionHistory.TransactionType.READONLY_TRANSACTION;

public abstract class SPSI extends Consistency {
    /**
     * This constant should be used as the key to mark a transaction as SER or PSI.
     * <p>
     * Every SSERPSI transaction should be marked as SER or PSI. A transaction marked as SER behaves like a
     * transaction executed under serializability. On the other hand a transaction marked as PSI behaves like a
     * transaction executed under parallel snapshot isolation.
     */
    public static final String LEVEL = SPSI.class.getName() + "::LEVEL";

    /**
     * This constant should be used to mark a transaction as PSI.
     * <p>
     * Every SSERPSI transaction should be marked as SER or PSI. A transaction marked as SER behaves like a
     * transaction executed under serializability. On the other hand a transaction marked as PSI behaves like a
     * transaction executed under parallel snapshot isolation.
     */
    public static final Integer PSI = 0;

    /**
     * This constant should be used to mark a transaction as SER.
     * <p>
     * Every SSERPSI transaction should be marked as SER or PSI. A transaction marked as SER behaves like a
     * transaction executed under serializability. On the other hand a transaction marked as PSI behaves like a
     * transaction executed under parallel snapshot isolation.
     */
    public static final Integer SER = 1;

    /**
     * Log information.
     */
    protected static Logger logger = Logger.getLogger(SPSI.class);

    /**
     * Create a new instance of SSERPSI consistency level.
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
     * model.
     *
     * @param h1 The first history to check.
     * @param h2 The second history to check.
     * @return true if the two transactions commute.
     */
    @Override
    public boolean certificationCommute(ExecutionHistory h1, ExecutionHistory h2) {
        // We do a fine grained commutativity check.

        // If one of the two transactions is marked as serializable we check for read-write and write-write conflicts.
        // Since rs is a superset of ws (we always read an object before write), the check for read-write conflicts
        // includes write-write conflicts.
        if (h1.getExtra(LEVEL) == SER || h2.getExtra(LEVEL) == SER) {
            if (h1.getReadSet() != null && h2.getWriteSet() != null) {
                // The implementation of the method isIntersectingWith takes every element of the first set and
                // checks if it is contained in the second set. The smaller is the first set, the faster we are.
                // Write sets are tipically much smaller than read sets, so we supply the write set as first argument.
                if (CollectionUtils.isIntersectingWith(h2.getWriteSet().getKeys(), h1.getReadSet().getKeys()))
                    return false;
            }

            if (h1.getWriteSet() != null && h2.getWriteSet() != null) {
                if (CollectionUtils.isIntersectingWith(h1.getWriteSet().getKeys(), h2.getReadSet().getKeys()))
                    return false;
            }
        } else { // LEVEL = PSI
            // If a transaction is marked as PSI we guarantee global PSI and SI per replica.
            if (h1.getWriteSet() != null && h2.getWriteSet() != null) {
                if (CollectionUtils.isIntersectingWith(h1.getWriteSet().getKeys(), h2.getWriteSet().getKeys()))
                    return false;
            }
        }

        return true;
    }

    /**
     * When a transaction ends it's submitted for certification. Submitted transactions are put in a queue and then
     * certified one at a time. This way transaction Ti has to wait transactions T0, T1, ..., Ti-1 to be certified in
     * order to start its certification. To minimize this convoy effect we define the concept of commutation. Two
     * transactions commute if they can be certified in parallel. In general this is up to the specific consistency
     * model.
     *
     * @param k1 The first set of keys to check.
     * @param k2 The second set of keys to check.
     * @return true if the two transactions commute.
     */
    @Override
    public boolean certificationCommute(TransactionTouchedKeys k1, TransactionTouchedKeys k2) {
        // Messages can be delivered out of order if the two write sets do not intersect. This is ok because if two
        // transactions do commute (ww-conflict free) then it doesn't matter if their messages are delivered out of
        // order.
        return !CollectionUtils.isIntersectingWith(k1.writeKeys, k2.writeKeys);
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
     * In the case of SSERPSI concistency model we should only distinguish between PSI transaction and SER transactions.
     *
     * @param h      The execution history.
     * @param target The target.
     * @return The set of concerning keys for that target.
     */
    @Override
    public Set<String> getConcerningKeys(ExecutionHistory h, ConcernedKeysTarget target) {
        Set<String> concerningKeys = new HashSet<>();

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
