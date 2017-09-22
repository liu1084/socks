package com.jim.proxy.remote;


import com.jim.common.proxy.OriginalConnection;
import com.jim.common.proxy.ProxyChannel;

public interface ProxyRemoteChannel extends ProxyChannel {

	public String getId();

	public void connect(OriginalConnection originalConnection, String ip, int port, int timeout);

	String resolveName(int syncNumber, String hostname, int timeout);

}
