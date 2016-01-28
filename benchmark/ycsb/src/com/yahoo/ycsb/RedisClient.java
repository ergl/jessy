/**
 * Redis client binding for YCSB.
 *
 * All YCSB records are mapped to a Redis *hash field*.  For scanning
 * operations, all keys are saved (by an arbitrary hash) in a sorted set.
 */

package com.yahoo.ycsb;
import com.yahoo.ycsb.DB;
import com.yahoo.ycsb.DBException;
import com.yahoo.ycsb.ByteIterator;
import com.yahoo.ycsb.StringByteIterator;
import com.yahoo.ycsb.workloads.YCSBTransactionalCreateRequest;
import com.yahoo.ycsb.workloads.YCSBTransactionalReadRequest;
import com.yahoo.ycsb.workloads.YCSBTransactionalUpdateRequest;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol;

public class RedisClient extends DB {

    private Jedis jedis;

    public static final String HOST_PROPERTY = "redis.host";
    public static final String PORT_PROPERTY = "redis.port";
    public static final String PASSWORD_PROPERTY = "redis.password";

    public static final String INDEX_KEY = "_indices";

    public void init() throws DBException {
        Properties props = getProperties();
        int port;

        String portString = props.getProperty(PORT_PROPERTY);
        if (portString != null) {
            port = Integer.parseInt(portString);
        }
        else {
            port = Protocol.DEFAULT_PORT;
        }
        String host = props.getProperty(HOST_PROPERTY);
        
        jedis = new Jedis("localhost", 6379);
        jedis.connect();

        String password = props.getProperty(PASSWORD_PROPERTY);
        if (password != null) {
            jedis.auth(password);
        }
    }

    public void cleanup() throws DBException {
        jedis.disconnect();
    }

    /* Calculate a hash for a key to store it in an index.  The actual return
     * value of this function is not interesting -- it primarily needs to be
     * fast and scattered along the whole space of doubles.  In a real world
     * scenario one would probably use the ASCII values of the keys.
     */
    private double hash(String key) {
        return key.hashCode();
    }

    //XXX jedis.select(int index) to switch to `org.imdea.benchmark.rubis.table`

    @Override
    public int read(String table, String key, Set<String> fields,
            HashMap<String, String> result) {
        if (fields == null) {
        	HashMap<String, String> h = (HashMap<String, String>) jedis.hgetAll(key);
            for(String k : h.keySet()) {
            	result.put(k,h.get(k) );
            }
            	
        }
        else {
            String[] fieldArray = (String[])fields.toArray(new String[fields.size()]);
            List<String> values = jedis.hmget(key, fieldArray);

            Iterator<String> fieldIterator = fields.iterator();
            Iterator<String> valueIterator = values.iterator();

            while (fieldIterator.hasNext() && valueIterator.hasNext()) {
                result.put(fieldIterator.next(),valueIterator.next());
            }
            assert !fieldIterator.hasNext() && !valueIterator.hasNext();
        }
        return result.isEmpty() ? 1 : 0;
    }

    @Override
    public int insert(String table, String key, HashMap<String, String> values) {
        if (jedis.hmset(key, values).equals("OK")) {
            jedis.zadd(INDEX_KEY, hash(key), key);
            return 0;
        }
        return 1;
    }

    @Override
    public int delete(String table, String key) {
        return jedis.del(key) == 0
            && jedis.zrem(INDEX_KEY, key) == 0
               ? 1 : 0;
    }

    @Override
    public int update(String table, String key, HashMap<String, String> values) {
        return jedis.hmset(key, values).equals("OK") ? 0 : 1;
    }

    @Override
    public int scan(String table, String startkey, int recordcount,
            Set<String> fields, Vector<HashMap<String, String>> result) {
        Set<String> keys = jedis.zrangeByScore(INDEX_KEY, hash(startkey),
                                Double.POSITIVE_INFINITY, 0, recordcount);

        HashMap<String,String> values;
        for (String key : keys) {
            values = new HashMap<String, String>();
            read(table, key, fields, values);
            result.add(values);
        }

        return 0;
    }

	@Override
	public int createTransaction(YCSBTransactionalCreateRequest createRequest) {
		// TODO Auto-generated method stub
		return 0;
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

}
