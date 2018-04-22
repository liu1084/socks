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

package com.jim.proxy.remote;

import com.jim.common.BlockingExecutor;
import com.jim.common.SingleResultBlockingExecutor;
import com.jim.common.crypt.Cryptor;
import com.jim.common.exception.ExecuteException;
import com.jim.common.proxy.OriginalConnection;
import com.jim.common.proxy.ProxyChannelBase;
import com.jim.common.proxy.ProxyMessage;
import com.jim.common.util.MessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class ProxyRemoteChannelBase extends ProxyChannelBase implements ProxyRemoteChannel {

	private static final Logger logger = LoggerFactory.getLogger(ProxyRemoteChannelBase.class);
	private final BlockingExecutor<Integer, ProxyMessage, ProxyMessage, ProxyMessage> remoteCommand;
	private String id;

	public ProxyRemoteChannelBase(String name, String id, boolean needCheckRead, boolean needCheckWrite,
                                  int readHeartbeatTiemoutTimes, int heartbeatTimePeriod, Cryptor cryptor) {
		super(name, needCheckRead, needCheckWrite, readHeartbeatTiemoutTimes, heartbeatTimePeriod, cryptor);
		this.id = id;

		remoteCommand = new SingleResultBlockingExecutor<Integer, ProxyMessage, ProxyMessage, ProxyMessage>() {

			@Override
			protected void execute(ProxyMessage content) throws ExecuteException {
				writeMessage(content);
			}

			@Override
			protected ProxyMessage convert(ProxyMessage result) {
				return result;
			}
		};

	}

	public ProxyRemoteChannelBase(String name, boolean needCheckRead, boolean needCheckWrite,
                                  int readHeartbeatTiemoutTimes, int heartbeatTimePeriod, Cryptor cryptor) {
		this(name, null, needCheckRead, needCheckWrite, readHeartbeatTiemoutTimes, heartbeatTimePeriod, cryptor);
	}

	public void resume(int syncNumber, ProxyMessage result) {
		if (getCryptor() != null) {
			result.setData(getCryptor().decrypt(result.getData()));
		}
		remoteCommand.resume(syncNumber, result);
	}

	@Override
	public String resolveName(int syncNumber, String hostname, int timeout) {
		logger.debug("{}远程解析Hostname：{}", getName(), hostname);
		ProxyMessage message = MessageUtil.createResolveNameMessage(syncNumber, hostname);

		try {
			String ip = new String(remoteCommand.blockingExecute(syncNumber, message, timeout).getData());
			logger.debug("{}远程解析Hostname：{}，ip：{}", getName(), hostname, ip);
			return ip;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void connect(OriginalConnection originalConnection, String ip, int port, int timeout) {

		addOriginalConnection(originalConnection);

		ProxyMessage message = MessageUtil.createConnectMessage(originalConnection.getId(), ip, port);

		boolean connectResult;
		try {
			connectResult = MessageUtil.getConnectResult(
					remoteCommand.blockingExecute(originalConnection.getId(), message, timeout).getData());
		} catch (Exception e) {
			map.remove(originalConnection.getId());
			throw new RuntimeException(e);
		}
		if (!connectResult) {
			map.remove(originalConnection.getId());
			throw new RuntimeException("远程创建连接失败。");
		}
	}

	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
