package com.jim.common.io;

import java.nio.ByteBuffer;

public abstract class MessageWriterByteBase<T> extends MessageWriterBase<T, byte[]> {

	public MessageWriterByteBase(String name, ByteBuffer buffer, com.jim.common.io.IOWriter ioWriter) {
		super(name, buffer, ioWriter);
	}

	public MessageWriterByteBase(String name, int bufferSize, IOWriter ioWriter) {
		this(name, ByteBuffer.allocate(bufferSize), ioWriter);
	}

	@Override
	protected int getDataLength(byte[] data) {
		return data.length;
	}

	@Override
	protected void fillBufferFromData(ByteBuffer buffer, byte[] data, int dataWritedSize, int canFillLength) {
		buffer.put(data, dataWritedSize, canFillLength);
	}

}
