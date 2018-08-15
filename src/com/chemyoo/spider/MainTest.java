package com.chemyoo.spider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import com.chemyoo.image.analysis.SimilarityAnalysisor;
import com.chemyoo.spider.core.SelectFiles;

public class MainTest {
	
	private static int count = 0;
	
	private static CountDownLatch countDownLatch = null;

	public static void main(String[] args) {
		File file = SelectFiles.getSavePath();
		if(file != null) {
			long start = System.currentTimeMillis();
			System.out.println("开始处理...");
//			directory = new File("F:/deletedir");
			Map<String,String> md5Values = new ConcurrentHashMap<>();
//			getMd5Values(file, md5Values);
			LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
			getFiles(file, queue);
			getMd5Values(queue, md5Values);
			System.out.println("处理文件个数：" + md5Values.size());
			md5Values.clear();
			md5Values = null;
			System.out.println("删除文件个数：" + count);
			System.out.println("处理结束...");
			long take = System.currentTimeMillis() - start;
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(take);
			System.out.println(take + "ms");
			System.out.println(calendar.getTime().toString());
		}
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
	
//	private static void getMd5Values(File file,Map<String,String> md5Values) {
//		if(file.isDirectory()) {
//			File[] files = file.listFiles();
//			for(File f : files) {
//				if(f.isFile()) {
//					String md5 = getMD5(f);
//					if(!md5Values.containsKey(md5)) {
//						md5Values.put(md5, f.getAbsolutePath());
//					} else {
//						checkExists(f, new File(md5Values.get(md5)));
//					}
//				} else {
//					getMd5Values(f, md5Values);
//				}
//			}
//		}
//	}
	
	private static void getMd5Values(LinkedBlockingQueue<String> queue,Map<String,String> md5Values) {
		int size = Runtime.getRuntime().availableProcessors();
		countDownLatch = new CountDownLatch(size);
		for(int i = 0; i < size; i++) {
			run(queue, md5Values);
		}
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private static void run(final LinkedBlockingQueue<String> queue, final Map<String,String> md5Values) {
		new Thread() {
			@Override
			public void run() {
				while (!queue.isEmpty()) {
					File f = new File(queue.poll());
					if (f.exists()) {
						String md5 = getMD5(f);
						if (!md5Values.containsKey(md5)) {
							md5Values.put(md5, f.getAbsolutePath());
						} else {
							checkExists(f, new File(md5Values.get(md5)));
						}
					}
				} 
				countDownLatch.countDown();
			}
		}.start();
	}
	
	private static String getMD5(File file) {
		try (InputStream is = new FileInputStream(file)){
			return DigestUtils.md5Hex(is);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	private static void checkExists(File curfile, File file2) {
		double similar =  SimilarityAnalysisor.getSimilarity(curfile,file2);
		if(similar > 0.9D) {
			FileUtils.deleteQuietly(curfile);
			System.err.println("删除文件：" + curfile.getAbsolutePath());
//			FileUtils.moveFileToDirectory(curfile, directory, true);
			count ++;
		} else {
			System.err.println("保留文件：" + curfile.getAbsolutePath());
		}
	}

}
