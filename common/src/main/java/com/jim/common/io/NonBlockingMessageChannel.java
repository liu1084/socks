package com.jim.common.io;

public interface NonBlockingMessageChannel<R, W> extends MessageChannel<R, W> {

	public void writeMessage(W message);

	public void onMessage(R message);

	public void handleError(Exception e, W message);

}
