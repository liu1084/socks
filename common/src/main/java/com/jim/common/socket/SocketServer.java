package com.jim.common.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;

public abstract class SocketServer {

	private static final Logger logger = LoggerFactory.getLogger(com.jim.common.socket.SocketServer.class);

	protected final String name;
	protected final ExecutorService threadPool;
	private final ServerSocketChannel serverChannel;
	private com.jim.common.socket.SocketServerProcessor socketServerProcessor;
	private SelectionKey key;

	public SocketServer(String name, int port, ExecutorService threadPool) throws IOException {
		this.name = name;
		this.threadPool = threadPool;

		logger.info("{}监听端口：{}", name, port);
		serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);
		serverChannel.socket().bind(new InetSocketAddress(port));
	}

	public SocketServer(String name, int port) throws IOException {
		this(name, port, null);
	}

	public void register(SocketServerProcessor socketServerProcessor) {
		this.socketServerProcessor = socketServerProcessor;
		try {
			key = serverChannel.register(socketServerProcessor.getSelector(), SelectionKey.OP_ACCEPT, this);
		} catch (ClosedChannelException e) {
			logger.error(name + "发生异常，将关闭。", e);
			close();
		}
	}

	public void close() {
		logger.info("{}关闭端口监听。", name);
		try {
			serverChannel.close();
		} catch (IOException e) {
			logger.warn(name + "关闭时发生异常。", e);
		}
		if (key != null) {
			key.cancel();
			key.attach(null);
		}
	}

	public void accept() {
		SocketChannel socketChannel;
		try {
			socketChannel = serverChannel.accept();
			logger.info("{}接受远程连接，远程地址：{}", name, socketChannel.socket().getRemoteSocketAddress());
			socketChannel.configureBlocking(false);
		} catch (IOException e) {
			logger.warn(name + "接受连接时发生异常。", e);
			return;
		}
		final SocketConnection<?, ?> connection = createConnection(socketChannel);
		Runnable acceptThread = new Runnable() {

			@Override
			public void run() {
				try {
					dispatch(getSocketProcessor(), connection);
				} catch (Exception e) {
					logger.warn(name + "接受连接处理时发生异常", e);
					connection.close();
				}
			}

		};
		if (threadPool != null) {
			threadPool.execute(acceptThread);
		} else {
			new Thread(acceptThread).start();
		}
	}

	protected abstract SocketConnection<?, ?> createConnection(SocketChannel socketChannel);

	protected abstract void dispatch(SocketProcessor socketProcessor, SocketConnection<?, ?> connection);

	protected SocketProcessor getSocketProcessor() {
		return socketServerProcessor;
	}

}
