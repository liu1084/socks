package com.jim.client;

import com.jim.common.io.ChannelHeartbeatMonitor;
import com.jim.common.socket.SocketProcessor;
import com.jim.config.Config;
import com.jim.remote.ProxyRemoteClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.File;
import java.io.IOException;

public class RemoteClient {
	private static final Logger logger = LoggerFactory.getLogger(RemoteClient.class);

	public static void main(String[] args) throws IOException {
		Config config = new Config();
		config.setConfig("client.config.properties");

		logger.debug("Config file is:" + config.getConfig());

		File file = new File(config.getConfig());
		logger.debug(file.getAbsolutePath());
		logger.debug(Boolean.toString(file.exists()));

		logger.debug("channel.heartbeat.check.time.period ---> " + config.getParameter("channel.heartbeat.check.time.period"));
		logger.debug("Parse period from String to int ---> " + Integer.parseInt(config.getParameter("channel.heartbeat.check.time.period")));

		final ChannelHeartbeatMonitor channelMonitor = new ChannelHeartbeatMonitor(Integer.parseInt(config.getParameter("channel.heartbeat.check.time.period")));
		channelMonitor.startup();

		final SocketProcessor socketProcessor = new SocketProcessor();
		socketProcessor.startup();

		final ProxyRemoteClient client = new ProxyRemoteClient(config.getParameter("reverse.server.ip"),
				Integer.parseInt(config.getParameter("reverse.server.port")), config.getParameter("remote.client.id"),
				socketProcessor, channelMonitor, Integer.parseInt(config.getParameter("reconnect.retry.times")),
				Integer.parseInt(config.getParameter("reconnect.retry.time.period")));

		client.startup();

		SignalHandler signalHandler = new SignalHandler() {

			public void handle(Signal signal) {
				client.shutdown();
				socketProcessor.shutdown();
				channelMonitor.shutdown();
			}

		};

		Signal.handle(new Signal("INT"), signalHandler);

		Signal.handle(new Signal("TERM"), signalHandler);
	}

}
