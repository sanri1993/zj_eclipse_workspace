package zhengjin.fl.pipeline.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import zhengjin.fl.pipeline.http.HttpUtils;

public final class FlTemplateApi {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(FlTemplateApi.class);

	public static String listTemplatePipelines(String templateId) throws IOException {
		final String url = Common.BASE_URL
				+ String.format("template-market/v1/pipeline/template/%s/list/byPage", templateId);

		Map<String, String> params = new HashMap<>();
		params.put("size", Common.DEFAULT_PAGE_SIZE);

		String response = "";
		response = HttpUtils.get(url, params);
		Common.verifyStatusCode(response);
		return response;
	}

	public static String listTemplateJobs(String templateId) throws IOException {
		final String url = Common.BASE_URL
				+ String.format("template-market/v1/job/template/%s/list/byPage", templateId);

		Map<String, String> params = new HashMap<>();
		params.put("size", Common.DEFAULT_PAGE_SIZE);

		String response = "";
		response = HttpUtils.get(url, params);
		Common.verifyStatusCode(response);
		return response;
	}

	public static String listTemplatePipelineJobs(@NonNull String templateId, @NonNull String pipelineId)
			throws IOException {
		String response = getTemplatePipelineData(templateId, pipelineId);
		JSONObject json = (JSONObject) JSONObject.parse(response);
		JSONArray jobs = json.getJSONObject("data").getJSONArray("nodes");
		return JSONObject.toJSONString(jobs);
	}

	public static String getTemplatePipelineData(@NonNull String templateId, @NonNull String pipelineId)
			throws IOException {
		final String url = Common.BASE_URL
				+ String.format("template-market/v1/pipeline/template/%s/list/byPage", templateId);

		Map<String, String> params = new HashMap<>();
		params.put("size", Common.DEFAULT_PAGE_SIZE);

		String response = "";
		response = HttpUtils.get(url, params);
		Common.verifyStatusCode(response);

		JSONObject json = (JSONObject) JSONObject.parse(response);
		JSONArray pipelines = json.getJSONObject("data").getJSONArray("engineJobPipelineTemplateList");
		for (int i = 0; i < pipelines.size(); i++) {
			JSONObject pipeline = pipelines.getJSONObject(i);
			if (pipelineId.equals(pipeline.getString("id"))) {
				response = JSONObject.toJSONString(pipeline);
				break;
			}
		}
		return response;
	}

	public static String getTemplateJobData(String templateId, String jobId) throws IOException {
		final String url = Common.BASE_URL
				+ String.format("template-market/v1/job/template/%s/list/byPage", templateId);

		Map<String, String> params = new HashMap<>();
		params.put("size", Common.DEFAULT_PAGE_SIZE);

		String response = "";
		response = HttpUtils.get(url, params);
		Common.verifyStatusCode(response);

		JSONObject json = (JSONObject) JSONObject.parse(response);
		JSONArray jobs = json.getJSONObject("data").getJSONArray("engineJobTemplateList");
		for (int i = 0; i < jobs.size(); i++) {
			JSONObject job = jobs.getJSONObject(i);
			if (jobId.equals(job.getString("id"))) {
				response = JSONObject.toJSONString(job);
				break;
			}
		}
		return response;
	}

}
