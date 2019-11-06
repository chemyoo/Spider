package com.chemyoo.spider.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.codec.digest.DigestUtils;

public class LinkQueue {

	private LinkQueue() {}
	
	private static int count = 0;
	
	// 未访问的url
	private static List<String> unVisited = new LinkedList<>();
	
	// 未访问的图片路径
	private static List<String> imageUrl = new ArrayList<>();
	
	private static Map<String, String> pageUrls = new TreeMap<>(new Comparator<String>() {
		@Override
		public int compare(String s1, String s2) {
			int num = s1.length() - s2.length();
			return num == 0 ? s1.compareTo(s2) : num;
		}
	});
	
	private static Set<String> visited = new HashSet<>();
	private static Set<String> visitPage = new HashSet<>();
	
	// 未访问的URL出队列
	public static String unVisitedPop() {
		if (!unVisited.isEmpty()) {
			String link = unVisited.remove(0);
			if(visited.size() > 60000) {
				visited.clear();
				visitPage.clear();
			}
			visited.add(DigestUtils.md5Hex(link));
			return link;
		}
		return null;
	}
	
	public static void sizeAddOne() {
			count ++;
	}
	
	public static int getUnVisitedSize() {
		return unVisited.size() + pageUrls.size();
	}
	
	public static int getVisitedSize() {
		return visited.size() + visitPage.size();
	}
	
	public static synchronized int getImageSize() {
		return count;
	}
	
	// 未访问的imageUrl出队列
	public static String imageUrlPop() {
		if (!imageUrl.isEmpty()) {
			return imageUrl.remove(0);
		}
		return null;
	}
	
	public static void addPageUrl(String pageUrl) {
		if(isNotBlank(pageUrl)) {
			String mdhex = DigestUtils.md5Hex(pageUrl);
			if(!visitPage.contains(mdhex) && !pageUrls.containsKey(mdhex)) {
				pageUrls.put(mdhex, pageUrl);
				visitPage.add(mdhex);
			}
		} 
	}
	
	public static String getPageUrl() {
		if (!pageUrls.isEmpty()) {
			Map.Entry<String, String> en = null;
			for(Map.Entry<String, String> entry : pageUrls.entrySet()) {
				en = entry;
				break;
			}
			if(en != null) {
				pageUrls.remove(en.getKey());
				return en.getValue();
			}
		}
		return null;
	}
	
	public static void imageUrlpush(String url) {
		if (isNotBlank(url) && url.startsWith("http") && !imageUrl.contains(url))
			imageUrl.add(url);
	}
	
	public static void push(String url) {
		if (isNotBlank(url) && getUnVisitedSize() < 10000 && !visited.contains(DigestUtils.md5Hex(url)) 
				&& url.startsWith("http") && !unVisited.contains(url))
			unVisited.add(url);
	}
	
	// 判断未访问的URL队列中是否为空
	public static boolean unVisitedEmpty() {
		return unVisited.isEmpty() && pageUrls.isEmpty();
	}
	
	public static boolean imageUrlEmpty() {
		return imageUrl.isEmpty();
	}
	
	public static boolean isNotBlank(String...args) {
		for(String arg : args) {
			if(arg == null || "".equals(arg.trim())) {
				return false;
			}
		}
		return true;
	}
	
	public static synchronized void clear() {
		unVisited.clear();
		visited.clear();
		pageUrls.clear();
		imageUrl.clear();
		visitPage.clear();
		count = 0;
	}
	
	public static synchronized int find(String url) {
		int index = 0;
		for(String a : unVisited) {
			if(a.contains(url)) {
				return index + 1;
			}
			index ++;
		}
		if(index == 0) {
			String mdhex = DigestUtils.md5Hex(url);
			int temp = 0;
			for(Map.Entry<String, String> entry : pageUrls.entrySet()) {
				if(entry.getKey().equals(mdhex)) {
					return unVisited.size() + temp + 1;
				}
				temp ++;
			}
		}
		url = DigestUtils.md5Hex(url);
		for(String a : visited) {
			if(a.equalsIgnoreCase(url)) {
				return 0;
			}
		}
		return -1;
	}
	
}