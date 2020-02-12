package zhengjin.rtidb.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("resource")
public final class RtidbEnv {

	private static final String TAG = RtidbApp.class.getSimpleName() + " => ";
	private static final Logger LOG = LoggerFactory.getLogger(RtidbEnv.class);

	// rtidb configs
	public static String zkEndpoints;
	public static String zkRootPath;

	public static String threadNum;
	public static String runSecs;
	public static String tbName;

	private static Properties properties = new Properties();

	static {
		final String fileName = "rtidb.properties";
		String filePath = System.getProperty("user.dir") + File.separator + fileName;

		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(filePath);
		} catch (FileNotFoundException e) {
			LOG.warn(String.format(TAG + "Rtidb properties file not exist (%s), and use default", filePath));
			inputStream = Object.class.getResourceAsStream("/" + fileName);
		}

		try {
			properties.load(inputStream);
		} catch (IOException e) {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
		}

		zkEndpoints = properties.getProperty("zk_cluster");
		if (zkEndpoints == null) {
			LOG.error("zkEndpoints is not set.");
			System.exit(99);
		}
		zkRootPath = properties.getProperty("zk_root_path");
		if (zkRootPath == null) {
			LOG.error("zkRootPath is not set!");
			System.exit(99);
		}

		tbName = properties.getProperty("tbname", "zj_ritdb_tb_test");
		threadNum = properties.getProperty("thread_num", "1");
		runSecs = properties.getProperty("run_secs", "10");
	}

	public static void main(String[] args) {

		LOG.info("table:{}, threads number:{}, run seconds:{}", tbName, threadNum, runSecs);
	}

}
