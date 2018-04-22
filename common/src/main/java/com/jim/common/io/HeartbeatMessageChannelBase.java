/*
 * Copyright 2018 Jim Liu.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * By the way, this Soft can only by education, can not be used in commercial products. All right be reserved.
 */

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
