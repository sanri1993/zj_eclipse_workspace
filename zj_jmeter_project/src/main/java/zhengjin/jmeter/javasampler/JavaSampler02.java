package zhengjin.jmeter.javasampler;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import okhttp3.Response;
import zhengjin.jmeter.utils.Common;
import zhengjin.jmeter.utils.HttpClient;

/**
 * Java sampler for Post method.
 * 
 * @author zhengjin
 *
 */
public final class JavaSampler02 extends AbstractJavaSamplerClient {

	private static final Logger LOG = LoggerFactory.getLogger(JavaSampler02.class);
	private static final String TAG = JavaSampler02.class.getSimpleName();

	private static final String baseUrl = "http://127.0.0.1:17891";
	private static final String keyReqBody = "reqBody";

	private String reqBody;

	@Override
	public Arguments getDefaultParameters() {
		LOG.info("{}: getDefaultParameters [pid:{}]", TAG, Thread.currentThread().getId());
		final String jsonFileName = "data.json";

		String filePath = Common.getCurrentPath() + File.separator + jsonFileName;
		String reqBody = "";
		try {
			reqBody = Common.readFileContent(filePath);
		} catch (IOException e) {
			LOG.warn("file not found ({}), and read default", filePath);
			try {
				filePath = File.separator + jsonFileName;
				reqBody = Common.readResource(filePath);
			} catch (IOException e1) {
				LOG.warn("read resource ({}) failed, error: {}", filePath, e1.getMessage());
				e1.printStackTrace();
			}
		}
		LOG.info("request body:\n{}", reqBody);

		Arguments params = new Arguments();
		params.addArgument(keyReqBody, reqBody);
		return params;
	}

	@Override
	public void setupTest(JavaSamplerContext context) {
		LOG.info("{}: setupTest [pid:{}]", TAG, Thread.currentThread().getId());
		super.setupTest(context);
		this.reqBody = context.getParameter(keyReqBody);
	}

	@Override
	public void teardownTest(JavaSamplerContext context) {
		LOG.info("{}: teardownTest [pid:{}]", TAG, Thread.currentThread().getId());
		super.teardownTest(context);
	}

	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		LOG.info("{}: runTest [pid:{}]", TAG, Thread.currentThread().getId());

		SampleResult sr = new SampleResult();
		sr.setSampleLabel("JavaSamplerForPost");
		sr.sampleStart();

		if (this.reqBody.length() == 0) {
			errorHandler(sr, "Request body is empty!");
			return sr;
		}
		sr.setDataType(SampleResult.TEXT);
		sr.setSamplerData(this.reqBody);

		final String url = baseUrl + "/demo/3";
		Response resp = null;
		try {
			resp = HttpClient.postJsonMethod(url, Collections.emptyMap(), Collections.emptyMap(),
					JSONObject.parse(this.reqBody));
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
