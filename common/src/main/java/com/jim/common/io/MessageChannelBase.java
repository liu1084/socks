package com.jim.common.io;


public abstract class MessageChannelBase<R, W> implements MessageChannel<R, W> {

	private final String name;

	private boolean closed;

	public MessageChannelBase(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void close() {
		closed = true;
	}

	public boolean isClosed() {
		return closed;
	}

}
