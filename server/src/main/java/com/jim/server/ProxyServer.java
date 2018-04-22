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
