/**
 * Copyright (c) 2010 Yahoo! Inc. All rights reserved. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */

package com.yahoo.ycsb.workloads;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Vector;

import com.yahoo.ycsb.Client;
import com.yahoo.ycsb.DB;
import com.yahoo.ycsb.Utils;
import com.yahoo.ycsb.Workload;
import com.yahoo.ycsb.WorkloadException;
import com.yahoo.ycsb.generator.CounterGenerator;
import com.yahoo.ycsb.generator.DiscreteGenerator;
import com.yahoo.ycsb.generator.Generator;
import com.yahoo.ycsb.generator.IntegerGenerator;
import com.yahoo.ycsb.generator.ScrambledZipfianGenerator;
import com.yahoo.ycsb.generator.SkewedLatestGenerator;
import com.yahoo.ycsb.generator.UniformIntegerGenerator;
import com.yahoo.ycsb.generator.ZipfianGenerator;
import com.yahoo.ycsb.measurements.Measurements;

import fr.inria.jessy.ConstantPool;
import fr.inria.jessy.DistributedJessy;

/**
 * The core benchmark scenario. Represents a set of clients doing simple CRUD
 * operations. The relative proportion of different kinds of operations, and
 * other properties of the workload, are controlled by parameters specified at
 * runtime.
 * 
 * Properties to control the client:
 * <UL>
 * <LI><b>fieldcount</b>: the number of fields in a record (default: 10)
 * <LI><b>fieldlength</b>: the size of each field (default: 100)
 * <LI><b>readallfields</b>: should reads read all fields (true) or just one
 * (false) (default: true)
 * <LI><b>writeallfields</b>: should updates and read/modify/writes update all
 * fields (true) or just one (false) (default: false)
 * <LI><b>readproportion</b>: what proportion of operations should be reads
 * (default: 0.95)
 * <LI><b>updateproportion</b>: what proportion of operations should be updates
 * (default: 0.05)
 * <LI><b>insertproportion</b>: what proportion of operations should be inserts
 * (default: 0)
 * <LI><b>scanproportion</b>: what proportion of operations should be scans
 * (default: 0)
 * <LI><b>readmodifywriteproportion</b>: what proportion of operations should be
 * read a record, modify it, write it back (default: 0)
 * <LI><b>requestdistribution</b>: what distribution should be used to select
 * the records to operate on - uniform, zipfian or latest (default: uniform)
 * <LI><b>maxscanlength</b>: for scans, what is the maximum number of records to
 * scan (default: 1000)
 * <LI><b>scanlengthdistribution</b>: for scans, what distribution should be
 * used to choose the number of records to scan, for each scan, between 1 and
 * maxscanlength (default: uniform)
 * <LI><b>insertorder</b>: should records be inserted in order by key
 * ("ordered"), or in hashed order ("hashed") (default: hashed)
 * </ul>
 */
public class TransactionalWorkload extends Workload {
	
	private enum TransactionType{
		Local, Global;
	}

	/**
	 * The name of the database org.imdea.benchmark.rubis.table to run queries against.
	 */
	public static final String TABLENAME_PROPERTY = "org/imdea/benchmark/rubis/table";

	/**
	 * The default name of the database org.imdea.benchmark.rubis.table to run queries against.
	 */
	public static final String TABLENAME_PROPERTY_DEFAULT = "usertable";

	public static String table;

	/**
	 * The name of the property for the number of fields in a record.
	 */
	public static final String FIELD_COUNT_PROPERTY = "fieldcount";

	/**
	 * Default number of fields in a record.
	 */
	public static final String FIELD_COUNT_PROPERTY_DEFAULT = "10";

	int fieldcount;

	/**
	 * The name of the property for the length of a field in bytes.
	 */
	public static final String FIELD_LENGTH_PROPERTY = "fieldlength";

	/**
	 * The default length of a field in bytes.
	 */
	public static final String FIELD_LENGTH_PROPERTY_DEFAULT = "100";

	int fieldlength;

	/**
	 * The name of the property for deciding whether to read one field (false)
	 * or all fields (true) of a record.
	 */
	public static final String READ_ALL_FIELDS_PROPERTY = "readallfields";

