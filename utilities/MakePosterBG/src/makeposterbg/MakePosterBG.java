/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package makeposterbg;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author mfiume
 */
public class MakePosterBG {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        int width = 1200;
        int height = 900;

        JFrame f = new JFrame();
        JPanel p = new JPanel();
        p.setPreferredSize(new Dimension(width, height));
        f.add(p);
        p.setBackground(Color.white);

        p.setLayout(new FlowLayout());

        generateTwo(p);

        f.pack();
        f.setVisible(true);

        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.createGraphics();
        p.paint(g);  //this == JComponent
        g.dispose();
        try {
            ImageIO.write(bi, "png", new File("test.png"));
        } catch (Exception e) {
        }
    }

    public static void generateTwo(JPanel p) {
        int counter = 0;
        int ringDiameter = 30;

        Random generator = new Random();

        double max = (1200/ringDiameter)*(900/ringDiameter);


        while (counter++ != max) {


            double hit = generator.nextDouble()/2;

            RingChart ringChart = new RingChart();
            ringChart.setMinimumSize(new Dimension(ringDiameter, ringDiameter));
            ringChart.setMaximumSize(new Dimension(ringDiameter, ringDiameter));
            ringChart.setPreferredSize(new Dimension(ringDiameter, ringDiameter));
            Ring r1 = new Ring();


            r1.addItem("1", hit, new Color(72, 181, 249));
            r1.addItem("2", 1 - hit, Color.gray);
            ringChart.setRings(Arrays.asList(r1));
            p.add(ringChart);

        }
    }

    public static void generateOne(JPanel p) {
        double progress = 1.0;
        int counter = 0;
        int ringDiameter = 200;

        Random generator = new Random();

        double max = 20;


        while (counter++ != max) {

            if (progress < 0) {
                progress = 0;
            }

            double hit = generator.nextDouble();
            hit = hit / (max / 2);

            if (counter == max) {
                progress = 0.02;
            }

            RingChart ringChart = new RingChart();
            ringChart.setMinimumSize(new Dimension(ringDiameter, ringDiameter));
            ringChart.setMaximumSize(new Dimension(ringDiameter, ringDiameter));
            ringChart.setPreferredSize(new Dimension(ringDiameter, ringDiameter));
            Ring r1 = new Ring();


            r1.addItem("1", progress, new Color(72, 181, 249));
            r1.addItem("2", 1 - progress, Color.gray);
            ringChart.setRings(Arrays.asList(r1));
            p.add(ringChart);

            progress -= hit;
        }
    }
}
