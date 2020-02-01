package zhengjin.jmeter.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class HttpClient {

	private static final Logger LOG = LoggerFactory.getLogger(HttpClient.class);
	private static final OkHttpClient CLIENT;

	static {
		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		builder.readTimeout(30, TimeUnit.SECONDS);
		builder.connectTimeout(10, TimeUnit.SECONDS);
		builder.writeTimeout(60, TimeUnit.SECONDS);

		List<Protocol> protocols = new ArrayList<Protocol>();
		protocols.add(Protocol.HTTP_1_1);
		protocols.add(Protocol.HTTP_2);
		builder.protocols(protocols);

		CLIENT = builder.build();
	}

	/**
	 * Get请求
	 * 
	 * @param url
	 * @param urlParams
	 * @param headerParams
	 * @return
	 * @throws IOException
	 */
	public static Response getMethod(String url, Map<String, Object> urlParams, Map<String, Object> headerParams)
			throws IOException {
		Request request = new Request.Builder().url(url + setUrlParams(urlParams)).headers(setHeaders(headerParams))
				.get().build();
		LOG.info("Http Request Info:" + request.toString());
		LOG.info("Http Headers:" + request.headers().toString());

		Call call = CLIENT.newCall(request);
		return call.execute();
	}

	public static String getBodyString(Response response) throws IOException {
		return new String(response.body().bytes(), StandardCharsets.UTF_8);
	}

	/**
	 * 异步Get请求
	 * 
	 * @param url
	 * @param urlParams
	 * @param headerParams
	 * @param callback
	 * @throws UnsupportedEncodingException
	 */
	public static void getAsyncMethod(String url, Map<String, Object> urlParams, Map<String, Object> headerParams,
			Callback callback) throws UnsupportedEncodingException {
		Request request = new Request.Builder().url(url + setUrlParams(urlParams)).headers(setHeaders(headerParams))
				.get().build();
		LOG.info("Http Request Info:" + request.toString());
		LOG.info("Http Headers:" + request.headers().toString());

		Call call = CLIENT.newCall(request);
		call.enqueue(callback);
	}

	/**
	 * 发送JSON Post请求
	 * 
	 * @param url
	 * @param urlParams
	 * @param jsonObj
	 * @param headerParams
	 * @param callback
	 * @throws IOException
	 */
	public static Response postJsonMethod(String url, Map<String, Object> urlParams, Map<String, Object> headerParams,
			Object jsonObj) throws IOException {
		MediaType mediaType = MediaType.Companion.parse("application/json;charset=utf-8");
		RequestBody requestBody = RequestBody.Companion.create(JSONObject.toJSONString(jsonObj), mediaType);

		Request request = new Request.Builder().url(url + setUrlParams(urlParams)).headers(setHeaders(headerParams))
				.post(requestBody).build();
		LOG.info("Http Request Info:" + request.toString());
		LOG.info("Http Headers:" + request.headers().toString());
		LOG.info("Http Post Body:" + request.body().toString());

		Call call = CLIENT.newCall(request);
		return call.execute();
	}

	/**
	 * 发送JSON 异步post请求
	 * 
	 * @param url
	 * @param urlParams
	 * @param jsonParams
	 * @param headerParams
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static void postJsonAsyncMethod(String url, Map<String, Object> urlParams, Object jsonObj,
			Map<String, Object> headerParams, Callback callback) throws UnsupportedEncodingException {
		MediaType mediaType = MediaType.Companion.parse("application/json;charset=utf-8");
		RequestBody requestBody = RequestBody.Companion.create(JSONObject.toJSONString(jsonObj), mediaType);

		Request request = new Request.Builder().url(url + setUrlParams(urlParams)).headers(setHeaders(headerParams))
				.post(requestBody).build();
		LOG.info("Http Request Info:" + request.toString());
		LOG.info("Http Header:" + request.headers().toString());
		LOG.info("Http Post Body:" + request.body().toString());

		Call call = CLIENT.newCall(request);
		call.enqueue(callback);
	}

	/**
	 * 设置Header头
	 * 
	 * @param headersParams
	 * @return
	 */
	private static Headers setHeaders(Map<String, Object> headersParams) {
		Headers.Builder headerBuilder = new Headers.Builder();

		if (headersParams != null) {
			for (String key : headersParams.keySet()) {
				headerBuilder.add(key, headersParams.get(key).toString());
			}
		}
		return headerBuilder.build();
	}

	/**
	 * 设置get连接拼接参数
	 * 
	 * @param params
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private static String setUrlParams(Map<String, Object> params) throws UnsupportedEncodingException {
		StringBuilder param = new StringBuilder(params.size() * 2);
		boolean isFirst = true;

		for (String key : params.keySet()) {
			if (isFirst) {
				param.append("?");
				isFirst = false;
			} else {
				param.append("&");
			}
			param.append(String.format("%s=%s", key, URLEncoder.encode(params.get(key).toString(), "UTF-8")));
		}
		return param.toString();
	}

}
