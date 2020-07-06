package zhengjin.fl.pipeline.http;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.ConnectionPool;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

public final class HttpUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);

	private static final OkHttpClient client;

	static {
		LOGGER.info("init OkHttpClient.");
		ConnectionPool connectionPool = new ConnectionPool(Constants.MAX_IDLE_CONNECTIONS, Constants.KEEP_ALLIVE_TIME,
				TimeUnit.MILLISECONDS);

		// intercepter order: NetworkIntercepter -> RetryIntercepter
		client = new OkHttpClient().newBuilder().connectTimeout(Constants.CONNECTION_TIME_OUT, TimeUnit.SECONDS)
				.readTimeout(Constants.SOCKET_TIME_OUT, TimeUnit.SECONDS)
				.writeTimeout(Constants.SOCKET_TIME_OUT, TimeUnit.SECONDS).retryOnConnectionFailure(false)
				.connectionPool(connectionPool).addInterceptor(new RetryInterceptor(Constants.MAX_RETRY_COUNT))
				.addInterceptor(new LogInterceptor()).cookieJar(new CookieHandler()).build();
	}

	public static OkHttpClient getHttpClient() {
		return client;
	}

	public static String get(String url) throws IOException {
		return get(url, Collections.emptyMap(), Collections.emptyMap());
	}

	public static String get(String url, Map<String, String> params) throws IOException {
		return get(url, params, Collections.emptyMap());
	}

	public static String get(String url, Map<String, String> params, Map<String, String> headers) throws IOException {
		HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
		for (String key : params.keySet()) {
			builder.addQueryParameter(key, params.get(key));
		}

		Request.Builder request = new Request.Builder().url(builder.build().toString());
		for (String key : headers.keySet()) {
			request.addHeader(key, headers.get(key));
		}

		Response response = client.newCall(request.build()).execute();
		if (!response.isSuccessful()) {
			throw new ErrorCodeException(
					String.format("status code [%d], response body [%s]", response.code(), response.body().string()));
		}

		return response.body().string();
	}

	public static String post(String url, String body) throws IOException {
		return post(url, Collections.emptyMap(), Collections.emptyMap(), body);
	}

	public static String post(String url, Map<String, String> headers, String body) throws IOException {
		return post(url, Collections.emptyMap(), headers, body);
	}

	public static String post(String url, Map<String, String> params, Map<String, String> headers, String body)
			throws IOException {
		HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
		for (String key : params.keySet()) {
			builder.addQueryParameter(key, params.get(key));
		}

		RequestBody requestBody = RequestBody.create(body, Constants.MEDIA_TYPE_JSON);
		Request.Builder request = new Request.Builder().url(builder.build().toString()).post(requestBody);
		for (String key : headers.keySet()) {
			request.addHeader(key, headers.get(key));
		}

		Response response = client.newCall(request.build()).execute();
		if (!response.isSuccessful()) {
			throw new ErrorCodeException(
					String.format("status code [%d], response body [%s]", response.code(), response.body().string()));
		}

		return response.body().string();
	}

	public static String delete(String url) throws IOException {
		return delete(url, "{}");
	}

	public static String delete(String url, String body) throws IOException {
		return delete(url, Collections.emptyMap(), body);
	}

	public static String delete(String url, Map<String, String> headers, String body) throws IOException {
		return putOrDelete(url, headers, body, RequestType.DELETE);
	}

	public static String put(String url, String body) throws IOException {
		return put(url, Collections.emptyMap(), body);
	}

	public static String put(String url, Map<String, String> headers, String body) throws IOException {
		return putOrDelete(url, headers, body, RequestType.PUT);
	}

	public static String putOrDelete(String url, Map<String, String> headers, String body, RequestType type)
			throws IOException {
		HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
		RequestBody requestBody = RequestBody.create(body, Constants.MEDIA_TYPE_JSON);
		Request.Builder request = new Request.Builder().url(builder.build().toString());
		if (type == RequestType.DELETE) {
			request.delete(requestBody);
		} else if (type == RequestType.PUT) {
			request.put(requestBody);
		} else {
			throw new IllegalArgumentException("invalid RequestType!");
		}

		for (String key : headers.keySet()) {
			request.addHeader(key, headers.get(key));
		}

		Response response = client.newCall(request.build()).execute();
		if (!response.isSuccessful()) {
			throw new ErrorCodeException(
					String.format("status code [%d], response body [%s]", response.code(), response.body().string()));
		}

		return response.body().string();
	}

	public static String getRequestBody(Request request) {
		String requestContent = "";
		if (request == null) {
			return requestContent;
		}

		RequestBody body = request.body();
		if (body == null) {
			return requestContent;
		}

		Buffer buffer = new Buffer();
		try {
			body.writeTo(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		requestContent = buffer.readString(Charset.forName("utf-8"));
		return requestContent;
	}

	private static enum RequestType {
		PUT, DELETE;
	}

}
