package com.chemyoo.spider.util;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * @author 作者 : jianqing.liu
 * @version 创建时间：2018年5月10日 上午9:34:17
 * @since 2018年5月10日 上午9:34:17
 * @description 后台打开网页工具，从一个URL获取返回结果的工具
 */
public class HttpClientUtils {

	private static Logger logger = Logger.getLogger(HttpClientUtils.class);
	
	protected static Random random = new Random();

	private HttpClientUtils() {
	}
	
	public static String post(String text,String link) {
		String result = "500";
        try {
            URL url = new URL(link);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Connection", "Keep-Alive");//保持长链接
            conn.setRequestProperty("Charset", "UTF-8");
            // 设置文件类型:
            conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            // 设置接收类型否则返回415错误
            // conn.setRequestProperty("accept","*/*")此处为设置接受所有类型，以此来防范返回415;
            conn.setRequestProperty("accept","application/json");
            
			// 设置连接主机超时（单位：毫秒）  
            conn.setConnectTimeout(60 * 1000);
			// 设置从主机读取数据超时（单位：毫秒） 
            conn.setReadTimeout(90 * 1000);
			
			String userAgent = new String []{"Mozilla/4.0","Mozilla/5.0","Opera/9.80"}[random.nextInt(3)];
			
			conn.setRequestProperty("User-agent", userAgent);
            // 往服务器里面发送数据
            if (text != null && !text.isEmpty()) {
                byte[] writebytes = text.getBytes();
                // 设置文件长度
                conn.setRequestProperty("Content-Length", String.valueOf(writebytes.length));
                OutputStream outwritestream = conn.getOutputStream();
                outwritestream.write(writebytes);
                outwritestream.flush();
                outwritestream.close();
            }
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                result = IOUtils.toString(conn.getInputStream(), "UTF-8");
            }
            conn.disconnect();
        } catch (Exception e) {
        	logger.error(e.getMessage(), e);
        }
		return result;
	}
	
}
