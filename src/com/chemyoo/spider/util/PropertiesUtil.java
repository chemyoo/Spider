package com.chemyoo.spider.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import org.apache.log4j.Logger;

import com.chemyoo.spider.ui.SpiderUI;

public class PropertiesUtil {
	
	private static final Logger log = Logger.getLogger(PropertiesUtil.class);
	
	private PropertiesUtil() {}
	
	private static Properties properties = new Properties();
	
	public static Properties getInstance() {
		if(properties.isEmpty()) {
			init2();
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
	
	public static synchronized void init2() {
		//初始化读取配置文件中的分表信息
		try(FileInputStream fileInput = new FileInputStream(getPropertiesFolder());
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInput, "UTF-8"));){
			properties.load(bufferedReader);
		} catch (Exception e) {
			log.error("获取config.properties失败：", e);
			init3();
		}
	}
	
	private static void init3() {
		log.info("正在从当前文件夹中获取config.properties文件内容...");
		//初始化读取配置文件中的分表信息
		try(FileInputStream fileInput = new FileInputStream(getPropertiesPath());
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInput, "UTF-8"));){
			properties.load(bufferedReader);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public static InputStream getPropertiesInputStream() {
		// 获取jar内文件
		return PropertiesUtil.class.getClassLoader().getResourceAsStream("config.properties");
	}
	
	public static String getPropertiesFolder() {
		return PropertiesUtil.class.getClassLoader().getResource("config.properties").getPath();
	}
	
	private static String getPropertiesPath() {
		return SpiderUI.DEFAULT_PATH + "/config.properties";
	}
	
	public static String getLineSeparator() {
		return System.getProperty("line.separator");
	}

	public static String getFileSeparator() {
		return System.getProperty("file.separator");
	}
	
}