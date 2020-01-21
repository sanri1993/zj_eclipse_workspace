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
		zkRootPath = properties.getProperty("zk_root_path");
	}

}
