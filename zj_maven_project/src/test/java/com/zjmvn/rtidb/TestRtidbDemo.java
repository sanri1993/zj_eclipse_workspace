package com.zjmvn.rtidb;

import com._4paradigm.rtidb.client.GetFuture;
import com._4paradigm.rtidb.client.PutFuture;
import com._4paradigm.rtidb.client.TableAsyncClient;
import com._4paradigm.rtidb.client.TableSyncClient;
import com._4paradigm.rtidb.client.TabletException;
import com._4paradigm.rtidb.client.ha.RTIDBClientConfig;
import com._4paradigm.rtidb.client.ha.impl.NameServerClientImpl;
import com._4paradigm.rtidb.client.ha.impl.RTIDBClusterClient;
import com._4paradigm.rtidb.client.impl.TableAsyncClientImpl;
import com._4paradigm.rtidb.client.impl.TableSyncClientImpl;
import com._4paradigm.rtidb.ns.NS;
import com.google.protobuf.ByteString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Ignore
public class TestRtidbDemo {

	private final static String TAG = TestRtidbDemo.class.getSimpleName() + " => ";
	private final static String kVTableName = "test_kv_table_01";
	private final static String SchemaTableName = "test_schema_table_01";

	private static Logger logger = Logger.getLogger(TestRtidbDemo.class);

	private static NameServerClientImpl nsc = null;
	private static RTIDBClusterClient clusterClient = null;
	private static TableSyncClient tableSyncClient = null;
	private static TableAsyncClient tableAsyncClient = null;

	@BeforeClass
	public static void beforeClass() throws Exception {
		String zkEndpoints = "172.27.128.31:48121,172.27.128.32:48121,172.27.128.33:48121";
		String zkRootPath = "/rtidb/rtidb_cluster";
		String leaderPath = zkRootPath + "/leader";

		nsc = new NameServerClientImpl(zkEndpoints, leaderPath);
		nsc.init();

		RTIDBClientConfig config = new RTIDBClientConfig();
		config.setZkEndpoints(zkEndpoints);
		config.setZkRootPath(zkRootPath);
		clusterClient = new RTIDBClusterClient(config);
		clusterClient.init();

		tableSyncClient = new TableSyncClientImpl(clusterClient);
		tableAsyncClient = new TableAsyncClientImpl(clusterClient);
	}

	@AfterClass
	public static void afterClass() {
		if (clusterClient != null) {
			clusterClient.close();
		}
		if (nsc != null) {
			nsc.close();
		}
	}

	@Test
	public void testCreateKVTable() {
		NS.TableInfo.Builder builder = NS.TableInfo.newBuilder();
		builder = NS.TableInfo.newBuilder().setName(kVTableName)
//				.setReplicaNum(3)
//				.setPartitionNum(8)
//				.setCompressType(NS.CompressType.kSnappy)
//				.setTtlType("kLatestTime")
				.setTtl(144000);
		NS.TableInfo table = builder.build();
		boolean ok = nsc.createTable(table);
		Assert.assertTrue(ok);

		if (clusterClient != null) {
			clusterClient.refreshRouteTable();
		}
	}

	@Test
	public void testShowKVTables() {
		List<NS.TableInfo> tlist = nsc.showTable(kVTableName);
		if (tlist.size() == 0) {
			Assert.fail("table not found: " + kVTableName);
		}
		for (NS.TableInfo t : tlist) {
			logger.info(TAG + "table: " + t.getName());
		}
	}

	@Test
	public void testSyncPutKVTable() throws TimeoutException, TabletException {
		long ts = System.currentTimeMillis();
		Assert.assertTrue(tableSyncClient.put(kVTableName, "key1", ts, "value0"));
		Assert.assertTrue(tableSyncClient.put(kVTableName, "key1", ts + 1, "value1"));
		Assert.assertTrue(tableSyncClient.put(kVTableName, "key2", ts + 2, "value2"));
	}

	@Test
	public void testSyncGetKVTable() throws TimeoutException, TabletException {
		String key = "key1";
		long ts = 0;
		ByteString bs = tableSyncClient.get(kVTableName, key, ts);
		Assert.assertNotNull(bs);
		logger.info(TAG + String.format("%s = %s", key, new String(bs.toByteArray())));
	}

