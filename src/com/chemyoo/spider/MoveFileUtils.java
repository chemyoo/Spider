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
	
	public static void main(String[] args) {
		File file = SelectFiles.getSavePath();
		LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
		MoveFileUtils.getFiles(file, queue);
		try {
			MoveFileUtils.run(queue);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void run(LinkedBlockingQueue<String> queue) throws InterruptedException, IOException {
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
	
	private static void getFiles(File file, LinkedBlockingQueue<String> queue) {
		if(file != null && file.isDirectory()) {
			File[] dir = file.listFiles();
			for(File f : dir)
				if(f.isFile()) {
					queue.add(f.getAbsolutePath());
				} 
		}
	}
	
}
