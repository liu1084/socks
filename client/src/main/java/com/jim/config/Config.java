/*
 * Copyright 2018 Jim Liu.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * By the way, this Soft can only by education, can not be used in commercial products. All right be reserved.
 */

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