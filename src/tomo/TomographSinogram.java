/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tomo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_BYTE_BINARY;
import static java.awt.image.BufferedImage.TYPE_BYTE_GRAY;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class TomographSinogram {
    private static int IMAGE_SIZE = 300;
    Map<Integer, ArrayList<Float>> valueFromDetectorAngle = new HashMap<>();
    ArrayList<ArrayList<Float>> sinogramValues = new ArrayList<ArrayList<Float>>();
    float[][] redrawnImagePixels = new float[IMAGE_SIZE][IMAGE_SIZE];
    ArrayList<Float> angles = new ArrayList<Float>();
    ArrayList<Float> values;
    //img image;
//    int l = 90;
//    int detectorsNumber = 180;
//    int emiterIterations = 360;
    
    public BufferedImage copyImg(BufferedImage source) {
        BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        Graphics g = b.getGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return b;
    }
    
    public float calculateTopLight(img image) {
        int w = image.getWidth();
        int h = image.getHeight();
        float topLight = 0;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                if (image.getPixel(i, j) > topLight) {
                    topLight = image.getPixel(i, j);
                } 
            }
        }
        return topLight;
    }
    
    public BufferedImage scanObject(int l, float r, int w, int h, int detectorsNumber, int emiterIterations, img image, float topLight) {
        BufferedImage temp = copyImg(image.getImage());
        Graphics2D rect = temp.createGraphics();
        rect.setColor(Color.RED);
        rect.drawOval(0, 0, w, h);
        
        float angleIteration = 360/emiterIterations;
        for (float i=0; i<360; i+=angleIteration) {
            values = new ArrayList<>();
            angles.add(i);
            Point point;
            point = pointOnCircle(r, i, w, h);
            rect.drawOval(point.x, point.y, 1, 1);
            for(int k=1; k<detectorsNumber; k++) {
               double detectorAngle = i + 180 + l - 2*l*k/detectorsNumber;
               Point detectorPoint = pointOnCircle(r, (float) detectorAngle, w, h);
               rect.drawOval(detectorPoint.x, detectorPoint.y, 2, 2);
               bresenhamLine(point.x,point.y,detectorPoint.x,detectorPoint.y,image, (int) topLight, rect);
            }
           
            sinogramValues.add(values);
            //this.valueFromDetectorAngle.put(i, values); 
        }
        return temp;
    }
    public void cleanUp() {
        values.clear();
        angles.clear();
        this.valueFromDetectorAngle.clear();
        this.sinogramValues.clear();
        for(int i=0; i<IMAGE_SIZE; i++) {
            for(int k=0;k<IMAGE_SIZE; k++) {
                this.redrawnImagePixels[i][k] = 0;
            }
        }
        
    }
    public BufferedImage createSinogram(int l, int detectors, int emiterIterations ){
        BufferedImage sinogram = new BufferedImage(IMAGE_SIZE,IMAGE_SIZE,TYPE_BYTE_GRAY);
   
        float sizeWidth = IMAGE_SIZE;
        float sizeHeight = IMAGE_SIZE;

        float rowHeight = sizeHeight / sinogramValues.get(0).size();
        float elementWidth = sizeWidth / sinogramValues.size();

        int currentX=0;
        int currentY=0;
            
        for (int i = 0; i<angles.size(); i++) {
            for(int k=0; k<sinogramValues.get(i).size() ; k++) {
                float valueSum = sinogramValues.get(i).get(k);
                Color color = new Color(valueSum,valueSum, valueSum);
                int rgb = color.getRGB();
                
                for(int x=currentX; x<(i+1)*elementWidth ; x++) {
                    for(int y=currentY; y<(k+1)*rowHeight ; y++) {
                        sinogram.setRGB(x, y, rgb);
                    }
                }
                
                currentY = (int) ((k+1)*rowHeight);
            }
            currentX = (int) ((i+1)*elementWidth);
        }
        return sinogram;
    }
    
    
    public BufferedImage createObjectFromSinogram(int l, int detectorsNumber, int emiterIterations) {
        BufferedImage redrawnImage = new BufferedImage(IMAGE_SIZE,IMAGE_SIZE,TYPE_BYTE_GRAY);
        Graphics2D redrawnImageGraphics = redrawnImage.createGraphics();
        int r = IMAGE_SIZE/2 - 5;
        for (int i = 0; i<angles.size(); i++) {
            Point point;
            point = pointOnCircle(r, angles.get(i), IMAGE_SIZE, IMAGE_SIZE);
            for(int k=0; k<sinogramValues.get(i).size() ; k++) {
                float valueSum = sinogramValues.get(i).get(k);
                double detectorAngle = angles.get(i) + 180 + l - 2*l*k/detectorsNumber;
                Point detectorPoint = pointOnCircle(r, (float) detectorAngle, IMAGE_SIZE, IMAGE_SIZE);
                bresenhamDrawLine(point.x,point.y,detectorPoint.x,detectorPoint.y, valueSum);           
            }
        }
        
        
        //drawing
        float topValue = 0;
        for (int i=0;i<IMAGE_SIZE;i++){
            for(int k=0;k<IMAGE_SIZE;k++){
                float value = redrawnImagePixels[i][k];
                if(topValue<value) {
                    topValue = value;
                }
            }
        }
        
        
        for (int i=0;i<IMAGE_SIZE;i++){
            for(int k=0;k<IMAGE_SIZE;k++){
                float valueSum = redrawnImagePixels[i][k];
                System.out.println(valueSum);
                
                Color color = new Color(valueSum/topValue,valueSum/topValue, valueSum/topValue);
                redrawnImage.setRGB(i, k, color.getRGB());
            }
        }
        return redrawnImage;
    } 
    
    public img loadImage(String path) {
        return new img(path);
    }
    
    public TomographSinogram(){
//        img image = this.loadImage("/Users/pawelszudrowicz/Downloads/box.png");
//        
//        int w = image.getWidth();
//        int r = w/2 - 5;
//        int h = image.getHeight();
//        float topLight = this.calculateTopLight(image);
//        
//        BufferedImage temp = this.scanObject(90, r, w, h, 180, 360, image, topLight);
//        BufferedImage sinogram = this.createSinogram();
//        BufferedImage redrawnImage = this.createObjectFromSinogram();
        
//        ImageIcon sinogramIcon = new ImageIcon(sinogram);
//        ImageIcon redrawnImageIcon = new ImageIcon(redrawnImage);
//        ImageIcon second = new ImageIcon(temp);
//        JLabel panel = new JLabel(sinogramIcon);
//        JLabel panelRedrawn = new JLabel(redrawnImageIcon);
//        JLabel panel2 = new JLabel(second);
//        
//        JPanel br = new JPanel();
//        JButton b1 = new JButton("- step");
//        JButton b2 = new JButton("+ step");
//        JButton b3 = new JButton("- detectors");
//        JButton b4 = new JButton("+ detectors");
//        JButton b5 = new JButton("- angle width");
//        JButton b6 = new JButton("+ angle width");
//        b1.setPreferredSize(new Dimension(150, 35));
//        b1.addActionListener(new ActionListener() { 
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                System.out.print("ASDASD");
//            }
//        } );
//        b2.setPreferredSize(new Dimension(150, 35));
//        b2.addActionListener(new ActionListener() { 
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                System.out.print("ASDASD");
//            }
//        } );
//        b3.setPreferredSize(new Dimension(150, 35));
//        b3.addActionListener(new ActionListener() { 
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                System.out.print("ASDASD");
//            }
//        } );
//        b4.setPreferredSize(new Dimension(150, 35));
//        b4.addActionListener(new ActionListener() { 
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                System.out.print("ASDASD");
//            }
//        } );
//        b5.setPreferredSize(new Dimension(150, 35));
//        b5.addActionListener(new ActionListener() { 
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                System.out.print("ASDASD");
//            }
//        } );
//        b6.setPreferredSize(new Dimension(150, 35));
//        b6.addActionListener(new ActionListener() { 
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                System.out.print("ASDASD");
//            }
//        } );
//        br.add(b1);
//        br.add(b2);
//        br.add(b3);
//        br.add(b4);
//        br.add(b5);
//        br.add(b6);
//        JFrame frame = new JFrame();
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.getContentPane().add(br, BorderLayout.BEFORE_FIRST_LINE);
//        frame.getContentPane().add(panel, BorderLayout.WEST);
//        frame.getContentPane().add(panelRedrawn, BorderLayout.CENTER);
//        frame.getContentPane().add(panel2, BorderLayout.EAST);
//        frame.pack();
//        frame.setVisible(true);
    }
    
    
    public final Point pointOnCircle(float r, float angleInDegrees,float w, float h) {
        double angle = angleInDegrees * Math.PI / 180;
        int myX = (int) ((int) (Math.cos(angle) * r) + w/2);
        int myY = (int) ((int) (Math.sin(angle) * r) + h/2);
        Point point = new Point();
        point.x = myX;
        point.y = myY;
        return point;
    }
    
   
    public void bresenhamLine(int x1, int y1, int x2, int y2, img image, int topLight, Graphics2D rect)
    { 

     int d, dx, dy, ai, bi, xi, yi;
     int x = x1, y = y1;
     float sum = 0;
     int counter = 0;
     // ustalenie kierunku rysowania
     if (x1 < x2)
     { 
         xi = 1;
         dx = x2 - x1;
     } 
     else
     { 
         xi = -1;
         dx = x1 - x2;
     }
     // ustalenie kierunku rysowania
     if (y1 < y2)
     { 
         yi = 1;
         dy = y2 - y1;
     } 
     else
     { 
         yi = -1;
         dy = y1 - y2;
     }

     sum += image.getPixel(x, y);
     counter++;
     rect.drawOval(x, y, 1, 1);
     
     // oś wiodąca OX
     if (dx > dy)
     {
         ai = (dy - dx) * 2;
         bi = dy * 2;
         d = bi - dx;
         // pętla po kolejnych x
         while (x != x2)
         { 
             // test współczynnika
             if (d >= 0)
             { 
                 x += xi;
                 y += yi;
                 d += ai;
             } 
             else
             {
                 d += bi;
                 x += xi;
             }
          sum += image.getPixel(x, y);
          rect.drawOval(x, y, 1, 1);
          counter++;
         }
     } 
     // oś wiodąca OY
     else
     { 
         ai = ( dx - dy ) * 2;
         bi = dx * 2;
         d = bi - dy;
         // pętla po kolejnych y
         while (y != y2)
         { 
             // test współczynnika
             if (d >= 0)
             { 
                 x += xi;
                 y += yi;
                 d += ai;
             }
             else
             {
                 d += bi;
                 y += yi;
             }
          sum += image.getPixel(x,y);
          rect.drawOval(x, y, 1, 1);
          counter++;
         }
     }
     sum = sum/(counter*topLight);
     values.add(sum);
     
    }
    
    
    
    
    
    
    public void bresenhamDrawLine(int x1, int y1, int x2, int y2,  float colorValue)
    { 

     int d, dx, dy, ai, bi, xi, yi;
     int x = x1, y = y1;
     
     // ustalenie kierunku rysowania
     if (x1 < x2)
     { 
         xi = 1;
         dx = x2 - x1;
     } 
     else
     { 
         xi = -1;
         dx = x1 - x2;
     }
     // ustalenie kierunku rysowania
     if (y1 < y2)
     { 
         yi = 1;
         dy = y2 - y1;
     } 
     else
     { 
         yi = -1;
         dy = y1 - y2;
     }
     
     redrawnImagePixels[x][y] += colorValue;
     
     
     // oś wiodąca OX
     if (dx > dy)
     {
         ai = (dy - dx) * 2;
         bi = dy * 2;
         d = bi - dx;
         // pętla po kolejnych x
         while (x != x2)
         { 
             // test współczynnika
             if (d >= 0)
             { 
                 x += xi;
                 y += yi;
                 d += ai;
             } 
             else
             {
                 d += bi;
                 x += xi;
             }
          redrawnImagePixels[x][y] += colorValue;
         }
     } 
     // oś wiodąca OY
     else
     { 
         ai = ( dx - dy ) * 2;
         bi = dx * 2;
         d = bi - dy;
         // pętla po kolejnych y
         while (y != y2)
         { 
             // test współczynnika
             if (d >= 0)
             { 
                 x += xi;
                 y += yi;
                 d += ai;
             }
             else
             {
                 d += bi;
                 y += yi;
             }
          redrawnImagePixels[x][y] += colorValue;
         }
     }
     
    }

}
