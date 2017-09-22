package com.jim.channel;

import com.jim.proxy.remote.ProxyRemoteChannel;
import com.jim.proxy.server.SelectedChannel;

import java.util.Map;

public abstract class ChannelSelector {

	protected Map<String, ProxyRemoteChannel> channelMap;

	public abstract SelectedChannel selectChannel(String hostname, String ip);

	public final void setChannelMap(Map<String, ProxyRemoteChannel> channelMap) {
		this.channelMap = channelMap;
	}
}
