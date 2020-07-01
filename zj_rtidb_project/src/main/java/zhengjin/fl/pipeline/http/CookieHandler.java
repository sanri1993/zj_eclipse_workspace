package zhengjin.fl.pipeline.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public final class CookieHandler implements CookieJar {

	private final Map<String, List<Cookie>> cookieStore = new HashMap<>();

	@Override
	public List<Cookie> loadForRequest(HttpUrl url) {
		List<Cookie> cookies = this.cookieStore.get(url.host());
		return cookies != null ? cookies : new ArrayList<Cookie>();
	}

	@Override
	public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
		this.cookieStore.put(url.host(), cookies);
	}

}
