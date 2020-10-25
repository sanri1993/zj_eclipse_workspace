package zhengjin.fl.pipeline.api;

import java.io.IOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public final class FlowengineApiTest {

	private final String workspaceId = "47";
	private final String instanceId = "371";
	private final String templateId = "1";

	@BeforeClass
	public static void loginTest() {
		Assert.assertTrue(FlowengineApi.login());
	}

	@Test
	public void listRunningFlowenginesTest() throws IOException {
		String response = FlowengineApi.listRunningFlowengines(workspaceId);
		JSONObject json = (JSONObject) JSONObject.parse(response);

		JSONArray list = json.getJSONObject("data").getJSONArray("appList");
		for (int i = 0; i < list.size(); i++) {
			JSONObject instance = list.getJSONObject(i);
			System.out.println(String.format("instanceId=%s, status=%s, appName=%s", instance.getString("instanceId"),
					instance.getString("status"), instance.getString("appName")));
		}
	}

	@Test
	public void listFlPipelinesTest() throws IOException {
		String response = FlowengineApi.listFlPipelines(instanceId, templateId);
		JSONObject json = (JSONObject) JSONObject.parse(response);

		JSONArray list = json.getJSONObject("data").getJSONArray("engineJobPipelineTemplateList");
		for (int i = 0; i < list.size(); i++) {
			JSONObject instance = list.getJSONObject(i);
			System.out.println(
					String.format("templateId=%s, pipelineId=%s, status=%s", instance.getString("engineTemplateId"),
							instance.getString("id"), instance.getJSONObject("data").getString("status")));
		}
	}

	@Test
	public void runFlPipelineTest() {
		Assert.assertTrue(FlowengineApi.runFlPipeline(instanceId, templateId, "6"));
	}

	@Test
	public void listFlPipelineHistoryTasksTest() throws IOException {
		String response = FlowengineApi.listFlPipelineHistoryTasks(instanceId, "3");
		JSONObject json = (JSONObject) JSONObject.parse(response);

		JSONArray resList = new JSONArray();
		JSONArray tasks = json.getJSONArray("data");
		for (int i = 0; i < tasks.size(); i++) {
			JSONObject task = (JSONObject) tasks.get(i);
			JSONObject instance = new JSONObject();
			instance.put("id", task.getString("id"));
			instance.put("status", task.getString("status"));
			resList.add(instance);
		}

		System.out.println(JSONObject.toJSONString(resList));
	}

	@Test
	public void stopFlPipelineTaskTest() {
		Assert.assertTrue(FlowengineApi.stopFlPipelineTask(instanceId, "6", "2"));
	}

	@Test
	public void resumeFlPipelineTaskTest() {
		Assert.assertTrue(FlowengineApi.resumeFlPipelineTask(instanceId, "6", "2"));
	}

}
