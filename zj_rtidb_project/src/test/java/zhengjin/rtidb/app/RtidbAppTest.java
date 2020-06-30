package zhengjin.rtidb.app;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._4paradigm.rtidb.client.KvIterator;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RtidbAppTest {

	private static final String TAG = RtidbApp.class.getSimpleName() + " => ";
	private static final Logger LOG = LoggerFactory.getLogger(RtidbAppTest.class);

	private static final String kvTbName = "zj_kv_test1";
	private static final String schemaTbName01 = "zj_schema_test1";
	private static final String schemaTbName02 = "zj_schema_test2";

	private static RtidbUtils rtidb;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		LOG.info(TAG + "Rtidb Test");
		rtidb = RtidbUtils.getInstance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		RtidbClient.close();
	}

	@Test
	public void test01CreateAndPutKVTable() throws Exception {
		Assert.assertTrue(rtidb.createKVTable(kvTbName));

		String key = "key1";
		long ts = System.currentTimeMillis();
		rtidb.syncPutKVTable(kvTbName, key, ts, "value0");
		rtidb.syncPutKVTable(kvTbName, key, ts + 1, "value1");
	}

	@Test
	public void test02GetKVTable() throws Exception {
		String key = "key1";
		long ts = 1579593309601L;
		String value = rtidb.syncGetKVTable(kvTbName, key, ts);
		Assert.assertNotNull(value);
		System.out.println("KV table get value: " + value);
	}

	@Test
	public void test03ScanKVTable() throws Exception {
		String key = "key1";
		long ts = 1579593309601L;

		List<String> values = rtidb.syncScanKVTable(kvTbName, key, ts + 1, 0);
		Assert.assertTrue(values.size() > 0);
		System.out.println("KV Table scan values:");
		for (String v : values) {
			System.out.println(v);
		}
	}

	@Test
	public void test11CreateSchemaTable() throws Exception {
		Assert.assertTrue(rtidb.createSchemaTable01(schemaTbName01));
	}

	@Test
	public void test12PutSchemaTable() throws Exception {
		long ts = System.currentTimeMillis();

		Map<String, Object> row = new HashMap<String, Object>();
		row.put("card", "card0");
		row.put("mcc", "mcc0");
		row.put("money", 1.3F);
		Assert.assertTrue(rtidb.syncPutSchemaTable(schemaTbName01, ts, row));

		row.clear();
		row.put("card", "card0");
		row.put("mcc", "mcc1");
		row.put("money", 15.8F);
		Assert.assertTrue(rtidb.syncPutSchemaTable(schemaTbName01, ts + 1, row));

		Object[] arr = new Object[] { "card1", "mcc1", 9.15f };
		Assert.assertTrue(rtidb.syncPutSchemaTable(schemaTbName01, ts + 2, arr));
	}

	@Test
	public void test13GetSchemaTable() throws Exception {
		long ts = 1579593525731L;

		Object[] fields = rtidb.syncGetSchemaTable(schemaTbName01, ts, "card0", "card");
		Assert.assertTrue(fields.length > 0);
		System.out.println("Schema table get record:");
		for (Object f : fields) {
			System.out.print(f + ",");
		}
		System.out.println();

		// ts设置为0, 返回最新插⼊的⼀条数据
		fields = rtidb.syncGetSchemaTable(schemaTbName01, 0, "card0", "card");
		Assert.assertTrue(fields.length > 0);
		System.out.println("Schema table get latest record:");
		for (Object f : fields) {
			System.out.print(f + ",");
		}
		System.out.println();
	}

	@Test
	public void test14ScanSchemaTable() throws Exception {
		long ts = 1579593525731L;

		// 为0, 返回起始时间之前的所有数据
		System.out.println("Schema table scan results:");
		List<Object[]> records = rtidb.syncScanSchemaTable(schemaTbName01, ts + 1, 0, "card0", "card");
		Assert.assertTrue(records.size() > 0);
		for (Object[] fields : records) {
			for (Object f : fields) {
				System.out.print(f + ",");
			}
			System.out.println();
		}
	}

	@Test
	public void perfTest15PutSchemaTable() throws Exception {
		int count = 10;
		long ts = System.currentTimeMillis();
		long start;
		Random rand = new Random();
		List<Long> timestamps = new LinkedList<>();
		Map<String, Object> row = new HashMap<String, Object>();

		for (int i = 0; i < count; i++) {
			row.put("card", "card1");
			row.put("mcc", "mcc1");
			row.put("money", rand.nextFloat() * 100F);

			start = System.currentTimeMillis();
			rtidb.syncPutSchemaTable(schemaTbName01, ts + i, row);
			timestamps.add(System.currentTimeMillis() - start);
			row.clear();
		}

		long sum = 0L;
		System.out.println("put time:");
		for (long t : timestamps) {
			System.out.print(t + ",");
			sum += t;
		}
		System.out.println("total put time: " + sum);
	}

	/**
	 * 组合key和指定ts列
	 */
	@Test
	public void test21CreateSchemaTable() throws Exception {
		Assert.assertTrue(rtidb.createSchemaTable02(schemaTbName02));
	}

	@Test
	public void test22PutAndScanSchemaTable() throws Exception {
		Map<String, Object> data = new HashMap<>();
		data.put("card", "card0");
		data.put("mcc", "mcc0");
		data.put("amt", 1.5);
		data.put("ts", 1234L);
		Assert.assertTrue(RtidbClient.getTableSyncClient().put(schemaTbName02, data));

		data.clear();
		data.put("card", "card0");
		data.put("mcc", "mcc1");
		data.put("amt", 1.6);
		data.put("ts", 1235L);
		Assert.assertTrue(RtidbClient.getTableSyncClient().put(schemaTbName02, data));

		Map<String, Object> scan_key = new HashMap<>();
		scan_key.put("card", "card0");
		scan_key.put("mcc", "mcc0");
		KvIterator it = RtidbClient.getTableSyncClient().scan(schemaTbName02, scan_key, "card_mcc", 1235L, 0L, "ts", 0);
		Object[] row = it.getDecodedValue();
		System.out.println("scan results for mcc0: " + row);

		scan_key.put("mcc", "mcc1");
		it = RtidbClient.getTableSyncClient().scan(schemaTbName02, scan_key, "card_mcc", 1235L, 0L, "ts", 0);
		System.out.println("scan results for mcc1: " + it.getDecodedValue());

		row = RtidbClient.getTableSyncClient().getRow(schemaTbName02, new Object[] { "card0", "mcc0" }, "card_mcc",
				1234, "ts", null);
		System.out.println("get results for mcc0: " + row);

		Map<String, Object> key_map = new HashMap<String, Object>();
		key_map.put("card", "card0");
		key_map.put("mcc", "mcc1");
		row = RtidbClient.getTableSyncClient().getRow(schemaTbName02, key_map, "card_mcc", 1235L, "ts", null);
		System.out.println("get results for mcc1: " + row);
	}

}
