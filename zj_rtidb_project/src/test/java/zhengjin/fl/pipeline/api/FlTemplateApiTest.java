package zhengjin.fl.pipeline.api;

import java.io.IOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public final class FlTemplateApiTest {

	private final String templateId = "10032";
	private final String pipelineId = "65";
	private final String jobId = "147";

	@BeforeClass
	public static void loginTest() {
		Assert.assertTrue(FlowengineApi.login());
	}

	@Test
	public void listTemplatePipelinesTest() throws IOException {
		String response = FlTemplateApi.listTemplatePipelines(templateId);
		JSONObject json = (JSONObject) JSONObject.parse(response);

		JSONArray list = json.getJSONObject("data").getJSONArray("engineJobPipelineTemplateList");
		for (int i = 0; i < list.size(); i++) {
			JSONObject instance = list.getJSONObject(i);
			System.out.println(String.format("id=%s, describe=%s", instance.getString("id"),
					instance.getJSONObject("data").getString("describe")));
		}
	}

	@Test
	public void listTemplateJobsTest() throws IOException {
		String response = FlTemplateApi.listTemplateJobs(templateId);
		JSONObject json = (JSONObject) JSONObject.parse(response);

		JSONArray list = json.getJSONObject("data").getJSONArray("engineJobTemplateList");
		for (int i = 0; i < list.size(); i++) {
			JSONObject instance = list.getJSONObject(i);
			System.out.println(String.format("id=%s, name=%s", instance.getString("id"), instance.getString("name")));
		}
	}

	@Test
	public void listTemplatePipelineJobsTest() throws IOException {
		String response = FlTemplateApi.listTemplatePipelineJobs(templateId, pipelineId);
		JSONArray jobs = (JSONArray) JSONObject.parse(response);
		for (int i = 0; i < jobs.size(); i++) {
			JSONObject job = jobs.getJSONObject(i);
			System.out.println("job name: " + job.getString("name"));
		}
	}

	@Test
	public void getTemplateJobDataTest() throws IOException {
		String response = FlTemplateApi.getTemplateJobData(templateId, jobId);
		JSONObject job = (JSONObject) JSONObject.parse(response);
		System.out.println(String.format("name=[%s], desc=[%s]", job.getString("name"), job.getString("desc")));
	}

}
