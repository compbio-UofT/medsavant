package org.ut.biolab.medsavant.client.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class NotificationsPanel extends JPanel {

    private final String name;
    private int notificationCount;
    private final JPopupMenu notifications;
    private final JPopupMenu noNotifications;
    private final HashSet<Integer> completedNotifications = new HashSet<Integer>();
    private final JButton button;

    public NotificationsPanel(String name) {
        this.name = name;

        ViewUtil.applyVerticalBoxLayout(this);

        setOpaque(false);
        button = ViewUtil.getIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.MENU_SERVER));

        this.notifications = new JPopupMenu();
        this.notifications.setBorder(null);

        this.noNotifications = new JPopupMenu();
        this.noNotifications.add(new JLabel("No notifications"));
        this.noNotifications.setBorder(ViewUtil.getMediumBorder());

        ViewUtil.applyVerticalBoxLayout(notifications);

        final NotificationsPanel instance = this;

        final JPanel thisInstance = this;

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JPopupMenu m;
                if (notificationCount == 0 && completedNotifications.isEmpty()) {
                    m = noNotifications;
                } else {
                    m = notifications;
                }
                m.show(instance, 0, thisInstance.getPreferredSize().height);
            }
        });

        setNotificationNumber(0);
    }

    public final void setNotificationNumber(int notificationNumber) {
        this.notificationCount = notificationNumber;
        String s = name + " (" + notificationNumber + ")";

        this.removeAll();
        this.add(ViewUtil.subTextComponent(button, s));
        setVisible(notificationCount != 0);
    }

    /*private void resize() {
        FontMetrics fm = countLabel.getFontMetrics(countLabel.getFont());
        int width = fm.stringWidth(countLabel.getText());

        System.err.println("resize to " + width);

        Dimension thisDimension = new Dimension(width, this.getPreferredSize().height);
        ViewUtil.fixSize(this,thisDimension);

        Dimension labelDimension = new Dimension(width, countLabel.getPreferredSize().height);
        ViewUtil.fixSize(countLabel, labelDimension);
    }
    */

    static int notificationID = 0;
    static Map<Integer, JPanel> notificationIDToPanelMap = new HashMap<Integer, JPanel>();

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
