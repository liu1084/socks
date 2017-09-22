package com.jim.common.io;

public interface MessageDriver<R, W> extends BlockingMessageChannel<R, W> {

	public void read();

	public void write();

	public void writeMessage(W message);

	public void setMessageChannel(NonBlockingMessageChannel<R, W> messageChannel);

}
