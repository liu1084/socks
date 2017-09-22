package com.jim.common.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public abstract class MessageReaderBase<T> implements com.jim.common.io.MessageReader<T> {

	private static final Logger logger = LoggerFactory.getLogger(MessageReaderBase.class);
	protected final ByteBuffer buffer;
	private final String name;
	protected boolean sectionComplete;
	protected int currentSection;

	private com.jim.common.io.IOReader reader;

	private boolean complete;

	private boolean finish;

	private int offset;

	private com.jim.common.io.enums.LengthType lengthType;

	private int dataLength;

	public MessageReaderBase(String name, ByteBuffer buffer, com.jim.common.io.IOReader reader) {
		this.name = name;
		sectionComplete = true;
		currentSection = -1;
		this.buffer = buffer;
		this.reader = reader;
		complete = false;
		finish = true;
	}

	public MessageReaderBase(String name, int bufferSize, com.jim.common.io.IOReader reader) {
		this(name, ByteBuffer.allocate(bufferSize), reader);
	}

	@Override
	public T getMessage() {
		T message = getMessageFromData();
		complete = false;

		logger.debug("{}读取到的消息：{}", name, message);

		return message;
	}

	protected abstract T getMessageFromData();

	@Override
	public void read() throws IOException {

		int readed = 0;
		if (finish) {
			buffer.clear();
			readed = reader.read(buffer);
			if (readed == 0) {
				// logger.debug("{}没有读取到任何数据。", name);
				return;
			} else if (readed == -1) {
				if (lengthType == null){
					return;
				}
				if (lengthType == com.jim.common.io.enums.LengthType.UNTIL_END) {
					complete = true;
					lengthType = null;
					return;
				}
				complete = false;
				throw new IOException(name + "读取到流结束。");
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("{}读取{}字节数据，数据内容：{}", name, readed, getLogString(buffer, readed));
				}
				finish = false;
			}
			buffer.flip();
			if (lengthType != null) {
				logger.debug("上一条消息未读完，继续读取。");
				processBufferData(buffer, lengthType, dataLength, currentSection);
				if (sectionComplete && isComplete(currentSection)) {
					currentSection = -1;
					complete = true;
					lengthType = null;

					finish = !buffer.hasRemaining();
					return;
				}
			}
		}

		do {
			if (sectionComplete) {
				currentSection++;
				offset = 0;

				lengthType = getLengthType(currentSection);
				dataLength = getDataLength(currentSection);

				processBufferData(buffer, lengthType, dataLength, currentSection);
			}

			while (!sectionComplete) {
				if (buffer.limit() < buffer.capacity()) {
					complete = false;
					finish = true;
					return;
				} else {
					buffer.clear();
					readed = reader.read(buffer);
					if (readed == 0) {
						complete = false;
						finish = true;
						return;
					} else if (readed == -1) {
						if (lengthType == com.jim.common.io.enums.LengthType.UNTIL_END) {
							complete = true;
							finish = true;
							lengthType = null;
							return;
						}
						complete = false;
						throw new IOException(name + "数据读取结束。");
					} else {
						if (logger.isDebugEnabled()) {
							logger.debug("{}读取{}字节数据，数据内容：{}", name, readed, getLogString(buffer, readed));
						}
					}
					buffer.flip();
					processBufferData(buffer, lengthType, dataLength, currentSection);
				}
			}

		} while (!isComplete(currentSection));

		currentSection = -1;
		complete = true;
		lengthType = null;

		finish = !buffer.hasRemaining();

	}

	protected String getLogString(ByteBuffer buffer, int length) {
		byte[] newdata = Arrays.copyOf(buffer.array(), length);
		return new String(newdata);
	}

	private void processBufferData(ByteBuffer buffer, com.jim.common.io.enums.LengthType lengthType, int dataLength, int currentSection) {
		logger.debug("准备读取数据：lengthType={}，dataLength={}，currentSection={}，offset={}，buffer.remaining={}", lengthType,
				dataLength, currentSection, offset, buffer.remaining());
		if (lengthType == com.jim.common.io.enums.LengthType.FIXED) {

			if (dataLength == 0) {
				sectionComplete = true;
				return;
			}

			if (!buffer.hasRemaining()) {
				sectionComplete = false;
				return;
			}

			int length = dataLength - offset;

			if (length > buffer.remaining()) {
				length = buffer.remaining();
			}

			// buffer.get(sectionData[section], sectionOffset[section], length);
			offset = offset + length;
			if (offset == dataLength) {
				sectionComplete = true;
			} else {
				sectionComplete = false;
			}

			processData(currentSection, buffer, length, offset, sectionComplete);

		}

		if (lengthType == com.jim.common.io.enums.LengthType.VARIANT) {
			sectionComplete = false;
			while (buffer.hasRemaining()) {
				byte b = buffer.get();
				offset++;
				sectionComplete = processData(currentSection, offset, b);
				if (sectionComplete) {
					break;
				}
			}
		}

		if (lengthType == com.jim.common.io.enums.LengthType.UNTIL_END) {
			offset = offset + buffer.remaining();
			sectionComplete = false;
			processData(currentSection, buffer, buffer.remaining(), offset, sectionComplete);
		}
		logger.debug("读取数据：offset={}，buffer.remaining={}，sectionComplete={}", offset, buffer.remaining(),
				sectionComplete);
	}

	protected abstract boolean processData(int currentSection, int currentSectionSize, byte b);

	protected abstract void processData(int currentSection, ByteBuffer buffer, int bufferCanReadLength,
                                        int currentSectionSize, boolean sectionComplete);

	protected abstract int getDataLength(int currentSection);

	protected abstract com.jim.common.io.enums.LengthType getLengthType(int currentSection);

	@Override
	public boolean isMessageCompleted() {
		return complete;
	}

	@Override
	public boolean isReadFinished() {
		return finish;
	}

	protected abstract boolean isComplete(int currentSection);

	@Override
	public com.jim.common.io.IOReader getIOReader() {
		return reader;
	}

}