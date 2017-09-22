package com.jim.proxy.server;

import com.jim.common.io.HeartbeatMessageChannelBase;
import com.jim.common.io.MessageReader;
import com.jim.common.io.MessageWriter;
import com.jim.common.socket.SocketConnection;
import com.jim.common.proxy.ProxyChannel;
import com.jim.common.proxy.ProxyMessage;

import java.nio.channels.SocketChannel;


public class ProxyLocalConnection extends HeartbeatMessageChannelBase<ProxyMessage, ProxyMessage>
		implements ProxyChannel {

	private boolean readHeartbeatTimeout;

	private boolean writeHeartbeatTimeout;

	public ProxyLocalConnection(String name, MessageReader<ProxyMessage> reader, MessageWriter<ProxyMessage> writer,
                                SocketChannel socketChannel, int heartbeatTimePeriod) {
		this(name, heartbeatTimePeriod);
		setDriver(new SocketConnection<ProxyMessage, ProxyMessage>(name, reader, writer, socketChannel));
	}

	public ProxyLocalConnection(String name, int heartbeatTimePeriod) {
		super(name, true, true, 0, heartbeatTimePeriod);
		this.readHeartbeatTimeout = false;
		this.writeHeartbeatTimeout = false;
	}

	@Override
	public void close(int originalConnectionId) {
		close();
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
	protected boolean preOnMessage(ProxyMessage message) {
		readHeartbeatTimeout = false;
		return super.preOnMessage(message);
	}

	@Override
	protected boolean preWriteMessage(ProxyMessage message) {
		writeHeartbeatTimeout = false;
		return super.preWriteMessage(message);
	}

	@Override
	public boolean isChannelDown() {
		return readHeartbeatTimeout && writeHeartbeatTimeout;
	}

}
