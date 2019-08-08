package com.chemyoo.spider;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.FileUtils;

import com.chemyoo.spider.core.SelectFiles;
import com.chemyoo.spider.util.ImageUtils;
import com.chemyoo.spider.util.NumberUtils;
import com.chemyoo.spider.util.PictureColor;

/** 
 * @author Author : jianqing.liu
 * @version version : created time：2018年9月29日 下午4:18:38 
 * @since since from 2018年9月29日 下午4:18:38 to now.
 * @description class description
 */
public class PictureColorFilter {
	
	private static int count = 1;
	
	private static CountDownLatch countDownLatch = null;
	
	public static void main(String[] args) {
		File file = SelectFiles.getSavePath();
		LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
		getFiles(file, queue);
		process(queue);
	}
	
	private static void getFiles(File file, LinkedBlockingQueue<String> queue) {
		if(file != null && file.isDirectory()) {
			File[] dir = file.listFiles();
			for(File f : dir)
				if(f.isDirectory()) {
					getFiles(f, queue);
				} else {
					queue.add(f.getAbsolutePath());
				}
		}
	}
	
	private static void run(final LinkedBlockingQueue<String> queue) {
		new Thread() {
			@Override
			public void run() {
				while (!queue.isEmpty()) {
					File f = new File(queue.poll());
					if (f.exists()) {
						PictureColor colorInfo = ImageUtils.getWhiteColorPer(f);
						double value = NumberUtils.setScale(colorInfo.percent * 100, 2);
						if(value == 0D || value > 33D || colorInfo.average > 230) {
							System.err.println("第" + count + "张图片，文件名：" 
									+ f.getAbsolutePath() + "，主要色彩占比：" + value 
									+ "，颜色分布离散度：" + colorInfo.average + "，色彩单调或颜色分布不均，执行删除文件。");
//							String fileName = f.getParentFile().getParentFile()+"/" + 
//									value + "_" + colorInfo.average + "_" + f.getName(); 
//							FileUtils.moveFile(f, new File(fileName));
							FileUtils.deleteQuietly(f);
//							try {
//								FileUtils.moveFileToDirectory(f, f.getParentFile().getParentFile(), true);
//							} catch (IOException e) {
//								e.printStackTrace();
//							}
						}
						count ++;
					}
				} 
				countDownLatch.countDown();
			}
		}.start();
	}
	
	private static void process(LinkedBlockingQueue<String> queue) {
		int size = Runtime.getRuntime().availableProcessors();
		countDownLatch = new CountDownLatch(size);
		for(int i = 0; i < size; i++) {
			run(queue);
		}
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}

}
