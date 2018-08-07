package com.chemyoo.spider;

import com.chemyoo.spider.core.Spider;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;

public class Test {

    public static void main(String[] args){
        File file = new File("C:/Users/Administrator/Desktop/a.txt");
        try (FileInputStream fis = new FileInputStream(file);){
            BufferedImage sourceImg = ImageIO.read(fis);
            sourceImg.flush();
            Spider.closeQuietly(fis);
        } catch (Exception e) {
        	e.printStackTrace();
        }
        System.err.println(file.delete());
    }

}
