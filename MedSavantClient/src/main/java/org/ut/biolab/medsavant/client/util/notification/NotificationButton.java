/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.util.notification;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.view.ViewController;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 * The notification button that displays on the menu, and helps manage notifications.  
 * This should seldom be used directly. Instead, if you want to create a "Job", use
 * 'VisibileMedSavantWorker'.
 * 
 * @see VisibleMedSavantWorker
 */
public class NotificationButton extends JPanel {

    private static final String NO_NOTIFICATIONS_MESSAGE = "No Notifications";
    private String name;
    private JButton button;
    private JPopupMenu notificationMenu;
    private static final Log LOG = LogFactory.getLog(NotificationButton.class);
    private Set<JPanel> notificationViews = new HashSet<JPanel>();

    public NotificationButton(String name, ImageIcon icon) {
        this.name = name;
        ViewUtil.applyVerticalBoxLayout(this);
        setOpaque(false);
        button = ViewUtil.getIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.MENU_SERVER));

        this.notificationMenu = new JPopupMenu();
        this.notificationMenu.setBorder(null);

        //These listeners are a bit of a hack to fix a visual bug where clicking
        //the 'jobs' button partially erases the 'Notifications' button on Linux
        //when the popup is closed.
        this.notificationMenu.addPopupMenuListener(new PopupMenuListener() {
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

        ViewUtil.applyVerticalBoxLayout(notificationMenu);

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                showPopup();
            }
        });
        this.notificationMenu.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        refreshMenu();
    }

    private void showPopup() {
        if (this.isVisible()) {
            notificationMenu.show(this, 0, getPreferredSize().height);
        } else {
            notificationMenu.setVisible(false);
        }
    }

    private void refreshButton() {
        String s = name + " (" + notificationViews.size() + ")";
        this.removeAll();
        this.add(ViewUtil.subTextComponent(button, s));
        setVisible(notificationViews.size() > 0);
        revalidate();
        repaint();
    }

    private void refreshMenu() {
        notificationMenu.removeAll();
        if (notificationViews.isEmpty()) {
            notificationMenu.add(new JLabel(NO_NOTIFICATIONS_MESSAGE));
        } else {
            for (JPanel p : notificationViews) {
                notificationMenu.add(p);
            }

            notificationMenu.revalidate();
            notificationMenu.repaint();
        }
        refreshButton();
    }

    void addNotification(JPanel notificationView) {
        if (!notificationViews.contains(notificationView)) {
            if (notificationViews.isEmpty()) {
                notificationMenu.removeAll();
            }
            notificationMenu.add(notificationView);
            notificationViews.add(notificationView);
            refreshButton();
        }
    }

    void removeNotificationView(JPanel notificationView) {
        if (notificationViews.contains(notificationView)) {
            System.out.println("Removed notification, " + notificationViews.size() + " notifications remaining");
            notificationViews.remove(notificationView);
            refreshMenu();
            notificationMenu.setVisible(false);
        }

    }
}
