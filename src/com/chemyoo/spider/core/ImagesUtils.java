package com.chemyoo.spider.core;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/***
 * java抓取网络图片
 */
public class ImagesUtils {
	
	private ImagesUtils() {}
	
	private static final Logger LOG = Logger.getLogger(ImagesUtils.class);
	
	// 编码
	private static final String ECODING = "UTF-8";
	// 获取img标签正则
	private static final String IMGURL_REG = "<img.*src=(.*?)[^>]*?>";
	// 获取src路径的正则
	private static final String IMGSRC_REG = "http:\"?(.*?)(\"|>|\\s+)";

	public static void downloadPic(String dir, String referer) {
		// 获得html文本内容
		download(dir, referer);
	}

	/***
	 * 获取HTML内容
	 * 
	 * @param url
	 * @return
	 * @throws IOException 
	 * @throws Exception
	 */
	public static String getHTML(String url) throws IOException {
		URL uri = new URL(url);
		URLConnection connection = uri.openConnection();
		InputStream in = connection.getInputStream();
		byte[] buf = new byte[1024];
		StringBuilder sb = new StringBuilder();
		while (in.read(buf, 0, buf.length) > 0) {
			sb.append(new String(buf, ECODING));
		}
		in.close();
		return sb.toString();
	}

	/***
	 * 获取ImageUrl地址
	 * 
	 * @param html
	 * @return
	 */
	private static List<String> getImageUrl(String html) {
		Matcher matcher = Pattern.compile(IMGURL_REG).matcher(html);
		List<String> listImgUrl = new ArrayList<>();
		String matcherHtml;
		boolean isAdd;
		while (matcher.find()) {
			isAdd = true;
			matcherHtml = matcher.group();
			Document doc = Jsoup.parse(matcherHtml); 
			Elements elements = doc.getAllElements();
			Iterator<Element> iterator = elements.iterator();
			Element element;
			String width;
			String height;
			while(iterator.hasNext()) {
				element = iterator.next();
				width = element.attr("width");
				height = element.attr("height");
				if(LinkQueue.isNotBlank(width , height)) {
					if(Integer.parseInt(width.trim().replace("px", "")) <100 
							|| Integer.parseInt(height.trim().replace("px", "")) <100) {
						isAdd = false;
						break;
					}
				}
			}
			if(isAdd)
				listImgUrl.add(matcherHtml);
		}
		return listImgUrl;
	}

	/***
	 * 获取ImageSrc地址
	 * 
	 * @param listImageUrl
	 * @return
	 */
	private static List<String> getImageSrc(List<String> listImageUrl) {
		List<String> listImgSrc = new ArrayList<>();
		for (String image : listImageUrl) {
			Matcher matcher = Pattern.compile(IMGSRC_REG).matcher(image);
			while (matcher.find()) {
				listImgSrc.add(matcher.group().substring(0,
						matcher.group().length() - 1));
			}
		}
		return listImgSrc;
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
		FileOutputStream fileOutStream = null;
		HttpURLConnection httpConnection = null;
		String url;
		String imageName = null;
		while(!LinkQueue.imageUrlEmpty()) {
			try {
				url = LinkQueue.imageUrlPop();
				imageName = url.substring(url.lastIndexOf('/') + 1,
						url.length());
				
				if(imageName.contains("?")) {
					imageName = imageName.substring(0,imageName.lastIndexOf('?'));
				}
				
				//非图片，不进行下载
				if(!"gif,png,jpg,jpeg,bmp".contains(getFileExt(imageName))) {
					continue;
				}
				
				URL uri = new URL(url);
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
				httpConnection.setReadTimeout(60 * 1000);
				
				// 连接网站
				httpConnection.connect();
				// LOG.info("下载文件：【" + url + "】");
				
				// 网址连接失败就继续向下一个网址执行。
				if(httpConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
					LOG.info("网址：" + url + "访问失败：" 
							+ IOUtils.toString(httpConnection.getErrorStream(),"gb2312"));
					httpConnection.disconnect();
					continue;
				}
				in = httpConnection.getInputStream();
				fileOutStream = new FileOutputStream(new File(dir + imageName));
				byte[] buf = new byte[1024];
				int length = 0;
				while ((length = in.read(buf, 0, buf.length)) != -1) {
					fileOutStream.write(buf, 0, length);
				}
			} catch (Exception e) {
				LOG.error("下载图片发生异常");
			} finally {
				Spider.closeQuietly(in);
				Spider.closeQuietly(fileOutStream);
//				待文件流被释放后，下载成功，进行文件分辨率辨识		
				in = null;fileOutStream = null;
				DeleteImages.checkImageSize(new File(dir + imageName), dir);
				if(httpConnection != null)
					httpConnection.disconnect();
			}
		}
	}
	
	private static String getFileExt(String fileName) {
		return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
	}

}