	/**
	 * The default value for the readallfields property.
	 */
	public static final String READ_ALL_FIELDS_PROPERTY_DEFAULT = "true";

	boolean readallfields;

	/**
	 * The name of the property for deciding whether to write one field (false)
	 * or all fields (true) of a record.
	 */
	public static final String WRITE_ALL_FIELDS_PROPERTY = "writeallfields";

	/**
	 * The default value for the writeallfields property.
	 */
	public static final String WRITE_ALL_FIELDS_PROPERTY_DEFAULT = "false";

	boolean writeallfields;

	/**
	 * The name of the property for the proportion of transactions that are
	 * reads.
	 */
	public static final String READ_PROPORTION_PROPERTY = "readproportion";

	/**
	 * The default proportion of transactions that are reads.
	 */
	public static final String READ_PROPORTION_PROPERTY_DEFAULT = "0.95";

	/**
	 * The name of the property for the proportion of transactions that are
	 * updates.
	 */
	public static final String UPDATE_PROPORTION_PROPERTY = "updateproportion";

	/**
	 * The default proportion of transactions that are updates.
	 */
	public static final String UPDATE_PROPORTION_PROPERTY_DEFAULT = "0.05";

	/**
	 * The name of the property for the proportion of transactions that are
	 * inserts.
	 */
	public static final String INSERT_PROPORTION_PROPERTY = "insertproportion";

	/**
	 * The default proportion of transactions that are inserts.
	 */
	public static final String INSERT_PROPORTION_PROPERTY_DEFAULT = "0.0";

	/**
	 * The name of the property for the proportion of transactions that are
	 * scans.
	 */
	public static final String SCAN_PROPORTION_PROPERTY = "scanproportion";

	/**
	 * The default proportion of transactions that are scans.
	 */
	public static final String SCAN_PROPORTION_PROPERTY_DEFAULT = "0.0";

	/**
	 * The name of the property for the proportion of transactions that are
	 * read-modify-write.
	 */
	public static final String READMODIFYWRITE_PROPORTION_PROPERTY = "readmodifywriteproportion";

	/**
	 * The default proportion of transactions that are scans.
	 */
	public static final String READMODIFYWRITE_PROPORTION_PROPERTY_DEFAULT = "0.0";

	/**
	 * The name of the property for the the distribution of requests across the
	 * keyspace. Options are "uniform", "zipfian" and "latest"
	 */
	public static final String REQUEST_DISTRIBUTION_PROPERTY = "requestdistribution";

	/**
	 * The default distribution of requests across the keyspace
	 */
	public static final String REQUEST_DISTRIBUTION_PROPERTY_DEFAULT = "uniform";

	/**
	 * The name of the property for the max scan length (number of records)
	 */
	public static final String MAX_SCAN_LENGTH_PROPERTY = "maxscanlength";

	/**
	 * The default max scan length.
	 */
	public static final String MAX_SCAN_LENGTH_PROPERTY_DEFAULT = "1000";

	/**
	 * The name of the property for the scan length distribution. Options are
	 * "uniform" and "zipfian" (favoring short scans)
	 */
	public static final String SCAN_LENGTH_DISTRIBUTION_PROPERTY = "scanlengthdistribution";

	/**
	 * The default max scan length.
	 */
	public static final String SCAN_LENGTH_DISTRIBUTION_PROPERTY_DEFAULT = "uniform";

	/**
	 * The name of the property for the order to insert records. Options are
	 * "ordered" or "hashed"
	 */
	public static final String INSERT_ORDER_PROPERTY = "insertorder";

	/**
	 * Default insert order.
	 */
	public static final String INSERT_ORDER_PROPERTY_DEFAULT = "hashed";

	IntegerGenerator keysequence;

	DiscreteGenerator operationchooser;

	IntegerGenerator keychooser;

	Generator fieldchooser;

	CounterGenerator transactioninsertkeysequence;

	IntegerGenerator scanlength;

	boolean orderedinserts;

	int recordcount;
	
	Random localPercentage;

