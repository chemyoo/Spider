package com.chemyoo.spider;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.FileUtils;

import com.chemyoo.spider.core.SelectFiles;

public class MoveFileUtils {
	
	private static final Date date = Calendar.getInstance().getTime();
	
	private static final DateFormat format = DateFormat.getDateInstance();
	
	private static final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
	
	public static void main(String[] args) {
		File file = SelectFiles.getSavePath();
		MoveFileUtils.getAllFiles(file);
		try {
			MoveFileUtils.runRemove();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
	}
	
	public static void run() throws InterruptedException, IOException {
		while(!queue.isEmpty()) {
			File file = new File(queue.take());
			String[] arrayStr = file.getName().split("_");
			String fileName = null;
			if(arrayStr.length >= 3) {
				fileName = arrayStr[2];
			} else {
				fileName = file.getName();
			}
			String deskPath = file.getParentFile().getAbsolutePath() + 
					"/" + format.format(date) + "/" + fileName;
			FileUtils.moveFile(file, new File(deskPath));
		}
	}
	
	public static void runRemove() throws InterruptedException {
		while(!queue.isEmpty()) {
			File file = new File(queue.take());
			String[] abPath = file.getAbsolutePath().split("[\\" + File.separator + "]");
			String root = null;
			if(abPath.length > 2) {
				root = abPath[0] + File.separator + abPath[1] + File.separator;
			} else {
				root = file.getAbsolutePath();
			}
			String fileName = file.getName();
			String deskPath = root + 
					"/壁纸All-IN/" + fileName;
			try {
				FileUtils.moveFile(file, new File(deskPath));
			} catch (IOException e) {
				FileUtils.deleteQuietly(file);
			}
		}
	}
	
	private static void getAllFiles(File file) {
		if(file != null) {
			if(file.isDirectory()) {
				File[] dir = file.listFiles();
				for(File f : dir) {
					if(f.isFile()) {
						queue.add(f.getAbsolutePath());
					} else {
						getAllFiles(f);
					}
				}
			} else {
				queue.add(file.getAbsolutePath());
			}
		}
	}
	
//	private static void getFiles(File file, LinkedBlockingQueue<String> queue) {
//		if(file != null && file.isDirectory()) {
//			File[] dir = file.listFiles();
//			for(File f : dir)
//				if(f.isFile()) {
//					queue.add(f.getAbsolutePath());
//				} 
//		}
//	}
	
}
