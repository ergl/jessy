package fr.inria.jessy.protocol;

import fr.inria.jessy.ConstantPool;
import fr.inria.jessy.communication.JessyGroupManager;
import fr.inria.jessy.communication.message.TerminateTransactionRequestMessage;
import fr.inria.jessy.store.DataStore;
import fr.inria.jessy.transaction.ExecutionHistory;
import fr.inria.jessy.transaction.TransactionState;
import fr.inria.jessy.transaction.termination.TwoPhaseCommit;
import fr.inria.jessy.transaction.termination.vote.Vote;
import fr.inria.jessy.transaction.termination.vote.VotePiggyback;
import fr.inria.jessy.vector.PartitionDependenceVector;

import java.util.Set;

import static fr.inria.jessy.ConstantPool.PROTOCOL_ATOMIC_COMMIT;

public class SPSI_PDV_2PC extends SPSI_PDV_GC {
    static {
        PROTOCOL_ATOMIC_COMMIT = ConstantPool.ATOMIC_COMMIT_TYPE.TWO_PHASE_COMMIT;
    }

    public SPSI_PDV_2PC(JessyGroupManager m, DataStore s) {
        super(m, s);
    }

    @Override
    public Set<String> getVotersToJessyProxy(Set<String> terminationReceivers, ExecutionHistory history) {
        // Coordinator needs to only wait for the vote from the 2PC manager.
        terminationReceivers.clear();
        terminationReceivers.add(TwoPhaseCommit.getCoordinatorId(history, manager.getPartitioner()));
        return terminationReceivers;
    }

    @Override
    public void quorumReached(TerminateTransactionRequestMessage msg, TransactionState state, Vote vote) {
        // Transaction manager needs to collect all votes, computes the final vector, and send it to everybody.
        PartitionDependenceVector<String> commitVC = receivedVectors.get(vote.getTransactionHandler().getId());
        vote.setVotePiggyBack(new VotePiggyback(commitVC));
    }

    @Override
    public void voteReceived(Vote vote) {
        // If vote.getVotePiggyBack() is null, it means that it is preemptively aborted in DistributedTermination, and
        // DistributedTermination sets votePiggyback to null.
        if (vote.getVotePiggyBack() == null)
            return;

        super.voteReceived(vote);
    }
}
