package zhengjin.fl.pipeline.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zhengjin.fl.pipeline.http.HttpUtils;

public final class FlTemplateApi {

	private static final Logger LOGGER = LoggerFactory.getLogger(FlTemplateApi.class);

	public static String listTemplatePipelines(String templateId) {
		final String url = Constants.BASE_URL
				+ String.format("template-market/v1/pipeline/template/%s/list/byPage", templateId);

		Map<String, String> params = new HashMap<>();
		params.put("size", Constants.DEFAULT_PAGE_SIZE);

		String response = "";
		try {
			response = HttpUtils.get(url, params);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
		return response;
	}

	public static String listTemplateJobs(String templateId) {
		final String url = Constants.BASE_URL
				+ String.format("template-market/v1/job/template/%s/list/byPage", templateId);

		Map<String, String> params = new HashMap<>();
		params.put("size", Constants.DEFAULT_PAGE_SIZE);

		String response = "";
		try {
			response = HttpUtils.get(url, params);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
		return response;
	}

}
