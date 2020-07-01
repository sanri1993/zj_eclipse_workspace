package zhengjin.fl.pipeline.http;

import okhttp3.MediaType;

public final class Constants {

	static final int CONNECTION_TIME_OUT = 3;
	static final int SOCKET_TIME_OUT = 3;
	static final int MAX_IDLE_CONNECTIONS = 5;
	static final long KEEP_ALLIVE_TIME = 60000L;

	// total requests if failed
	static final int MAX_RETRY_COUNT = 3;

	static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json;charset=utf-8");

}