	/**
	 * Initialize the scenario. Called once, in the main client thread, before
	 * any operations are started.
	 */
	public void init(Properties p) throws WorkloadException {
		table = p.getProperty(TABLENAME_PROPERTY, TABLENAME_PROPERTY_DEFAULT);
		fieldcount = Integer.parseInt(p.getProperty(FIELD_COUNT_PROPERTY,
				FIELD_COUNT_PROPERTY_DEFAULT));
		fieldlength = Integer.parseInt(p.getProperty(FIELD_LENGTH_PROPERTY,
				FIELD_LENGTH_PROPERTY_DEFAULT));
		double readproportion = Double.parseDouble(p.getProperty(
				READ_PROPORTION_PROPERTY, READ_PROPORTION_PROPERTY_DEFAULT));
		double updateproportion = Double
				.parseDouble(p.getProperty(UPDATE_PROPORTION_PROPERTY,
						UPDATE_PROPORTION_PROPERTY_DEFAULT));
		double insertproportion = Double
				.parseDouble(p.getProperty(INSERT_PROPORTION_PROPERTY,
						INSERT_PROPORTION_PROPERTY_DEFAULT));
		double scanproportion = Double.parseDouble(p.getProperty(
				SCAN_PROPORTION_PROPERTY, SCAN_PROPORTION_PROPERTY_DEFAULT));
		double readmodifywriteproportion = Double.parseDouble(p.getProperty(
				READMODIFYWRITE_PROPORTION_PROPERTY,
				READMODIFYWRITE_PROPORTION_PROPERTY_DEFAULT));
		recordcount = Integer.parseInt(p
				.getProperty(Client.RECORD_COUNT_PROPERTY));
		String requestdistrib = p.getProperty(REQUEST_DISTRIBUTION_PROPERTY,
				REQUEST_DISTRIBUTION_PROPERTY_DEFAULT);
		int maxscanlength = Integer.parseInt(p.getProperty(
				MAX_SCAN_LENGTH_PROPERTY, MAX_SCAN_LENGTH_PROPERTY_DEFAULT));
		String scanlengthdistrib = p.getProperty(
				SCAN_LENGTH_DISTRIBUTION_PROPERTY,
				SCAN_LENGTH_DISTRIBUTION_PROPERTY_DEFAULT);

		int insertstart = Integer.parseInt(p.getProperty(INSERT_START_PROPERTY,
				INSERT_START_PROPERTY_DEFAULT));

		readallfields = Boolean.parseBoolean(p.getProperty(
				READ_ALL_FIELDS_PROPERTY, READ_ALL_FIELDS_PROPERTY_DEFAULT));
		writeallfields = Boolean.parseBoolean(p.getProperty(
				WRITE_ALL_FIELDS_PROPERTY, WRITE_ALL_FIELDS_PROPERTY_DEFAULT));

		if (p.getProperty(INSERT_ORDER_PROPERTY, INSERT_ORDER_PROPERTY_DEFAULT)
				.compareTo("hashed") == 0) {
			orderedinserts = false;
		} else {
			orderedinserts = true;
		}

		keysequence = new CounterGenerator(insertstart);
		operationchooser = new DiscreteGenerator();
		if (readproportion > 0) {
			operationchooser.addValue(readproportion, "READ");
		}

		if (updateproportion > 0) {
			operationchooser.addValue(updateproportion, "UPDATE");
		}

		if (insertproportion > 0) {
			operationchooser.addValue(insertproportion, "INSERT");
		}

		if (scanproportion > 0) {
			operationchooser.addValue(scanproportion, "SCAN");
		}

		if (readmodifywriteproportion > 0) {
			operationchooser.addValue(readmodifywriteproportion,
					"READMODIFYWRITE");
		}

		transactioninsertkeysequence = new CounterGenerator(recordcount);
		if (requestdistrib.compareTo("uniform") == 0) {
			keychooser = new UniformIntegerGenerator(0, recordcount - 1 -10); //-10 is added by masoud to prevent requesting for keys that miraclously are not in the system (last keys usually are not there.)
		} else if (requestdistrib.compareTo("zipfian") == 0) {
			// it does this by generating a random "next key" in part by taking
			// the modulus over the number of keys
			// if the number of keys changes, this would shift the modulus, and
			// we don't want that to change which keys are popular
			// so we'll actually construct the scrambled zipfian generator with
			// a keyspace that is larger than exists at the beginning
			// of the test. that is, we'll predict the number of inserts, and
			// tell the scrambled zipfian generator the number of existing keys
			// plus the number of predicted keys as the total keyspace. then, if
			// the generator picks a key that hasn't been inserted yet, will
			// just ignore it and pick another key. this way, the size of the
			// keyspace doesn't change from the perspective of the scrambled
			// zipfian generator

			int opcount = Integer.parseInt(p
					.getProperty(Client.OPERATION_COUNT_PROPERTY));
			int expectednewkeys = (int) (((double) opcount) * insertproportion * 2.0); // 2
																						// is
																						// fudge
																						// factor

//			keychooser = new ScrambledZipfianGenerator(recordcount
//					+ expectednewkeys);
			keychooser = new ScrambledZipfianGenerator(0,recordcount-1);
		} else if (requestdistrib.compareTo("latest") == 0) {
			keychooser = new SkewedLatestGenerator(transactioninsertkeysequence);
		} else {
			throw new WorkloadException("Unknown distribution \""
					+ requestdistrib + "\"");
		}

		fieldchooser = new UniformIntegerGenerator(0, fieldcount - 1);

		if (scanlengthdistrib.compareTo("uniform") == 0) {
			scanlength = new UniformIntegerGenerator(1, maxscanlength);
		} else if (scanlengthdistrib.compareTo("zipfian") == 0) {
			scanlength = new ZipfianGenerator(1, maxscanlength);
		} else {
			throw new WorkloadException("Distribution \"" + scanlengthdistrib
					+ "\" not allowed for scan length");
		}
		
		localPercentage=new Random();
	}

