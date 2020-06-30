package zhengjin.fl.pipeline.apis;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public final class RetryIntercepter implements Interceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(RetryIntercepter.class);

	private int maxRetryCount;
	private int count = 0;

	public RetryIntercepter(int maxRetryCount) {
		this.maxRetryCount = maxRetryCount;
	}

	@Override
	public Response intercept(Chain chain) throws IOException {
		Response response = null;
		Request request = chain.request();
		while (this.count < this.maxRetryCount) {
			this.count++;
			try {
				response = chain.proceed(request);
				if (response.isSuccessful()) {
					return response;
				}
				// invoke response.body().string() instead of response.close() to close stream
				// when exception
				throw new IOException(
						String.format("status code: %d, response body: %s", response.code(), response.body().string()));
			} catch (IOException e) {
				LOGGER.warn("Error: " + e.getMessage());
				LOGGER.info("Failed and retry " + String.valueOf(this.count));
				try {
					TimeUnit.MILLISECONDS.sleep(300L);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}

		return chain.proceed(request);
	}

}
