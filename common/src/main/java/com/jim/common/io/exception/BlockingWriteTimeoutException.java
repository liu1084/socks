package com.jim.common.io.exception;


import com.jim.common.exception.TimeoutException;

public class BlockingWriteTimeoutException extends TimeoutException {

	private static final long serialVersionUID = 1L;

	private final boolean lastMessageCompleted;

	public BlockingWriteTimeoutException(String msg, boolean lastMessageCompleted) {
		super(msg);
		this.lastMessageCompleted = lastMessageCompleted;
	}

	public boolean isLastMessageCompleted() {
		return lastMessageCompleted;
	}

}
