package com.chemyoo.spider.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import org.apache.log4j.Logger;

public class PropertiesUtil {
	
	private static final Logger log = Logger.getLogger(PropertiesUtil.class);
	
	private PropertiesUtil() {}
	
	private static Properties properties = new Properties();
	
	public static Properties getInstance() {
		if(properties.isEmpty()) {
			init();
		}
		return properties;
	}
	
	public static synchronized void init() {
		//初始化读取配置文件中的分表信息
		try(
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(getPropertiesInputStream(), "UTF-8"));){
			properties.load(bufferedReader);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public static InputStream getPropertiesInputStream() {
		// 获取jar内文件
		return PropertiesUtil.class.getClassLoader().getResourceAsStream("config.properties");
	}
	
	public static String getLineSeparator() {
		return System.getProperty("line.separator");
	}

	public static String getFileSeparator() {
		return System.getProperty("file.separator");
	}
	
}