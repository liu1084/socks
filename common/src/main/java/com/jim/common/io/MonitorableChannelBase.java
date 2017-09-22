package com.jim.common.io;

public abstract class MonitorableChannelBase<R, W> extends NonBlockingMessageChannelBase<R, W>
		implements MonitorableChannel<R, W> {

	private long lastReadTime;

	private long lastWriteTime;

	public MonitorableChannelBase(String name) {
		super(name);
		lastReadTime = System.currentTimeMillis();
		lastWriteTime = lastReadTime;
	}

	@Override
	public ChannelStatus getChannelStatus() {
		return new ChannelStatus(isClosed(), lastReadTime, lastWriteTime);
	}

	@Override
	protected boolean preOnMessage(R message) {
		updateReadTime(message);
		return super.preOnMessage(message);
	}

	private void updateReadTime(R message) {
		lastReadTime = System.currentTimeMillis();
	}

	@Override
	protected boolean preWriteMessage(W message) {
		updateWriteTime(message);
		return super.preWriteMessage(message);
	}

	private void updateWriteTime(W message) {
		lastWriteTime = System.currentTimeMillis();
	}

}
