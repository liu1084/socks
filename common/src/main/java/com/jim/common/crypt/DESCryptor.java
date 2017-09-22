package com.jim.common.crypt;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.Key;

public class DESCryptor implements Cryptor {

	private Key key;

	public DESCryptor(byte[] key) {
		try {
			DESKeySpec dks = new DESKeySpec(key);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			this.key = keyFactory.generateSecret(dks);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte[] encrypt(byte[] data) {
		try {
			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			return cipher.doFinal(data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte[] decrypt(byte[] data) {
		try {
			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(Cipher.DECRYPT_MODE, key);
			return cipher.doFinal(data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
