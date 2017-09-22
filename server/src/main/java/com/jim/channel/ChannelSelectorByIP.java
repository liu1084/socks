package com.jim.channel;

import com.jim.proxy.server.SelectedChannel;

public class ChannelSelectorByIP extends ChannelSelector {

	@Override
	public SelectedChannel selectChannel(String hostname, String ip) {
		SelectedChannel resolvedChannelId = new SelectedChannel();
		if (ip == null) {
			resolvedChannelId.setChannelId(ip);
		} else {
			if (channelMap.get(ip) != null) {
				resolvedChannelId.setChannelId(ip);
			}
		}
		resolvedChannelId.setIp(ip);
		return resolvedChannelId;
	}

}
