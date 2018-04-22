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

package com.jim.socks;

import com.jim.common.io.IOReader;
import com.jim.common.io.MessageReaderBase;
import com.jim.common.io.enums.LengthType;
import com.jim.common.util.MessageUtil;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class Socks4ConnectMsgReader extends MessageReaderBase<Socks4ConnectMsg> {

	private final static int BTYE_ARRAY_SIZE = 100;

	private int index;

	private byte[] data;

	private String ip;

	private int port;

	private String id;

	private String hostname;

	public Socks4ConnectMsgReader(String name, int bufferSize, IOReader reader) {
		super(name, bufferSize, reader);
		data = new byte[BTYE_ARRAY_SIZE];
	}

	@Override
	protected Socks4ConnectMsg getMessageFromData() {
		Socks4ConnectMsg message = new Socks4ConnectMsg();
		message.setPort(port);
		message.setIp(ip);
		message.setId(id);
		message.setHostname(hostname);
		return message;
	}

	@Override
	protected boolean processData(int currentSection, int currentSectionSize, byte b) {
		if (currentSection == 3) {
			if (b == 0) {
				try {
					id = new String(data, 0, index, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
				return true;
			}
			data[index] = b;
			index = currentSectionSize;
			return false;
		}
		if (currentSection == 4) {
			if (b == 0) {
				try {
					hostname = new String(data, 0, index, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
				return true;
			}
			data[index] = b;
			index = currentSectionSize;
			return false;
		}
		throw new RuntimeException("消息处理出错。");
	}

	@Override
	protected void processData(int currentSection, ByteBuffer buffer, int bufferCanReadLength, int currentSectionSize,
                               boolean sectionComplete) {
		if (currentSection == 0) {
			buffer.get(data, index, bufferCanReadLength);
			index = currentSectionSize;
			if (sectionComplete) {
				if (data[0] != 0x04 && data[1] != 0x01) {
					throw new RuntimeException("Socks协议版本不正确，只支持Socks4协议。");
				}
				index = 0;
			}
		} else if (currentSection == 1) {
			buffer.get(data, index, bufferCanReadLength);
			index = currentSectionSize;
			if (sectionComplete) {
				port = MessageUtil.getPort(data[0], data[1]);
				index = 0;
			}
		} else if (currentSection == 2) {
			buffer.get(data, index, bufferCanReadLength);
			index = currentSectionSize;
			if (sectionComplete) {
				ip = MessageUtil.getIp(data[0], data[1], data[2], data[3]);
				if (ip.startsWith("0.0.0.")) {
					ip = null;
				}
				index = 0;
			}
		}
	}

	@Override
	protected int getDataLength(int currentSection) {
		if (currentSection == 0) {
			return 2;
		} else if (currentSection == 1) {
			return 2;
		} else if (currentSection == 2) {
			return 4;
		}
		return 0;
	}

	@Override
	protected LengthType getLengthType(int currentSection) {
		if (currentSection == 0) {
			return LengthType.FIXED;
		} else if (currentSection == 1) {
			return LengthType.FIXED;
		} else if (currentSection == 2) {
			return LengthType.FIXED;
		}
		return LengthType.VARIANT;
	}

	@Override
	protected boolean isComplete(int currentSection) {
		if (currentSection == 3) {
			if (ip != null) {
				hostname = null;
				return true;
			}
		} else if (currentSection == 4) {
			return true;
		}
		return false;
	}

}
