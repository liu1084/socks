package com.jim.common.crypt;

public interface Cryptor {

	public byte[] encrypt(byte[] data);

	public byte[] decrypt(byte[] data);

}
