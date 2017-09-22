package com.jim.common.io;

public interface MessageHandler<R, W> {

	public void onMessage(R message);

	public void handleError(Exception e, W message);

}
