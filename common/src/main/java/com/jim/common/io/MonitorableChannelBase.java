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