	@Test
	@Ignore
	public void testDeleteKVTable() {
		Assert.assertTrue(nsc.dropTable(kVTableName));
		if (clusterClient != null) {
			clusterClient.refreshRouteTable();
		}
	}

	@Test
	public void testCreateSchemaTable() {
		NS.TableInfo.Builder builder = NS.TableInfo.newBuilder();
		builder = NS.TableInfo.newBuilder().setName(SchemaTableName)
				// .setReplicaNum(3)
				// .setPartitionNum(8)
				// .setCompressType(NS.CompressType.kSnappy)
				// .setTtlType("kLatestTime")
				.setTtl(144000);

		NS.ColumnDesc col0 = NS.ColumnDesc.newBuilder().setName("card").setAddTsIdx(true).setType("string").build();
		NS.ColumnDesc col1 = NS.ColumnDesc.newBuilder().setName("mcc").setAddTsIdx(true).setType("string").build();
		NS.ColumnDesc col2 = NS.ColumnDesc.newBuilder().setName("money").setAddTsIdx(false).setType("float").build();
		builder.addColumnDesc(col0).addColumnDesc(col1).addColumnDesc(col2);
		NS.TableInfo table = builder.build();

		boolean ok = nsc.createTable(table);
		Assert.assertTrue(ok);
		if (clusterClient != null) {
			clusterClient.refreshRouteTable();
		}
	}

	@Test
	public void testShowSchemaTables() {
		List<NS.TableInfo> tlist = nsc.showTable(SchemaTableName);
		if (tlist.size() == 0) {
			Assert.fail("table not found: " + kVTableName);
		}

		for (NS.TableInfo table : tlist) {
			logger.info(TAG + "table: " + table.getName());
			for (NS.ColumnDesc col : table.getColumnDescList()) {
				logger.info(TAG + String.format("column %s, type: %s", col.getName(), col.getType()));
			}
		}
	}

	@Test
	public void testSyncPutSchemaTable() throws TimeoutException, TabletException {
		long ts = System.currentTimeMillis();
		Map<String, Object> row = new HashMap<>();
		row.put("card", "card0");
		row.put("mcc", "mcc0");
		row.put("money", 1.3f);
		Assert.assertTrue(tableSyncClient.put(SchemaTableName, ts, row));

		row.clear();
		row.put("card", "card0");
		row.put("mcc", "mcc1");
		row.put("money", 15.8f);
		Assert.assertTrue(tableSyncClient.put(SchemaTableName, ts + 1, row));
	}

	@Test
	public void testSyncGetSchemaTable() throws TimeoutException, TabletException {
		Object[] rows = tableSyncClient.getRow(SchemaTableName, "card0", "card", 0);
		Assert.assertTrue(rows.length > 0);
		for (Object row : rows) {
			logger.info(TAG + row);
		}
	}

	@Test
	public void testAsyncPutSchemaTable() throws TabletException, InterruptedException, ExecutionException {
		long ts = System.currentTimeMillis();
		Map<String, Object> row = new HashMap<String, Object>();
		row.put("card", "acard1");
		row.put("mcc", "amcc1");
		row.put("money", 1.6f);
		PutFuture pf1 = tableAsyncClient.put(SchemaTableName, ts, row);

		row.clear();
		row.put("card", "acard1");
		row.put("mcc", "amcc2");
		row.put("money", 25.8f);
		PutFuture pf2 = tableAsyncClient.put(SchemaTableName, ts + 1, row);

		Assert.assertTrue(pf1.get());
		Assert.assertTrue(pf2.get());
	}

	@Test
	public void testAsyncGetSchemaTable() throws TabletException, InterruptedException, ExecutionException {
		GetFuture gf = tableAsyncClient.get(SchemaTableName, "acard1", "card", 0);
		Assert.assertNotNull(gf);

		Object[] rows = gf.getRow();
		for (Object row : rows) {
			logger.info(TAG + row);
		}
	}

}
