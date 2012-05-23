/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import org.ut.biolab.medsavant.login.LoginView;

/**
 *
 * @author mfiume
 */
public class PaintUtil {

    private static final Color skyColor = new Color(200,235,254);

    public static void paintSky(Graphics g, Component c) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.white);
        g2.fillRect(0, 0, c.getWidth(), c.getHeight());


        GradientPaint p = new GradientPaint(0, c.getHeight()-200, Color.white/*new Color(97,135,172)*/, 0, c.getHeight(),
                new Color(92,168,229));
        //GradientPaint p = new GradientPaint(0, 200, Color.white, 0, c.getHeight(),
        //        skyColor);
        g2.setPaint(p);
        g2.fillRect(0, 0, c.getWidth(), c.getHeight());
    }


    public static void paintMeBackground(Graphics g, Component c) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.white);
        g2.fillRect(0, 0, c.getWidth(), c.getHeight());


        GradientPaint p = new GradientPaint(0, 0, Color.darkGray, 0, c.getHeight(),
                Color.black);
        //GradientPaint p = new GradientPaint(0, 200, Color.white, 0, c.getHeight(),
        //        skyColor);
        g2.setPaint(p);
        g2.fillRect(0, 0, c.getWidth(), c.getHeight());
    }

    public static void paintDarkMenu(Graphics g, Component c) {
        paintEvenGradient(g,c,Color.black,Color.darkGray);
    }

    private static void paintEvenGradient(Graphics g, Component c, Color bottom, Color top) {
        Graphics2D g2 = (Graphics2D) g;
        GradientPaint p = new GradientPaint(0, 0, top, 0, c.getHeight(),
                bottom);
        g2.setPaint(p);
        g2.fillRect(0, 0, c.getWidth(), c.getHeight());
    }



    public static void paintLightMenu(Graphics g, Component c) {
        paintEvenGradient(g,c,Color.lightGray,Color.white);
    }

    public static void paintFlexedMediumMenu(Graphics g, Component c) {
        paintEvenGradient(g,c,Color.white,Color.lightGray);
    }

     public static void paintSelectedMenu(Graphics g, Component c) {
        paintEvenGradient(g,c,Color.gray,Color.white);
    }

    public static void paintSolid(Graphics g, Component c, Color color) {
        g.setColor(color);
        g.fillRect(0, 0, c.getWidth(), c.getHeight());
    }
}
