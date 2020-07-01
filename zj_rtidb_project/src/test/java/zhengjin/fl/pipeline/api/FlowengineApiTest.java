package zhengjin.fl.pipeline.api;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import zhengjin.fl.pipeline.api.FlowengineApi;

public final class FlowengineApiTest {

	private final String workspaceId = "1";
	private final String instanceId = "1";
	private final String templateId = "6";

	@BeforeClass
	public static void loginTest() {
		Assert.assertTrue(FlowengineApi.login());
	}

	@Test
	public void listRunningFlowenginesTest() {
		String response = FlowengineApi.listRunningFlowengines(workspaceId);
		JSONObject json = (JSONObject) JSONObject.parse(response);
		Assert.assertTrue("0".equals(json.getString("status")));

		JSONArray list = json.getJSONObject("data").getJSONArray("appList");
		for (int i = 0; i < list.size(); i++) {
			JSONObject instance = list.getJSONObject(i);
			System.out.println(String.format("instanceId=%s, status=%s, appName=%s", instance.getString("instanceId"),
					instance.getString("status"), instance.getString("appName")));
		}
	}

	@Test
	public void listFlPipelinesTest() {
		String response = FlowengineApi.listFlPipelines(instanceId, templateId);
		JSONObject json = (JSONObject) JSONObject.parse(response);
		Assert.assertTrue("0".equals(json.getString("status")));

		JSONArray list = json.getJSONObject("data").getJSONArray("engineJobPipelineTemplateList");
		for (int i = 0; i < list.size(); i++) {
			JSONObject instance = list.getJSONObject(i);
			System.out.println(
					String.format("templateId=%s, pipelineId=%s, status=%s", instance.getString("engineTemplateId"),
							instance.getString("id"), instance.getJSONObject("data").getString("status")));
		}
	}

}
