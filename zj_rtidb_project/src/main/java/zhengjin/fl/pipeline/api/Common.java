package zhengjin.fl.pipeline.api;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;

public final class Common {

	static final String BASE_URL = "http://172.27.128.236:40121/";
	static final String DEFAULT_PAGE_SIZE = "20";

	public static void verifyStatusCode(String response) throws IOException {
		JSONObject json = (JSONObject) JSONObject.parse(response);
		String statusCode = json.getString("status");
		if (!"0".equals(statusCode)) {
			throw new IOException(String.format("statusCode=[%s], response=[%s]", statusCode, response));
		}
	}

}
