package com.chemyoo.spider.core;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/***
 * java抓取网络图片
 */
public class ImagesUtils {
	
	// 编码
	private static final String ECODING = "UTF-8";
	// 获取img标签正则
	private static final String IMGURL_REG = "<img.*src=(.*?)[^>]*?>";
	// 获取src路径的正则
	private static final String IMGSRC_REG = "http:\"?(.*?)(\"|>|\\s+)";

	public static void downloadPic(String dir, String referer) {
		// 获得html文本内容
		download(dir, referer);
	}

	/***
	 * 获取HTML内容
	 * 
	 * @param url
	 * @return
	 * @throws IOException 
	 * @throws Exception
	 */
	public static String getHTML(String url) throws IOException {
		URL uri = new URL(url);
		URLConnection connection = uri.openConnection();
		InputStream in = connection.getInputStream();
		byte[] buf = new byte[1024];
		StringBuilder sb = new StringBuilder();
		while (in.read(buf, 0, buf.length) > 0) {
			sb.append(new String(buf, ECODING));
		}
		in.close();
		return sb.toString();
	}

	/***
	 * 获取ImageUrl地址
	 * 
	 * @param html
	 * @return
	 */
	private static List<String> getImageUrl(String html) {
		Matcher matcher = Pattern.compile(IMGURL_REG).matcher(html);
		List<String> listImgUrl = new ArrayList<>();
		String matcherHtml;
		boolean isAdd;
		while (matcher.find()) {
			isAdd = true;
			matcherHtml = matcher.group();
			Document doc = Jsoup.parse(matcherHtml); 
			Elements elements = doc.getAllElements();
			Iterator<Element> iterator = elements.iterator();
			Element element;
			String width;
			String height;
			while(iterator.hasNext()) {
				element = iterator.next();
				width = element.attr("width");
				height = element.attr("height");
				if(LinkQueue.isNotBlank(width , height)) {
					if(Integer.parseInt(width.trim().replace("px", "")) <100 
							|| Integer.parseInt(height.trim().replace("px", "")) <100) {
						isAdd = false;
						break;
					}
				}
			}
			if(isAdd)
				listImgUrl.add(matcherHtml);
		}
		return listImgUrl;
	}

	/***
	 * 获取ImageSrc地址
	 * 
	 * @param listImageUrl
	 * @return
	 */
	private static List<String> getImageSrc(List<String> listImageUrl) {
		List<String> listImgSrc = new ArrayList<>();
		for (String image : listImageUrl) {
			Matcher matcher = Pattern.compile(IMGSRC_REG).matcher(image);
			while (matcher.find()) {
				listImgSrc.add(matcher.group().substring(0,
						matcher.group().length() - 1));
			}
		}
		return listImgSrc;
	}

	/***
	 * 下载图片
	 * 
	 * @param dir
	 */
	private static void download(String dir,String referer) {
		
		if(!(dir.endsWith("/") || dir.endsWith("\\"))) {
			dir += System.getProperty("file.separator");
		}
		File path = new File(dir);
		if(!path.exists()) {
			path.mkdirs();
		}
		
		InputStream in = null;
		FileOutputStream fileOutStream = null;
		String url;
		String imageName = null;
		while(!LinkQueue.imageUrlEmpty()) {
			try {
				url = LinkQueue.imageUrlPop();
				imageName = url.substring(url.lastIndexOf('/') + 1,
						url.length());
				
				if(imageName.contains("?")) {
					imageName = imageName.substring(0,imageName.lastIndexOf('?'));
				}

				//非图片，不进行下载
				if(!"gif,png,jpg,jpeg,bmp".contains(getFileExt(imageName))) {
					continue;
				}
				
				URL uri = new URL(url);
				URLConnection urlConnection = uri.openConnection();
				HttpURLConnection httpConnection = (HttpURLConnection) urlConnection;
				httpConnection.setRequestProperty("referer", referer);
				httpConnection.setRequestProperty("cookie", "");
				in = httpConnection.getInputStream();
				fileOutStream = new FileOutputStream(new File(dir + imageName));
				byte[] buf = new byte[1024];
				int length = 0;
				while ((length = in.read(buf, 0, buf.length)) != -1) {
					fileOutStream.write(buf, 0, length);
				}
				in.close();
				fileOutStream.close();
//						
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				Spider.closeQuietly(in);
				Spider.closeQuietly(fileOutStream);
				//待文件流被释放后，下载成功，进行文件分辨率辨识		
				DeleteImages.checkImageSize(new File(dir + imageName), dir);
			}
		}
	}
	
	private static String getBaseUri(String url) {
		int index = url.replaceFirst("//", "--").indexOf('/');
		return url.substring(0, index);
	}
	
	private static String getFileExt(String fileName) {
		return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
	}

}