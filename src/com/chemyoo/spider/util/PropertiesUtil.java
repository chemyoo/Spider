package com.chemyoo.spider.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
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
		try(FileInputStream fileInput = new FileInputStream(getPropertiesFolder());
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInput, "UTF-8"));){
			properties.load(bufferedReader);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public static String getPropertiesFolder() {
		return PropertiesUtil.class.getClassLoader().getResource("config.properties").getPath();
	}
	
	public static String getLineSeparator() {
		return System.getProperty("line.separator");
	}

	public static String getFileSeparator() {
		return System.getProperty("file.separator");
	}
	
}