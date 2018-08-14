package com.chemyoo.spider.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;

public class LinkQueue {

	private LinkQueue() {}
	
	// 未访问的url
	private static List<String> unVisited = new LinkedList<>();
	
	// 未访问的图片路径
	private static List<String> imageUrl = new ArrayList<>();
	
	private static Set<String> visited = new HashSet<>();
	
	private static Set<String> menuUrl = new HashSet<>();
	
	// 未访问的URL出队列
	public static String unVisitedPop() {
		
		if (!unVisited.isEmpty()) {
			String link = unVisited.remove(0);
			if(visited.size() > 60000 && menuUrl.size() < 50000) {
				visited.clear();
				visited.addAll(menuUrl);
			}
			visited.add(DigestUtils.md5Hex(link));
			return link;
		}
		return null;
	}
	
	public static int getUnVisitedSize() {
		return unVisited.size();
	}
	
	// 未访问的imageUrl出队列
	public static String imageUrlPop() {
		if (!imageUrl.isEmpty()) {
			return imageUrl.remove(0);
		}
		return null;
	}
	
	
	public static void addmenuUrl(String value) {
		if(isNotBlank(value) && !menuUrl.contains(value)) {
			menuUrl.add(value);
		}
	}
	
	public static void imageUrlpush(String url) {
		if (isNotBlank(url) && url.startsWith("http") && !imageUrl.contains(url) && !menuUrl.contains(url))
			imageUrl.add(url);
	}
	
	public static void push(String url) {
		if (isNotBlank(url) && !visited.contains(DigestUtils.md5Hex(url)) && url.startsWith("http") && !unVisited.contains(url))
			unVisited.add(url);
	}
	
	// 判断未访问的URL队列中是否为空
	public static boolean unVisitedEmpty() {
		return unVisited.isEmpty();
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
	
	public static void clear() {
		unVisited.clear();
		visited.clear();
		menuUrl.clear();
	}
	
}