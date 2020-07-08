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

	public static boolean login() {
		final String url = Common.BASE_URL + "keystone/v1/sessions";

		Map<String, String> body = new HashMap<>();
		body.put("username", Common.USER_NAME);
		body.put("password", Common.PWD);

		try {
			String response = HttpUtils.post(url, JSONObject.toJSONString(body));
			JSONObject json = (JSONObject) JSONObject.parse(response);
			return "0".equals(json.getString("status"));
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			return false;
		}
	}

	public static String listRunningFlowengines(String workspaceId) throws IOException {
		final String url = Common.BASE_URL + "automl-manager/v1/appList";

		Map<String, String> params = new HashMap<>();
		params.put("workspaceId", workspaceId);
		params.put("status", "RUNNING");
		params.put("size", Common.DEFAULT_PAGE_SIZE);

		String response = "";
		response = HttpUtils.get(url, params);
		Common.verifyStatusCode(response);
		return response;
	}

	public static String listFlPipelines(String instanceId, String templateId) throws IOException {
		final String url = Common.BASE_URL
				+ String.format("automl-engine/%s/automl/v1/pipeline/%s/list", instanceId, templateId);

		String response = "";
		response = HttpUtils.get(url);
		Common.verifyStatusCode(response);
		return response;
	}

	public static boolean runFlPipeline(String instanceId, String templateId, String pipelineId) {
		final String url = Common.BASE_URL
				+ String.format("automl-engine/%s/automl/v1/pipeline/%s/%s/start", instanceId, templateId, pipelineId);

		try {
			String response = HttpUtils.post(url, "{}");
			Common.verifyStatusCode(response);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			return false;
		}
		return true;
	}

	public static String listFlPipelineHistoryTasks(String instanceId, String pipelineId) throws IOException {
		final String url = Common.BASE_URL
				+ String.format("automl-engine/%s/automl/v1/pipeline/%s/historyList", instanceId, pipelineId);

		String response = HttpUtils.get(url);
		Common.verifyStatusCode(response);
		return response;
	}

	public static boolean stopFlPipelineTask(String instanceId, String pipelineId, String taskId) {
		final String url = Common.BASE_URL + String.format(
				"automl-engine/%s/automl/v1/pipeline/%s/stopHistory?engineId=%s", instanceId, taskId, pipelineId);

		try {
			String response = HttpUtils.delete(url, "{}");
			Common.verifyStatusCode(response);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			return false;
		}
		return true;
	}

	public static boolean resumeFlPipelineTask(String instanceId, String pipelineId, String taskId) {
		final String url = Common.BASE_URL + String.format(
				"automl-engine/%s/automl/v1/pipeline/%s/resumeHistory?engineId=%s", instanceId, taskId, pipelineId);

		try {
			String response = HttpUtils.post(url, "{}");
			Common.verifyStatusCode(response);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			return false;
		}
		return true;
	}

}
