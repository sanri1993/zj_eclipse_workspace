package zhengjin.rtidb.app;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._4paradigm.rtidb.client.TabletException;

public class RtidbApp {

	private static final String TAG = RtidbApp.class.getSimpleName() + " => ";
	private static final Logger LOG = LoggerFactory.getLogger(RtidbApp.class);

	RtidbUtils rtidb = RtidbUtils.getInstance();

	public static void main(String[] args) throws Exception {

		RtidbApp app = new RtidbApp();
		app.kvTableTest();
	}

	private void kvTableTest() throws TimeoutException, TabletException, InterruptedException {
		LOG.info(TAG + "KV Table Test");
		final String tname = "zj_test1";
		if (!rtidb.createKVTable(tname)) {
			LOG.error("Create KV Table failed");
		}

		long ts = System.currentTimeMillis();
		rtidb.syncPutKVTable(tname, "key1", ts, "value0");
		rtidb.syncPutKVTable(tname, "key1", ts + 1, "value1");
		rtidb.syncPutKVTable(tname, "key2", ts + 2, "value2");
		TimeUnit.SECONDS.sleep(1);

		List<String> records = rtidb.syncScanKVTable(tname, "key1", ts + 1, 0);
		System.out.println("KV Table scan results:");
		for (String r : records) {
			System.out.println(r);
		}

		System.out.println("KV Table get results:");
		String record = rtidb.syncGetKVTable(tname, "key1", ts);
		System.out.println(record);
	}

}
