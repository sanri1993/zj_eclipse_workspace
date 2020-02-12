package zhengjin.rtidb.app;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zhengjin.rtidb.runner.MultipleProcess;

public final class RtidbApp {

	private static final String TAG = RtidbApp.class.getSimpleName() + " => ";
	private static final Logger LOG = LoggerFactory.getLogger(RtidbApp.class);

	/**
	 * Run cmd: java -cp zj-rtidb-app.jar zhengjin.rtidb.app.RtidbApp 10
	 * 
	 * Rtidb cli: ./bin/rtidb
	 * --zk_cluster=172.27.128.33:5181,172.27.128.32:5181,172.27.128.31:5181
	 * --zk_root_path=/rtidb_cluster --role=ns_client
	 * 
	 */
	public static void main(String[] args) throws Exception {
		LOG.info(String.format("Connect to rtidb, zk:[%s], root:[%s]", RtidbEnv.zkEndpoints, RtidbEnv.zkRootPath));

		boolean runFlag1 = false;
		if (runFlag1) {
			RtidbDemo demo = new RtidbDemo();
			int count = 10;
			if (args.length > 0) {
				count = Integer.parseInt(args[0]);
			}
			demo.schemaTablePutPerfTest02(count);
		}

		int threadNum = 1;
		int delay = 10;
		if (args.length < 2) {
			LOG.warn("No intput arguments [threadNum] and [delay] found, and use default 1 and 10.");
		} else {
			threadNum = Integer.parseInt(args[0]);
			delay = Integer.parseInt(args[0]);
		}

		MultipleProcess p = new MultipleProcess(threadNum, delay);
		while (!RtidbClient.isReady()) {
			LOG.info("Wait for rtidb client ready");
			TimeUnit.SECONDS.sleep(3);
		}
		p.runProcesses(new PutDataProcess());

		LOG.info(TAG + "Rtidb App");
	}

	private static class PutDataProcess implements Runnable {

		private static RtidbDemo demo = new RtidbDemo();

		@Override
		public void run() {
			try {
				while (MultipleProcess.isRunning()) {
					demo.schemaTablePutDataTest03();
				}
			} catch (Exception e) {
				e.printStackTrace();
				LOG.warn(e.getMessage());
			}
		}
	}

}
