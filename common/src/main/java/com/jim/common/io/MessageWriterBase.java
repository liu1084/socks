package com.jim.common.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public abstract class MessageWriterBase<T, DATA> implements MessageWriter<T> {

	private static final Logger logger = LoggerFactory.getLogger(com.jim.common.io.MessageWriterBase.class);
	protected final ByteBuffer buffer;
	private final String name;
	protected T message;
	protected DATA data;
	protected int offset;
	protected boolean dataComplete;
	private com.jim.common.io.IOWriter writer;

	private boolean messageCompleted;

	private int currentSection;

	public MessageWriterBase(String name, ByteBuffer buffer, com.jim.common.io.IOWriter writer) {
		this.name = name;
		this.writer = writer;
		this.buffer = buffer;
		messageCompleted = true;
		currentSection = -1;
	}

	public MessageWriterBase(String name, int bufferSize, com.jim.common.io.IOWriter writer) {
		this(name, ByteBuffer.allocate(bufferSize), writer);
	}

	@Override
	public void setMessage(T message) {
		if (!messageCompleted) {
			throw new RuntimeException(name + "前一消息还未处理完成。");
		}

		logger.debug("{}准备写入的消息：{}", name, message);

		this.message = message;
		dataComplete = true;
		currentSection = -1;
	}

	@Override
	public boolean isMessageCompleted() {
		return messageCompleted;
	}

	@Override
	public void write() throws IOException {

		int writed = 0;
		if (!messageCompleted) {
			fillBufferFromData();
			writed = writer.write(buffer);
			if (logger.isDebugEnabled()) {
				logger.debug("{}写入{}字节数据，数据内容：{}", name, writed, getLogString(buffer, writed));
			}
			if (buffer.hasRemaining()) {
				buffer.compact();
				messageCompleted = false;
				return;
			} else {
				buffer.clear();
			}
			if (dataComplete && isLastData(currentSection)) {
				messageCompleted = true;
				return;
			}
		}

		do {
			if (dataComplete) {
				currentSection++;
				data = getDataFromMessage(currentSection);
				offset = 0;
				dataComplete = false;
			}

			do {
				fillBufferFromData();
				writed = writer.write(buffer);
				if (logger.isDebugEnabled()) {
					logger.debug("{}写入{}字节数据，数据内容：{}", name, writed, getLogString(buffer, writed));
				}
				if (buffer.hasRemaining()) {
					buffer.compact();
					messageCompleted = false;
					return;
				} else {
					buffer.clear();
				}
			} while (!dataComplete);

		} while (!isLastData(currentSection));

		messageCompleted = true;

	}

	protected String getLogString(ByteBuffer buffer, int length) {
		byte[] newdata = Arrays.copyOf(buffer.array(), length);
		return new String(newdata);
	}

	protected abstract DATA getDataFromMessage(int currentSection);

	protected abstract boolean isLastData(int currentSection);

	private void fillBufferFromData() {
		if (dataComplete) {
			buffer.flip();
			return;
		}

		int length = getDataLength(data) - offset;

		if (length > buffer.remaining()) {
			length = buffer.remaining();
		}

		fillBufferFromData(buffer, data, offset, length);
		offset = offset + length;
		buffer.flip();

		if (offset == getDataLength(data)) {
			dataComplete = true;
		}

	}

	protected abstract int getDataLength(DATA data);

	protected abstract void fillBufferFromData(ByteBuffer buffer, DATA data, int dataWritedSize, int canFillLength);

	@Override
	public IOWriter getIOWriter() {
		return writer;
	}

}
