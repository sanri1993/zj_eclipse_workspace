package zhengjin.jmeter.javasampler;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.Response;
import zhengjin.jmeter.utils.HttpClient;

public class JavaSampler01 extends AbstractJavaSamplerClient {

	private static final Logger LOG = LoggerFactory.getLogger(JavaSampler01.class);
	private static final String TAG = JavaSampler01.class.getSimpleName();

	private static final String baseUrl = "http://127.0.0.1:17891";
	private static final String keyUserID = "userid";
	private static final String keyUserName = "username";

	@Override
	public void setupTest(JavaSamplerContext context) {
		super.setupTest(context);
	}

	@Override
	public void teardownTest(JavaSamplerContext context) {
		super.teardownTest(context);
	}

	public Arguments getDefaultParameters() {
		LOG.info(TAG + ":getDefaultParameters");
		Arguments params = new Arguments();
		params.addArgument(keyUserID, "xxx");
		params.addArgument(keyUserName, "xxx");
		return params;
	}

	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		LOG.info(TAG + ":runTest");
		final String url = baseUrl + "/demo/1";

		SampleResult sr = new SampleResult();
		sr.sampleStart();
		sr.setSamplerData("empty");

		Map<String, Object> urlParams = new HashMap<>();
		urlParams.put("userid", context.getParameter(keyUserID));
		urlParams.put("username", context.getParameter(keyUserName));

		Response resp = null;
		try {
			resp = HttpClient.getMethod(url, urlParams, Collections.emptyMap());
		} catch (IOException e) {
			e.printStackTrace();
			errorHandler(sr, e.getMessage());
		}

		try {
			sr.setResponseData(resp.body().string(), "utf-8");
		} catch (IOException e) {
			e.printStackTrace();
			errorHandler(sr, e.getMessage());
		}

		if (resp.isSuccessful() && resp.code() == 200) {
			sr.setSuccessful(true);
			sr.setResponseCode("200");
			sr.setResponseCodeOK();
		} else {
			sr.setSuccessful(false);
		}
		sr.sampleEnd();

		return null;
	}

	private void errorHandler(SampleResult sr, String message) {
		sr.setResponseData(message, "utf-8");
		sr.setSuccessful(false);
		sr.sampleEnd();
	}

}
