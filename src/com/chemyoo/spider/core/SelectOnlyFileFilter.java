package com.chemyoo.spider.core;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/** 
 * @author 作者 : jianqing.liu
 * @version 创建时间：2018年6月8日 下午2:30:32 
 * @since 2018年6月8日 下午2:30:32 
 * @description 文件夹过滤类
 */
public class SelectOnlyFileFilter extends FileFilter implements Filter{

	@Override
	public String getDescription() {
		return "文件(.task文件)";
	}
	
	@Override
	public boolean accept(File f) {
		return (f.isFile() && f.getName().endsWith(".task")) || f.isDirectory();
	}

	@Override
	public boolean accept(String url) {
		return true;
	}
	
}
