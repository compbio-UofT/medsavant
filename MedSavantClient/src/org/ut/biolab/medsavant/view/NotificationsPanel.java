package org.ut.biolab.medsavant.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class NotificationsPanel extends JPanel {

    private final Color color;
    private final String name;
    private int notificationNumber;
    private static final int width = 17;
    private static final int height = width;
    private final JPopupMenu notifications;
    private final JPopupMenu noNotifications;

    public NotificationsPanel(String name, Color c) {
        this.name = name;
        this.color = c;
        this.setPreferredSize(new Dimension(width, height));

        this.notifications = new JPopupMenu();
        this.notifications.setBorder(null);

        this.noNotifications = new JPopupMenu();
        this.noNotifications.add(new JLabel("No notifications"));
        this.noNotifications.setBorder(ViewUtil.getMediumBorder());

        ViewUtil.applyVerticalBoxLayout(notifications);

        final NotificationsPanel instance = this;

        this.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent me) {
                JPopupMenu m;
                if (notificationNumber == 0) {
                    m = noNotifications;
                } else {
                    m = notifications;
                }
                m.show(instance, 0, -m.getHeight());
                m.show(instance, 0, -m.getHeight());
            }

            @Override
            public void mousePressed(MouseEvent me) {
            }

            @Override
            public void mouseReleased(MouseEvent me) {
            }

            @Override
            public void mouseEntered(MouseEvent me) {
            }

            @Override
            public void mouseExited(MouseEvent me) {
            }

        });
    }

    public void setNotificationNumber(int notificationNumber) {
        this.notificationNumber = notificationNumber;
        this.repaint();
    }
    private static Font f = new Font("Arial", Font.BOLD, 11);
    private FontMetrics fm;
    int fh, ascent;

    @Override
    public void paintComponent(Graphics g) {
        if (notificationNumber == 0) {
            g.setColor(Color.lightGray);
        } else {
            g.setColor(color);
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.fillRoundRect(0, 0, width, height, 5, 5);
        g.setColor(Color.white);
        g.setFont(f);
        if (fm == null) {
            fm = g.getFontMetrics();
            ascent = fm.getAscent();
            fh = ascent + fm.getDescent();
        }
        String str = ViewUtil.numToString(notificationNumber);
        g.drawString(str, (width / 2) - fm.stringWidth(str) / 2, height / 2 + ascent / 2);
    }

    public void addNotification(JPanel notification) {
        this.notifications.add(notification);
        this.notificationNumber++;
        this.repaint();
    }

    public void removeNotification(JPanel notification) {
        this.notifications.remove(notification);
        this.notificationNumber--;
        this.repaint();
    }

    private static Map<String,NotificationsPanel> panels = new HashMap<String,NotificationsPanel>();
    public static final String JOBS_PANEL_NAME = "Jobs";

    public static NotificationsPanel getNotifyPanel(String name) {
        NotificationsPanel p;
        if (!panels.containsKey(name)) {
            p = new NotificationsPanel(name, new Color(82, 174, 221));
            panels.put(name, p);
        } else {
            p = panels.get(name);
        }
        return p;
    }
}
