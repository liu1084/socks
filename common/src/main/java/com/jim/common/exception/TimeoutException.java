package com.jim.common.exception;

public class TimeoutException extends Exception {

	private static final long serialVersionUID = 1L;

	public TimeoutException(String msg) {
		super(msg);
	}

}
