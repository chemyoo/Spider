package com.chemyoo.spider.core;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/** 
 * @author 作者 : jianqing.liu
 * @version 创建时间：2018年5月30日 上午11:44:29 
 * @since 2018年5月30日 上午11:44:29 
 * @description 类说明 
 */
public class Spider {
	
	private String url;
	
	private String dir;
	
	private JButton button;
	
	private Timer time;

	private JLabel message;
	
	private String referer;
	
	private final String pictureExt = "gif,png,jpg,jpeg,bmp"; 
	
	private Map<String,Integer> urlVisitedCount = new HashMap<>();
	
	public Spider(String url, String dir, JButton button, JLabel message,String referer) {
		this.url = url;
		this.dir = dir;
		this.button = button;
		this.message = message;
		this.setReferer(referer);
		deletetimer();
	}
	
	private void setReferer(String referer) {
		if(this.referer == null && StringUtils.isNotBlank(referer)) {
			int index = referer.replaceFirst("//", "--").indexOf('/');
			this.referer = referer.substring(0, index);
		}
	}
	
	private String getReferer() {
		return this.referer;
	}
	
	public void start() {
		if(LinkQueue.unVisitedEmpty()) {
			LinkQueue.push(this.url);
		}
		String link;
		while(!LinkQueue.unVisitedEmpty() && !button.isEnabled()) {
			link = LinkQueue.unVisitedPop();
			this.message.setText("正在访问网址连接:" + link);
			this.connectUrl(link);
			ImagesUtils.downloadPic(this.dir, this.getReferer());
		}
		button.setEnabled(true);
		button.setText("开始爬取");
		this.message.setVisible(false);
		time.cancel();
	}
	
	private void deletetimer() {
        time = new Timer();
        //每天定时发送
        /**注意Timer的缺陷，同时注入多个schedule会有延时问题，
         * 只用当前的执行完，后面的任务才会执行，并且前面抛出异常，
         * 后面的任务就不会执行
         * 可以使用java.util.concurrent.ScheduledExecutorService来优化
         */
        time.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					DeleteImages.delete(dir);
				} catch (Exception e) {
					e.printStackTrace();
				}
		}}, 0, 1 * 60 * 1000L);
	}
	
	private void openUrl(String url) {
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
		    	System.err.println(it.next().asText());
		    }
		    
		    DomNodeList<HtmlElement> imgNode= body.getElementsByTagName("img");
		    this.getHtmlElement(imgNode, 1);
		    DomNodeList<HtmlElement> aHref = body.getElementsByTagName("a");
		    this.getHtmlElement(aHref, 2);
		    DomNodeList<HtmlElement> frame = body.getElementsByTagName("frame");
		    this.getHtmlElement(frame, 2);
			    
		} catch (IOException e) {
			e.printStackTrace();
		}
			
	}
	
	private void connectUrl(String url) {
		try {
			if(pictureExt.contains(getFileExt(url))) {
				LinkQueue.imageUrlpush(url);
				return;
			}
			//.ignoreContentType(true)忽略请求头
			Document doc = Jsoup.connect(url)
					.userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko")
					.timeout(60 * 1000).get();
			
			Elements body = doc.getElementsByTag("body");
			this.getUrls(body);
			this.getImagesUrls(body);
			this.getIframe(body);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void getIframe(Elements body){
		Elements href = body.select("iframe");
		Iterator<Element> it = href.iterator();
		Element ele;
		String herfurl;
		String baseUrl = this.getBaseUri();
		while(it.hasNext()) {
			ele = it.next();
			herfurl = ele.absUrl("src");
			this.recognizeUrl(herfurl);

			if(herfurl.equals(baseUrl) || (herfurl.endsWith("/") && herfurl.equals(baseUrl+"/"))) {
				continue;
			}
			if(pictureExt.contains(getFileExt(herfurl))) {
				LinkQueue.push(herfurl);
			}
		}
	}
	
	private void getUrls(Elements body) {
		Elements href = body.select("a[href]");
		Iterator<Element> it = href.iterator();
		Element ele;
		String herfurl;
//		String tempuri;
		String text;
		String baseUrl = this.getBaseUri();

		while(it.hasNext()) {
			ele = it.next();
			herfurl = ele.absUrl("href");
			text = ele.text();

//			if(this.url.contains(".")){
//				tempuri = herfurl.substring(herfurl.indexOf('.') + 1);
//			} else {
//				tempuri = herfurl;
//			}
			
			this.recognizeUrl(herfurl);
			
//			保证为本站链接
//			if(tempuri.equals(baseUrl) || (tempuri.endsWith("/") && tempuri.equals(baseUrl+"/"))) {
//				continue;
//			}
			
//			如果是源网址，则忽略
			if(herfurl.equals(baseUrl) || (herfurl.endsWith("/") && herfurl.equals(baseUrl+"/"))) {
				continue;
			}
			
			

			if(pictureExt.contains(getFileExt(herfurl))) {
				LinkQueue.imageUrlpush(herfurl);
			} else if(herfurl.startsWith(baseUrl) || (herfurl.contains(".htm") || herfurl.contains(".html"))) {
				LinkQueue.push(herfurl);
			} else if(text.contains("原图") || (text.contains("下载") && text.contains("图"))){
				LinkQueue.push(herfurl);
			} else if(text.contains("查看") && text.contains("大图")){
				LinkQueue.push(herfurl);
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
		if(urlVisitedCount.containsKey(url)) {
			int count = urlVisitedCount.get(url) + 1;
			if(count < 5) {
				urlVisitedCount.put(url, count);
			} else if(count < 6){
				urlVisitedCount.put(url, count + 1);
				LinkQueue.addmenuUrl(url);
			}
		} else {
			urlVisitedCount.put(url, 1);
		}
	}
	
	private void getImagesUrls(Elements body) {
		Elements href = body.select("img[src]");
		Iterator<Element> it = href.iterator();
		Element ele;
		while(it.hasNext()) {
			ele = it.next();
			LinkQueue.imageUrlpush(ele.absUrl("src"));
		}
	}
	
	private void printlnBody(Document doc) {
		Elements body = doc.getElementsByTag("body");
		Iterator<Element> it = body.iterator();
		while(it.hasNext()) {
			System.err.println(it.next().toString());
		}
	}
	
	private void printlnElements(Elements Elements) {
		Iterator<Element> it = Elements.iterator();
		Element ele;
		while(it.hasNext()) {
			ele = it.next();
			System.err.println(ele.toString());
			System.err.println(ele.absUrl("href"));
		}
	}
	
	private void getHtmlElement(DomNodeList<HtmlElement> htmlElements,int type) {
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
	}
	
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
