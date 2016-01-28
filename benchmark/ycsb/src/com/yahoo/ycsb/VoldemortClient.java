package com.yahoo.ycsb;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import voldemort.client.ClientConfig;
import voldemort.client.SocketStoreClientFactory;
import voldemort.client.StoreClient;
import voldemort.versioning.VectorClock;
import voldemort.versioning.Versioned;

import com.yahoo.ycsb.workloads.YCSBTransactionalCreateRequest;
import com.yahoo.ycsb.workloads.YCSBTransactionalReadRequest;
import com.yahoo.ycsb.workloads.YCSBTransactionalUpdateRequest;

public class VoldemortClient extends DB {

	private StoreClient<String, HashMap<String, String>> storeClient;
	private SocketStoreClientFactory socketFactory;
	private String storeName;
	private final Logger logger = Logger.getLogger(VoldemortClient.class);

	public static final int OK = 0;
	public static final int ERROR = -1;

	/**
	 * Initialize the DB layer. This accepts all properties allowed by the Voldemort client.
	 * A store maps to a org.imdea.benchmark.rubis.table.
	 * Required : bootstrap_urls
	 * Additional property : store_name -> to preload once, should be same as -t <org.imdea.benchmark.rubis.table>
	 * 
	 * {@linktourl http://project-voldemort.com/javadoc/client/voldemort/client/ClientConfig.html}
	 */
	public void init() throws DBException {
		ClientConfig clientConfig = new ClientConfig(getProperties());
		socketFactory = new SocketStoreClientFactory(clientConfig);

		// Retrieve store name
		storeName = getProperties().getProperty("store_name", "usertable");

		// Use store name to retrieve client
		storeClient = socketFactory.getStoreClient(storeName);
		if ( storeClient == null )
			throw new DBException("Unable to instantiate store client");

	}

	public void cleanup() throws DBException {
		socketFactory.close();
	}

	@Override
	public int delete(String table, String key) {
		if ( checkStore(table) == ERROR ) {
			return ERROR;
		}

		if ( storeClient.delete(key) )
			return OK;
		else
			return ERROR;
	}

	@Override
	public int insert(String table, String key, HashMap<String, String> values) {
		if ( checkStore(table) == ERROR ) {
			return ERROR;
		}
		storeClient.put(key, values);
		return OK;
	}

	@Override
	public int read(String table, String key, Set<String> fields,
			HashMap<String, String> result) {
		if ( checkStore(table) == ERROR ) {
			return ERROR;
		}

		Versioned<HashMap<String, String>> versionedValue = storeClient.get(key);

		if (versionedValue == null)
			return OK;


		if ( fields != null ) {
			for (String field : fields) {
				String val = versionedValue.getValue().get(field);
				if ( val != null)
					result.put(field, val);
			}
		} else {
			result.putAll( versionedValue.getValue());
		}
		return OK;
	}

	@Override
	public int scan(String table, String startkey, int recordcount,
			Set<String> fields, Vector<HashMap<String, String>> result) {
		logger.warn("Voldemort does not support Scan semantics");
		return OK;
	}

	@Override
	public int update(String table, String key, HashMap<String, String> values) {
		if ( checkStore(table) == ERROR ) {
			return ERROR;
		}

		Versioned<HashMap<String, String>> versionedValue = storeClient.get(key);
		HashMap<String, String> value = new HashMap<String, String>();
		VectorClock version;
		if ( versionedValue != null) {
			version = ((VectorClock) versionedValue.getVersion()).incremented(0, 1);
			value = versionedValue.getValue();
			for ( Entry<String, String> entry : values.entrySet()) {
				value.put(entry.getKey(), entry.getValue());
			}
		} else {
			version = new VectorClock();
			value.putAll(values);
		}

		storeClient.put(key, Versioned.value(value, version));
		return OK;
	}

	private int checkStore(String table) {
		if ( table.compareTo(storeName) != 0) {
			try {
				storeClient = socketFactory.getStoreClient(table);
				if ( storeClient == null ) {
					logger.error("Could not instantiate storeclient for " + table);
					return ERROR;
				}
				storeName = table;
			} catch ( Exception e ) {
				return ERROR;
			}
		}
		return OK;
	}

	@Override
	public int readTransaction(List<YCSBTransactionalReadRequest> readList) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int updateTransaction(List<YCSBTransactionalReadRequest> readList,
			List<YCSBTransactionalUpdateRequest> updateList) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int createTransaction(YCSBTransactionalCreateRequest createRequest) {
		// TODO Auto-generated method stub
		return 0;
	}

}
