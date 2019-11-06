package com.chemyoo.spider.core;

import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Properties;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JLabel;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.chemyoo.spider.ui.SpiderUI;
import com.chemyoo.spider.util.Message;
import com.chemyoo.spider.util.NumberUtils;
import com.chemyoo.spider.util.PropertiesUtil;
//import com.gargoylesoftware.htmlunit.BrowserVersion;
//import com.gargoylesoftware.htmlunit.WebClient;
//import com.gargoylesoftware.htmlunit.html.DomNode;
//import com.gargoylesoftware.htmlunit.html.DomNodeList;
//import com.gargoylesoftware.htmlunit.html.HtmlElement;
//import com.gargoylesoftware.htmlunit.html.HtmlPage;

/** 
 * @author 作者 : jianqing.liu
 * @version 创建时间：2018年5月30日 上午11:44:29 
 * @since 2018年5月30日 上午11:44:29 
 * @description 类说明 
 */
public class Spider {
	
	private static final Logger LOG = Logger.getLogger(Spider.class);
	
	private String url;
	
	private String dir;
	
	private JButton button;
	
	private Timer time;

	private JLabel message;
	
	private String referer;
	
	private Random random = new Random();
	
	private int count;
	
	Properties properties = PropertiesUtil.getInstance();
	
	private static final String PICTURE_EXT = "gif,png,jpg,jpeg,bmp"; 
	
	private String errorUrlFilePath;
	
	private FileWriter writer;

	public Spider(String url, String dir, JButton button, JLabel message,String referer) throws IOException {
		this.url = url;
		this.dir = dir;
		this.button = button;
		this.message = message;
		this.setReferer(referer);
		deletetimer();
		if(LinkQueue.unVisitedEmpty()) {
			LinkQueue.push(this.url);
		}
		this.count = 0;
		this.errorUrlFilePath = SpiderUI.DEFAULT_PATH + PropertiesUtil.getFileSeparator() 
		+ referer.split("//")[1].split("/")[0] + ".Error.task";
		this.writer = new FileWriter(errorUrlFilePath, true);
	}
	
	private void setReferer(String referer) {
		if(this.referer == null && StringUtils.isNotBlank(referer)) {
			int index = referer.replaceFirst("//", "--").indexOf('/') + 1;
			if(index == 0) {
				index = referer.length();
			}
			this.referer = referer.substring(0, index);
		} else {
			this.referer = getBaseUri();
		}
	}
	
	private String getReferer() {
		return this.referer;
	}
	
	public String start() throws IOException {
		LOG.info("程序已启动...");
		this.message.setText("");
		String res = Auth.login();
		
		if(Message.SUCCESS.equals(res)) {
			while(!LinkQueue.unVisitedEmpty() && !button.isEnabled() && !button.isSelected()) {
				String unVisited = LinkQueue.unVisitedPop();
				if(unVisited == null) {
					unVisited = LinkQueue.getPageUrl();
				}
				String link = URLDecoder.decode(unVisited, "utf-8");
				this.message.setText("正在访问网址链接:" + link);
				this.connectUrl(link);
				ImagesUtils.downloadPic(this.dir, this.getReferer());
				this.count ++;
			}
			if(button.isSelected())
				LOG.info("程序暂停...");
			else
				LOG.info("程序终止...");
		} else {
			this.message.setText("登入失败:" + res);
			return Message.FAILURE;
		}
		time.cancel();
		closeQuietly(writer);
		return Message.SUCCESS;
	}
	
