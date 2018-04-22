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


import com.jim.common.crypt.Cryptor;
import com.jim.common.io.HeartbeatMessageChannelBase;
import com.jim.common.enums.MessageType;
import com.jim.common.util.MessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public abstract class ProxyChannelBase extends HeartbeatMessageChannelBase<ProxyMessage, ProxyMessage>
		implements ProxyChannel {

	private static final Logger logger = LoggerFactory.getLogger(ProxyChannelBase.class);

	protected final Map<Integer, OriginalConnection> map;

	private final Cryptor cryptor;

	public ProxyChannelBase(String name, boolean needCheckRead, boolean needCheckWrite, int readHeartbeatTiemoutTimes,
                            int heartbeatTimePeriod, Cryptor cryptor) {
		super(name, needCheckRead, needCheckWrite, readHeartbeatTiemoutTimes, heartbeatTimePeriod);
		map = new HashMap<Integer, OriginalConnection>();
		this.cryptor = cryptor;
	}

	public void addOriginalConnection(OriginalConnection originalConnection) {
		logger.info("{}增加本地连接，连接号：{}", getName(), originalConnection.getId());
		OriginalConnection prev = map.put(originalConnection.getId(), originalConnection);
		if (prev != null) {
			logger.warn("{}增加的连接号{}重复，关闭旧连接。", getName(), originalConnection.getId());
			prev.close();
		}
	}

	public void closeOriginalConnection(int originalConnectionId) {
		logger.info("{}关闭本地连接，连接号：{}", getName(), originalConnectionId);
		OriginalConnection originalConnection = map.remove(originalConnectionId);
		if (originalConnection != null) {
			originalConnection.close();
		} else {
			logger.warn("{}关闭的连接号{}不存在，忽略。", getName(), originalConnectionId);
		}
	}

	public void transferData(int originalConnectionId, byte[] data) {
		if (getCryptor() != null) {
			data = getCryptor().decrypt(data);
		}
		logger.info("{}向本地连接转发数据，连接号：{}", getName(), originalConnectionId);
		OriginalConnection originalConnection = map.get(originalConnectionId);
		if (originalConnection != null) {
			originalConnection.writeMessage(data);
		} else {
			logger.warn("{}连接号{}不存在，通知远程将其关闭。", getName(), originalConnectionId);
			close(originalConnectionId);
		}
	}

	@Override
	public void close() {
		for (OriginalConnection originalConnection : map.values()) {
			originalConnection.close();
		}
		map.clear();
		super.close();
	}

	@Override
	public void close(int originalConnectionId) {
		logger.info("{}向本地连接发送关闭消息，连接号：{}", getName(), originalConnectionId);
		// OriginalConnection originalConnection =
		// map.remove(originalConnectionId);
		// if (originalConnection == null) {
		// logger.warn("{}关闭的连接号{}不存在，忽略。", getName(), originalConnectionId);
		// }
		writeMessage(MessageUtil.createCloseMessage(originalConnectionId));
	}

	@Override
	public boolean isHeartbeat(ProxyMessage message) {
		return message.getType() == MessageType.HEARTBEAT;
	}

	@Override
	protected ProxyMessage createHeartbeat() {
		return MessageUtil.createHeartbeatMessage();
	}

	@Override
	protected boolean preWriteMessage(ProxyMessage message) {
		if (getCryptor() != null) {
			switch (message.getType()) {
				case CONNECT:
				case CONNECT_RESULT:
				case DATA:
				case RESOLVE_NAME:
					message.setData(getCryptor().encrypt(message.getData()));
					break;
				default:
					break;
			}
		}
		return super.preWriteMessage(message);
	}

	public Cryptor getCryptor() {
		return cryptor;
	}

}
