package com.jim.proxy.local;

import com.jim.common.io.IOReader;
import com.jim.common.io.MessageReaderBase;
import com.jim.common.io.enums.LengthType;
import com.jim.common.proxy.ProxyMessage;
import com.jim.common.enums.MessageType;

import java.nio.ByteBuffer;

public class LocalConnectionReader extends MessageReaderBase<ProxyMessage> {

	private final int id;

	private byte[] data;

	public LocalConnectionReader(String name, int id, int bufferSize, IOReader reader) {
		super(name, bufferSize, reader);
		this.id = id;
	}

	@Override
	public ProxyMessage getMessageFromData() {
		ProxyMessage message = new ProxyMessage();
		message.setType(MessageType.DATA);
		message.setId(id);
		message.setData(data);
		return message;
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
