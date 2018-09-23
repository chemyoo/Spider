package com.chemyoo.spider;

import com.chemyoo.spider.core.Spider;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeFilter;


public class Test {

    public static void main(String[] args) throws IOException{
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

}
