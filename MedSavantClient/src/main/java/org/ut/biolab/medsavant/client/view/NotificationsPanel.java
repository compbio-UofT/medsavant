package org.ut.biolab.medsavant.client.view;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class NotificationsPanel extends JPanel {

    private static final Log LOG = LogFactory.getLog(NotificationsPanel.class);
    private final String name;
    private int notificationCount;
    private final JPopupMenu notifications;
    private final JPopupMenu noNotifications;
    private final HashSet<Integer> completedNotifications = new HashSet<Integer>();
    private final JButton button;
    static int notificationID = 0;
    static Map<Integer, JPanel> notificationIDToPanelMap = new HashMap<Integer, JPanel>();

    public NotificationsPanel(String name) {
        this.name = name;

        ViewUtil.applyVerticalBoxLayout(this);

        setOpaque(false);
        button = ViewUtil.getIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.MENU_SERVER));

        this.notifications = new JPopupMenu();
        this.notifications.setBorder(null);

        //These listeners are a bit of a hack to fix a visual bug where clicking
        //the 'jobs' button partially erases the 'Notifications' button on Linux
        //when the popup is closed.
        this.notifications.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuCanceled(PopupMenuEvent popupMenuEvent) {
                ViewController.getInstance().getMenu().repaint();
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent popupMenuEvent) {
                ViewController.getInstance().getMenu().repaint();
            }

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {
            }
        });

        this.noNotifications = new JPopupMenu();
        this.noNotifications.add(new JLabel("No notifications"));
        this.noNotifications.setBorder(ViewUtil.getMediumBorder());
        this.noNotifications.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuCanceled(PopupMenuEvent popupMenuEvent) {
                ViewController.getInstance().getMenu().repaint();
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent popupMenuEvent) {
                ViewController.getInstance().getMenu().repaint();
            }

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {
            }
        });

        ViewUtil.applyVerticalBoxLayout(notifications);

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                showPopup();
            }
        });

        setNotificationNumber(0);
    }

    private void showPopup() {        
        if (this.isVisible()) {
            JPopupMenu m;


            if (notificationCount == 0 && completedNotifications.isEmpty()) {
                m = noNotifications;
            } else {
                m = notifications;
            }
            LOG.debug("notificationCount = " + notificationCount + " completedNotifications: " + completedNotifications.size());            
            m.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

            m.show(this, 0, getPreferredSize().height);            
        } else {
            notifications.setVisible(false);
            noNotifications.setVisible(false);
        }
    }

    public final void setNotificationNumber(int notificationNumber) {
        this.notificationCount = notificationNumber;
        String s = name + " (" + notificationNumber + ")";

        this.removeAll();
        this.add(ViewUtil.subTextComponent(button, s));
        setVisible(notificationCount != 0);
        //showPopup();
    }

    public int addNotification(JPanel notification) {
        notificationID++;
        notificationIDToPanelMap.put(notificationID, notification);
        this.notifications.add(notification);
        setNotificationNumber(++this.notificationCount);
        return notificationID;
    }

    public void removeNotification(int notificationID) {
        this.notifications.remove(notificationIDToPanelMap.get(notificationID));
        notificationIDToPanelMap.remove(notificationID);
        markNotificationAsComplete(notificationID);
    }
    private static Map<String, NotificationsPanel> panels = new HashMap<String, NotificationsPanel>();
    public static final String JOBS_PANEL_NAME = "Jobs";

    public static NotificationsPanel getNotifyPanel(String name) {
        NotificationsPanel p;
        if (!panels.containsKey(name)) {
            p = new NotificationsPanel(name);
            panels.put(name, p);
        } else {
            p = panels.get(name);
        }
        return p;
    }

    public void markNotificationAsComplete(int notificationID) {
        if (!completedNotifications.contains(notificationID)) {
            setNotificationNumber(--this.notificationCount);
            completedNotifications.add(notificationID);
        }

    }
}
