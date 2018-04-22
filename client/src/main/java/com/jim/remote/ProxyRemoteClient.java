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

package com.jim.remote;

import com.jim.common.Controllable;
import com.jim.common.Processor;
import com.jim.common.crypt.CryptUtil;
import com.jim.common.crypt.Cryptor;
import com.jim.common.crypt.DESCryptor;
import com.jim.common.exception.TimeoutException;
import com.jim.common.io.ByteReader;
import com.jim.common.io.ByteWriter;
import com.jim.common.io.ChannelHeartbeatMonitor;
import com.jim.common.io.MessageHandler;
import com.jim.common.io.exception.BlockingWriteTimeoutException;
import com.jim.common.proxy.*;
import com.jim.common.socket.SocketConnection;
import com.jim.common.socket.SocketProcessor;
import com.jim.common.socket.SocketReader;
import com.jim.common.socket.SocketWriter;
import com.jim.config.Config;
import com.jim.common.enums.MessageType;
import com.jim.common.util.MessageUtil;
import com.jim.common.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.Enumeration;

public class ProxyRemoteClient implements Controllable {

	private static final Logger logger = LoggerFactory.getLogger(ProxyRemoteClient.class);

	private static int ORIGINAL_BUFFER_SIZE;

	private static int PROXY_BUFFER_SIZE;

	private static int PROXY_INIT_TIMEOUT;

	private static int ORIGINAL_HEARTBEAT_TIME_PERIOD;

	private static int PROXY_HEARTBEAT_TIMEOUT_TIMES;

	private static int PROXY_HEARTBEAT_TIME_PERIOD;


	private final String serverIp;

	private final int serverPort;
	private final SocketProcessor socketProcessor;
	private final ChannelHeartbeatMonitor channelMonitor;
	private final Processor reconnectProcessor;
	private String clientIp;
	private ProxyConnection proxyConnection;

	public ProxyRemoteClient(String serverIp, int serverPort, String clientIp, SocketProcessor socketProcessor,
                             ChannelHeartbeatMonitor channelMonitor, final int reconnectTimes, final int reconnectTimePeriod) {
		Config config = new Config();
		config.setConfig("remote.client.properties");
		logger.debug("Config file is:" + config.getConfig());
		ORIGINAL_BUFFER_SIZE = Integer
				.parseInt(config.getParameter("original.connection.buffer.size"));

		PROXY_BUFFER_SIZE = Integer
				.parseInt(config.getParameter("proxy.connection.buffer.size"));

		PROXY_INIT_TIMEOUT = Integer
				.parseInt(config.getParameter("proxy.connection.init.timeout"));

		ORIGINAL_HEARTBEAT_TIME_PERIOD = Integer
				.parseInt(config.getParameter("original.connection.heartbeat.time.period"));

		PROXY_HEARTBEAT_TIMEOUT_TIMES = Integer
				.parseInt(config.getParameter("proxy.connection.heartbeat.timeout.times"));

		PROXY_HEARTBEAT_TIME_PERIOD = Integer
				.parseInt(config.getParameter("proxy.connection.heartbeat.time.period"));
		this.serverIp = serverIp;
		this.serverPort = serverPort;

		if (StringUtils.isNotBlank(clientIp)) {
			this.clientIp = clientIp;
		} else {
			try {
				this.clientIp = getLocalHostLANAddress().getHostAddress();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		this.socketProcessor = socketProcessor;
		this.channelMonitor = channelMonitor;
		reconnectProcessor = new Processor("重新连接处理") {

			private int reconnectCount;

			@Override
			protected void init() {
				reconnectCount = 0;
			}

			@Override
			protected boolean process() throws Exception {
				if ((reconnectTimes >= 0 && reconnectCount < reconnectTimes) || reconnectTimes < 0) {
					logger.info("第{}次尝试重新连接。", reconnectCount + 1);
					connect();
				} else {
					logger.info("超出重试次数，不再重新连接。");
				}
				return false;
			}

			@Override
			protected boolean handleError(Exception e) {
				logger.error("重新连接时发生异常。", e);
				reconnectCount++;
				if (reconnectTimes >= 0 && reconnectCount >= reconnectTimes) {
					logger.info("超出重试次数，不再重新连接。");
					return false;
				}
				try {
					Thread.sleep(reconnectTimePeriod);
				} catch (InterruptedException e1) {
				}
				return true;
			}

		};
	}

	private static InetAddress getLocalHostLANAddress() throws UnknownHostException {
		try {
			InetAddress candidateAddress = null;
			for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
				NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
				for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
					InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
					if (!inetAddr.isLoopbackAddress()) {

						if (inetAddr.isSiteLocalAddress()) {
							return inetAddr;
						} else if (candidateAddress == null) {
							candidateAddress = inetAddr;
						}
					}
				}
			}
			if (candidateAddress != null) {
				return candidateAddress;
			}
			InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
			if (jdkSuppliedAddress == null) {
				throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
			}
			return jdkSuppliedAddress;
		} catch (Exception e) {
			UnknownHostException unknownHostException = new UnknownHostException("获取本地IP地址失败: " + e);
			unknownHostException.initCause(e);
			throw unknownHostException;
		}
	}

