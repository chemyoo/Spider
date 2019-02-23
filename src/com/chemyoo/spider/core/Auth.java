package com.chemyoo.spider.core;

import java.util.Properties;

import com.chemyoo.spider.ui.SpiderUI;
import com.chemyoo.spider.util.HttpClientUtils;
import com.chemyoo.spider.util.Message;
import com.chemyoo.spider.util.PropertiesUtil;

public class Auth {
	
	private static final String URL = "http://chemyoo.applinzi.com/auth";
	
	private static final String PARAMS = "user=%s&password=%s";
	
	private static final String USER = "user.name";
	private static final String USER_PASS = "user.password";
	
	private static final String ONLINE = "online";
	
	private static final String VALUE = "off-line";
	
	public static String login() {
		Properties props = PropertiesUtil.getInstance();
		if(VALUE.equalsIgnoreCase(props.getProperty(ONLINE))) {
			return Message.SUCCESS;
		}
		String text = String.format(PARAMS, props.getProperty(USER), props.getProperty(USER_PASS));
		try {
			String result = HttpClientUtils.post(text, URL);
			if(SpiderUI.isNotBlank(result) && result.length() > 3)
				return Message.FAILURE;
			return result;
		} catch (Exception e) {
			return Message.FAILURE;
		}
	}

}
