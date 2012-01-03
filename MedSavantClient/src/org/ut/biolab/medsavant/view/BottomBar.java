/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.images.ImagePanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class BottomBar extends JPanel {
    private static BottomBar instance;

    public static BottomBar getInstance() {
        if (instance == null) {
            instance = new BottomBar();
        }

        return instance;
    }
    private final JLabel statusLabel;
    private final JLabel loginStatusLabel;
    private final ImagePanel loginImagePanel;
    //private final ServerStatusPanel serverStatusPanel;
    private final ImportStatusPanel importStatusPanel;

    public BottomBar() {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setBorder(BorderFactory.createCompoundBorder(ViewUtil.getEndzoneLineBorder(),ViewUtil.getSmallBorder()));
        this.setPreferredSize(new Dimension(25,25));

        loginStatusLabel = new JLabel();

        statusLabel = new JLabel("");
        statusLabel.setFont(ViewUtil.getMediumTitleFont());

        ImageIcon im = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.LOGGED_IN);

        loginImagePanel = new ImagePanel(im.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH),15,15);

        //serverStatusPanel = new ServerStatusPanel();
        importStatusPanel = new ImportStatusPanel();

        //this.add(new JSeparator(JSeparator.VERTICAL));
        //this.add(ViewUtil.getSmallSeparator());
        this.add(loginImagePanel);
        this.add(ViewUtil.getSmallSeparator());
        this.add(loginStatusLabel);
        this.add(ViewUtil.getLargeSeparator());
        //this.add(serverStatusPanel);        
        this.add(importStatusPanel);
        this.add(Box.createHorizontalGlue());
        //this.add(statusLabel);
        //this.add(Box.createHorizontalGlue());

        updateLoginStatus();
    }

    @Override
    public void paintComponent(Graphics g) {
        GradientPaint p = new GradientPaint(0, 0, Color.white, 0, 40, Color.lightGray);
        ((Graphics2D) g).setPaint(p);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
    }

    public void updateLoginStatus() {
        if (LoginController.isLoggedIn()) {

            this.loginStatusLabel.setText("Signed in as " + LoginController.getUsername());
            loginStatusLabel.setToolTipText("Signed in since: " + (new SimpleDateFormat()).format((new Date())));
            loginImagePanel.setVisible(true);

        } else {
            this.loginStatusLabel.setText("Not signed in");
            loginStatusLabel.setToolTipText(null);
             loginImagePanel.setVisible(false);
        }
    }

    private void setStatus(String status) {
        statusLabel.setText(status);
    }
}
