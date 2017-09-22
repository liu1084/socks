package com.jim.remote;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {

	private static final Properties p;

	static {
		InputStream in = PropertiesUtil.class.getResourceAsStream("remote.client.properties");
		p = new Properties();
		try {
			p.load(in);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getProperty(String key) {
		return p.getProperty(key);
	}

}
