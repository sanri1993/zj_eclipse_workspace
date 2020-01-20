package zhengjin.rtidb.app;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RtidbAppTest01 {

	private static final String TAG = RtidbApp.class.getSimpleName() + " => ";
	private static final Logger LOG = LoggerFactory.getLogger(RtidbAppTest01.class);

	private static RtidbUtils rtidb;
	private static String tname;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		LOG.info(TAG + "KV Table Test");
		rtidb = RtidbUtils.getInstance();
		tname = "zj_test1";
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		RtidbClient.close();
	}

	@Test
	public void test01CreateKVTable() throws Exception {
		Assert.assertTrue(!rtidb.createKVTable(tname));
	}

	@Test
	public void test02PutAndGetKVTable() throws Exception {
		String key = "key1";
		long ts = System.currentTimeMillis();
		rtidb.syncPutKVTable(tname, key, ts, "value0");
		rtidb.syncPutKVTable(tname, key, ts + 1, "value1");
		TimeUnit.SECONDS.sleep(1);

		String record = rtidb.syncGetKVTable(tname, key, ts);
		Assert.assertNotNull(record);
		System.out.println("KV table get results:" + record);
	}

	@Test
	public void test03PutAndScanKVTable() throws Exception {
		String key = "key2";
		long ts = System.currentTimeMillis();
		rtidb.syncPutKVTable(tname, key, ts, "value0");
		rtidb.syncPutKVTable(tname, key, ts + 1, "value1");
		TimeUnit.SECONDS.sleep(1);

		List<String> records = rtidb.syncScanKVTable(tname, key, ts + 1, 0);
		System.out.println("KV Table scan results:");
		for (String r : records) {
			System.out.println(r);
		}
	}

}
