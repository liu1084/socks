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

package com.jim.common.enums;

public enum MessageType {

	HEARTBEAT((byte) 0x00), INIT((byte) 0x01), CRYPT_KEY((byte) 0x02), INIT_RESULT((byte) 0x03), RESOLVE_NAME(
			(byte) 0x04), CONNECT((byte) 0x05), CONNECT_RESULT((byte) 0x06), CLOSE((byte) 0x07), DATA((byte) 0x08);

	private byte value;

	private MessageType(byte value) {
		this.value = value;
	}

	public static MessageType parseValue(byte value) {
		for (MessageType type : MessageType.values()) {
			if (type.getValue() == value) {
				return type;
			}
		}
		throw new RuntimeException("No Such Message Type.");
	}

	public byte getValue() {
		return value;
	}

}
