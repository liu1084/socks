package com.jim.common.io;

import java.nio.ByteBuffer;

public class ByteWriter extends MessageWriterByteBase<byte[]> {

	public ByteWriter(String name, ByteBuffer buffer, IOWriter ioWriter) {
		super(name, buffer, ioWriter);
	}

	public ByteWriter(String name, int bufferSize, IOWriter ioWriter) {
		this(name, ByteBuffer.allocate(bufferSize), ioWriter);
	}

	@Override
	protected byte[] getDataFromMessage(int currentSection) {
		return message;
	}

	@Override
	protected boolean isLastData(int currentSection) {
		return true;
	}

}
