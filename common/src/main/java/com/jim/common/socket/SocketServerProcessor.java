package com.jim.common.socket;

import java.nio.channels.SelectionKey;

public class SocketServerProcessor extends SocketProcessor {

	private final SocketServer[] servers;

	public SocketServerProcessor(String name, SocketServer[] servers) {
		super(name);
		this.servers = servers;
	}

	public SocketServerProcessor(SocketServer[] servers) {
		this("网络服务端处理", servers);
	}

	@Override
	protected void init() {
		super.init();

		for (SocketServer server : servers) {
			server.register(this);
		}
	}

	@Override
	protected void destory() {
		for (SocketServer server : servers) {
			server.close();
		}

		super.destory();
	}

	@Override
	protected void processKey(SelectionKey key) {
		if (key.isAcceptable()) {
			SocketServer server = (SocketServer) key.attachment();
			server.accept();
		} else {
			super.processKey(key);
		}
	}

}
