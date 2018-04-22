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

package com.jim.server;


import com.jim.common.crypt.Cryptor;
import com.jim.common.crypt.DESCryptor;
import com.jim.common.exception.TimeoutException;
import com.jim.common.io.ChannelHeartbeatMonitor;
import com.jim.common.io.MessageHandler;
import com.jim.common.socket.*;
import com.jim.common.proxy.ProxyMessage;
import com.jim.common.proxy.ProxyMessageReader;
import com.jim.common.proxy.ProxyMessageWriter;
import com.jim.common.enums.MessageType;
import com.jim.common.util.MessageUtil;
import com.jim.common.util.Util;
import com.jim.proxy.server.ProxyChannelManager;
import com.jim.proxy.remote.ProxyRemoteConnection;
import com.jim.utils.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.channels.SocketChannel;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class ReverseServer extends SocketServer {

	private static final Logger logger = LoggerFactory.getLogger(ReverseServer.class);

	private static final int BUFFER_SIZE = Integer
			.parseInt(PropertiesUtil.getProperty("remote.proxy.connection.buffer.size"));

	private static final int INIT_TIMEOUT = Integer
			.parseInt(PropertiesUtil.getProperty("remote.proxy.connection.init.timeout"));

	private static final int HEARTBEAT_TIMEOUT_TIMES = Integer
			.parseInt(PropertiesUtil.getProperty("remote.proxy.connection.heartbeat.timeout.times"));

	private static final int HEARTBEAT_TIME_PERIOD = Integer
			.parseInt(PropertiesUtil.getProperty("remote.proxy.connection.heartbeat.time.period"));

	private final ProxyChannelManager channelManager;

	private final ChannelHeartbeatMonitor channelMonitor;

	private boolean needCrypt;

	private RSAPublicKey publicKey;

	private RSAPrivateKey privateKey;

	public ReverseServer(int port, boolean needCrypt, ProxyChannelManager channelManager,
						 ChannelHeartbeatMonitor channelMonitor) throws IOException {
		super("反向连接服务器", port);
		this.channelManager = channelManager;
		this.channelMonitor = channelMonitor;
		this.needCrypt = needCrypt;
		if (needCrypt) {
			KeyPairGenerator keyPairGen;
			try {
				keyPairGen = KeyPairGenerator.getInstance("RSA");
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
			keyPairGen.initialize(1024);

			KeyPair keyPair = keyPairGen.generateKeyPair();

			publicKey = (RSAPublicKey) keyPair.getPublic();

			privateKey = (RSAPrivateKey) keyPair.getPrivate();
		}


		//FIXME!!
	}

	@Override
	protected SocketConnection<?, ?> createConnection(SocketChannel socketChannel) {
		logger.info("接收到远程代理端连接，准备处理，远程代理端：{}", socketChannel.socket().getRemoteSocketAddress());

		String connectionName = Util.getConnectionName(socketChannel);

		SocketConnection<ProxyMessage, ProxyMessage> socketConnection = new SocketConnection<ProxyMessage, ProxyMessage>(
				connectionName, new ProxyMessageReader(connectionName, BUFFER_SIZE, new SocketReader(socketChannel)),
				new ProxyMessageWriter(connectionName, BUFFER_SIZE, new SocketWriter(socketChannel)), socketChannel);

		return socketConnection;
	}

	@Override
	protected void dispatch(SocketProcessor socketProcessor, SocketConnection<?, ?> connection) {

		SocketConnection<ProxyMessage, ProxyMessage> conn = (SocketConnection<ProxyMessage, ProxyMessage>) connection;

		ProxyMessage message;
		try {
			message = conn.blockingRead(INIT_TIMEOUT);
		} catch (TimeoutException e) {
			throw new RuntimeException("没有收到数据。", e);
		}

		String id;
		try {
			id = new String(message.getData(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		Cryptor cryptor = null;
		if (needCrypt) {
			message = MessageUtil.createMessage(MessageType.CRYPT_KEY, 0, publicKey.getEncoded());
			try {
				conn.blockingWrite(message, INIT_TIMEOUT);
			} catch (TimeoutException e) {
				throw new RuntimeException("发送秘钥超时。", e);
			}

			try {
				message = conn.blockingRead(INIT_TIMEOUT);
			} catch (TimeoutException e) {
				throw new RuntimeException("没有收到数据。", e);
			}

			if (message.getType() != MessageType.CRYPT_KEY) {
				throw new RuntimeException("数据错误。");
			}

			try {
				Cipher cipher = Cipher.getInstance("RSA");
				cipher.init(Cipher.DECRYPT_MODE, privateKey);
				byte[] desKey = cipher.doFinal(message.getData());
				cryptor = new DESCryptor(desKey);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		}

		message = MessageUtil.createMessage(MessageType.INIT_RESULT, 0, new byte[0]);

		final ProxyRemoteConnection proxyRemoteConnection = new ProxyRemoteConnection(conn.getName(),
				HEARTBEAT_TIMEOUT_TIMES, HEARTBEAT_TIME_PERIOD, cryptor);
		proxyRemoteConnection.setDriver(conn);

		proxyRemoteConnection.setId(id);

		try {
			channelManager.addChannel(proxyRemoteConnection);
		} catch (Exception e) {
			logger.error("添加通道时发生错误。", e);
			proxyRemoteConnection.close();
			return;
		}

		proxyRemoteConnection.writeMessage(message);

		proxyRemoteConnection.setMessageHandler(new MessageHandler<ProxyMessage, ProxyMessage>() {

			@Override
			public void onMessage(ProxyMessage message) {
				switch (message.getType()) {
					case CLOSE:
						proxyRemoteConnection.closeOriginalConnection(message.getId());
						break;
					case CONNECT_RESULT:
						proxyRemoteConnection.resume(message.getId(), message);
						break;
					case DATA:
						proxyRemoteConnection.transferData(message.getId(), message.getData());
						break;
					case RESOLVE_NAME:
						proxyRemoteConnection.resume(message.getId(), message);
						break;
					default:
						logger.warn("不正确的消息类型：{}，忽略。", message.getType());
						break;
				}
			}

			@Override
			public void handleError(Exception e, ProxyMessage message) {
				logger.error("通道发生错误，通道ID：" + proxyRemoteConnection.getId(), e);
				channelManager.removeChannel(proxyRemoteConnection.getId());
				// channelManager.handleError(proxyRemoteConnection.getId());
			}

		});

		socketProcessor.addConnection(conn);

		conn.startNonBlockingMode();

		channelMonitor.addChannel(proxyRemoteConnection);
	}

}
