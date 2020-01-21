package zhengjin.rtidb.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RtidbApp {

	private static final String TAG = RtidbApp.class.getSimpleName() + " => ";
	private static final Logger LOG = LoggerFactory.getLogger(RtidbApp.class);

	/**
	 * Run cmd: java -cp zj-rtidb-app.jar zhengjin.rtidb.app.RtidbApp 11
	 * 
	 * Rtidb cli: ./bin/rtidb
	 * --zk_cluster=172.27.128.33:5181,172.27.128.32:5181,172.27.128.31:5181
	 * --zk_root_path=/rtidb_cluster --role=ns_client
	 * 
	 */
	public static void main(String[] args) throws Exception {

		LOG.info(String.format("Connect to rtidb, zk:[%s], root:[%s]", RtidbEnv.zkEndpoints, RtidbEnv.zkRootPath));

		int count = 10;
		if (args.length > 0) {
			count = Integer.parseInt(args[0]);
		}

		RtidbDemo demo = new RtidbDemo();
		demo.schemaTablePutPerfTest01(count);

		LOG.info(TAG + "Rtidb App");
	}

}
