package com.chemyoo.spider.core;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.imageio.ImageIO;

import com.chemyoo.image.analysis.SimilarityAnalysisor;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
/** 
 * @author 作者 : jianqing.liu
 * @version 创建时间：2018年5月31日 下午12:55:13 
 * @since 2018年5月31日 下午12:55:13 
 * @description 类说明 
 */
public class DeleteImages {
	
	private DeleteImages() {}
	
	private static final Logger LOG = Logger.getLogger(Spider.class);
	
	private static final String IMAGES_DIR = "/images/";
	
	public static synchronized void delete(String dir) {
		File file = new File(dir);
		final long time = Calendar.getInstance().getTimeInMillis();
		File[] files = file.listFiles();
		if(files != null){
			for(File f : files) {
				if(f.isFile() && f.lastModified() < (time - 1 * 1000 * 60L)) {
					if(isAllowedSave(f)) {
						FileUtils.deleteQuietly(f);
						LOG.info("删除图片，图片大小:"+String.format("%.1f",f.length()/1024.0)+" kb");
					} else {
						moveFile(f, dir);
					}
				} else if(f.isFile() && "gif,png,jpg,jpeg,bmp".contains(getFileExt(f.getName()))){
					FileUtils.deleteQuietly(f);
				}
				
			}
		}
	}
	private static boolean isAllowedSave(File file){
			double width = 0d;
			double heigth = 0d;
			//try结束后会自动释放文件流fis
			try (FileInputStream fis = new FileInputStream(file)){
				BufferedImage sourceImg = ImageIO.read(fis);
				width = sourceImg.getWidth();
				heigth = sourceImg.getHeight();
				sourceImg.flush();
			} catch (Exception e) {
				LOG.error("获取图片分辨率失败：", e);
			}
			boolean flag = width < 1300 || heigth < 700;
			if(!flag)
				LOG.info(file.getPath() + "即将被保存，分辨率(宽 * 高):"+width+" * "+heigth);
			return flag;
	}

	public static void checkImageSize(File file, String dir) {
		if(file.exists() && file.isFile()) {

			if(isAllowedSave(file)) {
				FileUtils.deleteQuietly(file);
			} else {
				moveFile(file, dir);
			}
		}
	}
	
	private static void moveFile(File file,final String dir) {
		String path = dir + IMAGES_DIR + convertDateToString() + getFileSeparator();
		try {
			FileUtils.moveToDirectory(file, new File(path), true);
		} catch (IOException e) {
			LOG.error("移动文件失败");
			LOG.info("判断已存在的图片和当前下载的图片相似度...");
			// 如果图片相似度大于0.95则删除图片，否则进行重命名
            double similar = pictrueSimilarity(file, new File(path + file.getName()));
            LOG.info("图片相似度："+String.format("%.3f"));
			if(similar > 0.90D) {
				LOG.info("图片基本相似，删除图片不保存...");
				FileUtils.deleteQuietly(file);
			} else {
				LOG.info("文件相似度不大于0.95，进行文件重命名...");
				reName(file, dir);
			}
		}
	}

	private static double pictrueSimilarity(File f1, File f2){
		return SimilarityAnalysisor.getSimilarity(f1,f2);
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
			LOG.error("重命名文件失败");
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
