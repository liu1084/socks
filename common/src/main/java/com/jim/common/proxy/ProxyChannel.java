package com.jim.common.proxy;


import com.jim.common.io.HeartbeatMessageChannel;

public interface ProxyChannel extends HeartbeatMessageChannel<ProxyMessage, ProxyMessage> {

	public void close(int originalConnectionId);

}
