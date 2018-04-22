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
