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

package com.jim.common.util;


import com.jim.common.proxy.ProxyMessage;
import com.jim.common.enums.MessageType;

import java.io.UnsupportedEncodingException;

public class MessageUtil {

	public static ProxyMessage createDataMessage(int id, byte[] data) {
		return createMessage(MessageType.DATA, id, data);
	}

	public static ProxyMessage createCloseMessage(int id) {
		return createMessage(MessageType.CLOSE, id, new byte[0]);
	}

	public static ProxyMessage createConnectMessage(int id, String ip, int port) {
		return createMessage(MessageType.CONNECT, id, createIpPortbyte(ip, port));
	}

	public static ProxyMessage createConnectResultMessage(int id, boolean result) {
		byte[] data = new byte[1];
		data[0] = (byte) (result ? 0 : 1);
		return createMessage(MessageType.CONNECT_RESULT, id, data);
	}

	public static byte[] createIpPortbyte(String ip, int port) {
		byte[] message = new byte[6];
		message[0] = (byte) ((port >> 8) & 0xFF);
		message[1] = (byte) (port & 0xFF);
		String[] ips = ip.split("\\.");
		message[2] = Integer.valueOf(ips[0]).byteValue();
		message[3] = Integer.valueOf(ips[1]).byteValue();
		message[4] = Integer.valueOf(ips[2]).byteValue();
		message[5] = Integer.valueOf(ips[3]).byteValue();
		return message;
	}

	public static int getPort(byte b1, byte b2) {
		return ((b1 & 0xFF) << 8) | (b2 & 0xFF);
	}

	public static String getIp(byte b1, byte b2, byte b3, byte b4) {
		return String.valueOf(b1 & 0xFF) + "." + String.valueOf(b2 & 0xFF) + "." + String.valueOf(b3 & 0xFF) + "."
				+ String.valueOf(b4 & 0xFF);
	}

	public static boolean getConnectResult(byte[] data) {
		return data[0] == 0;
	}

	public static ProxyMessage createResolveNameMessage(int id, String address) {
		byte[] data = address.getBytes();
		return createMessage(MessageType.RESOLVE_NAME, id, data);
	}

	public static ProxyMessage createInitMessage(String id) {
		byte[] data;
		try {
			data = id.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return createMessage(MessageType.INIT, 0, data);
	}

	public static ProxyMessage createHeartbeatMessage() {
		return createMessage(MessageType.HEARTBEAT, 0, new byte[0]);
	}

	public static ProxyMessage createMessage(MessageType type, int id, byte[] data) {
		ProxyMessage message = new ProxyMessage();
		message.setType(type);
		message.setId(id);
		message.setData(data);
		return message;
	}

}
