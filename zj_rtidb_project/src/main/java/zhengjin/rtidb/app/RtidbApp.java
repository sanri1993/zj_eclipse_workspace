package zhengjin.rtidb.app;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zhengjin.rtidb.runner.MultipleProcess;

public final class RtidbApp {

	private static final String TAG = RtidbApp.class.getSimpleName() + " => ";
	private static final Logger LOG = LoggerFactory.getLogger(RtidbApp.class);

	public static void main(String[] args) throws Exception {
		// Rtidb cli: ./bin/rtidb
		// --zk_cluster=172.27.128.33:5181,172.27.128.32:5181,172.27.128.31:5181
		// --zk_root_path=/rtidb_cluster --role=ns_client
		LOG.info("Connect to rtidb, zk:[{}], root:[{}]", RtidbEnv.zkEndpoints, RtidbEnv.zkRootPath);

		LOG.debug("input arguments: {}", Arrays.toString(args));
		String runType = "test";
		if (args.length > 0) {
			runType = args[0];
		}
		RtidbDemo demo = new RtidbDemo();

		if ("test".equals(runType)) {
			int count = 10;
			if (args.length > 1) {
				count = Integer.parseInt(args[1]);
			}
			demo.schemaTablePutPerfTest02(count);
		}

		if ("putdata".equals(runType)) {
			int secs = Integer.parseInt(RtidbEnv.runSecs);
			MultipleProcess p = new MultipleProcess(secs);
			while (!RtidbClient.isReady()) {
				LOG.info("Wait for rtidb client ready");
				TimeUnit.SECONDS.sleep(3);
			}
			int threadNum = Integer.parseInt(RtidbEnv.threadNum);
			p.runProcesses(new PutDataProcess(demo, p), threadNum);
		}

		LOG.info(TAG + "Rtidb App.");
	}

	private static class PutDataProcess implements Runnable {

		private RtidbDemo demo;
		private MultipleProcess runner;

		public PutDataProcess(RtidbDemo app, MultipleProcess p) {
			this.demo = app;
			this.runner = p;
		}

		@Override
		public void run() {
			try {
				while (this.runner.isRunning()) {
					demo.schemaTablePutDataTest03();
				}
			} catch (Exception e) {
				e.printStackTrace();
				LOG.warn(e.getMessage());
			}
		}
	}

}
