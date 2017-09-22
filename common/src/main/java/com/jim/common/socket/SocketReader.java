package com.jim.common.socket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SocketReader implements com.jim.common.io.IOReader {

	private final SocketChannel socketChannel;

	public SocketReader(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
	}

	@Override
	public int read(ByteBuffer buffer) throws IOException {
		return socketChannel.read(buffer);
	}

}
