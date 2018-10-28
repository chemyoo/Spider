package com.chemyoo.spider;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeFilter;


public class Test {

    public static void main(String[] args) throws IOException{
    	// filterNode();
    	Date modified = getFileModifiedTime(new File("C:/Users/n_soul/Desktop","语录摘抄.txt"));
    	DateFormat formt = SimpleDateFormat.getDateTimeInstance();
    	System.err.println(formt.format(modified));
    	System.err.println(DigestUtils.md5Hex("http://img15.yixiu8.com:8080/picture/180402/pic5/13.jpg"));
    	// 第一次加载时，存入文件最后修改时间。
    	// 以后每次读取时，读取文件最后修改时间，比对时间是否改变。
    	// 如果最后修改时间发现变化，则重新读取文件。并更新内存中的最后修改时间。
    	
    	
    	
    }
    
    
    private static void calculate() {
    	
    }
    
    public static void filterNode() throws IOException {
    	String url = "http://www.5857.com/pcbz/76807.html";
    	Document doc = Jsoup.connect(url)
				.userAgent("Mozilla")
				.timeout(30 * 1000).get();
    	Elements body = doc.getElementsByTag("body");
    	Elements ele = body.select("div.main_center");
    	NodeFilter nodeFilter = new NodeFilter() {

			@Override
			public FilterResult head(Node arg0, int arg1) {
				// TODO Auto-generated method stub
				arg0.childNodes().get(9).childNodes().get(7).remove();
				return null;
			}

			@Override
			public FilterResult tail(Node arg0, int arg1) {
				// TODO Auto-generated method stub
				return null;
			}
    		
    	};
		ele.filter(nodeFilter);
    }
    
    public static Date getFileModifiedTime(File file) {
    	return new Date(file.lastModified());
    }

}
