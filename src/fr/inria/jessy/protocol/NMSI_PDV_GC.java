package fr.inria.jessy.protocol;


import static fr.inria.jessy.ConstantPool.PROTOCOL_ATOMIC_COMMIT;
import static fr.inria.jessy.transaction.ExecutionHistory.TransactionType.BLIND_WRITE;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.sourceforge.fractal.utils.CollectionUtils;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

import fr.inria.jessy.ConstantPool;
import fr.inria.jessy.communication.JessyGroupManager;
import fr.inria.jessy.communication.message.TerminateTransactionRequestMessage;
import fr.inria.jessy.consistency.NMSI;
import fr.inria.jessy.store.DataStore;
import fr.inria.jessy.store.JessyEntity;
import fr.inria.jessy.store.ReadRequest;
import fr.inria.jessy.transaction.ExecutionHistory;
import fr.inria.jessy.transaction.ExecutionHistory.TransactionType;
import fr.inria.jessy.transaction.TransactionHandler;
import fr.inria.jessy.transaction.termination.vote.Vote;
import fr.inria.jessy.transaction.termination.vote.VotePiggyback;
import fr.inria.jessy.transaction.termination.vote.VotingQuorum;
import fr.inria.jessy.vector.PartitionDependenceVector;
import fr.inria.jessy.vector.Vector.CompatibleResult;

/**
 * This class implements Non-Monotonic Snapshot Isolation consistency criterion.
 * 
 * @author Masoud Saeida Ardekani
 * 
 */
public class NMSI_PDV_GC extends NMSI {

	protected static ConcurrentHashMap<UUID, PartitionDependenceVector<String>> receivedVectors;

	static {
		votePiggybackRequired = true;
		receivedVectors = new ConcurrentHashMap<UUID, PartitionDependenceVector<String>>();
		PROTOCOL_ATOMIC_COMMIT = ConstantPool.ATOMIC_COMMIT_TYPE.ATOMIC_MULTICAST;
	}

	public NMSI_PDV_GC(JessyGroupManager m, DataStore dataStore) {
		super(m, dataStore);
	}

	@SuppressWarnings("unchecked")
	public boolean certify(ExecutionHistory executionHistory) {
		TransactionType transactionType = executionHistory.getTransactionType();

		if (ConstantPool.logging){
			logger.debug(executionHistory.getTransactionHandler() + " >> "
					+ transactionType.toString());
			logger.debug("ReadSet Vector"
					+ executionHistory.getReadSet().getCompactVector().toString());
			logger.debug("CreateSet Vectors"
					+ executionHistory.getCreateSet().getCompactVector().toString());
			logger.debug("WriteSet Vectors"
					+ executionHistory.getWriteSet().getCompactVector().toString());
		}

		/*
		 * if the transaction is a read-only transaction, it commits right away.
		 */
		if (transactionType == TransactionType.READONLY_TRANSACTION) {
			return true;
		}

		/*
		 * if the transaction is an initialization transaction, it first
		 * increments the vectors and then commits.
		 */
		if (transactionType == TransactionType.INIT_TRANSACTION) {
			
			for (JessyEntity tmp : executionHistory.getCreateSet()
					.getEntities()) {
				
				PartitionDependenceVector<String> commitVC=new PartitionDependenceVector<String>(manager.getMyGroup().name(),0);
				tmp.setLocalVector(commitVC);
			}

			return true;
		}

		/*
		 * If the transaction is not read-only or init, we consider the create
		 * operations as update operations. Thus, we move them to the writeSet
		 * List.
		 */
		executionHistory.getWriteSet().addEntity(
				executionHistory.getCreateSet());

		JessyEntity lastComittedEntity;

		for (JessyEntity tmp : executionHistory.getWriteSet().getEntities()) {

			if (!manager.getPartitioner().isLocal(tmp.getKey()))
				continue;

			try {

				lastComittedEntity = store
						.get(new ReadRequest<JessyEntity>(
								(Class<JessyEntity>) tmp.getClass(),
								"secondaryKey", tmp.getKey(), null))
						.getEntity().iterator().next();

				/*
				 * instead of locking, we simply checks against the latest
				 * committed values
				 */
				if (tmp.getLocalVector().isCompatible(lastComittedEntity.getLocalVector())!=CompatibleResult.COMPATIBLE){
					
//					if (ConstantPool.logging)
//						logger.error("Certification fails (writeSet) : Reads key "	+ tmp.getKey() 
//								+ " with the vector "
//							+ tmp.getLocalVector() + " while the last committed vector is "	+ lastComittedEntity.getLocalVector() + " transaction " + executionHistory.getTransactionHandler().getId());
					return false;
				}

			} catch (NullPointerException e) {
				// nothing to do.
				// the key is simply not there.
			}

		}

		return true;
	}