	/**
	 * Do one insert operation. Because it will be called concurrently from
	 * multiple client threads, this function must be thread safe. However,
	 * avoid synchronized, or the threads will block waiting for each other, and
	 * it will be difficult to reach the target throughput. Ideally, this
	 * function would have no side effects other than DB operations.
	 */
	public boolean doInsert(DB db, Object threadstate) {
		int keynum = keysequence.nextInt();
		if (!orderedinserts) {
			keynum = Utils.hash(keynum);
		}
		String dbkey = "user" + keynum;
		HashMap<String, String> values = new HashMap<String, String>();
		for (int i = 0; i < fieldcount; i++) {
			String fieldkey = "field" + i;
			String data = Utils.ASCIIString(fieldlength);
			values.put(fieldkey, data);
		}
		YCSBTransactionalCreateRequest request = new YCSBTransactionalCreateRequest(
				table, dbkey, values);
		if (db.createTransaction(request) == 0)
			return true;
		else
			return false;
	}

	/**
	 * Do one org.imdea.benchmark.rubis.transaction operation. Because it will be called concurrently from
	 * multiple client threads, this function must be thread safe. However,
	 * avoid synchronized, or the threads will block waiting for each other, and
	 * it will be difficult to reach the target throughput. Ideally, this
	 * function would have no side effects other than DB operations.
	 */
	public boolean doTransaction(DB db, Object threadstate) {
		String op = operationchooser.nextString();

		if (op.compareTo("READ") == 0) {
			db.readTransaction(createReadRequest(ConstantPool.READ_ONLY_TRANSACTION_READ_OPERATION_COUNT));
		} else if (op.compareTo("UPDATE") == 0) {
			db.updateTransaction(createReadRequest(ConstantPool.UPDATE_TRANSACTION_READ_OPERATION_COUNT),createUpdateRequest(ConstantPool.UPDATE_TRANSACTION_WRITE_OPERATION_COUNT));
		} else if (op.compareTo("INSERT") == 0) {
			doTransactionInsert(db);
		} else if (op.compareTo("SCAN") == 0) {
			doTransactionScan(db);
		} else {
			doTransactionReadModifyWrite(db);
		}

		return true;
	}

