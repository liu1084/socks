package com.jim.server;

import com.jim.channel.ChannelSelector;
import com.jim.common.io.ChannelHeartbeatMonitor;
import com.jim.common.socket.SocketServer;
import com.jim.common.socket.SocketServerProcessor;
import com.jim.proxy.server.ProxyChannelManager;
import com.jim.socks.Socks4Proxy;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.InputStream;
import java.util.Properties;

@SuppressWarnings("restriction")
public class ProxyServer {

	public static void main(String[] args) throws Exception {

		InputStream in = ProxyServer.class.getClassLoader().getResourceAsStream("server.config.properties");

		Properties p = new Properties();
		p.load(in);

		final ChannelHeartbeatMonitor channelMonitor = new ChannelHeartbeatMonitor(
				Integer.parseInt(p.getProperty("channel.heartbeat.check.time.period")));
		channelMonitor.startup();

		String channelSelectorClassName = p.getProperty("channel.selector.class");
		ChannelSelector channelSelector = (ChannelSelector) Class.forName(channelSelectorClassName).newInstance();

		ProxyChannelManager channelManager = new ProxyChannelManager();
		channelManager.setChannelSelector(channelSelector);

		Socks4Proxy proxy = new Socks4Proxy(Integer.parseInt(p.getProperty("proxy.server.port")), channelManager,
				channelMonitor);
		ReverseServer reverseServer = new ReverseServer(Integer.parseInt(p.getProperty("reverse.server.port")),
				Boolean.parseBoolean(p.getProperty("reverse.server.need.crypt")), channelManager, channelMonitor);

		SocketServer[] servers = new SocketServer[2];
		servers[0] = proxy;
		servers[1] = reverseServer;

		final SocketServerProcessor serverProcessor = new SocketServerProcessor(servers);

		serverProcessor.startup();

		SignalHandler signalHandler = new SignalHandler() {

			public void handle(Signal signal) {
				serverProcessor.shutdown();
				channelMonitor.shutdown();
			}

		};

		Signal.handle(new Signal("INT"), signalHandler);

		Signal.handle(new Signal("TERM"), signalHandler);
	}

}
