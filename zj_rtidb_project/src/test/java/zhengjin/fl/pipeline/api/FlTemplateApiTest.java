package zhengjin.fl.pipeline.api;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public final class FlTemplateApiTest {

	private final String templateId = "10032";

	@BeforeClass
	public static void loginTest() {
		Assert.assertTrue(FlowengineApi.login());
	}

	@Test
	public void listTemplatePipelinesTest() {
		String response = FlTemplateApi.listTemplatePipelines(templateId);
		JSONObject json = (JSONObject) JSONObject.parse(response);
		Assert.assertTrue("0".equals(json.getString("status")));

		JSONArray list = json.getJSONObject("data").getJSONArray("engineJobPipelineTemplateList");
		for (int i = 0; i < list.size(); i++) {
			JSONObject instance = list.getJSONObject(i);
			System.out.println(String.format("id=%s, describe=%s", instance.getString("id"),
					instance.getJSONObject("data").getString("describe")));
		}
	}

	@Test
	public void listTemplateJobsTest() {
		String response = FlTemplateApi.listTemplateJobs(templateId);
		JSONObject json = (JSONObject) JSONObject.parse(response);
		Assert.assertTrue("0".equals(json.getString("status")));

		JSONArray list = json.getJSONObject("data").getJSONArray("engineJobTemplateList");
		for (int i = 0; i < list.size(); i++) {
			JSONObject instance = list.getJSONObject(i);
			System.out.println(String.format("id=%s, name=%s", instance.getString("id"), instance.getString("name")));
		}
	}

}
