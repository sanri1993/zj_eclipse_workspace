package zhengjin.fl.pipeline.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.RetryListener;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Predicates;

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
 */
public final class OkHttpTest {

	private static OkHttpClient client;

	@BeforeClass
	public static void beforeClass() {
		client = new OkHttpClient().newBuilder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS)
				.readTimeout(30, TimeUnit.SECONDS).addInterceptor(new LoggingInterceptor()).build();
	}

	@Test
	public void ListToJsonString() {
		List<String> langs = new ArrayList<>();
		langs.add("Java");
		langs.add("Golang");
		langs.add("Python");
		System.out.println(JSONObject.toJSONString(langs));

		JSONArray langs2 = new JSONArray();
		langs2.add("Java");
		langs2.add("Golang");
		langs2.add("Python");
		System.out.println(JSONObject.toJSONString(langs2));
	}

	@Test
	public void mapToJsonString() {
		// map
		Map<String, String> sub = new HashMap<>();
		sub.put("server_name", "svr_a_002");
		sub.put("server_ip", "127.0.1.2");

		Map<String, Object> map = new HashMap<>();
		map.put("server", sub);
		map.put("server_group_id", "svr_grp_001");
		System.out.println("json: " + JSONObject.toJSONString(map));

		// json object
		JSONObject sub2 = new JSONObject();
		sub2.put("server_name", "svr_a_002");
		sub2.put("server_ip", "127.0.1.2");

		JSONObject json = new JSONObject();
		json.put("server", sub2);
		json.put("server_group_id", "svr_grp_001");
		System.out.println("json: " + JSONObject.toJSONString(json));
	}

	@Test
	public void okHttpTest01() throws InterruptedException {
		// 异步GET请求
		// Notes: finally, close response stream by invoke response.close() or
		// response.body().string()
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

	@Test
	public void httpUtilsTest01() throws IOException {
		// get
		String url = "http://127.0.0.1:17891/demo/1";

		Map<String, String> params = new HashMap<>();
		params.put("userid", "001");
		params.put("username", "Tester");

		Map<String, String> headers = new HashMap<>();
		headers.put("User-Agent", "OkHttp Example");
		headers.put("Accept", "application/json; q=0.5");

		String response = HttpUtils.get(url, params, headers);
		System.out.println("Response text: " + response);
	}

	@Test
	public void httpUtilsTest02() throws IOException {
		// post
		String url = "http://127.0.0.1:17891/demo/3";
		String requestBody = "{\"server_list\": [{\"server_name\": \"svr_a_002\",\"server_ip\": \"127.0.1.2\"}], \"server_group_id\": \"svr_grp_001\"}";

		Map<String, String> headers = new HashMap<>();
		headers.put("User-Agent", "OkHttp Example");
		headers.put("Accept", "application/vnd.github.v3+json");

		String response = HttpUtils.post(url, headers, requestBody);
		System.out.println("Response json text: " + response);
	}

	@Test
	public void httpUtilsTest03() {
		// retry for status code 5xx
		String url = "http://127.0.0.1:17891/mocktest/one/3";

		Map<String, String> params = new HashMap<>();
		params.put("code", "502");

		try {
			String response = HttpUtils.get(url, params);
			System.out.println("Response text: " + response);
		} catch (IOException e) {
			System.out.println("Failed: " + e.getMessage());
		}
	}

	@Test
	public void httpUtilsTest04() {
		// retry for connection timeout
		final String url = "http://127.0.0.1:17891/mocktest/one/4";

		final String timeout = "4";
		Map<String, String> params = new HashMap<>();
		params.put("wait", timeout);

		try {
			String response = HttpUtils.get(url, params);
			System.out.println("Response text: " + response);
		} catch (IOException e) {
			System.out.println("Failed: " + e.getMessage());
		}
	}

	@Test
	public void guavaRetryTest01() {
		final int maxRetryNumber = 3;

		Callable<String> callable = new Callable<String>() {
			int i = 0;

			@Override
			public String call() throws Exception {
				System.out.println("execute i++");
				i++;
				if (i > maxRetryNumber) {
					return String.valueOf(i);
				}
				throw new IOException("mock io error");
			}
		};

		RetryListener listener = new RetryListener() {
			@Override
			public <V> void onRetry(Attempt<V> attempt) {
				long cur = attempt.getAttemptNumber();
				System.out.println(String.format("retry %d times.", cur));
				if (cur == maxRetryNumber) {
					System.out.println(String.format("now, max retry number %d, and handle error.", maxRetryNumber));
				}
			}
		};

		Retryer<String> retryer = RetryerBuilder.<String>newBuilder().retryIfResult(Predicates.isNull())
				.retryIfRuntimeException().retryIfExceptionOfType(IOException.class)
				.withWaitStrategy(WaitStrategies.fixedWait(500L, TimeUnit.MILLISECONDS))
				.withStopStrategy(StopStrategies.stopAfterAttempt(maxRetryNumber)).withRetryListener(listener).build();

		try {
			retryer.call(callable);
		} catch (ExecutionException | RetryException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void guavaRetryTest02() {
		Callable<String> callable = new Callable<String>() {
			int i = 0;
			int[] mockCodes = new int[] { 502, 403, 200 };

			@Override
			public String call() throws Exception {
				int mockCode = mockCodes[new Random().nextInt(mockCodes.length)];
				System.out.println(String.format("retry %d with mock code %d", ++i, mockCode));
				return getMock(mockCode);
			}
		};

		Retryer<String> retryer = RetryerBuilder.<String>newBuilder().retryIfResult(Predicates.isNull())
				.retryIfExceptionOfType(IOException.class)
				.withWaitStrategy(WaitStrategies.fixedWait(500L, TimeUnit.MILLISECONDS))
				.withStopStrategy(StopStrategies.stopAfterAttempt(3)).build();

		try {
			String response = retryer.call(callable);
			System.out.println("response: " + response);
		} catch (ExecutionException | RetryException e) {
			e.printStackTrace();
		}
	}

	private String getMock(int mockCode) throws IOException {
		final String url = "http://127.0.0.1:17891/mocktest/one/3?code=" + String.valueOf(mockCode);

		final Request request = new Request.Builder().url(url).build();
		final Call call = client.newCall(request);
		Response response = call.execute();
		if (!response.isSuccessful()) {
			throw new IOException(
					String.format("error code=[%s], response=[%s]", response.code(), response.body().string()));
		}

		return response.body().string();
	}

}
