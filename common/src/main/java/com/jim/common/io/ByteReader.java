package com.jim.common.io;

import com.jim.common.io.enums.LengthType;

import java.nio.ByteBuffer;


public class ByteReader extends MessageReaderBase<byte[]> {

	private byte[] data;

	public ByteReader(String name, ByteBuffer buffer, com.jim.common.io.IOReader ioReader) {
		super(name, buffer, ioReader);
	}

	public ByteReader(String name, int bufferSize, IOReader ioReader) {
		this(name, ByteBuffer.allocate(bufferSize), ioReader);
	}

	@Override
	public byte[] getMessageFromData() {
		return data;
	}

	@Override
	protected boolean processData(int currentSection, int currentSectionSize, byte b) {
		throw new RuntimeException("不支持该方法。");
	}

	@Override
	protected void processData(int currentSection, ByteBuffer buffer, int bufferCanReadLength, int currentSectionSize,
                               boolean sectionComplete) {
		data = new byte[bufferCanReadLength];
		buffer.get(data);
	}

	@Override
	protected int getDataLength(int currentSection) {
		return buffer.remaining();
	}

	@Override
	protected LengthType getLengthType(int currentSection) {
		return LengthType.FIXED;
	}

	@Override
	protected boolean isComplete(int currentSection) {
		return true;
	}

}