	@Override
	public boolean certificationCommute(ExecutionHistory history1,
			ExecutionHistory history2) {

			return !CollectionUtils.isIntersectingWith(history1.getWriteSet()
					.getKeys(), history2.getWriteSet().getKeys());
	}
	
	@Override
	public boolean applyingTransactionCommute() {
		return false;
	}

	@Override
	public boolean transactionDeliveredForTermination(ConcurrentLinkedHashMap<UUID, Object> terminatedTransactions, ConcurrentHashMap<TransactionHandler, VotingQuorum>  quorumes, TerminateTransactionRequestMessage msg){
		try{
			if (msg.getExecutionHistory().getTransactionType() != TransactionType.INIT_TRANSACTION) {
				int seqNo=PartitionDependenceVector.lastCommitSeqNo.incrementAndGet();
				PartitionDependenceVector<String> vector=new PartitionDependenceVector<String>();
				for (JessyEntity entity: msg.getExecutionHistory().getReadSet().getEntities()){
					vector.update(entity.getLocalVector());
				}
				vector.update(PartitionDependenceVector.lastCommit.clone());
				vector.setSelfKey(manager.getMyGroup().name());
				vector.setValue(vector.getSelfKey(), seqNo);
				msg.setComputedObjectUponDelivery(vector.clone());
				
			}
		}
		catch (Exception ex){
			ex.printStackTrace();
		}
		
		return true;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Vote createCertificationVote(ExecutionHistory executionHistory, Object object) {
		/*
		 * First, it needs to run the certification test on the received
		 * execution history. A blind write always succeeds.
		 */
		
		boolean isCommitted = executionHistory.getTransactionType() == BLIND_WRITE
				|| certify(executionHistory);


		return new Vote(executionHistory.getTransactionHandler(), isCommitted,
				manager.getMyGroup().name(),
				new VotePiggyback(object));
	}

	/**
	 * @inheritDoc
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void voteReceived(Vote vote) {
		if (vote.getVotePiggyBack().getPiggyback() == null) {
			/*
			 * init transaction.
			 */
			return;
		}
		
		try {

			PartitionDependenceVector<String> commitVC = (PartitionDependenceVector<String>) vote
					.getVotePiggyBack().getPiggyback();


			PartitionDependenceVector<String> receivedVector = receivedVectors.putIfAbsent(
					vote.getTransactionHandler().getId(), commitVC);
			if (receivedVector != null) {
				receivedVector.update(commitVC);
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void prepareToCommit(TerminateTransactionRequestMessage msg) {
		ExecutionHistory executionHistory=msg.getExecutionHistory();

		PartitionDependenceVector<String> commitVC = receivedVectors.get(executionHistory
				.getTransactionHandler().getId());

		/*
		 * Assigning commitVC to the entities
		 */
		for (JessyEntity entity : executionHistory.getWriteSet()
				.getEntities()) {
			entity.setLocalVector(commitVC.clone());
			entity.temporaryObject=null;
			entity.getLocalVector().setSelfKey(manager.getMyGroup().name());
		}
	}

	@Override
	public void postCommit(ExecutionHistory executionHistory) {
		if (executionHistory.getTransactionType() != TransactionType.INIT_TRANSACTION) {
			PartitionDependenceVector<String> commitVC = receivedVectors.get(executionHistory
					.getTransactionHandler().getId());

			PartitionDependenceVector.lastCommit=commitVC.clone();
		}
		

		/*
		 * Garbage collect the received vectors. We don't need them anymore.
		 */
		if (receivedVectors.containsKey(executionHistory
				.getTransactionHandler().getId()))
			receivedVectors.remove(executionHistory.getTransactionHandler()
					.getId());

	}
	
	@Override
	public void postAbort(TerminateTransactionRequestMessage msg, Vote Vote){
		/*
		 * Garbage collect the received vectors. We don't need them anymore.
		 */
		if (receivedVectors.containsKey(msg.getExecutionHistory()
				.getTransactionHandler().getId()))
			receivedVectors.remove(msg.getExecutionHistory().getTransactionHandler()
					.getId());

	}
	
	@Override
	public JessyEntity createEntity(String key){
		JessyEntity e=super.createEntity(key);
		e.setLocalVector(new PartitionDependenceVector<String>(manager.getMyGroup().name(), 0));
		
		return e;
	}
}
