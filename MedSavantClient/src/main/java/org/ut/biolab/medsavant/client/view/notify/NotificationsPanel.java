/*
 * Copyright (C) 2014 University of Toronto, Computational Biology Lab.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.ut.biolab.medsavant.client.view.notify;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.view.component.ProgressWheel;
import org.ut.biolab.medsavant.client.view.images.ImagePanel;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 * A container for notifications that indicate a process' status
 * @author mfiume
 */
public class NotificationsPanel extends JPanel {

    private Log LOG = LogFactory.getLog(NotificationsPanel.class);
    
    int verticalOffset = 44;
    int inset = 10;
    int gap = 10;
    
    private final ArrayList<Notification> notifications;
    private final HashMap<Notification, NotificationPanel> map;
    private JPanel npanelContainer;

    public NotificationsPanel() {
        notifications = new ArrayList<Notification>();
        map = new HashMap<Notification, NotificationPanel>();
        initUI();
    }

    public void addNotification(Notification n) {
        notifications.add(n);
        NotificationPanel notificationPanel = new NotificationPanel(n);
        notificationPanel.setParent(this);
        map.put(n, notificationPanel);
        updateView();
    }

    public void removeNotification(Notification n) {
        notifications.remove(n);
        map.remove(n);
        updateView();
    }

    public static void main(String[] argv) {
        JFrame f = new JFrame();
        NotificationsPanel p = new NotificationsPanel();
        for (int i = 0; i < 4; i++) {
            Notification n = new Notification();
            n.setName("Hello World " + i);
            p.addNotification(n);
        }
        f.add(p);
        f.show();
    }

    double duration = 1.0;
    int travelDistance = 400;

    private void initUI() {
        this.setOpaque(false);
        
        this.setLayout(new BorderLayout());
        npanelContainer = ViewUtil.getClearPanel();
        
        npanelContainer.setLayout(new MigLayout(String.format("insets %d %d %d %d, gapy %d, fillx, alignx trailing, wrap, hidemode 3",this.verticalOffset+inset,inset,inset,inset,gap)));
        
        this.add(npanelContainer,BorderLayout.EAST);
    }

    private void updateView() {
        npanelContainer.removeAll();
        for (Notification n : notifications) {
            npanelContainer.add(map.get(n));
        }
        npanelContainer.updateUI();
    }


    private static class NotificationPanel extends JPanel implements Listener<Notification> {

        int height = 70;
        int leftWidth = 65;
        int middleWidth = 140;
        int rightWidth = 55;
        
        int insets = 3;
        int innerinsets = 2;
        
        //int width = 280;
        
        private final Notification notification;
        private JLabel nameLabel;
        private JLabel subTextLabel;
        private JButton closeButton;
        private NotificationsPanel parentPanel;
        private JProgressBar progress;
        
        private Color subTextErrorColor = Color.red;
        private Color subTextNormalColor; // a light gray, set later
        private ActionListener closeActionListener;
        private ProgressWheel progressIndifinite;
        private JButton actionButton;

        public NotificationPanel(Notification n) {

            this.notification = n;
            n.addListener(this);

            initUI();
            refreshUI();
        }
        
        void setParent(NotificationsPanel parent) {
            this.parentPanel = parent;
        }

        @Override
        public void handleEvent(Notification event) {
            refreshUI();
        }

        private void refreshUI() {
            
            if (notification.isClosed()) {
                closeActionListener.actionPerformed(null);
            }
            
            setVisible(!notification.isHidden());
            
            nameLabel.setText(notification.getName());
            
            // set the subtext color to red if the description contains the word "error"
            subTextLabel.setText(notification.getDescription());
            subTextLabel.setForeground(notification.getDescription().toLowerCase().contains("error") ? subTextErrorColor : subTextNormalColor);
            
            closeButton.setVisible(notification.canHide());
            
            ViewUtil.ellipsizeLabel(nameLabel, middleWidth-2*innerinsets);
            ViewUtil.ellipsizeLabel(subTextLabel, middleWidth-2*innerinsets);
            
            if (notification.isShowsProgress()) {
                progressIndifinite.setVisible(notification.isIndeterminateProgress());
                progress.setVisible(!notification.isIndeterminateProgress());
                progress.setValue((int) (notification.getProgress()*100));
            } else {
                progressIndifinite.setVisible(false);
                progress.setVisible(false);
            }
            
            if (notification.getAction() != null) {
                actionButton.removeActionListener(notification.getAction());
                actionButton.addActionListener(notification.getAction());
                actionButton.setText(ViewUtil.ellipsize(notification.getActionName(), rightWidth-innerinsets-actionButton.getInsets().left-actionButton.getInsets().right));
                actionButton.setVisible(true);
            }
            
            this.updateUI();
        }

        private void initUI() {
            this.setOpaque(false);
            JPanel p = ViewUtil.getSemiTransparentPanel(Color.white, 0.95f, 10, new Color(230,230,230));
            ViewUtil.consumeMouseEventsForComponent(p);
            p.setLayout(new MigLayout(String.format("fillx, insets %d, height %d",insets, height)));

            this.setLayout(new BorderLayout());
            this.add(p,BorderLayout.CENTER);

            JPanel leftSide = ViewUtil.getClearPanel();
            JPanel middle = ViewUtil.getClearPanel();
            JPanel rightSide = ViewUtil.getClearPanel();
            
            leftSide.setLayout(new MigLayout(String.format("width %d, insets %d, hidemode 3", leftWidth, innerinsets)));
            middle.setLayout(new MigLayout(String.format("width %d, insets %d, alignx left, fillx, wrap, gapy 2", middleWidth, innerinsets)));
            rightSide.setLayout(new MigLayout(String.format("width %d, insets %d, alignx left, wrap, hidemode 3, wrap 1, gapy 2", rightWidth, innerinsets)));
            
            p.add(leftSide);
            p.add(middle);
            p.add(rightSide);
           
            nameLabel = ViewUtil.getSettingsHeaderLabel("");
            subTextLabel = ViewUtil.getSettingsHelpLabel("");
            
            closeActionListener = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (parentPanel != null) {
                        parentPanel.removeNotification(notification);
                    }
                }
                
            };
            
            closeButton = ViewUtil.getTexturedButton("Hide");
            closeButton.addActionListener(closeActionListener);
            
            actionButton = ViewUtil.getTexturedButton("");
            actionButton.setVisible(false);
            
            progress = (JProgressBar) ViewUtil.makeMini(new JProgressBar());
            progressIndifinite = ViewUtil.getIndeterminateProgressBar();
                    
            middle.add(nameLabel, "growx 1.0");
            middle.add(subTextLabel);
            middle.add(progress,"width 100%, hidemode 3");
            middle.add(progressIndifinite,"hidemode 3");
            
            subTextNormalColor = subTextLabel.getForeground();
            
            progress.setVisible(false);
            progressIndifinite.setVisible(false);
            
            rightSide.add(closeButton);
            rightSide.add(actionButton);
            
            if (notification.getIcon() != null) {
                leftSide.removeAll();
                int dim = Math.min(height-2*insets-2*innerinsets, leftWidth-2*innerinsets);
                leftSide.add(new ImagePanel(notification.getIcon().getImage(),dim,dim));
            }
        }
    }
}
