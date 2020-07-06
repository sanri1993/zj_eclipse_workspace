package zhengjin.fl.pipeline.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import zhengjin.fl.pipeline.http.HttpUtils;

public final class FlTemplateApi {

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
				return response;
			}
		}
		return "";
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
				return response;
			}
		}
		return "";
	}

	public static boolean deleteTemplatePipeline(String templateId, String pipelineId) {
		final String url = Common.BASE_URL
				+ String.format("template-market/v1/pipeline/template/%s/%s/delete", templateId, pipelineId);

		String response = "";
		try {
			response = HttpUtils.delete(url);
			LOGGER.info("delete template pipeline response: " + response);
			Common.verifyStatusCode(response);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			return false;
		}
		return true;
	}

	public static boolean deleteTemplatePipelineJob(String templateId, String jobId) {
		final String url = Common.BASE_URL
				+ String.format("template-market/v1/job/template/%s/%s/delete", templateId, jobId);

		String response = "";
		try {
			response = HttpUtils.delete(url);
			LOGGER.info("delete template job response: " + response);
			Common.verifyStatusCode(response);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			return false;
		}
		return true;
	}

	public static boolean copyTemplatePipeline(String templateId, String srcPipelineId, String newPipelineName) {
		final String url = Common.BASE_URL
				+ String.format("template-market/v1/pipeline/template/%s/create", templateId);

		String srcPipeline = "";
		try {
			srcPipeline = getTemplatePipelineData(templateId, srcPipelineId);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			return false;
		}
		if (srcPipeline.length() == 0) {
			LOGGER.error(
					String.format("source templateId=[%s] pipeline=[%s] is not found!", templateId, srcPipelineId));
			return false;
		}

		JSONObject json = (JSONObject) JSONObject.parse(srcPipeline);
		JSONObject data = json.getJSONObject("data");
		data.put("name", newPipelineName);
		data.put("describe", String.format("pipeline copied from pipelineID=[%s]", srcPipelineId));

		JSONObject requestJson = new JSONObject();
		requestJson.put("engineTemplateId", templateId);
		requestJson.put("pipelineKey", newPipelineName + "_" + UUID.randomUUID().toString().split("-")[0]);
		requestJson.put("data", data);

		String requestJsonStr = JSONObject.toJSONString(requestJson);
		LOGGER.debug("copy template pipeline request: " + requestJsonStr);
		try {
			String response = HttpUtils.post(url, requestJsonStr);
			Common.verifyStatusCode(response);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			return false;
		}
		return true;
	}

	public static boolean copyTemplateJob(String templateId, String srcJobId, String newJobName) {
		final String url = Common.BASE_URL + String.format("template-market/v1/job/template/%s/create", templateId);

		String srcJob = "";
		try {
			srcJob = getTemplateJobData(templateId, srcJobId);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			return false;
		}
		if (srcJob.length() == 0) {
			LOGGER.error(String.format("source templateId=[%s] job=[%s] is not found!", templateId, srcJobId));
			return false;
		}

		JSONObject reqJson = (JSONObject) JSONObject.parse(srcJob);
		reqJson.put("id", null);
		reqJson.put("name", newJobName);
		reqJson.put("desc", String.format("job copied from jobID=[%s]", srcJobId));

		String reqJsonStr = JSONObject.toJSONString(reqJson);
		try {
			String response = HttpUtils.post(url, reqJsonStr);
			Common.verifyStatusCode(response);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			return false;
		}
		return true;
	}

	public static boolean updateTemplatePipelineDesc(String templateId, String pipelineId, String desc) {
		final String url = Common.BASE_URL
				+ String.format("template-market/v1/pipeline/template/%s/update", templateId);

		String srcPipeline = "";
		try {
			srcPipeline = getTemplatePipelineData(templateId, pipelineId);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			return false;
		}
		if (srcPipeline.length() == 0) {
			LOGGER.error(String.format("source templateId=[%s] pipeline=[%s] is not found!", templateId, pipelineId));
			return false;
		}

		JSONObject json = (JSONObject) JSONObject.parse(srcPipeline);
		json.getJSONObject("data").put("describe", desc);

		try {
			String response = HttpUtils.put(url, JSONObject.toJSONString(json));
			Common.verifyStatusCode(response);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			return false;
		}
		return true;
	}

	public static boolean updateTemplateJobDesc(String templateId, String jobId, String desc) {
		final String url = Common.BASE_URL + String.format("template-market/v1/job/template/%s/update", templateId);

		String srcJob = "";
		try {
			srcJob = getTemplateJobData(templateId, jobId);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			return false;
		}
		if (srcJob.length() == 0) {
			LOGGER.error(String.format("source templateId=[%s] job=[%s] is not found!", templateId, jobId));
			return false;
		}

		JSONObject json = (JSONObject) JSONObject.parse(srcJob);
		json.put("desc", desc);

		try {
			String response = HttpUtils.put(url, JSONObject.toJSONString(json));
			Common.verifyStatusCode(response);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			return false;
		}
		return true;
	}

}
