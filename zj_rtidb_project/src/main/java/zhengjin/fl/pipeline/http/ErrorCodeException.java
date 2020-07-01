package zhengjin.fl.pipeline.http;

import java.io.IOException;

public final class ErrorCodeException extends IOException {

	private static final long serialVersionUID = 1L;

	public ErrorCodeException(String desc) {
		super("ErrorCodeException: " + desc);
	}

}
