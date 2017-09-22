package com.jim.common.socket;


import com.jim.common.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SocketProcessor extends Processor {

	private static final Logger logger = LoggerFactory.getLogger(com.jim.common.socket.SocketProcessor.class);
	private final Queue<SocketConnection<?, ?>> queue;
	private Selector selector;

	public SocketProcessor(String name) {
		super(name);
		queue = new ConcurrentLinkedQueue<SocketConnection<?, ?>>();
	}

	public SocketProcessor() {
		this("网络客户端处理");
	}

	@Override
	protected void init() {
		try {
			selector = Selector.open();
		} catch (IOException e) {
			throw new RuntimeException(getName() + "网络初始化失败。", e);
		}
	}

	@Override
	protected void wakeup() {
		selector.wakeup();
	}

	@Override
	protected void destory() {
		for (SelectionKey key : selector.keys()) {
			Object object = key.attachment();
			if (object instanceof com.jim.common.socket.SocketConnection) {
				com.jim.common.socket.SocketConnection<?, ?> connection = (com.jim.common.socket.SocketConnection<?, ?>) object;
				connection.close();
			}
		}
		try {
			selector.close();
		} catch (IOException e) {
			logger.warn(getName() + "停止时网络关闭异常，忽略该异常。", e);
		}
	}

	@Override
	protected boolean process() throws IOException {
		int ready = 0;

		ready = selector.select();

		while (!queue.isEmpty()) {
			queue.poll().register(this);
		}

		if (ready == 0) {
			return true;
		}

		Set<SelectionKey> keys = selector.selectedKeys();
		Iterator<SelectionKey> iter = keys.iterator();
		while (iter.hasNext() && isRunning()) {
			SelectionKey key = iter.next();
			iter.remove();
			processKey(key);
		}

		return true;
	}

	protected void processKey(SelectionKey key) {
		if (key.isReadable()) {
			com.jim.common.socket.SocketConnection<?, ?> connection = (com.jim.common.socket.SocketConnection<?, ?>) key.attachment();
			connection.read();
		} else if (key.isWritable()) {
			com.jim.common.socket.SocketConnection<?, ?> connection = (com.jim.common.socket.SocketConnection<?, ?>) key.attachment();
			connection.write();
		}
	}

	public void addConnection(SocketConnection<?, ?> connection) {
		if (isRunning()) {
			queue.offer(connection);
			selector.wakeup();
			connection.awaitRegister();
		} else {
			throw new RuntimeException(getName() + "已经停止，不能增加连接。");
		}
	}

	public Selector getSelector() {
		return selector;
	}

}