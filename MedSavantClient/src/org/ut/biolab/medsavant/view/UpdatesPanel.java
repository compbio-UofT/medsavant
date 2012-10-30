/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import javax.swing.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.login.LoginEvent;
import org.ut.biolab.medsavant.model.Notification;
import org.ut.biolab.medsavant.model.ProjectDetails;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.util.PeriodicChecker;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author Andrew
 */
public class UpdatesPanel extends JComponent {

    private static final Log LOG = LogFactory.getLog(UpdatesPanel.class);
    private static final int UPDATE_INTERVAL = 1000 * 60; //one minute
    private static final Color ALERT_COLOUR = new Color(200,0,0);
    private static final int ICON_WIDTH = 17;
    private static final Dimension MENU_ICON_SIZE = new Dimension(260, 80);

    private Notification[] notifications;
    private JPopupMenu popup;

    public UpdatesPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(120, 20));
        //setBackground(Color.GREEN);
        setVisible(false);

        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showPopup(0);
            }
        });

        LoginController.getInstance().addListener(new PeriodicChecker(UPDATE_INTERVAL) {
            @Override
            public void actionPerformed(ActionEvent ae) {
                new MedSavantWorker<Notification[]>("Notifications") {

                    @Override
                    protected void showProgress(double fraction) {}

                    @Override
                    protected void showSuccess(Notification[] result) {
                        setNotifications(result);
                    }

                    @Override
                    protected void showFailure(Throwable ex) {
                        LOG.error("Unable to get notifications.", ex);
                        setNotifications(null);
                    }

                    @Override
                    protected Notification[] doInBackground() throws Exception {
                        return MedSavantClient.NotificationManager.getNotifications(LoginController.sessionId, LoginController.getInstance().getUserName());
                    }

                }.execute();
            }

            @Override
            public void handleEvent(LoginEvent evt) {
                super.handleEvent(evt);
                if (evt.getType() != LoginEvent.Type.LOGGED_IN) {
                    setNotifications(null);
                }
            }
        });
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(Color.BLACK);
        g2.drawString(notifications.length == 1 ? "Notification" : "Notifications", ICON_WIDTH + 3, ICON_WIDTH - 4);

        g2.setColor(ALERT_COLOUR);
        g2.fill(new Ellipse2D.Double(0.0, 0.0, ICON_WIDTH, ICON_WIDTH));

        g2.setColor(Color.WHITE);
        g2.setFont(getFont().deriveFont(Font.BOLD, 10.0f));
        ClientMiscUtils.drawCentred(g2, Integer.toString(notifications.length), new Rectangle2D.Double(0.0, 0.0, ICON_WIDTH, ICON_WIDTH));
    }

    private void showPopup(final int start) {
        popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        boolean headerAdded = false;
        if (notifications == null) {
            popup.add(new NotificationIcon(null, null));
        } else {

            //add notifications
            for (int i = start; i < Math.min(start+5, notifications.length); i++) {
                popup.add(new NotificationIcon(notifications[i], popup));
                if (i != Math.min(start + 5, notifications.length) - 1) {
                    popup.add(createSeparator());
                }
            }

            //add page header
            if (notifications.length > 5) {
                JPanel header = new JPanel();
                header.setMinimumSize(new Dimension(1, 15));
                header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
                if (start >= 5) {
                    JLabel prevButton = ViewUtil.createLabelButton("  Prev Page  ");
                    prevButton.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            showPopup(start - 5);
                        }
                    });
                    header.add(prevButton);
                }
                header.add(Box.createHorizontalGlue());
                if (start + 5 < notifications.length) {
                    JLabel nextButton = ViewUtil.createLabelButton("  Next Page  ");
                    nextButton.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            showPopup(start + 5);
                        }
                    });
                    header.add(nextButton);
                }
                popup.add(createSeparator());
                popup.add(header);
                headerAdded = true;
            }
        }

        int offset = -Math.min(5, notifications.length - start) * (MENU_ICON_SIZE.height + 2) -3 - (headerAdded ? 16 : 0);
        popup.show(this, 0, offset);
    }

    private JSeparator createSeparator() {
        JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
        sep.setMaximumSize(new Dimension(1000,1));
        sep.setBackground(Color.WHITE);
        sep.setForeground(Color.LIGHT_GRAY);
        return sep;
    }

    private void setNotifications(Notification[] list) {
        notifications = list;
        setVisible(list != null && list.length > 0);
    }

    class NotificationIcon extends JPanel {

        public NotificationIcon(final Notification n, final JPopupMenu p) {

            setPreferredSize(MENU_ICON_SIZE);
            setLayout(new BorderLayout());
            setBackground(Color.white);
            setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(new Color(240,240,240));
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(Color.white);
                }
            });

            if (n == null) {
                add(new JLabel("<HTML><P>You have no notifications</P></HTML>"), BorderLayout.CENTER);
                return;
            }

            add(new JLabel("<HTML><P>" + n.getMessage() + "</P></HTML>"), BorderLayout.CENTER);

            switch (n.getType()) {
                case PUBLISH:

                    JLabel publishButton = new JLabel("Click to publish or remove");
                    publishButton.setFont(publishButton.getFont().deriveFont(Font.BOLD));
                    add(publishButton, BorderLayout.SOUTH);
                    addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseReleased(MouseEvent e) {

                            p.setVisible(false);

                            //get db lock
                            try {
                                if (MedSavantClient.SettingsManager.getDBLock(LoginController.sessionId)) {
                                    try {
                                        ProjectController.getInstance().promptToPublish((ProjectDetails)n.getData());
                                    } finally {
                                        try {
                                            MedSavantClient.SettingsManager.releaseDBLock(LoginController.sessionId);
                                        } catch (Exception ex1) {
                                            LOG.error("Error releasing database lock.", ex1);
                                        }
                                    }
                                } else {
                                    DialogUtils.displayMessage("Cannot make changes", "Another user is making changes to the database. You must wait until this user has finished. ");
                                }
                            } catch (Exception ex) {
                                ClientMiscUtils.reportError("Error getting database lock: %s", ex);
                            }
                        }
                    });
                    break;
            }
        }
    }
}
