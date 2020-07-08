package zhengjin.fl.pipeline.api;

import java.io.IOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public final class FlTemplateApiTest {

	private final String templateId = "10027";
	private final String pipelineId = "33";
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

	@Test
	public void deleteTemplatePipelineTest() {
		Assert.assertTrue(FlTemplateApi.deleteTemplatePipeline(templateId, "75"));
	}

	@Test
	public void deleteTemplatePipelineJobTest() {
		Assert.assertTrue(FlTemplateApi.deleteTemplatePipelineJob(templateId, "147"));
	}

	@Test
	public void copyTemplatePipelineTest() throws IOException {
		Assert.assertTrue(FlTemplateApi.copyTemplatePipeline(templateId, "65", "copied_pipeline_test11"));
	}

	@Test
	public void copyTemplateJobTest() {
		Assert.assertTrue(FlTemplateApi.copyTemplateJob(templateId, "114", "copied_job_test11"));
	}

	@Test
	public void updateTemplatePipelineDescTest() {
		String desc = "copied pipeline, updated.";
		Assert.assertTrue(FlTemplateApi.updateTemplatePipelineDesc(templateId, "77", desc));
	}

	@Test
	public void updateTemplateJobDescTest() {
		String desc = "copied job, updated.";
		Assert.assertTrue(FlTemplateApi.updateTemplateJobDesc(templateId, "150", desc));
	}

}
