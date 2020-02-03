package zhengjin.jmeter.junitsampler;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import zhengjin.jmeter.utils.Common;

/**
 * Junit samplers for Get and Post methods.
 * 
 * @author zhengjin
 *
 */
public final class JunitSampler01 {

	private static final Logger LOG = LoggerFactory.getLogger(JunitSampler01.class);
	private static final String TAG = JunitSampler01.class.getSimpleName();
	private static final String baseUrl = "http://127.0.0.1:17891";

	public JunitSampler01() {
	}

	public JunitSampler01(String label) {
		LOG.info("Label:" + label);
	}

	@BeforeClass
	public static void oneTimeSetUp() throws Exception {
		LOG.info("{}: oneTimeSetUp [pid:{}]", TAG, Thread.currentThread().getId());
	}

	@AfterClass
	public static void oneTimeTearDown() throws Exception {
		LOG.info("{}: oneTimeTearDown [pid:{}]", TAG, Thread.currentThread().getId());
	}

	@Before
	public void setUp() throws Exception {
		LOG.info("{}: setUp [pid:{}]", TAG, Thread.currentThread().getId());
	}

	@After
	public void tearDown() throws Exception {
		LOG.info("{}: tearDown [pid:{}]", TAG, Thread.currentThread().getId());
	}

	@Test
	public void test01GetMethod() {
		LOG.info("{}: test01GetMethod [pid:{}]", TAG, Thread.currentThread().getId());
		final String url = baseUrl + "/demo/1";

		Response resp = RestAssured.given().param("userid", "xxx").param("username", "xxx").when().get(url).andReturn();
		LOG.info("response code:{}, body:{}", resp.getStatusCode(), resp.getBody().asString());
		resp.then().assertThat().statusCode(200);
	}

	@Test
	public void test02PostMethod() {
		LOG.info("{}: test02PostMethod [pid:{}]", TAG, Thread.currentThread().getId());
		final String jsonFileName = "data.json";
		final String url = baseUrl + "/demo/3";

		String dirPath = Common.getCurrentPath() + File.separator + jsonFileName;
		String reqBody = "";
		try {
			reqBody = Common.readFileContent(dirPath);
		} catch (IOException e) {
			LOG.warn("file not found {}, and read default", dirPath);
			try {
				reqBody = Common.readResource(File.separator + jsonFileName);
			} catch (IOException e1) {
				e1.printStackTrace();
				Assert.fail(e.getMessage());
			}
		}
		Assert.assertTrue("verify request body is not empty", reqBody.length() > 0);
		LOG.info("request body:\n{}", reqBody);

		Response resp = RestAssured.given().contentType(ContentType.JSON).body(reqBody).when().post(url).andReturn();
		LOG.info("response code:{}, body:\n{}", resp.getStatusCode(), resp.getBody().asString());
		resp.then().assertThat().statusCode(200);
	}

}
