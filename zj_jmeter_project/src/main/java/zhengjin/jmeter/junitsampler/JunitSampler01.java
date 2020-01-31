package zhengjin.jmeter.junitsampler;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import zhengjin.jmeter.utils.Common;

public class JunitSampler01 {

	private static final Logger LOG = LoggerFactory.getLogger(JunitSampler01.class);
	private static final String baseUrl = "http://127.0.0.1:17891";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test01GetMethod() {
		final String url = baseUrl + "/demo/1";
		Response resp = RestAssured.given().param("userid", "xxx").param("username", "xxx").when().get(url).andReturn();
		LOG.info("response code:{}, body:{}", resp.getStatusCode(), resp.getBody().asString());
		resp.then().assertThat().statusCode(200);
	}

	@Test
	public void test02PostMethod() throws IOException {
		final String jsonFileName = "data.json";
		final String url = baseUrl + "/demo/3";

		String reqBody;
		try {
			reqBody = Common.readFileContent(Common.getCurrentPath() + File.separator + jsonFileName);
			LOG.info(Common.getCurrentPath() + File.separator + jsonFileName);
		} catch (IOException e) {
			LOG.info("read json body default.");
			reqBody = Common.readFileContent(Object.class.getResource("/" + jsonFileName).getPath());
		}
		LOG.info("request body:{}", reqBody);

		Response resp = RestAssured.given().contentType(ContentType.JSON).body(reqBody).when().post(url).andReturn();
		LOG.info("response code:{}, body:\n{}", resp.getStatusCode(), resp.getBody().asString());
		resp.then().assertThat().statusCode(200);
	}

}
