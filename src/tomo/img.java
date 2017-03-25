/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tomo;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by Piotr on 2017-03-10.
 */
public class img {
    private BufferedImage image = null;
    public img(String path) {
        try {
            this.image = ImageIO.read(new File(path));
        } catch (IOException e) {
            System.out.println("Problem with reading file");
            System.out.println(e);
        }
    }
   

    public BufferedImage getImage() {
        return this.image;
    }

    public int getPixel(int x, int y) {
        return this.image.getRGB(x, y) & 0xFF;
       
    }

    public void setPixel(int x, int y, int value) {
        Color color = new Color(value, value, value);
        int temp = color.getRGB();
        this.image.setRGB(x, y, temp);
    }

    public int getWidth() {
        return this.image.getWidth();
    }

    public int getHeight() {
        return this.image.getHeight();
    }

}
