package com.jim.common.io;

public interface MonitorableChannel<R, W> extends NonBlockingMessageChannel<R, W> {

	public ChannelStatus getChannelStatus();

}
