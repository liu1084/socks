package com.jim.proxy.remote;

import com.jim.common.crypt.Cryptor;
import com.jim.common.io.MessageReader;
import com.jim.common.io.MessageWriter;
import com.jim.common.socket.SocketConnection;
import com.jim.common.proxy.ProxyMessage;

import java.nio.channels.SocketChannel;


public class ProxyRemoteConnection extends ProxyRemoteChannelBase {

	public ProxyRemoteConnection(String name, String id, MessageReader<ProxyMessage> reader,
                                 MessageWriter<ProxyMessage> writer, SocketChannel socketChannel, int readHeartbeatTimeoutTimes,
                                 int heartbeatTimePeriod, Cryptor cryptor) {
		this(name, id, readHeartbeatTimeoutTimes, heartbeatTimePeriod, cryptor);
		setDriver(new SocketConnection<ProxyMessage, ProxyMessage>(id, reader, writer, socketChannel));
	}

	public ProxyRemoteConnection(String name, String id, int readHeartbeatTimeoutTimes, int heartbeatTimePeriod,
                                 Cryptor cryptor) {
		super(name, id, true, true, readHeartbeatTimeoutTimes, heartbeatTimePeriod, cryptor);
	}

	public ProxyRemoteConnection(String name, int readHeartbeatTimeoutTimes, int heartbeatTimePeriod, Cryptor cryptor) {
		this(name, null, readHeartbeatTimeoutTimes, heartbeatTimePeriod, cryptor);
	}

}
