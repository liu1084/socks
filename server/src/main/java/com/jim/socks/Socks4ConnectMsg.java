package com.jim.socks;


import org.apache.commons.lang3.builder.ToStringBuilder;

public class Socks4ConnectMsg {

	private int port;

	private String ip;

	private String id;

	private String hostname;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("port", port).append("ip", ip).append("id", id)
				.append("hostname", hostname).toString();
	}

}
