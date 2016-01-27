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
        // TODO: Not only a transaction marked as SER should execute in a serializable fashion. Two transaction T1
        // TODO: and T2 executing on the same machine should be "implicitly" serializable (meaning that they should be
        // TODO: serializable even if they are marked as PSI).
        // TODO: Or, better, the projection of a transaction on a single shard should be serializable, meaning that
        // TODO: the operations of a transaction made on a single machine should be serializable.
        if (isMarkedSerializable(h1) || isMarkedSerializable(h2)) {
            // If one of the two transaction was marked as serializable, then the write sets of the two transaction
            // must be disjoint; also the one's read set must not intersect with the other's write set.

            // TODO: In a comment in PSI consistency they state that if the group size is greater than one, the two
            // TODO: transaction cannot commute under any condition. Then,
            // TODO: if (manager.getMyGroup().size() > 1) return false;

            // TODO: Here we check for write-write intersections, conversely of what they did in SER. Is their
            // TODO: implementation incomplete? It probably is, considering the TODO comment they wrote above.
            // TODO: Thinking about it, probably it is not the case, i.e. blind writes.

            // The two transaction's write sets should be disjoint.
            if (h1.getWriteSet() != null && h2.getWriteSet() != null) {
                if (CollectionUtils.isIntersectingWith(h1.getWriteSet().getKeys(), h2.getWriteSet().getKeys()))
                    return false;
            }

            // Check h1's read set against h2's write set.
            if (h1.getReadSet() != null && h2.getWriteSet() != null) {
                if (CollectionUtils.isIntersectingWith(h1.getReadSet().getKeys(), h2.getWriteSet().getKeys()))
                    return false;
            }

            // Then, do the opposite: check h2's read set against h1's write set.
            if (h2.getReadSet() != null && h1.getWriteSet() != null) {
                if (CollectionUtils.isIntersectingWith(h2.getReadSet().getKeys(), h1.getWriteSet().getKeys()))
                    return false;
            }

            return true;

            // TODO: The stuff above works (I hope) if the group is composed by only one replica. A thing like this,
            // TODO: if (manager.getMyGroup().size() > 1) return false;
            // TODO: may be more efficient. This may be what the first TODO comment was about.
            // Always return false to guarantee a global order within replicas on certification.
            //return false;
        } else {
            // If a transaction is not marked as serializable (and then it executes under PSI), it commutes with
            // another transaction only if the two write sets are disjoint.
            if (CollectionUtils.isIntersectingWith(h1.getWriteSet().getKeys(), h2.getWriteSet().getKeys()))
                return false;

            return true;
        }
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
        // TODO: No way to determine if a transaction is marked as serializable or not using TransactionTouchedKeys: to
        // TODO: be sure, and not getting anomalies that we do not want, we should always return false.
        // TODO: But
        // TODO: If the two transaction commute under SER they also commute under PSI. If we use the SER criterion
        // TODO: here, we probably gain a boost since two read only transaction commute.
        // TODO: Also,
        // TODO: if (manager.getMyGroup().size() > 1) return false;

//        // The two write sets should be disjoint.
//        if (CollectionUtils.isIntersectingWith(k1.writeKeys, k2.writeKeys))
//            return false;
//
//        // Check k1's read set against k2's write set.
//        if (CollectionUtils.isIntersectingWith(k1.readKeys, k2.writeKeys))
//            return false;
//
//        // Then, do the opposite: check k2's read set against k1's write set.
//        if (CollectionUtils.isIntersectingWith(k2.readKeys, k1.writeKeys))
//            return false;
//
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
     * @param h The execution history.
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

            if (destGroups.size() == 1 && h.getTransactionType() == READONLY_TRANSACTION)
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
