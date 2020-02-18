package zhengjin.jmeter.javasampler;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.Response;
import zhengjin.jmeter.utils.HttpClient;

/**
 * Java sampler for Get method.
 * 
 * Scenario: CSV Data Set Config (userID,userName) + Java Request
 * (userid:${userID}, username:${userName})
 * 
 * @author zhengjin
 *
 */
public final class JavaSampler01 extends AbstractJavaSamplerClient {

	private static final Logger LOG = LoggerFactory.getLogger(JavaSampler01.class);
	private static final String TAG = JavaSampler01.class.getSimpleName();

	private static final String baseUrl = "http://127.0.0.1:17891";
	private static final String keyUserID = "userid";
	private static final String keyUserName = "username";

	private String userID;
	private String userName;

	@Override
	public Arguments getDefaultParameters() {
		LOG.info("{}: getDefaultParameters [pid:{}]", TAG, Thread.currentThread().getId());
		Arguments params = new Arguments();
		params.addArgument(keyUserID, "xxx");
		params.addArgument(keyUserName, "xxx");
		return params;
	}

	@Override
	public void setupTest(JavaSamplerContext context) {
		// 每个线程执行一次
		LOG.info("{}: setupTest [pid:{}]", TAG, Thread.currentThread().getId());
		super.setupTest(context);
		// if variables are constant, init here
//		this.userID = context.getParameter(keyUserID);
//		this.userName = context.getParameter(keyUserName);
	}

	@Override
	public void teardownTest(JavaSamplerContext context) {
		LOG.info("{}: teardownTest [pid:{}]", TAG, Thread.currentThread().getId());
		super.teardownTest(context);
	}

	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		LOG.info("{}: runTest [pid:{}]", TAG, Thread.currentThread().getId());
		Properties p = context.getJMeterProperties();
		LOG.info("sampleresult.useNanoTime={}", p.get("sampleresult.useNanoTime"));

		this.userID = context.getParameter(keyUserID);
		this.userName = context.getParameter(keyUserName);
		LOG.debug("Java Sampler Parameter (userid): " + this.userID);
		LOG.debug("Jmeter Variable (userID): " + context.getJMeterVariables().get("userID"));

		SampleResult sr = new SampleResult();
		sr.setSampleLabel("JavaSamplerForGet");

		sr.sampleStart();
		sr.setDataType(SampleResult.TEXT);
		sr.setSamplerData(String.format("%s=%s&%s=%s", keyUserID, this.userID, keyUserName, this.userName));

		Map<String, Object> urlParams = new HashMap<>();
		urlParams.put(keyUserID, this.userID);
		urlParams.put(keyUserName, this.userName);

		final String url = baseUrl + "/demo/1";
		Response resp = null;
		try {
			resp = HttpClient.getMethod(url, urlParams, Collections.emptyMap());
		} catch (IOException e) {
			e.printStackTrace();
			errorHandler(sr, e.getMessage());
			return sr;
		}

		try {
			sr.setResponseData(resp.body().string(), "utf-8");
		} catch (IOException e) {
			e.printStackTrace();
			errorHandler(sr, e.getMessage());
			return sr;
		}

		if (resp.isSuccessful() && resp.code() == 200) {
			sr.setSuccessful(true);
			sr.setResponseCode("200");
			sr.setResponseCodeOK();
		} else {
			sr.setSuccessful(false);
		}
		sr.sampleEnd();

		return sr;
	}

	private void errorHandler(SampleResult sr, String message) {
		sr.setResponseData(message, "utf-8");
		sr.setSuccessful(false);
		sr.sampleEnd();
	}

}