	private boolean retry(TransactionType type, String parition, String key){
		if (type==TransactionType.Global){
			return false;
		}
		else{
			if (parition.equals(DistributedJessy.getInstance().partitioner.resolve(key).name())){
				return false;
			}
		}
		return true;
		
	}
	
	private List<YCSBTransactionalReadRequest> createReadRequest(int numberOfRequests) {
		List<YCSBTransactionalReadRequest> result = new ArrayList<YCSBTransactionalReadRequest>();

		YCSBTransactionalReadRequest tmp;
		String partition="";
		boolean tryAgain=false;
		int keynum;
		String keyname;
		HashSet<String> fields = null;

		TransactionType type=TransactionType.Global;
		if (ConstantPool.LOCAL_TRANSACTION_PERCENT>0){
			//there must be a portion of local transactions as well. 
			float rand=localPercentage.nextFloat();
			if (rand<=ConstantPool.LOCAL_TRANSACTION_PERCENT){
				type=TransactionType.Local;
			}
		}

		for (int i = 0; i < numberOfRequests; i++) {
			do {

				do {
					keynum = keychooser.nextInt();
				} while (keynum > transactioninsertkeysequence.lastInt());

				if (!orderedinserts) {
					keynum = Utils.hash(keynum);
				}
				keyname = "user" + keynum;

				if (i==0){
					partition=DistributedJessy.getInstance().partitioner.resolve(keyname).name();
					tryAgain=false;
				}
				else{
					tryAgain=retry(type, partition, keyname);
				}
			}while (tryAgain);


			fields = null;

			if (!readallfields) {
				// read a random field
				String fieldname = "field" + fieldchooser.nextString();

				fields = new HashSet<String>();
				fields.add(fieldname);
			}

			//FIX ME
			if (keyname.equals("user100000") || keyname.equals("user200000") || keyname.equals("user300000"))
				continue;
				
			tmp = new YCSBTransactionalReadRequest(table, keyname, fields,
					new HashMap<String, String>());
			result.add(tmp);
		}

		return result;
	}

	private List<YCSBTransactionalUpdateRequest> createUpdateRequest(int numberOfRequests) {
		List<YCSBTransactionalUpdateRequest> result = new ArrayList<YCSBTransactionalUpdateRequest>();
		YCSBTransactionalUpdateRequest tmp;

		String partition="";
		boolean tryAgain=false;
		int keynum;
		String keyname;
		HashMap<String, String> values;

		TransactionType type=TransactionType.Global;
		if (ConstantPool.LOCAL_TRANSACTION_PERCENT>0){
			//there must be a portion of local transactions as well. 
			float rand=localPercentage.nextFloat();
			if (rand<=ConstantPool.LOCAL_TRANSACTION_PERCENT){
				type=TransactionType.Local;
			}
		}
		
		for (int r = 0; r < numberOfRequests; r++) {
			do{
				do {
					keynum = keychooser.nextInt();
				} while (keynum > transactioninsertkeysequence.lastInt());

				if (!orderedinserts) {
					keynum = Utils.hash(keynum);
				}
				keyname = "user" + keynum;
				if (r==0){
					partition=DistributedJessy.getInstance().partitioner.resolve(keyname).name();
					tryAgain=false;
				}
				else{
					tryAgain=retry(type, partition, keyname);
				}
			}while (tryAgain);
			
			values = new HashMap<String, String>();

			if (writeallfields) {
				// new data for all the fields
				for (int i = 0; i < fieldcount; i++) {
					String fieldname = "field" + i;
					String data = Utils.ASCIIString(fieldlength);
					values.put(fieldname, data);
				}
			} else {
				// update a random field
				String fieldname = "field" + fieldchooser.nextString();
				String data = Utils.ASCIIString(fieldlength);
				values.put(fieldname, data);
			}
			

			//FIX ME
			if (keyname.equals("user100000") || keyname.equals("user200000") || keyname.equals("user300000"))
				continue;
			
			tmp=new YCSBTransactionalUpdateRequest(table, keyname, values);
			result.add(tmp);
		}

		return result;
	}

