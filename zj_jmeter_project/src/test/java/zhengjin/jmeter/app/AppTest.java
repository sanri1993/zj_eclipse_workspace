package zhengjin.jmeter.app;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zhengjin.jmeter.utils.Common;

/**
 * Unit test for simple App.
 */
public final class AppTest {

	private static final Logger LOG = LoggerFactory.getLogger(AppTest.class);

	@Before
	public void setUp() {
		LOG.info("Test Setup");
	}

	@Test
	public void test01CheckResource() {
		JmeterApp app = new JmeterApp();
		Assert.assertTrue(app.checkResource("/data.json"));
	}

	@Test
	public void test02ReadResource() {
		String content = "";
		try {
			content = Common.readResource("/data.json");
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}

		Assert.assertTrue(content.length() > 0);
		LOG.info("File Content:\n" + content);
	}

}
