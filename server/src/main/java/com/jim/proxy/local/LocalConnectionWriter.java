package com.jim.proxy.local;

import com.jim.common.io.IOWriter;
import com.jim.common.io.MessageWriterByteBase;
import com.jim.common.proxy.ProxyMessage;
import com.jim.common.enums.MessageType;

public class LocalConnectionWriter extends MessageWriterByteBase<ProxyMessage> {

	public LocalConnectionWriter(String name, int bufferSize, IOWriter writer) {
		super(name, bufferSize, writer);
	}

	@Override
	public void setMessage(ProxyMessage message) {
		if (message.getType() == MessageType.DATA) {
			super.setMessage(message);
		} else {
			throw new RuntimeException("不支持该消息类型。");
		}
	}

	@Override
	protected byte[] getDataFromMessage(int currentSection) {
		return message.getData();
	}

	@Override
	protected boolean isLastData(int currentSection) {
		return true;
	}

}
