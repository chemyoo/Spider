package com.chemyoo.spider;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.chemyoo.spider.core.SelectFiles;

public class MusicMove
{
	public static void main(String[] args)
	{
		File file = SelectFiles.getSavePath();
		if (file != null)
		{
			File root = file.getParentFile();
			handle(file, root);
			File[] child = file.listFiles();
			if (child != null && child.length == 0)
			{
				FileUtils.deleteQuietly(file);
			}
		}
	}

	public static File getRootPath(File file)
	{
		if (file != null)
		{
			File parent = file.getParentFile();
			if (parent != null)
			{
				return getRootPath(file);
			}
			return file;
		}
		else
		{
			return file;
		}
	}

	public static void handle(File file, File root)
	{
		if (file.isDirectory())
		{
			File[] files = file.listFiles();
			if (files != null && files.length > 0)
			{
				for (File f : files)
				{
					handle(f, root);
				}
			}
		}
		else
		{
			String fileName = file.getName();
			String ext = FilenameUtils.getExtension(fileName);
			if (StringUtils.isNotBlank(ext))
			{
				char[] array = FilenameUtils.getBaseName(fileName).toCharArray();
				boolean isMove = false;
				for (char c : array)
				{

					try
					{
						if (c > 127)
						{
							FileUtils.moveFileToDirectory(file, new File(root.getPath(), "中文歌曲"), true);
							isMove = true;
							break;
						}
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
				try
				{
					if (file.exists() && !isMove)
					{
						FileUtils.moveFileToDirectory(file, new File(root.getPath(), "英文歌曲"), true);
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
