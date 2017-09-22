package com.jim.common.io;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class ChannelStatus {

	private boolean closed;

	private long lastReadTime;

	private long lastWriteTime;

	public ChannelStatus(boolean closed, long lastReadTime, long lastWriteTime) {
		this.closed = closed;
		this.lastReadTime = lastReadTime;
		this.lastWriteTime = lastWriteTime;
	}

	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	public long getLastReadTime() {
		return lastReadTime;
	}

	public void setLastReadTime(long lastReadTime) {
		this.lastReadTime = lastReadTime;
	}

	public long getLastWriteTime() {
		return lastWriteTime;
	}

	public void setLastWriteTime(long lastWriteTime) {
		this.lastWriteTime = lastWriteTime;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("closed", closed).append("lastReadTime", lastReadTime)
				.append("lastWriteTime", lastWriteTime).toString();
	}

}
