package com.jim.common.proxy;


import com.jim.common.crypt.Cryptor;
import com.jim.common.io.MessageReader;
import com.jim.common.io.MessageWriter;
import com.jim.common.socket.SocketConnection;

import java.nio.channels.SocketChannel;

public class ProxyConnection extends ProxyChannelBase {

	public ProxyConnection(String name, MessageReader<ProxyMessage> reader, MessageWriter<ProxyMessage> writer,
                           SocketChannel socketChannel, int readHeartbeatTimeoutTimes, int heartbeatTimePeriod, Cryptor cryptor) {
		this(name, heartbeatTimePeriod, heartbeatTimePeriod, cryptor);
		setDriver(new SocketConnection<ProxyMessage, ProxyMessage>(name, reader, writer, socketChannel));
	}

	public ProxyConnection(String name, int readHeartbeatTimeoutTimes, int heartbeatTimePeriod, Cryptor cryptor) {
		super(name, true, true, heartbeatTimePeriod, heartbeatTimePeriod, cryptor);
	}

}
