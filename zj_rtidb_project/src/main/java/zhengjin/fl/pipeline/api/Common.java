package zhengjin.fl.pipeline.api;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;

public final class Common {

	static final String BASE_URL = "http://kp.hwwt2.com/";
	static final String USER_NAME = "xxxxx";
	static final String PWD = "xxxxx";
	static final String DEFAULT_PAGE_SIZE = "20";

	public static void verifyStatusCode(String response) throws IOException {
		JSONObject json = (JSONObject) JSONObject.parse(response);
		String statusCode = json.getString("status");
		if (!"0".equals(statusCode)) {
			throw new IOException(String.format("statusCode=[%s], response=[%s]", statusCode, response));
		}
	}

}
