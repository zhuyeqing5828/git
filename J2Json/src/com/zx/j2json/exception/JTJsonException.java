package com.zx.j2json.exception;

public class JTJsonException extends RuntimeException {

	private static final long serialVersionUID = 7954610110486698382L;

	public JTJsonException() {
		super();
	}

	public JTJsonException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public JTJsonException(String message, Throwable cause) {
		super(message, cause);
	}

	public JTJsonException(String message) {
		super(message);
	}

	public JTJsonException(Throwable cause) {
		super(cause);
	}

}
