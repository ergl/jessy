package fr.inria.jessy.transaction.termination;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import net.sourceforge.fractal.FractalManager;
import net.sourceforge.fractal.Learner;
import net.sourceforge.fractal.Stream;
import net.sourceforge.fractal.membership.Membership;
import net.sourceforge.fractal.rmcast.RMCastStream;
import net.sourceforge.fractal.rmcast.WanMessage;
import net.sourceforge.fractal.wanamcast.WanAMCastStream;
import utils.ExecutorPool;
import fr.inria.jessy.ConstantPool;
import fr.inria.jessy.Jessy;
import fr.inria.jessy.Partitioner;
import fr.inria.jessy.consistency.ConsistencyFactory;
import fr.inria.jessy.transaction.ExecutionHistory;
import fr.inria.jessy.transaction.ExecutionHistory.TransactionType;
import fr.inria.jessy.transaction.TransactionHandler;

//TODO COMMENT ME
//TODO GARBAGE COLLECT ME
public class DistributedTermination implements Learner, Runnable {

	private static DistributedTermination instance;
	private static Jessy jessy;

	public static DistributedTermination getInstance(Jessy j) {
		if (instance == null) {
			jessy = j;
			instance = new DistributedTermination();
		}
		return instance;
	}

	public enum TerminationResult {
		/**
		 * the transaction is committed
		 */
		COMMITTED,
		/**
		 * the transaction is aborted.
		 */
		ABORTED,
		/**
		 * the transaction outcome is not clear yet!
		 */
		UNKNOWN
	};

	private ExecutorPool pool = ExecutorPool.getInstance();
	private WanAMCastStream amStream;
	private RMCastStream rmStream;

	private ConcurrentMap<TransactionHandler, Future<TerminationResult>> futures;
	private ConcurrentMap<TransactionHandler, TerminationResult> terminationResults;
	private ConcurrentMap<TransactionHandler, VotingQuorum> votingQuorums;

	// TODO this might be costy. Can't we just simply use an arraylist?
	private LinkedBlockingQueue<TerminateTransactionMessage> terminateTransactionMessages;

	private String myGroup = Membership.getInstance().myGroup().name();

	private DistributedTermination() {
		Membership.getInstance().getOrCreateTCPGroup("ALLNODES");

		amStream = FractalManager.getInstance().getOrCreateWanAMCastStream(
				"DistributedTerminationStream",
				Membership.getInstance().myGroup().name());
		amStream.registerLearner("TerminateTransactionMessage", this);

		rmStream = FractalManager.getInstance().getOrCreateRMCastStream(
				"VoteMessage", Membership.getInstance().myGroup().name());
		rmStream.registerLearner("TerminateTransactionMessage", this);

		futures = new ConcurrentHashMap<TransactionHandler, Future<TerminationResult>>();
		terminationResults = new ConcurrentHashMap<TransactionHandler, TerminationResult>();
		terminateTransactionMessages = new LinkedBlockingQueue<TerminateTransactionMessage>();
		votingQuorums = new ConcurrentHashMap<TransactionHandler, VotingQuorum>();
	}

	public Future<TerminationResult> terminateTransaction(ExecutionHistory ex) {
		Future<TerminationResult> reply = pool
				.submit(new TerminateTransactionTask(ex));
		futures.put(ex.getTransactionHandler(), reply);
		return reply;
	}

	@Deprecated
	public void learn(Stream s, Serializable v) {
		if (v instanceof TerminateTransactionMessage) {
			terminateTransactionMessages.add((TerminateTransactionMessage) v);

		} else {
			addVote((Vote) v);
		}

	}

