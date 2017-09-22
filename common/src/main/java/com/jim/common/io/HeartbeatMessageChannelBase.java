package com.jim.common.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class HeartbeatMessageChannelBase<R, W> extends MonitorableChannelBase<R, W>
		implements HeartbeatMessageChannel<R, W> {

	private static final Logger logger = LoggerFactory.getLogger(com.jim.common.io.HeartbeatMessageChannelBase.class);

	private final boolean needCheckRead;

	private final boolean needCheckWrite;

	private final int readHeartbeatTimeoutTimes;
	private final int heartbeatTimePeriod;
	private int readHeartbeatTimeoutCount;

	public HeartbeatMessageChannelBase(String name, boolean needCheckRead, boolean needCheckWrite,
                                       int readHeartbeatTimeoutTimes, int heartbeatTimePeriod) {
		super(name);
		this.needCheckRead = needCheckRead;
		this.needCheckWrite = needCheckWrite;
		this.readHeartbeatTimeoutTimes = readHeartbeatTimeoutTimes;
		this.heartbeatTimePeriod = heartbeatTimePeriod;
	}

	@Override
	protected boolean preOnMessage(R message) {
		boolean returnValue = super.preOnMessage(message);
		readHeartbeatTimeoutCount = 0;
		if (isHeartbeat(message)) {
			logger.debug("{}接收到心跳消息，丢弃该消息。", getName());
			return false;
		} else {
			return returnValue;
		}
	}

	@Override
	public void onReadHeartbeatTimeout() {
		readHeartbeatTimeoutCount++;
		logger.warn("{}连续{}次未收到心跳消息。", getName(), readHeartbeatTimeoutCount);
	}

	@Override
	public void onWriteHeartbeatTimeout() {
		W message = createHeartbeat();
		if (message != null) {
			logger.debug("{}发送心跳。", getName());
			writeMessage(message);
		}
	}

	@Override
	public boolean needCheckRead() {
		return needCheckRead;
	}

	@Override
	public boolean needCheckWrite() {
		return needCheckWrite;
	}

	@Override
	public boolean isHeartbeat(R message) {
		return false;
	}

	@Override
	public int getHeartbeatTimePeriod() {
		return heartbeatTimePeriod;
	}

	protected W createHeartbeat() {
		return null;
	}

	@Override
	public boolean isChannelDown() {
		if (readHeartbeatTimeoutCount > readHeartbeatTimeoutTimes) {
			logger.error("{}长时间未收到心跳消息，连接将关闭。", getName());
			return true;
		}
		return false;
	}

}
