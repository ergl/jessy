package fr.inria.jessy.store;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import fr.inria.jessy.ConstantPool;
import fr.inria.jessy.utils.Compress;
import fr.inria.jessy.vector.CompactVector;
import fr.inria.jessy.vector.Vector;

//TODO Comment me and all methods
public class ReadRequest<E extends JessyEntity> implements Externalizable {

	private static final long serialVersionUID = ConstantPool.JESSY_MID;

	private static AtomicInteger requestCounter = new AtomicInteger();

	private String entityClassName;
	private CompactVector<String> readSet;

	/**
	 * If true, the query is on one key, thus {@code oneKey} holds the key.
	 */
	boolean isOneKeyRequest = true;

	/**
	 * For performance reasons during marshaling and unmarshaling, the keys are
	 * seperated. For most queries, the query is only with one key. Thus
	 * {@code firstKey} is used.
	 */
	private ReadRequestKey<?> oneKey;

	/**
	 * If the user needs to perform a query on several keys,
	 * {@code isOneKeyRequest} is set to false, and this variable is filled with
	 * the request.
	 */
	private List<ReadRequestKey<?>> multiKeys;

	private int readRequestId;

	public Vector<String> temporaryVector; 
	/**
	 * For externalizable interface
	 */
	@Deprecated
	public ReadRequest() {

	}

	/**
	 * This constructor should be called if the {@code ReadRequest} is only on
	 * one {@code ReadRequestKey}
	 * 
	 * @param <K>
	 * @param entityClass
	 * @param keyName
	 * @param keyValue
	 * @param readSet
	 * @param partitioningKey
	 */
	public <K> ReadRequest(Class<E> entityClass, String keyName, K keyValue,
			CompactVector<String> readSet) {
		this.entityClassName = Compress
				.compressClassName(entityClass.getName());
		this.readSet = readSet;

		oneKey = new ReadRequestKey<K>(keyName, keyValue);
		readRequestId = requestCounter.incrementAndGet();
	}

	/**
	 * This constructor should be called if the {@code ReadRequest} is only on
	 * several {@code ReadRequestKey}
	 * 
	 * @param entityClass
	 * @param keys
	 * @param readSet
	 * @param partitioningKey
	 */
	public ReadRequest(Class<E> entityClass, List<ReadRequestKey<?>> keys,
			CompactVector<String> readSet) {
		this.entityClassName = Compress
				.compressClassName(entityClass.getName());
		this.readSet = readSet;

		isOneKeyRequest = false;
		this.multiKeys = keys;
		readRequestId = requestCounter.incrementAndGet();
	}

	public String getEntityClassName() {
		return entityClassName;
	}

	public CompactVector<String> getReadSet() {
		return readSet;
	}

	public Integer getReadRequestId() {
		return readRequestId;
	}

	public ReadRequestKey<?> getOneKey() {
		return oneKey;
	}

	public List<ReadRequestKey<?>> getMultiKeys() {
		return multiKeys;
	}

	/**
	 * 
	 * @return
	 */
	public String getPartitioningKey() {
		if(isOneKeyRequest) {
			return oneKey.toString();
		}  else {
			StringBuilder builder = new StringBuilder();

			for (ReadRequestKey<?> rk : multiKeys)
				builder.append(rk.toString());

			return builder.toString();
		}
	}

	@Override
	public String toString() {
		return "RReQ" + getReadRequestId().toString();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(entityClassName);
		out.writeObject(readSet);
		out.writeInt(readRequestId);
		out.writeBoolean(isOneKeyRequest);
		if (isOneKeyRequest) {
			out.writeObject(oneKey);
		} else {
			out.writeObject(multiKeys);
		}
		
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {

		entityClassName = (String) in.readObject();
		readSet = (CompactVector<String>) in.readObject();
		readRequestId = in.readInt();
		isOneKeyRequest = in.readBoolean();
		if (isOneKeyRequest) {
			oneKey = (ReadRequestKey<?>) in.readObject();
		} else {
			multiKeys = (List<ReadRequestKey<?>>) in.readObject();
		}
		
	}

	public boolean isOneKeyRequest() {
		return isOneKeyRequest;
	}

}
