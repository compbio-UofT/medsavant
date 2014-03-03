/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import org.apache.commons.lang3.ArrayUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.shared.model.Notification;
import org.ut.biolab.medsavant.shared.model.ProjectDetails;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.util.PeriodicChecker;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author Andrew
 */
public class UpdatesPanel extends JPanel {

    private static final Log LOG = LogFactory.getLog(UpdatesPanel.class);
    private static final int UPDATE_INTERVAL = 1000 * 60; //one minute
    private static final Color ALERT_COLOUR = new Color(200, 0, 0);
    private static final int ICON_WIDTH = 17;
    //private static final Dimension MENU_ICON_SIZE = new Dimension(260, 90);
    private static final Dimension MENU_ICON_SIZE = new Dimension(290, 90);
    private Notification[] notifications;
    private JPopupMenu popup;
    //private final JLabel numNotifications;
    private final JPanel buttonContainer;
    private static boolean serverChecks = true;

    public UpdatesPanel() {
        //setOpaque(false);
        //setPreferredSize(new Dimension(120, 20));
        //setBackground(Color.GREEN);
        //setVisible(false);
        setOpaque(false);

        ViewUtil.applyVerticalBoxLayout(this);
        final JButton button = ViewUtil.getIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.MENU_NOTIFY));

        //numNotifications = new JLabel("0");
        buttonContainer = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(buttonContainer);
        buttonContainer.add(button);
        //buttonContainer.add(ViewUtil.getSmallSeparator());
        //buttonContainer.add(ViewUtil.centerVertically(numNotifications));

        //setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                showPopup(0);
            }
        });

        button.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent me) {
                super.mouseEntered(me);
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent me) {
                super.mouseExited(me);
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }

        });

        new PeriodicChecker(UPDATE_INTERVAL) {
            @Override
            public void actionPerformed(ActionEvent ae) {
                update();
                if (!serverChecks) {
                    stop();
                }
            }
        };
        setNotifications(new Notification[0]);
    }

    public void update() {
        if (LoginController.getInstance().isLoggedIn()) {
            new MedSavantWorker<Notification[]>("Notifications") {
                @Override
                protected void showProgress(double fraction) {
                }

                @Override
                protected void showSuccess(Notification[] result) {
                    setNotifications(result);
                }

                @Override
                protected void showFailure(Throwable ex) {
                    if (serverChecks) {
                        LOG.error("Unable to get notifications.", ex);
                        setNotifications(null);
                    }
                }

                @Override
                protected Notification[] doInBackground() throws Exception {
                    if (serverChecks) {
                        return MedSavantClient.NotificationManager.getNotifications(LoginController.getSessionID(), LoginController.getInstance().getUserName());
                    }
                    return null;
                }
            }.execute();
        }
    }

    /**
     * Aborts checking the server for notifications from ALL update panels. Used
     * during the shutdown of MedSavant subsequent to auto-publishing (e.g.
     * variant import, removal, etc.)
     */
    public static void stopServerChecks() {
        serverChecks = false;
    }
    /*
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
     */

    private void showPopup(final int start) {
        popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        if (notifications == null) {
            popup.add(new NotificationIcon(null, null));
        } else {

            //add notifications
            for (int i = start; i < Math.min(start + 5, notifications.length); i++) {
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
            }
        }

        //int offset = -Math.min(5, notifications.length - start) * (MENU_ICON_SIZE.height + 2) -3 - (headerAdded ? 16 : 0);        
        popup.show(this, 0, this.getPreferredSize().height);
    }

    private JSeparator createSeparator() {
        JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
        sep.setMaximumSize(new Dimension(1000, 1));
        sep.setBackground(Color.WHITE);
        sep.setForeground(Color.LIGHT_GRAY);
        return sep;
    }

    private void removeNotification(Notification n) {
        if (notifications.length > 1) {
            ArrayUtils.removeElement(notifications, n);
            setNotifications(notifications);
        } else {
            setNotifications(null);
        }
    }

    private void setNotifications(Notification[] list) {
        notifications = list;
        refreshNotificationCount();
        //numNotifications.setText(notifications.length + "");
        setVisible(list != null && list.length > 0);
    }

    private void refreshNotificationCount() {
        removeAll();
        add(ViewUtil.subTextComponent(buttonContainer, "Notifications (" + (notifications == null ? 0 : notifications.length) + ")"));
    }

    class NotificationIcon extends JPanel {

        public NotificationIcon(final Notification n, final JPopupMenu p) {

            setPreferredSize(MENU_ICON_SIZE);
            setLayout(new BorderLayout());
            setBackground(Color.white);
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(new Color(240, 240, 240));
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

            String message = "<P>" + n.getMessage() + "</P>";

            switch (n.getType()) {
                case PUBLISH:
                    message = message + "<P><B>Click to publish or remove</B></P>";
                    addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseReleased(MouseEvent e) {

                            p.setVisible(false);

                            if (ProjectController.getInstance().promptToPublish((ProjectDetails) n.getData())) {
                                removeNotification(n);
                            }
                        }
                    });
                    break;
            }
            add(new JLabel("<HTML>" + message + "</HTML>"), BorderLayout.CENTER);

        }
    }
}
