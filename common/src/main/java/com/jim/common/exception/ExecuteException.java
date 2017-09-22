package com.jim.common.exception;

public class ExecuteException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ExecuteException(String msg, Exception e) {
		super(msg, e);
	}

}
