package com.chemyoo.spider.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;

/**
 * @author 作者 : jianqing.liu
 * @version 创建时间：2018年7月9日 下午4:09:58
 * @since 2018年7月9日 下午4:09:58
 * @description 类说明
 */
public class ImageUtils {
	private ImageUtils() {
	}
	
	/**
	 * 将黑色作为基准色
	 */
	private static final Color BLACK = new Color(0, 0, 0);

	private static final String[] types = ImageIO.getReaderFormatNames();

	/**
	 * 获取图片缩略图
	 * 
	 * @return
	 */
	public static Image getThumbnailImage(File image) throws IOException {
		if (!Arrays.toString(types).contains(getExtension(image))) {
			throw new IOException("非图片类型文件，请重新选择...");
		}
		BufferedImage img = ImageIO.read(image);
		if (img != null) {
			Image pic = img.getScaledInstance(150, 100, Image.SCALE_SMOOTH);
			return convertToBufferedFrom(pic);
			// try {
			// 以PNG解析，缩略图会比较清晰
			// ImageIO.write(convertToBufferedFrom(pic), "PNG", out);
			// pic.flush();
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
			// img.flush();
		}
		return null;
	}

	/** 将任意Image类型图像转换为BufferedImage类型 */
	private static BufferedImage convertToBufferedFrom(Image srcImage) {
		BufferedImage bufferedImage = new BufferedImage(srcImage.getWidth(null), srcImage.getHeight(null),
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bufferedImage.createGraphics();
		g.drawImage(srcImage, null, null);
		g.dispose();
		return bufferedImage;
	}

	/** 获取文件拓展名 */
	private static String getExtension(File file) {
		try {
			String fileName = file.getName();
			return fileName.substring(fileName.lastIndexOf('.') + 1);
		} catch (Exception e) {
			return StringUtils.EMPTY;
		}
	}

	public static PictureColor getWhiteColorPer(File file) {
		PictureColor colorInfo = new PictureColor();
		try {
			BufferedImage bi = (BufferedImage) ImageUtils.getThumbnailImage(file);

			if (bi != null) {
				// 获取图像的宽度和高度
				int width = bi.getWidth();
				int height = bi.getHeight();
				colorInfo = calculate(bi, width, height);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return colorInfo;
	}

	private static PictureColor calculate(BufferedImage bi, int width, int height) {
		PictureColor colorInfo = new PictureColor();
		if (width > 0 && height > 0) {
			// 像素点数量
			int count = width * height;
			// 主要颜色数量初始化
			int[] maxCount = new int[] {0, 0, 0, 0};
			Map<Integer, Integer> rgbMap = new HashMap<>();
			// 扫描图片
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {// 行扫描
					int dip = bi.getRGB(j, i);
					int key = (int) labColorDistance(getRGB(dip)); // 将颜色换成4096色。
					if(rgbMap.containsKey(key)) {
						rgbMap.replace(key, rgbMap.get(key) + 1);
					} else {
						rgbMap.put(key, 1);
					}
				}
			}
			// 获得数量最多的前四色。
			double sum = 0D;
			colorInfo.colorCount = rgbMap.size();
			for(Map.Entry<Integer, Integer> entry : rgbMap.entrySet()) {
				int value = entry.getValue();
				if(value > maxCount[0]) {
					maxCount[1] = maxCount[0];
					maxCount[0] = value;
				} else if(value > maxCount[1]) {
					maxCount[2] = maxCount[1];
					maxCount[1] = value;
				}
				else if(value > maxCount[2]) {
					maxCount[3] = maxCount[2];
					maxCount[2] = value;
				} else if(value > maxCount[3]) {
					maxCount[3] = value;
				}
				sum += distance(value, count / colorInfo.colorCount);
			}
			colorInfo.percent = sum(maxCount) * 1D / count;
			// 求标准差，计算颜色分布离散程度。值越大，颜色分布越离散。
			colorInfo.average = (int) Math.sqrt(sum / colorInfo.colorCount);
//			if(r > 30D)
//				System.err.println(getRGB(rgbValue));
//			Color color = getRGB(rgbValue);
//			System.err.println("16进制颜色：" + getHexColor(color) + "[r=" + 
//					color.getRed() + ",g=" + color.getGreen() + ",b=" + color.getBlue() + "]");
//			color = getRGB(secondRgbValue);
//			System.err.println("第二色彩颜色：" + getHexColor(color) + "[r=" + 
//					color.getRed() + ",g=" + color.getGreen() + ",b=" + color.getBlue() + "]，占比" 
//					+ NumberUtils.setScale(secondmaxCount * 100D / count, 2));
		}
		return colorInfo;
	}
	
	private static int sum(int[] array) {
		int sum = 0;
		if(array != null && array.length > 0) {
			for(int v : array) {
				sum += v;
			}
		}
		return sum;
	}
	
	private static Color getRGB(int colorRGB) {
		int r = (colorRGB & 0xff0000) >> 16;
		int g = (colorRGB & 0xFF00) >> 8;
		int b = (colorRGB & 0xFF);
		return new Color(r, g, b);
	}
	
	private static String getHexColor(Color color) {
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		String hexR = "0" + Integer.toHexString(r);
		String hexG = "0" + Integer.toHexString(g);
		String hexB = "0" + Integer.toHexString(b);
		StringBuilder hexColor = new StringBuilder("#");
		hexColor.append(slice(hexR, -2))
				.append(slice(hexG, -2))
				.append(slice(hexB, -2));
		return hexColor.toString();
	}
	
	private static String slice(String str, int offset) {
		String r = null;
		if(offset >= 0) {
			r = str.substring(offset);
		} else {
			char[] c = str.toCharArray();
			int length = c.length;
			int end = length + offset;
			StringBuilder builder = new StringBuilder();
			for(int i = length - 1; i > -1 && i >= end; i --) {
				builder.append(c[i]);
			}
			r = builder.reverse().toString();
		}
		return r;
	}
	
	private static int converRgbToArgb(Color color){
		return (0xFF << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
	}
	
	public static void main(String[] args) {
		System.err.println(getRGB(-1600000));
		System.err.println(converRgbToArgb(new Color(11,11,11)));
		System.err.println(labColorDistance(new Color(255,255,255)));
	}
	
	public static double distance(int x, int y) {
		return Math.pow(x - y, 2);
	}
	/**
	 * LAB 是最接近肉眼感官的算法。
	 * LAB 颜色空间求色差。值越大，色差越大。
	 */
	public static double labColorDistance(Color color) {
		double r_avg = (BLACK.getRed() + color.getRed()) / 2;
		double r = BLACK.getRed() - color.getRed();
		double g = BLACK.getRed() - color.getRed();
		double b = BLACK.getBlue() - color.getBlue();
		return Math.sqrt((2 + r_avg / 256) * Math.pow(r, 2) + 4 * Math.pow(g, 2) 
				+ (2 + (255 - r_avg) / 256) * Math.pow(b, 2));
	}
	
}
