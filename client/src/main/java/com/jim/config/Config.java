package com.jim.config;

import com.jim.common.utils.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Properties;

public class Config {
	static final Logger logger = LoggerFactory.getLogger(Config.class);
	public static Properties prop = null;

	public String getConfig() {
		return config;
	}

	public  void setConfig(String config) {
		this.config = config;
	}

	public String config;

	protected void init() {
		try {
			prop = PropertiesUtil.getProperties(getRootClassPath() + File.separatorChar + this.config);
		} catch (FileNotFoundException e) {
			logger.error("File: " + config + " is not exist!, detail:");
			logger.error(e.getMessage(), e);
		}
	}

	public String getParameter(String name) {
		this.init();
		if (prop == null) {
			return null;
		} else {
			return prop.getProperty(name);
		}
	}

	protected static Object getInstance(String className) {
		Object obj = null;
		try {
			obj = Class.forName(className).newInstance();
		} catch (Exception e) {
			logger.error("[" + className + "]构造失败！");
			logger.error(e.getMessage(), e);
		}
		return obj;
	}

	/**
	 * 获取存储用户数据的根文件夹
	 * 如果不配置，则会默认使用ClassPath
	 *
	 * @return
	 */
	public String getRelativeRootPath() {
		String path = getParameter("relativeRootPath");
		if (path != null && !"".equals(path.trim())) {
			path = path + File.separator;
		}
		return path;
	}

	private static String getRootClassPath() {
		String path = "";
		try {
//			File file = new File(Config.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
//			path = URLDecoder.decode(file.toString(), "UTF-8");
			//path = path + File.separator  + ".." + File.separator;
			path = Thread.currentThread().getContextClassLoader().getResource("").toURI().getPath();
			logger.debug("current path:" + path);
		} catch (Exception e) {
			return ".";
		}
		return path;
	}
}