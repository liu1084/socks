package com.jim.common.crypt;

import javax.crypto.KeyGenerator;
import java.security.Key;
import java.security.SecureRandom;

public class CryptUtil {

	public static Key generateDESKey() {
		SecureRandom secureRandom = new SecureRandom();

		KeyGenerator kg;
		try {
			kg = KeyGenerator.getInstance("DES");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		kg.init(secureRandom);

		return kg.generateKey();
	}

}
