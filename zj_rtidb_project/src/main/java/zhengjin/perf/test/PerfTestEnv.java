package zhengjin.perf.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PerfTestEnv {

	private static final Logger LOG = LoggerFactory.getLogger(PerfTestEnv.class);

	public static String action; // action type: create, put, get
	public static long runTime; // millis
	public static int threads;

	public static String keyPrefix;
	public static int keyRangeStart;
	public static int keyRangeEnd;
	public static int rpsLimit;

	public static int matrixInterval;
	public static String rsTimeUnit;
	public static boolean isDebug;

	private static Properties properties = new Properties();

	static {
		final String fileName = "perf_conf.properties";
		String filePath = System.getProperty("user.dir") + File.separator + fileName;

		InputStream inputStream = null;
		File f = new File(filePath);
		try {
			if (f.exists()) {
				inputStream = new FileInputStream(filePath);
			} else {
				LOG.warn(String.format("Perf test properties file not exist (%s), and use default", filePath));
				inputStream = Object.class.getResourceAsStream("/" + fileName);
			}
			properties.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}

		action = properties.getProperty("action", "get");
		runTime = Integer.valueOf(properties.getProperty("runtime", "5").toString());
		threads = Integer.valueOf(properties.getProperty("threads", "1").toString());

		keyPrefix = properties.getProperty("key_prefix", "id");
		keyRangeStart = Integer.valueOf(properties.getProperty("key_range_start", "1").toString());
		keyRangeEnd = Integer.valueOf(properties.getProperty("key_range_end", "10000").toString());
		rpsLimit = Integer.valueOf(properties.getProperty("rps_limit", "100").toString());

		matrixInterval = Integer.valueOf(properties.getProperty("matrix_interval", "30").toString());
		rsTimeUnit = properties.getProperty("rt_time_unit", "ms");
		isDebug = Boolean.parseBoolean(properties.getProperty("is_debug", "false"));
	}

	static void printPerfTestEnv() {
		LOG.info(String.format("[Config] action:%s, runtime:%d, threads:%d", action, runTime, threads));
		LOG.info(String.format("key_prefix:%s, key_range_start:%d, key_range_end:%d, rps_limit:%d", keyPrefix,
				keyRangeStart, keyRangeEnd, rpsLimit));
		LOG.info(String.format("matrix_interval: %d, rt_time_unit: %s, is_debug: %s", matrixInterval, rsTimeUnit,
				isDebug));
	}

	public static void main(String[] args) {

		printPerfTestEnv();
	}

}
