package com.chemyoo.spider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import com.chemyoo.image.analysis.SimilarityAnalysisor;
import com.chemyoo.spider.core.SelectFiles;

public class MainTest {
	
	private static int count = 0;

	public static void main(String[] args) {
		File file = SelectFiles.getSavePath();
		if(file != null) {
			System.out.println("开始处理...");
//			directory = new File("F:/deletedir");
			Map<String,String> md5Values = new LinkedHashMap<>();
			getMd5Values(file, md5Values);
			System.out.println("处理文件个数：" + md5Values.size());
			md5Values.clear();
			md5Values = null;
			System.out.println("删除文件个数：" + count);
			System.out.println("处理结束...");
		}
	}
	
	private static void getMd5Values(File file,Map<String,String> md5Values) {
		if(file.isDirectory()) {
			File[] files = file.listFiles();
			for(File f : files) {
				if(f.isFile()) {
					String md5 = getMD5(f);
					if(!md5Values.containsKey(md5)) {
						md5Values.put(md5, f.getAbsolutePath());
					} else {
						checkExists(f, new File(md5Values.get(md5)));
					}
				} else {
					getMd5Values(f, md5Values);
				}
			}
		}
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
