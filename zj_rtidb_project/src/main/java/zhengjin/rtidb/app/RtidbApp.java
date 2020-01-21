package zhengjin.rtidb.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RtidbApp {

	private static final String TAG = RtidbApp.class.getSimpleName() + " => ";
	private static final Logger LOG = LoggerFactory.getLogger(RtidbApp.class);

	/**
	 * Run cmd: java -cp target/zj-rtidb-app.jar zhengjin.rtidb.app.RtidbApp 11
	 */
	public static void main(String[] args) throws Exception {

		int count = 10;
		if (args.length > 0) {
			count = Integer.parseInt(args[0]);
		}

		RtidbDemo demo = new RtidbDemo();
		demo.schemaTablePutPerfTest(count);

		LOG.info(TAG + "Rtidb App");
	}

}
