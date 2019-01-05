package com.zjmvn.rtidb;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com._4paradigm.rtidb.client.TabletException;

public class RtidbApp {

	private static final String TAG = RtidbApp.class.getSimpleName() + " => ";
	private static final Logger logger = Logger.getLogger(RtidbApp.class);

	private static final String ARG_IS_USE_POOL = "pool";
	private static final String ARG_TABLE_NAME = "table";
	private static final String ARG_ZK_ENDPOINTS = "zkips";
	private static final String ARG_ZK_ROOT = "zkroot";
	private static final String ARG_THREAD_NUM = "tnum";
	private static final String ARG_ROW_COUNT = "rcount";

	private boolean isUsePool;
	private String tableName;
	private String zkEndpoints;
	private String zkRootPath;
	private String threadNum;
	private String rowCount;

	// cmd: java -jar test.jar table=applyfraud
	// zkips="172.27.133.60:44440,172.27.133.61:44440,172.27.133.62:44440"
	// zkroot="/rtidb/rtidb_cluster"
	// tnum=4 rcount=10 pool=y
	public static void main(String[] args) {

		RtidbApp app = new RtidbApp();
		app.parseArgs(args);

		RtidbClient client = null;
		try {
			client = RtidbClient.GetInstance().build(app.zkEndpoints, app.zkRootPath);
			RtidbUtils.getInstance().build(client);

			boolean ok = app.caseShowSchemaTable();
			if (!ok) {
				logger.error(TAG + "table not exist: " + app.tableName);
				return;
			}

			ok = app.casePutAndGetSchemaTable();
			if (!ok) {
				logger.error(TAG + "put or get error for table: " + app.tableName);
				return;
			}

			if (app.isUsePool) {
				app.casePerformTestByPool();
			} else {
				app.casePerformTestByThreads();
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(TAG + e.getMessage());
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

	private void parseArgs(String[] args) {
		Map<String, String> configs = new HashMap<>(args.length * 2);
		for (String arg : args) {
			String[] kv = arg.split("=");
			configs.put(kv[0].toLowerCase().trim(), kv[1]);
		}

		this.isUsePool = "y".equals(configs.getOrDefault(ARG_IS_USE_POOL, "y")) ? true : false;
		this.tableName = configs.getOrDefault(ARG_TABLE_NAME, "applyfraud");
		this.zkEndpoints = configs.getOrDefault(ARG_ZK_ENDPOINTS,
				"172.27.133.60:44440,172.27.133.61:44440,172.27.133.62:44440");
		this.zkRootPath = configs.getOrDefault(ARG_ZK_ROOT, "/rtidb/rtidb_cluster");
		this.threadNum = configs.getOrDefault(ARG_THREAD_NUM, "2");
		this.rowCount = configs.getOrDefault(ARG_ROW_COUNT, "10");
	}

	private boolean caseShowSchemaTable() {
		return RtidbUtils.getInstance().showSchemaTables(this.tableName);
	}

	private boolean casePutAndGetSchemaTable() throws TimeoutException, TabletException {
		String indexKey = "ins_var217";
		String value = "ins_var217_000001";

		Map<String, Object> row = new HashMap<>();
		row.put(indexKey, value);
		row.put("application", "application_000001");
		row.put("ins_var86", "ins_var86_000001");

		boolean ok = RtidbUtils.getInstance().syncPutSchemaTable(this.tableName, row);
		if (!ok) {
			logger.error(TAG + "put row failed: " + row);
			return false;
		}

		Object[] rows = RtidbUtils.getInstance().syncGetSchemaTable(this.tableName, indexKey, value);
		for (Object r : rows) {
			logger.info(TAG + r);
		}
		return rows.length > 0 ? true : false;
	}

	private void casePerformTestByPool() {
		final int poolSize = 6;
		int pCount = Integer.parseInt(this.threadNum);
		CountDownLatch countDownLatch = new CountDownLatch(pCount);

		ExecutorService pool = Executors.newFixedThreadPool(poolSize);
		for (int i = 0; i < pCount; i++) {
			final int idx = i;
			pool.execute(new PutRowsRunnable(this, idx, countDownLatch));
		}

		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		pool.shutdown();

		logger.info(TAG + String.format(TAG + "%d rows put done.", pCount * Integer.parseInt(this.rowCount)));
	}

	private void casePerformTestByThreads() {
		int pCount = Integer.parseInt(this.threadNum);
		CountDownLatch countDownLatch = new CountDownLatch(pCount);

		for (int i = 0; i < pCount; i++) {
			final int idx = i;
			new Thread(new PutRowsRunnable(this, idx, countDownLatch)).start();
		}

		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		logger.info(TAG + String.format("%d rows put done.", pCount * Integer.parseInt(this.rowCount)));
	}

	private static class PutRowsRunnable implements Runnable {

		private String tname;
		private RtidbUtils db;
		private int pid;
		private int rowCount;
		private CountDownLatch countDownLatch;

		public PutRowsRunnable(RtidbApp app, int pid, CountDownLatch countDownLatch) {
			this.tname = app.tableName;
			this.db = RtidbUtils.getInstance();
			this.pid = pid;
			this.rowCount = Integer.parseInt(app.rowCount);
			this.countDownLatch = countDownLatch;
		}

		@Override
		public void run() {
			long timestamp = System.currentTimeMillis() + new Random().nextInt(100);
			String tmpID = String.format("%d_%d_", timestamp, this.pid);
			Map<String, Object> row = new HashMap<>();

			try {
				for (int j = 0; j < this.rowCount; j++) {
					row.clear();
					String suffix = tmpID + String.valueOf(j);
					row.put("ins_var217", "ins_var217_" + suffix);
					row.put("application", "application_" + suffix);
					row.put("ins_var86", "ins_var86_" + suffix);
					boolean ok = db.syncPutSchemaTable(tname, row);

					if (j % (rowCount / 10) == 0) {
						String pName = Thread.currentThread().getName();
						logger.info(String.format(TAG + "%s put record %s: %s", pName, "ins_var217_" + suffix, ok));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				return;
			} finally {
				this.countDownLatch.countDown();
			}
		}
	}

}
