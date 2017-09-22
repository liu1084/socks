package com.jim.common.io;

public interface HeartbeatMessageChannel<R, W> extends MonitorableChannel<R, W> {

	boolean isHeartbeat(R message);

	int getHeartbeatTimePeriod();

	boolean needCheckRead();

	boolean needCheckWrite();

	void onReadHeartbeatTimeout();

	void onWriteHeartbeatTimeout();

	boolean isChannelDown();
}
