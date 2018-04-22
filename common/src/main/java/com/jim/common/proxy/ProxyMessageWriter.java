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
