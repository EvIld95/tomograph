/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tomo;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Piotr on 2017-03-03.
 */
public class Tomo {
    public static void main(String[] args) {
        TomographSinogram sinogram = new TomographSinogram();
        img image = sinogram.loadImage("/Users/pawelszudrowicz/Downloads/box.png");
        float topLight = sinogram.calculateTopLight(image);
        int w = image.getWidth();
        int r = w/2 - 5;
        int h = image.getHeight();
//        BufferedImage temp = sinogram.scanObject(90, r, w, h, 6, 1, image, topLight);//180 360
//        BufferedImage sinogramImage = sinogram.createSinogram();
//        BufferedImage redrawnImage = sinogram.createObjectFromSinogram();
        NewJFrame frame = new NewJFrame();
        frame.width = w;
        frame.height = h;
        frame.r = r;
        frame.image = image;
        frame.topLight = topLight;
        frame.sinogram = sinogram;
//        frame.loadFirstImage(temp);
//        frame.loadSecondImage(sinogramImage);
//        frame.loadThirdImage(redrawnImage);
        
        frame.setVisible(true);
    }
 
}