	private void deletetimer() {
		/* 
		 * 注意Timer的缺陷，同时注入多个schedule会有延时问题，
	     * 只用当前的执行完，后面的任务才会执行，并且前面抛出异常，
	     * 后面的任务就不会执行
	     * 可以使用java.util.concurrent.ScheduledExecutorService来优化
	     * <li><font size = +1>每5分钟执行一次</font><li>
	     */
        time = new Timer();
        time.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					LOG.info("定时任务已启动...");
					DeleteImages.delete(dir);
					int size = LinkQueue.getUnVisitedSize();
					LOG.info("待访问的网址数量：" + size);
					LOG.info("定时任务执行完成...");
					double rate = count / 5D;
					LOG.info("网址连接速度：" + rate + "个/分钟");
					if(rate > 0)
						LOG.info("预计完成需要：" + NumberUtils.setScale(size / rate, 3)+ "分钟");
				} catch (Exception e) {
					LOG.error("定时任务执行异常", e);
				} finally {
					count = 0;
				}
		}}, 30 * 1000L, 5 * 60 * 1000L);
	}
	
	private void connectUrl(String url) throws IOException {
		LOG.info("连接网址：【" + url + "】");
		try {
			if(PICTURE_EXT.contains(getFileExt(url))) {
				LinkQueue.imageUrlpush(url);
				return;
			}
			if(this.referer != null && !url.startsWith(this.referer)) {
				return;
			} 
			String userAgent = new String []{"Mozilla/4.0","Mozilla/5.0","Opera/9.80"}[random.nextInt(3)];
			//.ignoreContentType(true)忽略请求头
			//Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko 这是IE11的userAgent
			//Mozilla 为大多数浏览器
			Document doc = Jsoup.connect(url)
					.userAgent(userAgent)
					.timeout(30 * 1000).get();
			
			Elements body = doc.getElementsByTag("body");
			this.getUrls(body);
			this.getPageUrls(body);
			this.getImagesUrls(body);
			this.getIframe(body);
		} catch (Exception e) {
			LOG.error("打开网页发生异常",e);
			writer.write(url + PropertiesUtil.getLineSeparator());
		}
	}

	/**
	 * 从IFrame中获取链接
	 * @param body
	 */
	private void getIframe(Elements body){
		Elements href = body.select("iframe");
		Iterator<Element> it = href.iterator();
		Element ele;
		String herfurl;
		String baseUrl = this.referer;
		while(it.hasNext()) {
			ele = it.next();
			herfurl = ele.absUrl("src");

			if(herfurl.equals(baseUrl) || (herfurl.endsWith("/") && herfurl.equals(baseUrl+"/"))) {
				continue;
			}
			if(PICTURE_EXT.contains(getFileExt(herfurl))) {
				LinkQueue.imageUrlpush(herfurl);
			} else {
				LinkQueue.push(herfurl);
			}
		}
	}
	
	/**
	 * 获取页码区域链接
	 * @param body
	 */
	private void getPageUrls(Elements body) {
		String pageSelector =  properties.getProperty("dom.page");
		String notContain = properties.getProperty("not.contain");
		// 筛选出主区域
		Elements mainPage = new Elements();
		if(StringUtils.isNotBlank(pageSelector)) {
			String[] cssSelector = pageSelector.split(",");
			for(String css : cssSelector) {
				if(css.startsWith("a.") || css.startsWith("a#") || css.startsWith("a[") || css.endsWith("a[href]")) {
					mainPage.addAll(body.select(css.trim()));
				} else {
					mainPage.addAll(body.select(css.trim() + " a[href]"));
				}
			}
		}
		Iterator<Element> it = mainPage.iterator();
		label:
		while(it.hasNext()) {
			Element ele = it.next();
			String href = ele.absUrl("href");
			String text = ele.text();
			if(StringUtils.isNotBlank(href) && !href.startsWith("javascript:")) {
				if(StringUtils.isNotBlank(notContain)) {
					String[] words = notContain.split("[|]");
					for(String word : words) {
						boolean isNotAdd = true;
						String[] andWord = word.split("[+]");
						for(String and : andWord) {
							isNotAdd = isNotAdd && text.contains(and.trim());
						}
						if(isNotAdd)
							continue label;
					}
				} 
				if(href.startsWith(this.referer)) {
					// 只收集本网站地址
					LinkQueue.addPageUrl(href);
				}
			}
		}
	}
	
	private void getUrls(Elements body) {
		// 主区域选择器
		String classSelector = getProperties("dom.main", "a[href]");
		// 收集链接选择器
		String hrefSelect = properties.getProperty("dom.href.select");
		// 收集链接非选择器
		String removeItem = properties.getProperty("dom.href.not");
		// 关键字选择器
		String keyWord = properties.getProperty("key.word");
		String notContain = properties.getProperty("not.contain");
		
		// 筛选出主区域
		Elements main = new Elements();
		if(StringUtils.isNotBlank(classSelector)) {
			String[] cssSelector = classSelector.split(",");
			for(String css : cssSelector) {
				main.addAll(body.select(css.trim()));
			}
		}
		
		// 收集链接
		Elements href = new Elements();
		if(StringUtils.isNotBlank(hrefSelect)) {
			String[] cssSelector = hrefSelect.split(",");
			for(String css : cssSelector) {
				if(css.startsWith("a.") || css.startsWith("a#") || css.startsWith("a[")) {
					href.addAll(main.select(css.trim()));
				} else {
					href.addAll(main.select(css.trim() + " a[href]"));
				}
			}
		}
		
		// 需要过滤掉的链接
		Elements removeHref = new Elements();
		if(StringUtils.isNotBlank(removeItem)) {
			String[] cssSelector = removeItem.split(",");
			for(String css : cssSelector) {
				if(css.startsWith("a.") || css.startsWith("a#") || css.startsWith("a[")) {
					removeHref.addAll(main.select(css.trim()));
				} else {
					removeHref.addAll(main.select(css.trim() + " a[href]"));
				}
			}
		}
		href.removeAll(removeHref);
		Iterator<Element> it = href.iterator();
		String baseUrl = this.referer;
		label:
		while(it.hasNext()) {
			Element ele = it.next();
			String herfurl = ele.absUrl(getProperties("real.href.attr", "href"));
			String text = ele.text();

			// 如果是源网址，则忽略
			if("".equals(herfurl) || herfurl.equals(baseUrl) || (herfurl.endsWith("/") && herfurl.equals(baseUrl+"/"))) {
				continue;
			}
			
			
			if(StringUtils.isNotBlank(notContain)) {
				String[] words = notContain.split("[|]");
				for(String word : words) {
					boolean isNotAdd = true;
					String[] andWord = word.split("[+]");
					for(String and : andWord) {
						isNotAdd = isNotAdd && text.contains(and.trim());
					}
					if(isNotAdd)
						continue label;
				}
			}
			
			
			if(PICTURE_EXT.contains(getFileExt(herfurl))) {
				LinkQueue.imageUrlpush(herfurl);
			} else if(herfurl.startsWith(baseUrl)) {
				LinkQueue.push(herfurl);
			} else if(StringUtils.isNotBlank(keyWord)) {
				String[] words = keyWord.split("[|]");// | 表示或关系
				for(String word : words) {
					boolean addUrl = true;
					String[] andWord = word.split("[+]"); // + 表示与关系
					for(String and : andWord) {
						if(!text.contains(and.trim())) {
							addUrl = false;
						}
					}
					if(addUrl) {
						LinkQueue.push(herfurl);
					}
				}
			}
		}
	}

	private static String getFileExt(String fileName) {
		return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
	}

	private void getImagesUrls(Elements body) {
		String main = getProperties("dom.main","img[src]");
		String classSelector2 = properties.getProperty("dom.img.select");
//		String removeItem = properties.getProperty("dom.not");
		String notDownImg = properties.getProperty("dom.img.not");
		Elements mainDiv = new Elements();
		if(StringUtils.isNotBlank(main)) {
			String[] cssSelector = main.split(",");
			for(String css : cssSelector) {
				mainDiv.addAll(body.select(css.trim()));
			}
		}
		Elements href = new Elements();
		if(StringUtils.isNotBlank(classSelector2)) {
			String[] cssSelector = classSelector2.split(",");
			for(String css : cssSelector) {
				href.addAll(mainDiv.select(css.trim() + " img[src]"));
			}
		}
		Elements removeHref = new Elements();
//		if(StringUtils.isNotBlank(removeItem)) {
//			String[] cssSelector = removeItem.split(",");
//			for(String css : cssSelector) {
//				removeHref.addAll(mainDiv.select(css.trim() + " img[src]"));
//			}
//		}
		if(StringUtils.isNotBlank(notDownImg)) {
			String[] cssSelector = notDownImg.split(",");
			for(String css : cssSelector) {
				removeHref.addAll(mainDiv.select(css.trim() + " img[src]"));
			}
		}
		href.removeAll(removeHref);
		Iterator<Element> it = href.iterator();
		String filterUrl = properties.getProperty("filter.url");
		String src;
		lable:
		while(it.hasNext()) {
			src = it.next().absUrl(getProperties("real.img.src", "src"));
			if(StringUtils.isNotBlank(filterUrl)) {
				String[] words = filterUrl.split(",");
				for(String word : words) {
					if(src.contains(word.trim())) {
						continue lable;
					}
				}
			}
			LinkQueue.imageUrlpush(src);
		}
	}
	
	/*private void printlnBody(Document doc) {
		Elements body = doc.getElementsByTag("body");
		Iterator<Element> it = body.iterator();
		while(it.hasNext()) {
			LOG.debug(it.next().toString());
		}
	}/*
	
	/*private void printlnElements(Elements Elements) {
		Iterator<Element> it = Elements.iterator();
		Element ele;
		while(it.hasNext()) {
			ele = it.next();
			LOG.debug(ele.toString());
			LOG.debug(ele.absUrl("href"));
		}
	}*/
	
	/*private void getHtmlElement(DomNodeList<HtmlElement> htmlElements,int type) {
		ListIterator<HtmlElement> list = htmlElements.listIterator();
		HtmlElement element;
	    while(list.hasNext()) {
	    	element = list.next();
	    	if(type == 1) {
	    		if(element.hasAttribute("src"))
	    			LinkQueue.imageUrlpush(element.getAttribute("src"));
	    	} else if(element.hasAttribute("href")) {
	    		LinkQueue.push(element.getAttribute("href"));
			} else if(element.hasAttribute("src")) {
				LinkQueue.push(element.getAttribute("src"));
			}
	    }
	}*/
	
	private String getBaseUri() {
		int index = this.url.replaceFirst("//", "--").indexOf('/');
//		String uri = this.url.substring(0, index);
//		if(this.url.contains(".")){
//			return uri.substring(uri.indexOf('.') + 1);
//		}
		return this.url.substring(0, index);
	}
	
	public static void closeQuietly(Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (IOException ioe) {
			// ignore
		}
	}
	
	public static void closeQuietly(Closeable... closeables) {
		for(Closeable closeable : closeables) {
			closeQuietly(closeable);
		}
	}
	
	private String getProperties(String key, String defaultValue) {
		String value = properties.getProperty(key);
		if(StringUtils.isBlank(value)) {
			return defaultValue;
		}
		return value;
	}
}
