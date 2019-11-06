package com.chemyoo.spider.core;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.chemyoo.spider.util.PropertiesUtil;

/***
 * java抓取网络图片
 */
public class ImagesUtils {
	
	protected static Random random = new Random();
	
	private ImagesUtils() {}
	
	private static final Logger LOG = Logger.getLogger(ImagesUtils.class);
	
	public static void downloadPic(String dir, String referer) {
		download(dir, referer);
	}


	/***
	 * 下载图片
	 * 
	 * @param dir
	 */
	private static void download(String dir,String referer) {
		
		if(!(dir.endsWith("/") || dir.endsWith("\\"))) {
			dir += System.getProperty("file.separator");
		}
		File path = new File(dir);
		if(!path.exists()) {
			path.mkdirs();
		}
		
		InputStream in = null;
		HttpURLConnection httpConnection = null;
		String imageName = null;
		long milliseconds = 0;
//		enable.md5
		Boolean enableMd5 = Boolean.valueOf(PropertiesUtil.getInstance().getProperty("enable.md5", "false"));
		while(!LinkQueue.imageUrlEmpty()) {
			try {
				String url = LinkQueue.imageUrlPop();
				if(enableMd5) {
					imageName = DigestUtils.md5Hex(url) 
							+ url.substring(url.lastIndexOf('/') + 1, url.length());
				} else {
					imageName = url.substring(url.lastIndexOf('/') + 1, url.length());
				}
				
				if(imageName.contains("?")) {
					imageName = imageName.substring(0,imageName.lastIndexOf('?'));
				}
				
				//非图片，不进行下载
				if(!"gif,png,jpg,jpeg,bmp".contains(getFileExt(imageName))) {
					continue;
				}
				
				URL uri = new URL(url);
				HttpsURLConnection.setDefaultSSLSocketFactory(SelfSSLSocket.getSSLSocketFactory());
				HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
					@Override
					public boolean verify(String hostname, SSLSession session) {
						return true;
					}
				});
				httpConnection = (HttpURLConnection) uri.openConnection();
				/**
				 * 如果有cookie限制则可添加cookies值：
				 * httpConnection.setRequestProperty("cookie", "");
				 */
				if(StringUtils.isNotBlank(referer))
					httpConnection.setRequestProperty("referer", referer);
				
				// 默认GET方法，httpConnection.setRequestMethod("get".toUpperCase());
				// 设置连接主机超时（单位：毫秒）  
				httpConnection.setConnectTimeout(60 * 1000);
				// 设置从主机读取数据超时（单位：毫秒） 
				httpConnection.setReadTimeout(90 * 1000);
				
				String userAgent = new String []{"Mozilla/4.0","Mozilla/5.0","Opera/9.80"}[random.nextInt(3)];
				
				httpConnection.setRequestProperty("User-agent", userAgent);
				
				// 连接网站
				httpConnection.connect();
				// LOG.info("下载文件：【" + url + "】");
				
				// 网址连接失败就继续向下一个网址执行。
				if(httpConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
					in = httpConnection.getErrorStream();
					if(in == null)
						in = httpConnection.getInputStream();
					
					LOG.error("网址：" + url + "访问失败：" 
							+ IOUtils.toString(in, "gb2312"));
				} else {
					in = httpConnection.getInputStream();
					double fileSize = httpConnection.getContentLengthLong() / 1024D; // kb
					DeleteImages.checkImageSize(new File(dir + imageName), in, fileSize);
				}
				milliseconds = 100L * (random.nextInt(11) + 5);
			} catch (Exception e) {
				LOG.error("下载图片发生异常：" + e.getMessage());
				milliseconds = 0;
			} finally {
				Spider.closeQuietly(in);
				in = null;
				if(httpConnection != null)
					httpConnection.disconnect();
				try {
					// 设置休眠，防止IP被禁用。
					TimeUnit.MILLISECONDS.sleep(milliseconds);
				} catch (InterruptedException e) {
					LOG.error("下载图片发生异常",e);
					Thread.currentThread().interrupt();
				}
			}
		}
	}
	
	private static String getFileExt(String fileName) {
		return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
	}

}