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

	public static double getWhiteColorPer(File file) {
		double r = 0D;
		try {
			BufferedImage bi = (BufferedImage) ImageUtils.getThumbnailImage(file);

			if (bi != null) {
				// 获取图像的宽度和高度
				int width = bi.getWidth();
				int height = bi.getHeight();
				r = calculate(bi, width, height);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return r;
	}

	private static double calculate(BufferedImage bi, int width, int height) {
		double r = 0D;
		if (width > 0 && height > 0) {
			// 白色像素点数量
			int count = 0;
			// 其他颜色数量
			int maxCount = 0;
			int rgbValue = 0;
			Map<Integer, Integer> rgbMap = new HashMap<>();
			// 扫描图片
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {// 行扫描
					int dip = bi.getRGB(j, i);
					if(rgbMap.containsKey(dip)) {
						rgbMap.replace(dip, rgbMap.get(dip) + 1);
					} else {
						rgbMap.put(dip, 1);
					}
					count++;
				}
			}
			for(Map.Entry<Integer, Integer> entry : rgbMap.entrySet()) {
				int value = entry.getValue();
				if(value > maxCount) {
					maxCount = value;
					rgbValue = entry.getKey();
				}
			}
			r = maxCount * 1D / count;
			if(r > 26D)
				System.err.println(RGB(rgbValue));
		}
		return r;
	}
	
	private static Color RGB(int color) {
		int r = 0xFF & color;
		int g = 0xFF00 & color;
		g >>= 8;
		int b = 0xFF0000 & color;
		b >>= 16;
		return Color.getHSBColor(r, g, b);
	}
	
}
