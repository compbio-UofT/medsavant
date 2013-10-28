/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view;

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
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.images.ImagePanel;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 * 
 * Deprecated 11/07/2013.  This is never instantiated.
 */
@Deprecated
public class BottomBar extends JPanel {

    private final JLabel statusLabel;
   // private final JLabel loginStatusLabel;
   // private final ImagePanel loginImagePanel;
    private final UpdatesPanel notificationPanel;
    private final NotificationsPanel analyticsJobsPanel;

    public BottomBar() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createCompoundBorder(ViewUtil.getEndzoneLineBorder(),ViewUtil.getSmallBorder()));
        //setPreferredSize(new Dimension(25,25));

        //loginStatusLabel = new JLabel();

        statusLabel = new JLabel("");
        statusLabel.setFont(ViewUtil.getMediumTitleFont());

        //ImageIcon im = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.LOGGED_IN);

        //loginImagePanel = new ImagePanel(im.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH),15,15);

        notificationPanel = new UpdatesPanel();
        analyticsJobsPanel = NotificationsPanel.getNotifyPanel(NotificationsPanel.JOBS_PANEL_NAME);//new Color(246,127,0));

        //add(loginImagePanel);
        //add(ViewUtil.getSmallSeparator());
        //add(loginStatusLabel);
        add(ViewUtil.getLargeSeparator());
        add(notificationPanel);
        add(ViewUtil.getLargeSeparator());
        add(analyticsJobsPanel);
        add(Box.createHorizontalGlue());

        /*JPanel report = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(report);
        report.add(new JLabel("Found a bug?"));
        report.add(new JButton("Report it"));
        add(report);
        */

        //updateLoginStatus();
    }

    @Override
    public void paintComponent(Graphics g) {
        GradientPaint p = new GradientPaint(0, 0, Color.white, 0, 40, Color.lightGray);
        ((Graphics2D) g).setPaint(p);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
    }

    /*public final void updateLoginStatus() {
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
    */
}
