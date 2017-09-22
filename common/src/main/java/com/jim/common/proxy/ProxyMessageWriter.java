package com.jim.common.proxy;

public class ProxyMessageWriter extends com.jim.common.io.MessageWriterByteBase<ProxyMessage> {

	public ProxyMessageWriter(String name, int bufferSize, com.jim.common.io.IOWriter writer) {
		super(name, bufferSize, writer);
	}

	@Override
	protected byte[] getDataFromMessage(int currentSection) {
		int length = message.getData().length;
		byte[] data = new byte[7 + length];

		data[0] = message.getType().getValue();
		data[1] = (byte) ((message.getId() >> 24) & 0xFF);
		data[2] = (byte) ((message.getId() >> 16) & 0xFF);
		data[3] = (byte) ((message.getId() >> 8) & 0xFF);
		data[4] = (byte) (message.getId() & 0xFF);
		data[5] = (byte) ((length >> 8) & 0xFF);
		data[6] = (byte) (length & 0xFF);

		System.arraycopy(message.getData(), 0, data, 7, length);

		return data;
	}

	@Override
	protected boolean isLastData(int currentSection) {
		return true;
	}

}
