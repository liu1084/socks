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

package com.jim.common.proxy;

import java.nio.channels.SocketChannel;

public class OriginalConnection extends com.jim.common.io.HeartbeatMessageChannelBase<byte[], byte[]> {

	private final int id;

	private boolean readHeartbeatTimeout;

	private boolean writeHeartbeatTimeout;

	public OriginalConnection(String name, int id, com.jim.common.io.MessageReader<byte[]> reader, com.jim.common.io.MessageWriter<byte[]> writer,
                              SocketChannel socketChannel, int heartbeatTimePeriod) {
		this(name, id, heartbeatTimePeriod);
		setDriver(new com.jim.common.socket.SocketConnection<byte[], byte[]>(name, reader, writer, socketChannel));
	}

	public OriginalConnection(String name, int id, int heartbeatTimePeriod) {
		super(name, true, true, 0, heartbeatTimePeriod);
		this.id = id;
		this.readHeartbeatTimeout = false;
		this.writeHeartbeatTimeout = false;
	}

	public int getId() {
		return id;
	}

	@Override
	public void onReadHeartbeatTimeout() {
		readHeartbeatTimeout = true;
		super.onReadHeartbeatTimeout();
	}

	@Override
	public void onWriteHeartbeatTimeout() {
		writeHeartbeatTimeout = true;
		super.onWriteHeartbeatTimeout();
	}

	@Override
	protected boolean preOnMessage(byte[] message) {
		readHeartbeatTimeout = false;
		return super.preOnMessage(message);
	}

	@Override
	protected boolean preWriteMessage(byte[] message) {
		writeHeartbeatTimeout = false;
		return super.preWriteMessage(message);
	}

	@Override
	public boolean isChannelDown() {
		return readHeartbeatTimeout && writeHeartbeatTimeout;
	}

}
