package com.chemyoo.spider.core;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

/** 
 * @author 作者 : jianqing.liu
 * @version 创建时间：2018年5月30日 上午9:47:01 
 * @since 2018年5月30日 上午9:47:01 
 * @description 文件选择框
 */
public class SelectFiles {

	public static File getSavePath() {
		JFileChooser fileChooser = new JFileChooser("F:/pic");
//		FileSystemView fsv = FileSystemView.getFileSystemView();  //注意了，这里重要的一句
		//设置最初路径为桌面路径              
//		fileChooser.setCurrentDirectory(fsv.getHomeDirectory());
		fileChooser.setDialogTitle("请选择文件夹...");
		fileChooser.setApproveButtonText("确定");
		//只选择文件夹
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		//设置文件单选
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setAcceptAllFileFilterUsed(true);// 去掉显示所有文件的按钮
		if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			return fileChooser.getSelectedFile();
		}
		return null;
	}
	
}
