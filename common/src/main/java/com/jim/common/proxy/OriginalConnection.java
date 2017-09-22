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
