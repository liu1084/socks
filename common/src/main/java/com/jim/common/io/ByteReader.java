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
