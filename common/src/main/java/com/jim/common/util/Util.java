package com.jim.common.util;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class Util {

	public static String getConnectionName(SocketChannel socketChannel) {
		return "[" + ((InetSocketAddress) socketChannel.socket().getLocalSocketAddress()).getAddress().getHostAddress()
				+ ":" + ((InetSocketAddress) socketChannel.socket().getLocalSocketAddress()).getPort() + "<->"
				+ ((InetSocketAddress) socketChannel.socket().getRemoteSocketAddress()).getAddress().getHostAddress()
				+ ":" + ((InetSocketAddress) socketChannel.socket().getRemoteSocketAddress()).getPort() + "]";
	}

}
