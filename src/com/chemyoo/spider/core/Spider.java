package com.chemyoo.spider.core;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
//import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.swing.*;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
	
	Properties properties = PropertiesUtil.getInstance();
	
	private static final String PICTURE_EXT = "gif,png,jpg,jpeg,bmp"; 
	
	private Map<String,Integer> urlVisitedCount = new HashMap<>();

	public Spider(String url, String dir, JButton button, JLabel message,String referer) {
		this.url = url;
		this.dir = dir;
		this.button = button;
		this.message = message;
		this.setReferer(referer);
		deletetimer();
		// 重新读取配置文件
		PropertiesUtil.init();
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
	
	public void start() {
		LOG.info("程序已启动...");
		if(LinkQueue.unVisitedEmpty()) {
			LinkQueue.push(this.url);
		}
		while(!LinkQueue.unVisitedEmpty() && !button.isEnabled() && !button.isSelected()) {
			String link = LinkQueue.unVisitedPop();
			this.message.setText("正在访问网址链接:" + link);
			this.connectUrl(link);
			ImagesUtils.downloadPic(this.dir, this.getReferer());
			int linkSize = LinkQueue.getVisitedSize();
			if(linkSize % 500 == 0) {
				try {
					// 设置休眠，防止IP被禁用。
					TimeUnit.SECONDS.sleep(5L + random.nextInt(15));
				} catch (InterruptedException e) {
					LOG.info(e.getMessage(), e);
					Thread.currentThread().interrupt();
				}
			}
		}
		time.cancel();
		if(button.isSelected())
			LOG.info("程序暂停...");
		else
			LOG.info("程序终止...");
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
					System.out.println("待访问的网址数量：" + LinkQueue.getUnVisitedSize());
					LOG.info("定时任务执行完成...");
				} catch (Exception e) {
					LOG.error("定时任务执行异常", e);
				}
		}}, 0, 5 * 60 * 1000L);
	}
	
	/*private void openUrl(String url) {
		try (WebClient wc = new WebClient(BrowserVersion.CHROME);){
			
		    wc.getOptions().setUseInsecureSSL(true);  
		    wc.getOptions().setJavaScriptEnabled(true); // 启用JS解释器，默认为true  
		    wc.getOptions().setCssEnabled(false); // 禁用css支持  
		    wc.getOptions().setThrowExceptionOnScriptError(false); // js运行错误时，是否抛出异常  
		    wc.getOptions().setTimeout(100000); // 设置连接超时时间 ，这里是10S。如果为0，则无限期等待  
		    wc.getOptions().setDoNotTrackEnabled(false);  
		    HtmlPage page = wc.getPage(url);
		    HtmlElement body = page.getBody();
		    body.asText();
		    Iterable<DomNode> domnode = body.getChildren();
		    Iterator<DomNode> it = domnode.iterator();
		    while(it.hasNext()) {
		    	LOG.debug(it.next().asText());
		    }
		    
		    DomNodeList<HtmlElement> imgNode= body.getElementsByTagName("img");
		    this.getHtmlElement(imgNode, 1);
		    DomNodeList<HtmlElement> aHref = body.getElementsByTagName("a");
		    this.getHtmlElement(aHref, 2);
		    DomNodeList<HtmlElement> frame = body.getElementsByTagName("frame");
		    this.getHtmlElement(frame, 2);
			    
		} catch (IOException e) {
			LOG.error("打开网页发生异常");
		}
			
	}*/
	
	private void connectUrl(String url) {
		LOG.info("连接网址：【" + url + "】");
		try {
			if(PICTURE_EXT.contains(getFileExt(url))) {
				LinkQueue.imageUrlpush(url);
				return;
			}
			if(this.referer != null && !url.startsWith(this.referer)) {
				return;
			} 
			//.ignoreContentType(true)忽略请求头
			//Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko 这是IE11的userAgent
			//Mozilla 为大多数浏览器
			Document doc = Jsoup.connect(url)
					.userAgent("Mozilla")
					.timeout(30 * 1000).get();
			
			Elements body = doc.getElementsByTag("body");
			this.getUrls(body);
			this.getImagesUrls(body);
			this.getIframe(body);
		} catch (IOException e) {
			LOG.error("打开网页发生异常",e);
		}
	}

	private void getIframe(Elements body){
		Elements href = body.select("iframe");
		Iterator<Element> it = href.iterator();
		Element ele;
		String herfurl;
		String baseUrl = this.referer;
		while(it.hasNext()) {
			ele = it.next();
			herfurl = ele.absUrl("src");
			this.recognizeUrl(herfurl);

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
	
	private void getUrls(Elements body) {
		String classSelector = properties.getProperty("dom.class.first");
		String classSelector2 = properties.getProperty("dom.class.second");
		String keyWord = properties.getProperty("key.word");
		String notEndWith = properties.getProperty("not.end.with");
		String filterUrl = properties.getProperty("filter.url");
		String notContain = properties.getProperty("not.contain");
		Elements main = new Elements();
		if(StringUtils.isNotBlank(classSelector)) {
			String[] cssSelector = classSelector.split(",");
			for(String css : cssSelector) {
				main.addAll(body.select(css.trim()));
			}
		}
		Elements href = new Elements();
		if(StringUtils.isNotBlank(classSelector2)) {
			String[] cssSelector = classSelector2.split(",");
			for(String css : cssSelector) {
				href.addAll(main.select(css.trim() + " a[href]"));
			}
		}
		Iterator<Element> it = href.iterator();
		String baseUrl = this.referer;
		
		while(it.hasNext()) {
			Element ele = it.next();
			String herfurl = ele.absUrl("href");
			String text = ele.text();

			this.recognizeUrl(herfurl);
			
			// 如果是源网址，则忽略
			if("".equals(herfurl) || herfurl.equals(baseUrl) || (herfurl.endsWith("/") && herfurl.equals(baseUrl+"/"))) {
				continue;
			}
			
			if(StringUtils.isNotBlank(filterUrl)) {
				String[] words = filterUrl.split(",");
				lable:
				for(String word : words) {
					if(herfurl.contains(word.trim())) {
						continue lable;
					}
				}
			}
			
			if(StringUtils.isNotBlank(notContain)) {
				String[] words = notContain.split("[|]");
				label:
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
			
			if(StringUtils.isNotBlank(notEndWith)) {
				String[] words = notEndWith.split("[|]");
				label:
				for(String word : words) {
					boolean isNotAdd = true;
					String[] andWord = word.split("[+]");
					for(String and : andWord) {
						isNotAdd = isNotAdd && herfurl.endsWith(and.trim());
					}
					if(isNotAdd)
						continue label;
				}
			}
			
			if(PICTURE_EXT.contains(getFileExt(herfurl))) {
				LinkQueue.imageUrlpush(herfurl);
			} else if(herfurl.startsWith(baseUrl) || (herfurl.contains(".htm") || herfurl.contains(".html")
					|| herfurl.contains(".shtml"))) {
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

	/**
	 * 辨识URL,对重复出现多次的URL可认为是网站的导航链接
	 * @param url
	 */
	private void recognizeUrl(String url) {
		if(urlVisitedCount.size() > 50000) {
			urlVisitedCount.clear();
		}
		String value = DigestUtils.md5Hex(url);
		if(urlVisitedCount.containsKey(value)) {
			int count = urlVisitedCount.get(value) + 1;
			if(count < 3){
				urlVisitedCount.put(value, count + 1);
				LinkQueue.addmenuUrl(value);
			}
		} else {
			urlVisitedCount.put(value, 1);
		}
	}
	
	private void getImagesUrls(Elements body) {
		String main = properties.getProperty("dom.class.first");
		if(StringUtils.isBlank(main)) {
			main = StringUtils.EMPTY;
		} else {
			main += " ";
		}
		Elements href = body.select(main + "img[src]");
		Iterator<Element> it = href.iterator();
		String src;
		while(it.hasNext()) {
			src = it.next().absUrl("src");
			LinkQueue.imageUrlpush(src);
			this.recognizeUrl(src);
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
}
