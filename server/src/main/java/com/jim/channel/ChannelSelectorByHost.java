package com.jim.channel;

import com.jim.proxy.server.SelectedChannel;

public class ChannelSelectorByHost extends ChannelSelector {

	@Override
	public SelectedChannel selectChannel(String hostname, String ip) {
		SelectedChannel resolvedChannelId = new SelectedChannel();
		if (hostname == null) {
			resolvedChannelId.setChannelId(null);
		} else {
			String channelId;
			String[] strings = hostname.split("\\.");
			if (strings.length == 1) {
				channelId = hostname;
			} else {
				channelId = strings[strings.length - 2] + "." + strings[strings.length - 1];
			}
			if (channelMap.get(channelId) != null) {
				resolvedChannelId.setChannelId(channelId);
			}
		}
		resolvedChannelId.setIp(ip);
		return resolvedChannelId;
	}

}
