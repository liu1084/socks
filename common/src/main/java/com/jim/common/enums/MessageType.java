package com.jim.common.enums;

public enum MessageType {

	HEARTBEAT((byte) 0x00), INIT((byte) 0x01), CRYPT_KEY((byte) 0x02), INIT_RESULT((byte) 0x03), RESOLVE_NAME(
			(byte) 0x04), CONNECT((byte) 0x05), CONNECT_RESULT((byte) 0x06), CLOSE((byte) 0x07), DATA((byte) 0x08);

	private byte value;

	private MessageType(byte value) {
		this.value = value;
	}

	public static MessageType parseValue(byte value) {
		for (MessageType type : MessageType.values()) {
			if (type.getValue() == value) {
				return type;
			}
		}
		throw new RuntimeException("No Such Message Type.");
	}

	public byte getValue() {
		return value;
	}

}
