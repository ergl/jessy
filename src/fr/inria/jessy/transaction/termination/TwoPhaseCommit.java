package fr.inria.jessy.transaction.termination;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import net.sourceforge.fractal.membership.Group;
import fr.inria.jessy.communication.message.TerminateTransactionRequestMessage;
import fr.inria.jessy.communication.message.VoteMessage;
import fr.inria.jessy.consistency.Consistency.ConcernedKeysTarget;
import fr.inria.jessy.partitioner.Partitioner;
import fr.inria.jessy.transaction.ExecutionHistory;
import fr.inria.jessy.transaction.TransactionHandler;
import fr.inria.jessy.transaction.TransactionState;
import fr.inria.jessy.transaction.termination.vote.ProcessVotingQuorum;
import fr.inria.jessy.transaction.termination.vote.Vote;
import fr.inria.jessy.transaction.termination.vote.VotingQuorum;

public class TwoPhaseCommit extends AtomicCommit {

	String swid=""+jessy.manager.getSourceId();
	
	public TwoPhaseCommit(DistributedTermination termination) {
		super(termination);
	}

	@Override
	public boolean proceedToCertifyAndVote(
			TerminateTransactionRequestMessage msg) {
		synchronized(atomicDeliveredMessages){
			for (TerminateTransactionRequestMessage n : atomicDeliveredMessages) {
				if (n.equals(msg)) {
					continue;
				}
				if (!jessy.getConsistency().certificationCommute(
						n.getExecutionHistory(), msg.getExecutionHistory())) {
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public void setVoters(TerminateTransactionRequestMessage msg ,Set<String> voteReceivers, AtomicBoolean voteReceiver, Set<String> voteSenders, AtomicBoolean voteSender){

		/*
		 * In 2pc, those that receive the transaction, always participate in voting.
		 * Hence, vote sender is always true.
		 */
		voteSender.set(true);
		
		if (isCoordinator(msg)){
			
			/*
			 * If this is coordinator, wait for the votes from every body.
			 */
			Set<String> keys_for_SendVotes =jessy
					.getConsistency().getConcerningKeys(
							msg.getExecutionHistory(),
							ConcernedKeysTarget.SEND_VOTES);
			
			Group g;
			for (String str:keys_for_SendVotes){
				g=jessy.partitioner.resolve(str);
				for (int tmpswid:g.allNodes()){
					voteSenders.add(""+tmpswid);
				}
			}
			
			/*
			 * If this is the coordinator, send the vote to only coordinator
			 */
			voteReceivers.add(swid);
			voteReceiver.set(true);
		}
		else{
			/*
			 *If this is not the coordinator, it needs to send vote to the coordinator, and receive vote from coordinator.  
			 */
			voteSenders.add( getCoordinatorId(msg.getExecutionHistory(), jessy.partitioner));
			if (jessy.partitioner.resolveNames(jessy
					.getConsistency().getConcerningKeys(
							msg.getExecutionHistory(),
							ConcernedKeysTarget.RECEIVE_VOTES)).contains(group))
			{

				voteReceivers =new HashSet<String>();
				voteReceivers.add( getCoordinatorId(msg.getExecutionHistory(), jessy.partitioner));
				voteReceiver.set(true);
			}

		}
		
	}

	@Override
	public void sendVote(VoteMessage voteMessage,
			TerminateTransactionRequestMessage msg) {
		String firstWriteKey=msg.getExecutionHistory().getWriteSet().getKeys().iterator().next();
		voteMessage.getVote().setVoterEntityName(swid);
		voteMulticast.sendVote(voteMessage, Integer.parseInt(getCoordinatorId(msg.getExecutionHistory(), jessy.manager.getPartitioner())), "");
	}
	
	@Override
	public void quorumReached(TerminateTransactionRequestMessage msg,TransactionState state){
		if (isCoordinator(msg)){
			
			Set<String> voteReceivers=	jessy.partitioner.resolveNames(jessy
					.getConsistency().getConcerningKeys(
							msg.getExecutionHistory(),
							ConcernedKeysTarget.RECEIVE_VOTES));
			voteReceivers.remove(swid);
			boolean committed=(state==TransactionState.COMMITTED) ? true : false;
			Vote vote=new Vote(msg.getExecutionHistory().getTransactionHandler(), committed , swid, null);
			VoteMessage voteMsg = new VoteMessage(vote, voteReceivers,
					group.name(), jessy.manager.getSourceId());
			
			/*
			 * Send the final vote to every replica, and also to the client.
			 * Note that here, the coordinator is the client.  
			 */
			voteMulticast.sendVote(voteMsg, msg.getExecutionHistory().isCertifyAtCoordinator(), msg.getExecutionHistory().getCoordinatorSwid(), msg.getExecutionHistory().getCoordinatorHost());
		}
		
	}
	
	private boolean isCoordinator(TerminateTransactionRequestMessage msg){
		String firstWriteKey=msg.getExecutionHistory().getWriteSet().getKeys().iterator().next();
		if (jessy.partitioner.resolve(firstWriteKey).leader() == jessy.manager.getSourceId()){
			System.out.println("Coordinator of " + msg.getExecutionHistory().getTransactionHandler().getId() + " is " + swid);
			return true;
		}
		else{
			return false;
		}
	}

	@Override
	public VotingQuorum getNewVotingQuorum(TransactionHandler th) {
		return new ProcessVotingQuorum(th);
	}
	
	public static String getCoordinatorId(ExecutionHistory executionHistory, Partitioner partitioner){
		String firstWriteKey=executionHistory.getWriteSet().getKeys().iterator().next();
		return "" + partitioner.resolve(firstWriteKey).leader();
	}

}