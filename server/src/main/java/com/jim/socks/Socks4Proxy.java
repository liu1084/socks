package com.jim.socks;

import com.jim.common.exception.TimeoutException;
import com.jim.common.io.ByteReader;
import com.jim.common.io.ByteWriter;
import com.jim.common.io.ChannelHeartbeatMonitor;
import com.jim.common.io.MessageHandler;
import com.jim.common.socket.*;
import com.jim.common.proxy.OriginalConnection;
import com.jim.common.proxy.ProxyChannel;
import com.jim.common.proxy.ProxyMessage;
import com.jim.common.util.MessageUtil;
import com.jim.common.util.Util;
import com.jim.utils.PropertiesUtil;
import com.jim.proxy.server.ProxyChannelManager;
import com.jim.proxy.server.SelectedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SocketChannel;


public class Socks4Proxy extends SocketServer {

	private static final Logger logger = LoggerFactory.getLogger(Socks4Proxy.class);

	private static final int BUFFER_SIZE = Integer
			.parseInt(PropertiesUtil.getProperty("local.original.connection.buffer.size"));

	private static final int INIT_TIMEOUT = Integer
			.parseInt(PropertiesUtil.getProperty("local.original.connection.init.timeout"));

	private static final int HEARTBEAT_TIME_PERIOD = Integer
			.parseInt(PropertiesUtil.getProperty("local.original.connection.heartbeat.time.period"));

	private final ProxyChannelManager channelManager;

	private final ChannelHeartbeatMonitor channelMonitor;

	public Socks4Proxy(int port, ProxyChannelManager channelManager, ChannelHeartbeatMonitor channelMonitor)
			throws IOException {
		super("Socks4代理服务器", port);
		this.channelManager = channelManager;
		this.channelMonitor = channelMonitor;
	}

	@Override
	protected SocketConnection<?, ?> createConnection(SocketChannel socketChannel) {
		logger.info("接收到应用客户端连接，准备处理，客户端：{}", socketChannel.socket().getRemoteSocketAddress());

		String connectioName = Util.getConnectionName(socketChannel);
		SocketConnection<Socks4ConnectMsg, byte[]> connection = new SocketConnection<Socks4ConnectMsg, byte[]>(
				connectioName, new Socks4ConnectMsgReader(connectioName, BUFFER_SIZE, new SocketReader(socketChannel)),
				null, socketChannel);

		return connection;
	}

	@Override
	protected void dispatch(SocketProcessor socketProcessor, SocketConnection<?, ?> connection) {

		SocketConnection<Socks4ConnectMsg, byte[]> conn = (SocketConnection<Socks4ConnectMsg, byte[]>) connection;
		Socks4ConnectMsg socksConnectMsg;
		try {
			socksConnectMsg = conn.blockingRead(INIT_TIMEOUT);
		} catch (TimeoutException e) {
			throw new RuntimeException("没有收到数据。", e);
		}

		SelectedChannel selectedChannel = channelManager.resolveChannelId(socksConnectMsg.getHostname(),
				socksConnectMsg.getIp());

		logger.info("目标host：{}，目标ip：{}，目标port：{}，使用channel：{}", socksConnectMsg.getHostname(), selectedChannel.getIp(),
				socksConnectMsg.getPort(), selectedChannel.getChannelId());

		String connectioName = Util.getConnectionName(conn.getSocketChannel());

		SocketConnection<byte[], byte[]> socketConnection = new SocketConnection<byte[], byte[]>(connectioName,
				new ByteReader(connectioName, BUFFER_SIZE, new SocketReader(conn.getSocketChannel())),
				new ByteWriter(connectioName, BUFFER_SIZE, new SocketWriter(conn.getSocketChannel())),
				conn.getSocketChannel());
		socketProcessor.addConnection(socketConnection);

		final OriginalConnection originalConnection = new OriginalConnection(connectioName, channelManager.getNumber(),
				HEARTBEAT_TIME_PERIOD);
		originalConnection.setDriver(socketConnection);

		byte[] responseMsg = new byte[8];
		responseMsg[0] = 0x00;
		responseMsg[1] = 0x5a;
		System.arraycopy(MessageUtil.createIpPortbyte(selectedChannel.getIp(), socksConnectMsg.getPort()), 0,
				responseMsg, 2, 6);
		originalConnection.writeMessage(responseMsg);

		final ProxyChannel destChannel = channelManager.getChannel(selectedChannel.getChannelId(),
				selectedChannel.getIp(), socksConnectMsg.getPort(), socketProcessor, originalConnection);

		originalConnection.setMessageHandler(new MessageHandler<byte[], byte[]>() {

			@Override
			public void onMessage(byte[] message) {
				ProxyMessage dataMessage = MessageUtil.createDataMessage(originalConnection.getId(), message);
				destChannel.writeMessage(dataMessage);
			}

			@Override
			public void handleError(Exception e, byte[] message) {
				logger.error("应用客户端连接发生异常，连接号：" + originalConnection.getId(), e);
				destChannel.close(originalConnection.getId());
			}

		});
		socketConnection.startNonBlockingMode();

		channelMonitor.addChannel(originalConnection);
	}

}
