package com.jim.proxy.server;

import com.jim.channel.ChannelSelector;
import com.jim.common.io.MessageHandler;
import com.jim.common.socket.SocketConnection;
import com.jim.common.socket.SocketProcessor;
import com.jim.common.socket.SocketReader;
import com.jim.common.socket.SocketWriter;
import com.jim.common.proxy.OriginalConnection;
import com.jim.common.proxy.ProxyChannel;
import com.jim.common.proxy.ProxyMessage;
import com.jim.common.util.Util;
import com.jim.proxy.local.LocalConnectionReader;
import com.jim.proxy.local.LocalConnectionWriter;
import com.jim.proxy.remote.ProxyRemoteChannel;
import com.jim.utils.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ProxyChannelManager {

	private static final Logger logger = LoggerFactory.getLogger(ProxyChannelManager.class);

	private static final int BUFFER_SIZE = Integer.parseInt(PropertiesUtil.getProperty("local.proxy.connection.buffer.size"));

	private static final int CONNECT_TIMEOUT = Integer.parseInt(PropertiesUtil.getProperty("remote.connect.timeout"));

	private static final int HEARTBEAT_TIME_PERIOD = Integer
			.parseInt(PropertiesUtil.getProperty("local.proxy.connection.heartbeat.time.period"));

	private final Map<String, ProxyRemoteChannel> map;

	private final AtomicInteger number;

	private ChannelSelector channelSelector;

	public ProxyChannelManager() {
		map = new HashMap<String, ProxyRemoteChannel>();
		number = new AtomicInteger(0);
	}

	public void setChannelSelector(ChannelSelector channelSelector) {
		this.channelSelector = channelSelector;
		channelSelector.setChannelMap(map);
	}

	public int getNumber() {
		return number.incrementAndGet();
	}

	public ProxyChannel getChannel(String channelId, String ip, int port, SocketProcessor socketProcessor,
                                   OriginalConnection originalConnection) {

		if (channelId != null) {
			ProxyRemoteChannel channel = getChannel(channelId);

			if (channel != null) {
				channel.connect(originalConnection, ip, port, CONNECT_TIMEOUT);
				return channel;
			} else {
				throw new RuntimeException("不能得到对应的Channel，channelId = " + channelId);
			}

		}

		return createLocalConnection(ip, port, socketProcessor, originalConnection);
	}

	private ProxyLocalConnection createLocalConnection(final String ip, final int port, SocketProcessor socketProcessor,
                                                       final OriginalConnection originalConnection) {
		SocketChannel socketChannel;
		try {
			socketChannel = SocketChannel.open();
			socketChannel.connect(new InetSocketAddress(ip, port));
			socketChannel.configureBlocking(false);
		} catch (IOException e) {
			throw new RuntimeException(ip + ":" + port + "网络初始化失败。", e);
		}

		logger.info("创建本地连接，{}:{}", ip, port);

		String connectionName = Util.getConnectionName(socketChannel);

		SocketConnection<ProxyMessage, ProxyMessage> socketConnection = new SocketConnection<ProxyMessage, ProxyMessage>(
				connectionName,
				new LocalConnectionReader(connectionName, originalConnection.getId(), BUFFER_SIZE,
						new SocketReader(socketChannel)),
				new LocalConnectionWriter(connectionName, BUFFER_SIZE, new SocketWriter(socketChannel)), socketChannel);

		socketProcessor.addConnection(socketConnection);
		socketConnection.startNonBlockingMode();

		ProxyLocalConnection proxyLocalConnection = new ProxyLocalConnection(connectionName, HEARTBEAT_TIME_PERIOD);

		proxyLocalConnection.setDriver(socketConnection);

		proxyLocalConnection.setMessageHandler(new MessageHandler<ProxyMessage, ProxyMessage>() {

			@Override
			public void onMessage(ProxyMessage message) {
				originalConnection.writeMessage(message.getData());
			}

			@Override
			public void handleError(Exception e, ProxyMessage message) {
				logger.error("本地连接发生异常，" + ip + ":" + port, e);
				originalConnection.close();
			}

		});
		return proxyLocalConnection;
	}

	private ProxyRemoteChannel getChannel(String channelId) {
		return map.get(channelId);
	}

	public SelectedChannel resolveChannelId(String hostname, String ip) {

		SelectedChannel selectedChannel = channelSelector.selectChannel(hostname, ip);

		if (selectedChannel.getChannelId() == null) {
			if (hostname != null) {
				try {
					selectedChannel.setIp(InetAddress.getByName(hostname).getHostAddress());
				} catch (UnknownHostException e) {
					throw new RuntimeException(e);
				}
			} else {
				selectedChannel.setIp(ip);
			}
		} else {
			if (selectedChannel.getIp() == null) {
				ProxyRemoteChannel channel = getChannel(selectedChannel.getChannelId());
				if (channel == null) {
					throw new RuntimeException("不能得到对应的Channel，channelId = " + selectedChannel.getChannelId());
				}
				selectedChannel.setIp(channel.resolveName(getNumber(), hostname, CONNECT_TIMEOUT));
			}
		}
		return selectedChannel;
	}

	public synchronized void addChannel(ProxyRemoteChannel proxyRemoteChannel) {
		String channelId = proxyRemoteChannel.getId();
		ProxyRemoteChannel prev = map.get(channelId);
		if (prev != null) {
			throw new RuntimeException("对应的通道ID：" + channelId + "已存在存在。");
		} else {
			map.put(channelId, proxyRemoteChannel);
			logger.info("添加远程连接，通道ID：{}，通道数量：{}", channelId, map.size());
		}
	}

	public void removeChannel(String channelId) {
		map.remove(channelId);
		logger.info("删除远程连接，通道ID：{}，通道数量：{}", channelId, map.size());
	}

	// public void handleError(String channelId) {
	// }

	public class ResolvedChannelId {

		private String ip;

		private String channelId;

		public String getIp() {
			return ip;
		}

		public void setIp(String ip) {
			this.ip = ip;
		}

		public String getChannelId() {
			return channelId;
		}

		public void setChannelId(String channelId) {
			this.channelId = channelId;
		}

	}

}
