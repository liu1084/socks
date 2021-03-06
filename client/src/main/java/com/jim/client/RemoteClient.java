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
