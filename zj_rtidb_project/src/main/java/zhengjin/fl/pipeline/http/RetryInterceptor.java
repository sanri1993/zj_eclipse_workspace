package zhengjin.fl.pipeline.http;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public final class RetryInterceptor implements Interceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(RetryInterceptor.class);

	private int maxRetryCount;

	public RetryInterceptor(int maxRetryCount) {
		this.maxRetryCount = maxRetryCount;
	}

	@Override
	public Response intercept(Chain chain) throws IOException {
		Response response = null;
		Request request = chain.request();

		int count = 0;
		for (; count < this.maxRetryCount; count++) {
			if (count > 0) {
				LOGGER.info("Failed and retry {}.", String.valueOf(count));
			}

			try {
				response = chain.proceed(request);
				if (response.isSuccessful()) {
					return response;
				}
				throw new ErrorCodeException(String.format("status code [%d], response body [%s]", response.code(),
						response.body().string()));
			} catch (IOException e) {
				LOGGER.warn(e.getMessage());
				try {
					TimeUnit.MILLISECONDS.sleep(300L);
				} catch (InterruptedException e1) {
					LOGGER.warn(e1.getMessage());
				}
			}
		}

		LOGGER.info("Failed and retry {}.", String.valueOf(count));
		return chain.proceed(request);
	}

}