	public void doTransactionRead(DB db) {
		// choose a random key
		int keynum;
		do {
			keynum = keychooser.nextInt();
		} while (keynum > transactioninsertkeysequence.lastInt());

		if (!orderedinserts) {
			keynum = Utils.hash(keynum);
		}
		String keyname = "user" + keynum;

		HashSet<String> fields = null;

		if (!readallfields) {
			// read a random field
			String fieldname = "field" + fieldchooser.nextString();

			fields = new HashSet<String>();
			fields.add(fieldname);
		}

		db.read(table, keyname, fields, new HashMap<String, String>());
	}

	public void doTransactionReadModifyWrite(DB db) {
		// choose a random key
		int keynum;
		do {
			keynum = keychooser.nextInt();
		} while (keynum > transactioninsertkeysequence.lastInt());

		if (!orderedinserts) {
			keynum = Utils.hash(keynum);
		}
		String keyname = "user" + keynum;

		HashSet<String> fields = null;

		if (!readallfields) {
			// read a random field
			String fieldname = "field" + fieldchooser.nextString();

			fields = new HashSet<String>();
			fields.add(fieldname);
		}

		HashMap<String, String> values = new HashMap<String, String>();

		if (writeallfields) {
			// new data for all the fields
			for (int i = 0; i < fieldcount; i++) {
				String fieldname = "field" + i;
				String data = Utils.ASCIIString(fieldlength);
				values.put(fieldname, data);
			}
		} else {
			// update a random field
			String fieldname = "field" + fieldchooser.nextString();
			String data = Utils.ASCIIString(fieldlength);
			values.put(fieldname, data);
		}

		// do the org.imdea.benchmark.rubis.transaction

		long st = System.currentTimeMillis();

		db.read(table, keyname, fields, new HashMap<String, String>());

		db.update(table, keyname, values);

		long en = System.currentTimeMillis();

		Measurements.getMeasurements().measure("READ-MODIFY-WRITE",
				(int) (en - st));
	}

	public void doTransactionScan(DB db) {
		// choose a random key
		int keynum;
		do {
			keynum = keychooser.nextInt();
		} while (keynum > transactioninsertkeysequence.lastInt());

		if (!orderedinserts) {
			keynum = Utils.hash(keynum);
		}
		String startkeyname = "user" + keynum;

		// choose a random scan length
		int len = scanlength.nextInt();

		HashSet<String> fields = null;

		if (!readallfields) {
			// read a random field
			String fieldname = "field" + fieldchooser.nextString();

			fields = new HashSet<String>();
			fields.add(fieldname);
		}

		db.scan(table, startkeyname, len, fields,
				new Vector<HashMap<String, String>>());
	}

	public void doTransactionUpdate(DB db) {
		// choose a random key
		int keynum;
		do {
			keynum = keychooser.nextInt();
		} while (keynum > transactioninsertkeysequence.lastInt());

		if (!orderedinserts) {
			keynum = Utils.hash(keynum);
		}
		String keyname = "user" + keynum;

		HashMap<String, String> values = new HashMap<String, String>();

		if (writeallfields) {
			// new data for all the fields
			for (int i = 0; i < fieldcount; i++) {
				String fieldname = "field" + i;
				String data = Utils.ASCIIString(fieldlength);
				values.put(fieldname, data);
			}
		} else {
			// update a random field
			String fieldname = "field" + fieldchooser.nextString();
			String data = Utils.ASCIIString(fieldlength);
			values.put(fieldname, data);
		}

		db.update(table, keyname, values);
	}

	public void doTransactionInsert(DB db) {
		// choose the next key
		int keynum = transactioninsertkeysequence.nextInt();
		if (!orderedinserts) {
			keynum = Utils.hash(keynum);
		}
		String dbkey = "user" + keynum;

		HashMap<String, String> values = new HashMap<String, String>(fieldlength);
		for (int i = 0; i < fieldcount; i++) {
			String fieldkey = "field" + i;
			String data = Utils.ASCIIString(fieldlength);
			values.put(fieldkey, data);
		}
		db.insert(table, dbkey, values);
	}
}