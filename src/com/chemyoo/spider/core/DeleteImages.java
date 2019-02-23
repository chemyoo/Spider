package com.chemyoo.spider.core;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Properties;

import javax.imageio.ImageIO;

import com.chemyoo.image.analysis.SimilarityAnalysisor;
import com.chemyoo.spider.util.PropertiesUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
	
	private static final Properties props = PropertiesUtil.getInstance();
	
	private static final double MIN_H = Double.parseDouble(props.getProperty("min.height", "700"));
	
	private static final double MIN_W = Double.parseDouble(props.getProperty("min.width", "1000"));
	
	private static final double MIN_S = Double.parseDouble(props.getProperty("min.size", "100"));
	
	public static synchronized void delete(String dir) {
		File file = new File(dir);
		final long time = Calendar.getInstance().getTimeInMillis();
		File[] files = file.listFiles();
		if(files != null){
			for(File f : files) {
				if(f.isFile() && (f.lastModified() +  1000 * 60 * 5L) < time 
						&& Arrays.toString(ImageIO.getWriterFormatNames()).contains(getFileExt(f.getName()))) {
					if(isNotAllowedSave(f)) {
						FileUtils.deleteQuietly(f); 
					} else {
						moveFile(f, dir);
					}
				} 
			}
		}
	}
	/**
	 * @param file
	 * @return
	 */
	private static boolean isNotAllowedSave(File file){
			double width = 0d;
			double height = 0d;
			try (FileInputStream fis = new FileInputStream(file);){
				BufferedImage sourceImg = ImageIO.read(fis);
				width = sourceImg.getWidth();
				height = sourceImg.getHeight();
				sourceImg.flush();
				Spider.closeQuietly(fis);
			} catch (Exception e) {
				LOG.error("获取图片分辨率失败：", e);
			} 
			boolean flag = width < MIN_W || height < MIN_H;
			if(!flag) {
				String log = "保存文件：【%s】，分辨率(宽 * 高):%.1f * %.1f";
				LOG.info(String.format(log, file.getPath(), width, height));
			}
			return flag;
	}
	
	private static boolean isNotAllowedSave(File file, BufferedImage image, double fileSize){
		double width = 0d;
		double height = 0d;
		try {
			width = image.getWidth();
			height = image.getHeight();
		} catch (Exception e) {
			LOG.error("获取图片分辨率失败：", e);
		} 
		boolean flag = fileSize <= MIN_S || width < MIN_W || height < MIN_H;
		if(!flag) {
			String log = "保存文件：【%s】，分辨率(宽 * 高):%.1f * %.1f，文件大小：%.2fkb";
			LOG.info(String.format(log, file.getPath(), width, height, fileSize));
		}
		// not use 'else LOG.info("丢弃文件：【" + file.getPath() + "】，分辨率(宽 * 高):"+width+" * "+height);'
		return flag;
}

	public static synchronized void checkImageSize(File file, String dir) {
		if(file.exists() && file.isFile()) {
			if(isNotAllowedSave(file)) {
				FileUtils.deleteQuietly(file);
			} else {
				moveFile(file, dir);
			}
		}
	}
	
	public static synchronized void checkImageSize(File file, InputStream inputStream, double fileSize) throws IOException {
		try(ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1024)) {
			IOUtils.copy(inputStream, byteStream);	
			inputStream = new ByteArrayInputStream(byteStream.toByteArray());//转换后的输入流
			BufferedImage image = ImageIO.read(inputStream);
			if(!isNotAllowedSave(file, image, fileSize)) {
				try (OutputStream out = new FileOutputStream(file)){
					byteStream.writeTo(out);
				} catch (IOException e) {
					LOG.error("保存图片发生异常",e);
				}
				moveFile(file, file.getParentFile().getPath());
			} 
			image.flush();
		} finally {
			Spider.closeQuietly(inputStream);
		}
	}
	
	private static void moveFile(File file,final String dir) {
		String path = dir + IMAGES_DIR + convertDateToString() + getFileSeparator();
		try {
			FileUtils.moveToDirectory(file, new File(path), true);
		} catch (IOException e) {
			LOG.error("保存失败：【"+ file.getPath() + "】，文件已存在，正在进行图像相似度分析...");
			// 如果图片相似度大于0.95则删除图片，否则进行重命名
            double similar = pictrueSimilarity(file, new File(path + file.getName()));
            LOG.info("图片相似度："+String.format("%.2f", similar * 100) + "%");
			if(similar > 0.90D) {
				LOG.info("图片基本相似，删除图片不保存...");
				FileUtils.deleteQuietly(file);
			} else {
				LOG.info("文件相似度不大于0.90，进行文件重命名...");
				reName(file, dir);
			}
		}
	}

	private static double pictrueSimilarity(File f1, File f2){
		return SimilarityAnalysisor.getSimilarity(f1,f2);
	}
	
	private static String getFileExt(String fileName) {
		String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
		if("jpg".equalsIgnoreCase(ext)) {
			return "gif";
		}
		return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
	}
	
	private static void reName(File file, String dir) {
		int index = 0;
		String path = dir + IMAGES_DIR + convertDateToString() + getFileSeparator();
		File newFile = new File(path + apendString() + file.getName());
		while(newFile.exists()) {
			String fileName = file.getName();
			String fileExt = getFileExt(fileName);
			index ++;
			String newName = fileName.replace("." + fileExt, "(") 
					+ index +")." + fileExt;
			newFile = new File(path + newName);
		}
		try {
			double size = file.length() / 1024.0;
			FileUtils.copyFile(file, newFile);
			FileUtils.deleteQuietly(file);
			LOG.info("保存文件：【" + file.getPath() + " 】，已重新命名【" + newFile.getName() 
				+ "】，文件大小：" + String.format("%.2f kb", size));
		} catch (IOException e) {
			LOG.error("重命名文件失败");
		}
	}
	
	private static String convertDateToString() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.format(Calendar.getInstance().getTime());
	}
	
	private static String apendString() {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
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
