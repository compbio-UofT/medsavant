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

import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.images.ImagePanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class BottomBar extends JPanel {

    private final JLabel statusLabel;
    private final JLabel loginStatusLabel;
    private final ImagePanel loginImagePanel;
    private final UpdatesPanel notificationPanel;
    private final NotificationsPanel analyticsJobsPanel;

    public BottomBar() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createCompoundBorder(ViewUtil.getEndzoneLineBorder(),ViewUtil.getSmallBorder()));
        setPreferredSize(new Dimension(25,25));

        loginStatusLabel = new JLabel();

        statusLabel = new JLabel("");
        statusLabel.setFont(ViewUtil.getMediumTitleFont());

        ImageIcon im = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.LOGGED_IN);

        loginImagePanel = new ImagePanel(im.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH),15,15);

        notificationPanel = new UpdatesPanel();
        analyticsJobsPanel = NotificationsPanel.getNotifyPanel(NotificationsPanel.JOBS_PANEL_NAME);//new Color(246,127,0));

        add(loginImagePanel);
        add(ViewUtil.getSmallSeparator());
        add(loginStatusLabel);
        add(ViewUtil.getLargeSeparator());
        add(notificationPanel);
        add(ViewUtil.getLargeSeparator());
        add(analyticsJobsPanel);
        add(Box.createHorizontalGlue());

        updateLoginStatus();
    }

    @Override
    public void paintComponent(Graphics g) {
        GradientPaint p = new GradientPaint(0, 0, Color.white, 0, 40, Color.lightGray);
        ((Graphics2D) g).setPaint(p);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
    }

    public final void updateLoginStatus() {
        if (LoginController.getInstance().isLoggedIn()) {

            loginStatusLabel.setText("Signed in as " + LoginController.getInstance().getUserName());
            loginStatusLabel.setToolTipText("Signed in since: " + new SimpleDateFormat().format((new Date())));
            loginImagePanel.setVisible(true);

        } else {
            loginStatusLabel.setText("Not signed in");
            loginStatusLabel.setToolTipText(null);
             loginImagePanel.setVisible(false);
        }
    }
}
