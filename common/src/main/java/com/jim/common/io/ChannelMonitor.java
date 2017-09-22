package com.jim.common.io;

public interface ChannelMonitor {

	public void addChannel(MonitorableChannel<?, ?> channel);

	public void removeChannel(MonitorableChannel<?, ?> channel);

}
