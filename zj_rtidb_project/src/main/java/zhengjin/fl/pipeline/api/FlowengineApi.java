package zhengjin.fl.pipeline.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import zhengjin.fl.pipeline.http.HttpUtils;

public final class FlowengineApi {

	private static final Logger LOGGER = LoggerFactory.getLogger(FlowengineApi.class);
	private static final String BASE_URL = "http://172.27.128.236:40121/";

	public static boolean login() {
		final String url = BASE_URL + "keystone/v1/sessions";

		Map<String, String> body = new HashMap<>();
		body.put("username", "4pdadmin");
		body.put("password", "admin");

		try {
			String response = HttpUtils.post(url, JSONObject.toJSONString(body));
			LOGGER.debug(response);

			JSONObject json = (JSONObject) JSONObject.parse(response);
			return "0".equals(json.getString("status"));
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			return false;
		}
	}

	public static String listRunningFlowengines(String workspaceId) {
		final String url = BASE_URL + "automl-manager/v1/appList";

		Map<String, String> params = new HashMap<>();
		params.put("workspaceId", workspaceId);
		params.put("status", "RUNNING");
		params.put("size", Constants.DEFAULT_PAGE_SIZE);

		String response = "";
		try {
			response = HttpUtils.get(url, params);
			LOGGER.debug(response);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}

		return response;
	}
}
