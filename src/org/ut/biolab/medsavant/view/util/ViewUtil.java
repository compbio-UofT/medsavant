/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.util;

import com.jidesoft.plaf.basic.ThemePainter;
import com.jidesoft.swing.JideButton;
import com.jidesoft.swing.JideSplitButton;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.text.NumberFormat;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 *
 * @author mfiume
 */
public class ViewUtil {

    public static JPanel createClearPanel() {
        return (JPanel) clear(new JPanel());
    }

    public static JComponent clear(JComponent c) {
        c.setOpaque(false);
        return c;
    }

    public static JButton createHyperLinkButton(String string) {
        JideButton b = new JideButton(string);
        b.setButtonStyle(JideButton.HYPERLINK_STYLE);
        return b;
    }

    public static JideSplitButton createJideSplitButton(String name) {
        final JideSplitButton button = new JideSplitButton(name);
        button.setForegroundOfState(ThemePainter.STATE_DEFAULT, Color.BLACK);
        //button.setIcon(icon);
        return button;
    }

    public static Border getTinyBorder() {
        return new EmptyBorder(1,1,1,1);
    }
    
    public static Border getSmallBorder() {
        return new EmptyBorder(3,3,3,3);
    }

    public static Border getMediumBorder() {
        return new EmptyBorder(5,5,5,5);
    }

    public static Border getBigBorder() {
        return new EmptyBorder(10,10,10,10);
    }
    
    public static Border getGiganticBorder() {
        return new EmptyBorder(100,100,100,100);
    }

    public static Font getBigTitleFont() {
        return new Font("Arial", Font.BOLD, 18);
    }

    public static Font getMediumTitleFont() {
        return new Font("Arial", Font.BOLD, 13);
    }

    public static Font getSmallTitleFont() {
        return new Font("Arial", Font.PLAIN, 9);
    }

    public static Color getDarkColor() {
        return new Color(20,20,20);
    }

    public static JPanel getBannerPanel() {
        JPanel p = new JPanel() {

            @Override
            public void paintComponent(Graphics g) {
                GradientPaint p = new GradientPaint(0,0,Color.white,0,40,Color.lightGray);
                ((Graphics2D)g).setPaint(p);
                g.fillRect(0, 0, this.getWidth(), this.getHeight());
            }
        };
        
        p.setBorder(ViewUtil.getSmallBorder());

        p.setLayout(new BoxLayout(p,BoxLayout.X_AXIS));

        return p;
    }

    public static Component getMediumSeparator() {
        return Box.createHorizontalStrut(10);
    }

    public static Component getSmallVerticalSeparator() {
        return Box.createVerticalStrut(2);
    }

    public static Border getTinyLineBorder() {
        return new LineBorder(Color.darkGray,1);
    }

    public static Color getLightColor() {
        return new Color(200,200,200);
    }

    public static Color getMidColor() {
        return new Color(60,60,60);
    }

    public static Color getMenuColor() {
        return new Color(217,222,229);
    }

    public static JPanel getDropDownPanel(String str, boolean isSelected, boolean cellHasFocus) {
        
        JPanel p;
        if (isSelected) {
            p = ViewUtil.getBannerPanel();
        } else {
            p = new JPanel();
            p.setLayout(new BoxLayout(p,BoxLayout.X_AXIS));
            p.setBorder(ViewUtil.getSmallBorder());
            p.setBackground(Color.white);
            //p.setBorder(ViewUtil.getTinyLineBorder());
        }
        JLabel l = new JLabel(str);
        l.setFont(new Font("Tahoma", Font.PLAIN, 14));
        l.setOpaque(false);
        p.add(l);
        return p;
    }
    
    public enum OS { Unknown, Windows, Linux, Mac };
    
    private static OS thisOS = OS.Unknown;
    
    public static OS getOS() {
        if (thisOS == OS.Unknown) {
            String osname = System.getProperty("os.name").toLowerCase();
            
            if (osname.contains("windows")) {
                thisOS = OS.Windows;
            } else if (osname.contains("mac")) {
                thisOS = OS.Mac;
            // todo: check that this actually works (may need to check variants - e.g. centos, ubuntu, etc.)
            } else if (osname.contains("linux")) {
                thisOS = OS.Linux;
            }
            
            System.out.println("OS=" + thisOS);
        }
        
        return thisOS;
    }
    
    public static boolean isMac() {
        return getOS() == OS.Mac;
    }

    public static String numToString(int num) {
        return NumberFormat.getInstance().format(num);
    }
    
    public static JToggleButton getMenuToggleButton(String title) { //, int num) {
        
        JToggleButton button = new JToggleButton(title);
        
        if (isMac()) {
            button.putClientProperty( "JButton.buttonType", "segmentedGradient" );
            button.putClientProperty( "JButton.segmentPosition", "middle" );
            
            /*if (num == -1) {
                button.putClientProperty( "JButton.segmentPosition", "first" );
            } else if (num == 1) {
                button.putClientProperty( "JButton.segmentPosition", "last" );
            } else {
                button.putClientProperty( "JButton.segmentPosition", "middle" );
            }*/
        }
        
        return button;
    }
}
