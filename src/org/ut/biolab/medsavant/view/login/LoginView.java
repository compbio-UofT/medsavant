/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.login;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class LoginView extends JPanel {

    public LoginView() {
        this.setBackground(ViewUtil.getDarkColor());
        initView();
    }

    private void initView() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(Box.createVerticalGlue());
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(Box.createHorizontalGlue());
        p.add(new LoginForm());
        p.add(Box.createHorizontalGlue());
        this.add(p);
        this.add(Box.createVerticalGlue());
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        
        g2.setColor(Color.white);
        g2.fillRect(0, 0, this.getWidth(), this.getHeight());
        
        //GradientPaint p = new GradientPaint(0, 0, Color.darkGray, 0, this.getHeight(), Color.black);
        //g2.setPaint(p);
        //g2.fillRect(0, 0, this.getWidth(), this.getHeight());
    }
}
