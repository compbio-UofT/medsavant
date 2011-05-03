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
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
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

    public static Border getMediumBorder() {
        return new EmptyBorder(5,5,5,5);
    }

    public static Border getBigBorder() {
        return new EmptyBorder(10,10,10,10);
    }

    public static Font getBigTitleFont() {
        return new Font("Arial", Font.BOLD, 18);
    }

    public static Font getMediumTitleFont() {
        return new Font("Arial", Font.BOLD, 13);
    }

    public static Font getSmallTitleFont() {
        return new Font("Arial", Font.PLAIN, 11);
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

        p.setLayout(new BoxLayout(p,BoxLayout.X_AXIS));

        return p;
    }

    public static Component getMediumSeparator() {
        return Box.createHorizontalStrut(10);
    }

    public static Border getTinyLineBorder() {
        return new LineBorder(Color.darkGray,1);
    }

    public static Color getMidColor() {
        return new Color(60,60,60);
    }

}
