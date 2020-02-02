package zhengjin.jmeter.app;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import okhttp3.Response;
import zhengjin.jmeter.utils.Common;
import zhengjin.jmeter.utils.HttpClient;

public final class HttpClientTest {

	private static final Logger LOG = LoggerFactory.getLogger(HttpClientTest.class);
	private static final String baseUrl = "http://127.0.0.1:17891";

	@Test
	public void test01HttpClientGet() {
		final String url = baseUrl + "/demo/1";

		Map<String, Object> urlParams = new HashMap<>();
		urlParams.put("userid", "xxx");
		urlParams.put("username", "xxx");

		Response resp = null;
		try {
			resp = HttpClient.getMethod(url, urlParams, Collections.emptyMap());
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}

		Assert.assertNotNull(resp);
		Assert.assertTrue(resp.isSuccessful());
		Assert.assertEquals(200, resp.code());

		try {
			LOG.info("Response:{}", resp.body().string());
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void test02HttpClientPost() {
		final String jsonFileName = "data.json";
		final String url = baseUrl + "/demo/3";

		String reqBody = "";
		try {
			reqBody = Common.readFileContent(Common.getCurrentPath() + File.separator + jsonFileName);
			LOG.info(Common.getCurrentPath() + File.separator + jsonFileName);
		} catch (IOException e) {
			LOG.info("read json body default.");
			try {
				reqBody = Common.readResource(File.separator + jsonFileName);
			} catch (IOException e1) {
				e1.printStackTrace();
				Assert.fail(e1.getMessage());
			}
		}
		Assert.assertTrue(reqBody.length() > 0);

		Response resp = null;
		try {
			resp = HttpClient.postJsonMethod(url, Collections.emptyMap(), Collections.emptyMap(),
					JSONObject.parse(reqBody));
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}

		Assert.assertNotNull(resp);
		Assert.assertTrue(resp.isSuccessful());
		Assert.assertEquals(200, resp.code());

		// parse response json body
		try {
			String body = resp.body().string();
			LOG.info("Response:{}", body);

			JSONObject jsonObj = (JSONObject) JSONObject.parse(body);
			JSONArray svrList = jsonObj.getJSONObject("data").getJSONArray("server_list");
			for (Object svr : svrList) {
				JSONObject svrObj = (JSONObject) svr;
				LOG.info("server name:{}, ip:{}", svrObj.getString("server_name"), svrObj.getString("server_ip"));
			}
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

}
