/*
 * Copyright 2018 Jim Liu.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * By the way, this Soft can only by education, can not be used in commercial products. All right be reserved.
 */

package com.jim.common.proxy;

import java.nio.ByteBuffer;

public class ProxyMessageReader extends com.jim.common.io.MessageReaderBase<ProxyMessage> {

	private byte[] header;

	private int index;

	private byte[] data;

	public ProxyMessageReader(String name, int bufferSize, com.jim.common.io.IOReader reader) {
		super(name, bufferSize, reader);
		header = new byte[7];
		index = 0;
	}

	@Override
	public ProxyMessage getMessageFromData() {
		ProxyMessage message = new ProxyMessage();
		message.setType(com.jim.common.enums.MessageType.parseValue(header[0]));
		message.setId(((header[1] & 0xFF) << 24) | ((header[2] & 0xFF) << 16) | ((header[3] & 0xFF) << 8)
				| (header[4] & 0xFF));
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
		if (currentSection == 0) {
			buffer.get(header, index, bufferCanReadLength);
			index = currentSectionSize;
			if (sectionComplete) {
				data = new byte[((header[5] & 0xFF) << 8) | (header[6] & 0xFF)];
				index = 0;
			}
		} else {
			buffer.get(data, index, bufferCanReadLength);
			index = currentSectionSize;
			if (sectionComplete) {
				index = 0;
			}
		}
	}

	@Override
	protected int getDataLength(int currentSection) {
		if (currentSection == 0) {
			return 7;
		} else {
			return data.length;
		}
	}

	@Override
	protected com.jim.common.io.enums.LengthType getLengthType(int currentSection) {
		return com.jim.common.io.enums.LengthType.FIXED;
	}

	@Override
	protected boolean isComplete(int currentSection) {
		if (currentSection == 0) {
			return false;
		} else {
			return true;
		}
	}

}