	@Override
	public void run() {
		TerminateTransactionMessage msg;
		Boolean outcome;
		while (true) {

			msg = terminateTransactionMessages.poll();
			if (msg == null)
				continue;

			outcome = ConsistencyFactory.getConsistency()
					.certify(jessy.getLastCommittedEntities(),
							msg.getExecutionHistory());

			/*
			 * Send the outcome to the write-sets
			 * 
			 * TODO It can be executed in a seperate thread!!! What about
			 * safety?
			 */
			{
				Set<String> dest = Partitioner.getInstance()
						.resolveToGroupNames(
								msg.getExecutionHistory().getWriteSet()
										.getKeys());
				rmStream.reliableMulticast(new VoteMessage(new Vote(msg
						.getExecutionHistory().getTransactionHandler(),
						outcome, myGroup, msg.dest), dest));
			}

			// /*
			// * If it is a read-only transaction, it has nothing else to do. It
			// * can commit.
			// */
			// if (msg.getExecutionHistory().getTransactionType() ==
			// TransactionType.READONLY_TRANSACTION) {
			// /*
			// * If future is not null, then the transaction is managed by
			// * localJessy, and it should be notified along with the result
			// * of the termination.
			// */
			// notifyFuture(TerminationResult.COMMITTED, msg
			// .getExecutionHistory().getTransactionHandler());
			// continue;
			// }

			/*
			 * if it holds all keys, it can decide <i>locally</i>. Otherwise, it
			 * should wait to receive votes;
			 */
			if (msg.dest.size() == 1
					&& msg.dest.contains(Membership.getInstance().myGroup()
							.name())) {
				notifyFuture(TerminationResult.COMMITTED, msg
						.getExecutionHistory().getTransactionHandler(), msg
						.getExecutionHistory().isCertifyAtProxy());
			} else {
				TerminationResult result = votingQuorums.get(
						msg.getExecutionHistory().getTransactionHandler())
						.getTerminationResult();
				notifyFuture(result, msg.getExecutionHistory()
						.getTransactionHandler(), msg.getExecutionHistory()
						.isCertifyAtProxy());
			}

		}

	}

	private void addVote(Vote vote) {
		if (votingQuorums.containsKey(vote.getTransactionHandler())) {
			votingQuorums.get(vote.getTransactionHandler()).addVote(vote);
		} else {
			VotingQuorum vq = new VotingQuorum(vote.getTransactionHandler(),
					vote.getAllVoterGroups());
			vq.addVote(vote);
			votingQuorums.put(vote.getTransactionHandler(), vq);
		}
	}

	private void notifyFuture(TerminationResult terminationResult,
			TransactionHandler transactionHandler, boolean certifyAtProxy) {
		Future<TerminationResult> future = futures.get(transactionHandler);
		if (future != null) {
			terminationResults.put(transactionHandler, terminationResult);
			future.notify();
		} else if (!certifyAtProxy) {
			// TODO SEND the result back to the proxy, otherwise, it will
			// receive it itself.
		}
		
		/*
		 * TODO Garbage collect here!
		 */
	}

	public class VoteMessage extends WanMessage {
		private static final long serialVersionUID = ConstantPool.JESSY_MID;

		// For Fractal
		public VoteMessage() {
		}

		VoteMessage(Vote vote, Set<String> dest) {
			super(vote, dest, Membership.getInstance().myGroup().name(),
					Membership.getInstance().myId());
		}

		public Vote getVote() {
			return (Vote) serializable;
		}

	}

	public class TerminateTransactionMessage extends WanMessage {
		private static final long serialVersionUID = ConstantPool.JESSY_MID;
		ExecutionHistory executionHistory;

		// For Fractal
		public TerminateTransactionMessage() {
		}

		TerminateTransactionMessage(ExecutionHistory eh, Set<String> dest) {
			super(eh, dest, myGroup, Membership.getInstance().myId());
		}

		public ExecutionHistory getExecutionHistory() {
			return executionHistory;
		}

	}

	class TerminateTransactionTask implements Callable<TerminationResult> {

		private ExecutionHistory executionHistory;

		public ExecutionHistory getExecutionHistory() {
			return executionHistory;
		}

		private TerminateTransactionTask(ExecutionHistory eh) {
			this.executionHistory = eh;
		}

		public TerminationResult call() throws Exception {
			HashSet<String> destGroups = new HashSet<String>();
			Set<String> concernedKeys = ConsistencyFactory
					.getConcerningKeys(executionHistory);

			/*
			 * If there is no concerningkey, it means that the transaction can
			 * commit right away. e.g. read-only transaction with NMSI
			 * consistency.
			 */
			if (concernedKeys.size() == 0)
				return TerminationResult.COMMITTED;

			destGroups.addAll(Partitioner.getInstance().resolveToGroupNames(
					concernedKeys));

			/*
			 * if this process will receive this transaction through atomic
			 * multicast, certifyAtProxy is set to true, otherwise, it is set to
			 * false.
			 */
			if (destGroups.contains(myGroup))
				executionHistory.setCertifyAtProxy(true);
			else
				executionHistory.setCertifyAtProxy(false);

			amStream.atomicMulticast(new TerminateTransactionMessage(
					executionHistory, destGroups), destGroups);
			futures.get(executionHistory.getTransactionHandler()).wait();
			return terminationResults.get(executionHistory
					.getTransactionHandler());
		}
	}

}
