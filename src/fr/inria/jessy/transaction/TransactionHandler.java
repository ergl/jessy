package fr.inria.jessy.transaction;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import fr.inria.jessy.ConstantPool;
import fr.inria.jessy.utils.CustomUUID;

public class TransactionHandler implements Externalizable, Cloneable{

	private static final long serialVersionUID = ConstantPool.JESSY_MID;
	
	/**
	 * A unique id for identifying the previous aborted transaction by timeout.
	 * 
	 * 
	 * <p>
	 * When a transaction times out by the client or is aborted by a voting from a jessy replica, the client tries to
	 * re-execute the transaction again. In this case, it can happen that the
	 * second transaction is received in a jessy node, but it should wait in the
	 * queue, because the other transaction is not yet removed from the list.
	 * This variable helps us to check if the certification conflict is because
	 * of a dangling aborted transaction, or something else.
	 */
	private TransactionHandler previousTimedoutTransactionHandler;
	
	private  UUID id;

	private HashMap<String, Object> mExtras = new HashMap<>();
	
	public TransactionHandler(){
		this.id = CustomUUID.getNextUUID(); 
	}

	public UUID getId() {
		return id;
	}

	public TransactionHandler getPreviousTimedoutTransactionHandler() {
		return previousTimedoutTransactionHandler;
	}

	public void setPreviousTimedoutTransactionHandler(
			TransactionHandler previousAbortedTransactionHandler) {
		this.previousTimedoutTransactionHandler = previousAbortedTransactionHandler;
	}
	
	@Override
	public int hashCode(){
		return id.hashCode();
	}
	
	@Override
	public boolean equals(Object obj){
		if ( this == obj ) return true;
		if ( !(obj instanceof TransactionHandler) ) return false;
		return id.equals(((TransactionHandler)obj).id);
	}
	
	@Override
	public String toString(){
		return id.toString();
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		id = (UUID) in.readObject();
		previousTimedoutTransactionHandler=(TransactionHandler) in.readObject();
		mExtras = (HashMap<String, Object>) in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(id);

		if (previousTimedoutTransactionHandler!=null)
			out.writeObject(previousTimedoutTransactionHandler);
		else
			out.writeObject(null);

		out.writeObject(mExtras);
	}
	
	public TransactionHandler clone(){
		TransactionHandler result=null;
		try {
			result = (TransactionHandler) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		result.id=new UUID(this.id.getMostSignificantBits(), this.id.getLeastSignificantBits());
		
		return result;
	}

	/**
	 * Put extra information in this transaction.
	 * <p>
	 * Information is put in a key-value store. You can later get the information you put using
	 * {@link TransactionHandler#getExtra(String)}.
	 *
	 * @param extras A map containing a set of information.
	 */
	public void putAllExtras(Map<String, Object> extras) {
		for (String key : extras.keySet())
			putExtra(key, extras.get(key));
	}

	/**
	 * Put one extra information in this transaction handler.
	 * <p>
	 * Information is put in a key-value store. You can later get the information you put using
	 * {@link TransactionHandler#getExtra(String)}.
	 *
	 * @param key   The key assigned to the extra.
	 * @param value The value of the extra.
	 */
	public void putExtra(String key, Object value) {
		mExtras.put(key, value);
	}

	/**
	 * Get extra information set for this transaction handler.
	 * <p>
	 * Extra information is put like in a key-value store.
	 *
	 * @param key The key assigned to the extra.
	 * @return The extra.
	 */
	public Object getExtra(String key) {
		return mExtras.get(key);
	}

	/**
	 * Get all the extra information set for this transaction handler.
	 * <p>
	 * Extra information is put like in a key-value store.
	 *
	 * @return A map containing all the extra information of this transaction handler.
     */
	public Map<String, Object> getAllExtras() {
		return mExtras;
	}
}
