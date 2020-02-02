package zhengjin.jmeter.app;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zhengjin.jmeter.utils.Common;

public class JmeterApp {

	private static final String TAG = JmeterApp.class.getSimpleName();
	private static final Logger LOG = LoggerFactory.getLogger(JmeterApp.class);

	public static void main(String[] args) throws IOException {
		LOG.info(TAG + ":JMeter App start.");

		final String filepath = "/data.json";
		JmeterApp app = new JmeterApp();
		app.checkResource(filepath);
		String content = Common.readResource(filepath);
		LOG.info("file content:\n" + content);
	}

	public boolean checkResource(String filePath) {
		String path = Object.class.getResource(filePath).getPath();
		Assert.assertNotNull(path);

		File f = new File(path);
		if (f.exists()) {
			LOG.info("file exists: " + f.getAbsolutePath());
		} else {
			LOG.info("file not exists: " + f.getAbsolutePath());
		}
		return f.exists();
	}

}