	public void connect() {
		SocketChannel socketChannel;
		try {
			socketChannel = SocketChannel.open();
			socketChannel.connect(new InetSocketAddress(serverIp, serverPort));
			socketChannel.configureBlocking(false);
		} catch (IOException e) {
			throw new RuntimeException("网络初始化失败。", e);
		}

		logger.info("连接Socks代理反向服务器，{}:{}", serverIp, serverPort);

		String connectionName = Util.getConnectionName(socketChannel);

		SocketConnection<ProxyMessage, ProxyMessage> connection = new SocketConnection<ProxyMessage, ProxyMessage>(
				connectionName,
				new ProxyMessageReader(connectionName, PROXY_BUFFER_SIZE, new SocketReader(socketChannel)),
				new ProxyMessageWriter(connectionName, PROXY_BUFFER_SIZE, new SocketWriter(socketChannel)),
				socketChannel);

		ProxyMessage message = MessageUtil.createInitMessage(clientIp);

		try {
			connection.blockingWrite(message, PROXY_INIT_TIMEOUT);
		} catch (BlockingWriteTimeoutException e) {
			throw new RuntimeException(e);
		}

		try {
			message = connection.blockingRead(PROXY_INIT_TIMEOUT);
		} catch (TimeoutException e) {
			throw new RuntimeException(e);
		}

		Cryptor cryptor = null;
		if (message.getType() == MessageType.CRYPT_KEY) {
			byte[] desKey = CryptUtil.generateDESKey().getEncoded();

			X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(message.getData());

			byte[] data;

			try {
				KeyFactory keyFactory = KeyFactory.getInstance("RSA");
				Key publicKey = keyFactory.generatePublic(x509KeySpec);

				Cipher cipher = Cipher.getInstance("RSA");
				cipher.init(Cipher.ENCRYPT_MODE, publicKey);
				data = cipher.doFinal(desKey);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			message.setData(data);

			try {
				connection.blockingWrite(message, PROXY_INIT_TIMEOUT);
			} catch (TimeoutException e) {
				throw new RuntimeException("发送秘钥超时。", e);
			}

			try {
				message = connection.blockingRead(PROXY_INIT_TIMEOUT);
			} catch (TimeoutException e) {
				throw new RuntimeException("没有收到数据。", e);
			}

			cryptor = new DESCryptor(desKey);
		}

		if (message.getType() != MessageType.INIT_RESULT) {
			throw new RuntimeException("连接Socks代理失败。");
		}

		final ProxyConnection proxyConnection = new ProxyConnection(connectionName, PROXY_HEARTBEAT_TIMEOUT_TIMES,
				PROXY_HEARTBEAT_TIME_PERIOD, cryptor);

		proxyConnection.setDriver(connection);

		proxyConnection.setMessageHandler(new MessageHandler<ProxyMessage, ProxyMessage>() {

			@Override
			public void onMessage(final ProxyMessage message) {
				switch (message.getType()) {
					case RESOLVE_NAME:
						new Thread(new Runnable() {

							@Override
							public void run() {
								resolveName(message, proxyConnection);
							}

						}).start();
						break;
					case CLOSE:
						proxyConnection.closeOriginalConnection(message.getId());
						break;
					case CONNECT:
						new Thread(new Runnable() {

							@Override
							public void run() {
								createOriginalConnection(message, proxyConnection);
							}

						}).start();
						break;
					case DATA:
						proxyConnection.transferData(message.getId(), message.getData());
						break;
					default:
						logger.warn("不正确的消息类型：{}，忽略。", message.getType());
						break;
				}
			}

			@Override
			public void handleError(Exception e, ProxyMessage message) {
				logger.error("和反向服务器之间的连接发生异常。", e);
				logger.info("准备重新连接。");
				reconnectProcessor.startup();
			}

		});

		socketProcessor.addConnection(connection);

		connection.startNonBlockingMode();

		this.proxyConnection = proxyConnection;

		channelMonitor.addChannel(proxyConnection);
	}

	private void resolveName(ProxyMessage message, ProxyConnection proxyConnection) {
		byte[] data;
		if (proxyConnection.getCryptor() != null) {
			data = proxyConnection.getCryptor().decrypt(message.getData());
		} else {
			data = message.getData();
		}
		try {
			String hostname = new String(data, "UTF-8");
			String ip = InetAddress.getByName(hostname).getHostAddress();
			ProxyMessage respMessage = MessageUtil.createResolveNameMessage(message.getId(), ip);
			proxyConnection.writeMessage(respMessage);
		} catch (Exception e) {
			ProxyMessage respMessage = MessageUtil.createResolveNameMessage(message.getId(), "");
			proxyConnection.writeMessage(respMessage);
		}
	}

	private void createOriginalConnection(ProxyMessage message, final ProxyConnection proxyConnection) {

		final int originalConnectionId = message.getId();
		byte[] data;
		if (proxyConnection.getCryptor() != null) {
			data = proxyConnection.getCryptor().decrypt(message.getData());
		} else {
			data = message.getData();
		}
		int port = MessageUtil.getPort(data[0], data[1]);
		String ip = MessageUtil.getIp(data[2], data[3], data[4], data[5]);

		ProxyMessage respMessage;

		try {
			SocketChannel socketChannel = SocketChannel.open();
			logger.info("ip:" + ip + ", port:" + port);
			socketChannel.connect(new InetSocketAddress(ip, port));
			socketChannel.configureBlocking(false);

			logger.info("创建本地连接，{}:{}", ip, port);

			String connectionName = Util.getConnectionName(socketChannel);

			SocketConnection<byte[], byte[]> connection = new SocketConnection<byte[], byte[]>(connectionName,
					new ByteReader(connectionName, ORIGINAL_BUFFER_SIZE, new SocketReader(socketChannel)),
					new ByteWriter(connectionName, ORIGINAL_BUFFER_SIZE, new SocketWriter(socketChannel)),
					socketChannel);

			socketProcessor.addConnection(connection);

			connection.startNonBlockingMode();

			OriginalConnection originalConnection = new OriginalConnection(connectionName, originalConnectionId,
					new ByteReader(connectionName, ORIGINAL_BUFFER_SIZE, new SocketReader(socketChannel)),
					new ByteWriter(connectionName, ORIGINAL_BUFFER_SIZE, new SocketWriter(socketChannel)),
					socketChannel, ORIGINAL_HEARTBEAT_TIME_PERIOD);

			originalConnection.setDriver(connection);

			originalConnection.setMessageHandler(new MessageHandler<byte[], byte[]>() {

				@Override
				public void onMessage(byte[] message) {
					ProxyMessage dataMessage = MessageUtil.createDataMessage(originalConnectionId, message);
					proxyConnection.writeMessage(dataMessage);
				}

				@Override
				public void handleError(Exception e, byte[] message) {
					logger.error("本地连接发生异常，连接号：" + originalConnectionId, e);
					proxyConnection.close(originalConnectionId);
				}

			});

			channelMonitor.addChannel(originalConnection);

			respMessage = MessageUtil.createConnectResultMessage(originalConnectionId, true);

			proxyConnection.addOriginalConnection(originalConnection);

			proxyConnection.writeMessage(respMessage);

		} catch (Exception e) {
			logger.error("创建本地连接时发生错误。", e);

			respMessage = MessageUtil.createConnectResultMessage(originalConnectionId, false);

			proxyConnection.writeMessage(respMessage);
		}

	}

	@Override
	public void startup() {
		reconnectProcessor.startup();
	}

	@Override
	public void shutdown() {
		proxyConnection.close();
		reconnectProcessor.shutdown();
	}

	@Override
	public Object execute(String executeName, String[] args) {
		throw new RuntimeException("不支持本方法。");
	}

}
