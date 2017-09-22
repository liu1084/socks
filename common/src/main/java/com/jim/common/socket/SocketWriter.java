package com.jim.common.socket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SocketWriter implements com.jim.common.io.IOWriter {

	private final SocketChannel socketChannel;

	public SocketWriter(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
	}

	@Override
	public int write(ByteBuffer buffer) throws IOException {
		return socketChannel.write(buffer);
	}

}
