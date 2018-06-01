package com.chemyoo.spider.core;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;

/** 
 * @author 作者 : jianqing.liu
 * @version 创建时间：2018年5月31日 下午12:55:13 
 * @since 2018年5月31日 下午12:55:13 
 * @description 类说明 
 */
public class DeleteImages {
	
	private DeleteImages() {}
	
	private static final String IMAGES_DIR = "/images/";
	
	public static synchronized void delete(String dir) {
		File file = new File(dir);
		final long time = Calendar.getInstance().getTimeInMillis();
		File[] files = file.listFiles();
		if(files != null){
			for(File f : files) {
				if(f.isFile() && f.lastModified() < (time - 1 * 1000 * 60L)) {
					try (FileInputStream fis = new FileInputStream(f);){
						BufferedImage sourceImg =ImageIO.read(fis);
						double width = sourceImg.getWidth();
						double heigth=sourceImg.getHeight();
						sourceImg.flush();
						fis.close();
						if(width < 1000 || heigth < 700) {
							FileUtils.deleteQuietly(f);
							System.out.println(f.getName() + "被删除，分辨率(宽 * 高):"+width+" * "+heigth); 
							System.out.println("图片大小:"+String.format("%.1f",f.length()/1024.0)+" kb");
						} else {
							moveFile(f, dir);
						}
					} catch (Exception e) {
						e.printStackTrace();
						FileUtils.deleteQuietly(f);
					}
				} else if(f.isFile() && !"gif,png,jpg,jpeg,bmp".contains(getFileExt(f.getName()))){
					FileUtils.deleteQuietly(f);
				}
				
			}
		}
	}
	
	public static void checkImageSize(File file, String dir) {
		if(file.exists() && file.isFile()) {
			try (FileInputStream fis = new FileInputStream(file);){
				BufferedImage sourceImg =ImageIO.read(fis);
				double width = sourceImg.getWidth();
				double heigth=sourceImg.getHeight();
				sourceImg.flush();
				fis.close();
				if(width < 1000 || heigth < 700) {
					FileUtils.deleteQuietly(file);
				} else {
					moveFile(file, dir);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void moveFile(File file,final String dir) {
		try {
			String path = dir + IMAGES_DIR + convertDateToString();
			FileUtils.moveToDirectory(file, new File(path), true);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("进行文件重命名...");
			reName(file, dir);
		}
	}
	
	private static String getFileExt(String fileName) {
		return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
	}
	
	private static void reName(File file, String dir) {
		int index = 0;
		String path = dir + IMAGES_DIR + convertDateToString() + getFileSeparator();
		File newFile = new File(path + file.getName());
		while(newFile.exists()) {
			String fileName = file.getName();
			String fileExt = getFileExt(fileName);
			index ++;
			String newName = fileName.replace("." + fileExt, "(") 
					+ index +")." + fileExt;
			newFile = new File(path + newName);
		}
		try {
			FileUtils.copyFile(file, newFile);
			FileUtils.deleteQuietly(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static String convertDateToString() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.format(Calendar.getInstance().getTime());
	}
	
	/**
	 * 文件路径分隔符
	 * 
	 * @return
	 */
	private static String getFileSeparator() {
		return System.getProperty("file.separator");
	}
}
