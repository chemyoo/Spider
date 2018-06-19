package com.chemyoo.spider;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;

public class Test {

    public static void main(String[] args){
        File file = new File("D:/a.jpg");
        try (FileInputStream fis = new FileInputStream(file);) {
            BufferedImage sourceImg = ImageIO.read(fis);
            sourceImg.flush();
        } catch (Exception e) {
        	e.printStackTrace();
        }
        System.err.println(file.delete());
    }

}
