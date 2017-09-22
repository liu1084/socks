package com.jim.common.proxy;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class ProxyMessage {

	private com.jim.common.enums.MessageType type;

	private int id;

	private byte[] data;

	public com.jim.common.enums.MessageType getType() {
		return type;
	}

	public void setType(com.jim.common.enums.MessageType type) {
		this.type = type;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("type", type).append("id", id).append("data", data).toString();
	}

}
