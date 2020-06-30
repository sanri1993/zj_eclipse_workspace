package zhengjin.rtidb.app;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 
 * Refer: https://square.github.io/okhttp/recipes/#synchronous-get-kt-java
 *
 */
public class OkHttpTest {

	private static OkHttpClient client;

	@BeforeClass
	public static void beforeClass() {
		client = new OkHttpClient().newBuilder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS)
				.readTimeout(30, TimeUnit.SECONDS).addInterceptor(new LoggingInterceptor()).build();
	}

	@Test
	public void mapToJsonString() {
		Map<String, String> sub = new HashMap<>();
		sub.put("server_name", "svr_a_002");
		sub.put("server_ip", "127.0.1.2");

		Map<String, Object> map = new HashMap<>();
		map.put("server", sub);
		map.put("server_group_id", "svr_grp_001");

		String json = JSONObject.toJSONString(map);
		System.out.println("json: " + json);
	}

	@Test
	public void okHttpTest01() throws InterruptedException {
		// 异步GET请求
		String url = "http://127.0.0.1:17891/demo/1?userid=xxx&username=xxx";

		final Request request = new Request.Builder().url(url).header("User-Agent", "OkHttp Example")
				.addHeader("Accept", "application/json; q=0.5").addHeader("Accept", "application/vnd.github.v3+json")
				.get().build();
		Call call = client.newCall(request);
		call.enqueue(new Callback() {

			@Override
			public void onFailure(Call call, IOException e) {
				e.printStackTrace();
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (!response.isSuccessful()) {
					throw new IOException("Unexpected code " + response);
				}
				printResponseData(response);
			}
		});

		TimeUnit.SECONDS.sleep(2);
		System.out.println("wait async get request done.");
	}

	@Test
	public void okHttpTest02() throws IOException {
		// 同步GET请求
		String url = "http://127.0.0.1:17891/demo/1?userid=xxx&username=xxx";

		final Request request = new Request.Builder().url(url).build();
		final Call call = client.newCall(request);
		Response response = call.execute();
		if (!response.isSuccessful()) {
			throw new IOException("Unexpected code " + response);
		}

		printResponseData(response);
	}

	@Test
	public void okHttpTest03() throws IOException, InterruptedException {
		// 异步POST方式提交JSON
		String url = "http://127.0.0.1:17891/demo/3";
		MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
		String requestBody = "{\"server_list\": [{\"server_name\": \"svr_a_002\",\"server_ip\": \"127.0.1.2\"}], \"server_group_id\": \"svr_grp_001\"}";

		Request request = new Request.Builder().url(url).post(RequestBody.create(requestBody, mediaType)).build();
		client.newCall(request).enqueue(new Callback() {

			@Override
			public void onFailure(Call call, IOException e) {
				e.printStackTrace();
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (!response.isSuccessful()) {
					throw new IOException("Unexpected code " + response);
				}

				System.out.println("status code: " + response.code());
				String responseBody = response.body().string();
				System.out.println("response body: " + responseBody);

				System.out.println("json object info:");
				JSONObject jsonObject = (JSONObject) JSONObject.parse(responseBody);
				JSONObject jsonData = jsonObject.getJSONObject("data");
				System.out.println("server_group_id:" + jsonData.getString("server_group_id"));

				JSONArray jsonArray = jsonData.getJSONArray("server_list");
				for (int i = 0; i < jsonArray.size(); i++) {
					JSONObject object = (JSONObject) jsonArray.get(i);
					System.out.println("server_name: " + object.getString("server_name"));
					System.out.println("server_ip: " + object.getString("server_ip"));
				}
			}
		});

		TimeUnit.SECONDS.sleep(2);
		System.out.println("wait async get request done.");
	}

	private static class LoggingInterceptor implements Interceptor {

		@Override
		public Response intercept(Chain chain) throws IOException {
			Request request = chain.request();

			long startTime = System.currentTimeMillis();
			System.out.println(String.format("Sending request %s on %s%n%s", request.url(), chain.connection(),
					request.headers()));

			Response response = chain.proceed(request);

			long endTime = System.currentTimeMillis();
			System.out.println(
					String.format("Received response for %s in %dms", response.request().url(), (endTime - startTime)));
			return response;
		}
	}

	private void printResponseData(Response response) throws IOException {
		System.out.println(String.format("Response Info: protocol=%s, code=%d, message=%s", response.protocol(),
				response.code(), response.message()));

		System.out.println("\nResponse Headers:");
		Headers headers = response.headers();
		for (int i = 0; i < headers.size(); i++) {
			System.out.println(headers.name(i) + "=" + headers.value(i));
		}

		System.out.println("\nResponse Body: " + response.body().string());
	}

}
