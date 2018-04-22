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

package com.jim.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Properties;

public class PropertiesUtil {
	static Logger logger = LoggerFactory.getLogger(com.jim.common.utils.PropertiesUtil.class);

	private PropertiesUtil() {
	}

	/**
	 * 将properties文件中的属性赋给Java对象的变量。
	 * 1、要求变量是public 的，并且变量名与properties文件中的key相同；
	 * 2、默认都是String类型，对boolean做自适应处理；
	 *
	 * @param file properties文件位置
	 * @param obj  变量所属的Java对象
	 */
	public static void properties2variables(String file, Object obj) {
		Properties prop = null;
		try {
			prop = getProperties(file);
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		}
		properties2variables(prop, obj);
	}

	/**
	 * 将properties文件中的属性赋给Java对象的变量。
	 * 1、要求变量是public 的，并且变量名与properties文件中的key相同；
	 * 2、默认都是String类型，对boolean做自适应处理；
	 *
	 * @param prop
	 * @param obj
	 */
	public static void properties2variables(Properties prop, Object obj) {
		try {
			Iterator keys = prop.keySet().iterator();
			String key;
			String value;
			while (keys.hasNext()) {
				key = (String) keys.next();
				value = prop.getProperty(key).trim();
				try {
					Field field = obj.getClass().getField(key);
					if (field.getType().toString().toLowerCase().indexOf("boolean") > -1) {
						field.setBoolean(obj, new Boolean(value).booleanValue());
					} else if (field.getType().toString().toLowerCase().indexOf("int") > -1) {
						field.setInt(obj, new Integer(value).intValue());
					} else {
						field.set(obj, value);
					}
				} catch (NoSuchFieldException e) {
					logger.info("如下属性(" + key + ")未对应变量");
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * 将指定properties文件加载到内存
	 *
	 * @param fileName
	 * @return
	 * @throws FileNotFoundException
	 * @throws
	 */
	public static Properties getProperties(String fileName) throws FileNotFoundException {
		File file = new File(fileName);
		InputStream input = null;
		if (file.exists()) {
			input = new FileInputStream(file);
		}
		return getProperties(input);
	}

	public static Properties getProperties(InputStream input) {
		Properties prop = new Properties();
		if (input != null) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(input, "utf-8"));
				prop.load(reader);
			} catch (UnsupportedEncodingException e) {
				logger.error(e.getMessage(), e);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return prop;
	}

	/**
	 * 从指定properties文件中读取key对应的值
	 *
	 * @param file
	 * @param key
	 * @return
	 */
	public static String getPropertiesValue(String file, String key) {
		String value = null;
		try {
			Properties prop = getProperties(file);
			value = prop.getProperty(key);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return value;
	}

}
