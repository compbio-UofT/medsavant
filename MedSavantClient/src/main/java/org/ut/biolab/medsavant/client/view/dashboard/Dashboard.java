package org.ut.biolab.medsavant.client.view.dashboard;

import org.ut.biolab.medsavant.client.view.app.MenuFactory;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.PopupMenu;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import net.miginfocom.swing.MigLayout;
import org.javadev.AnimatingCardLayout;
import org.javadev.effects.Animation;
import org.javadev.effects.CubeAnimation;
import org.javadev.effects.DashboardAnimation;
import org.javadev.effects.FadeAnimation;
import org.javadev.effects.RadialAnimation;
import org.javadev.effects.SlideAnimation;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.app.AccountManagerApp;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.client.view.util.WrapLayout;

/**
 *
 * @author mfiume
 */
public class Dashboard extends JPanel {

    int appIconWidth = 128;

    private final ArrayList<DashboardSection> dashboardSections;
    private final JPanel baseLayer;
    private LaunchableApp previousApp;
    private final JPanel appLayer;

    private final String BASE_LAYER = "0";
    private final String APP_LAYER = "1";
    private final AnimatingCardLayout cardLayout;

    private TopMenu appTopMenu;
    private TopMenu homeMenu;
    private final LimitedQueue<LaunchableApp> history;
    private final HashSet<LaunchableApp> appHistoryBlackList;

    public Dashboard() {

        history = new LimitedQueue<LaunchableApp>(5);
        appHistoryBlackList = new HashSet<LaunchableApp>();

        this.setDoubleBuffered(true);

        this.setBackground(Color.white);

        //Animation anim = new SlideAnimation();
        //anim.setAnimationDuration(100);
        cardLayout = new AnimatingCardLayout();

        this.setLayout(cardLayout);

        dashboardSections = new ArrayList<DashboardSection>();

        baseLayer = new JPanel();
        baseLayer.setBackground(Color.white);

        this.add(baseLayer, BASE_LAYER);

        appLayer = new JPanel();
        appLayer.setBackground(Color.white);
        appLayer.setLayout(new BorderLayout());

        this.add(appLayer, APP_LAYER);

        cardLayout.show(this, BASE_LAYER);

        this.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                relayout();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
                //relayout();
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }

        });

    }

    public void addDashboardSection(DashboardSection s) {
        this.dashboardSections.add(s);
        //this.relayout();
    }

    private void relayout() {

        baseLayer.removeAll();

        JPanel middlePane = ViewUtil.getClearPanel();

        int gapHorizontal = 30;
        int gapVertical = gapHorizontal;
        int topAndBottomInsets = 100;

        int widthOfContainer = this.getParent().getSize().width;

        int centralWidth = Math.min(widthOfContainer / 2, 900);

        int numIconsPerRow = (centralWidth + gapHorizontal) / (appIconWidth + gapHorizontal);
        //System.out.println("Num icons " + numIconsPerRow + " width " + widthOfContainer + " left " + leftInset);

        int leftInset = widthOfContainer / 2 - (numIconsPerRow * (appIconWidth + gapHorizontal)) / 2;
        int rightInset = 0;//centralWidth/2;

        middlePane.setLayout(new MigLayout(String.format("gapy %d, insets %d %d %d %d", 0, topAndBottomInsets, leftInset, topAndBottomInsets, rightInset)));
        //"gapy 30, insets 100 0 100 0"));

        baseLayer.setOpaque(true);

        baseLayer.setLayout(new BorderLayout());

        //if (this.getParent() != null) {
        //    System.out.println("Width of parent is " + this.getParent().getSize().width);
        //} else {
        //    System.out.println("Parent is null");
        //}
        for (DashboardSection s : this.dashboardSections) {

            if (s.getApps().isEmpty()) {
                continue;
            }

            if (!s.getName().equals("Apps")) {
                JLabel l = new JLabel(s.getName().toUpperCase());
                l.setForeground(new Color(124,124,124));
                l.setFont(ViewUtil.getSmallTitleFont());
                middlePane.add(l, "wrap, center");
            }

            JPanel appPlaceholder = ViewUtil.getClearPanel();

            MigLayout layout = new MigLayout(String.format("gapx %d, gapy %d, wrap %d, insets 0", gapHorizontal, gapVertical, numIconsPerRow));
            appPlaceholder.setLayout(layout);
            for (DashboardApp launcher : s.getApps()) {
                appPlaceholder.add(getRepresentationForLauncher(launcher));
            }
            middlePane.add(appPlaceholder, String.format("wrap, gapy 5 %d",gapVertical));
        }

        JScrollPane p = ViewUtil.getClearBorderlessScrollPane(middlePane);
        p.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        MenuFactory.generateMenu(); // initialize the Apps in the menus

        final JButton menu = ViewUtil.getIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.BTN_MENU));
        menu.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JPopupMenu m = MenuFactory.generateMenu();
                m.show(menu, 0, 55);
            }

        });

        homeMenu = new TopMenu();
        homeMenu.addRightComponent(menu);

        baseLayer.add(homeMenu, BorderLayout.NORTH);

        baseLayer.add(p, BorderLayout.CENTER);
        baseLayer.updateUI();
    }

    public void goHome() {
        //System.out.println("Going home");
        //MedSavantFrame.getInstance().setTitle("MedSavant");

        if (previousApp != null) {
            previousApp.viewWillUnload();
        }

        cardLayout.show(this, BASE_LAYER);

        if (previousApp != null) {
            previousApp.viewDidUnload();
        }

        previousApp = null;
    }

    public void launchApp(LaunchableApp app) {

        System.out.println("Launching app " + app.getName());

        if (history.contains(app)) {
            history.remove(app);
        }
        if (!appHistoryBlackList.contains(app)) {
            history.add(app);
        }

        if (previousApp != null) {
            previousApp.viewWillUnload();
        }
        app.viewWillLoad();
        appLayer.removeAll();

        JPanel p = app.getView();

        JButton home = ViewUtil.getIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.BTN_UPARROW));
        home.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                goHome();
            }
        });

        final JButton menu = ViewUtil.getIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.BTN_MENU));

        menu.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JPopupMenu m = MenuFactory.generateMenu();
                m.show(menu, 0, 55);
            }

        });

        appTopMenu = new TopMenu();
        appTopMenu.addLeftComponent(home);
        appTopMenu.setTitle(app.getName());
        appTopMenu.addRightComponent(menu);

        appLayer.add(appTopMenu, BorderLayout.NORTH);
        appLayer.add(p, BorderLayout.CENTER);
        cardLayout.show(this, APP_LAYER);

        if (previousApp != null) {
            previousApp.viewDidUnload();
        }
        app.viewDidLoad();
        previousApp = app;

        appLayer.updateUI();
    }

    private JPanel getRepresentationForLauncher(final DashboardApp launcher) {
        JPanel p = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(p);

        JButton button = ViewUtil.getIconButton(resizeIconTo(launcher.getIcon(), appIconWidth));
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                launchApp(launcher);
            }
        });

        p.add(ViewUtil.centerHorizontally(button));
        p.add(Box.createVerticalStrut(3));

        JLabel title = ViewUtil.getGrayLabel(launcher.getName());
        p.add(ViewUtil.centerHorizontally(title));
        return p;
    }

    private ImageIcon resizeIconTo(ImageIcon icon, int itemSize) {
        Image img = icon.getImage();
        Image newimg = img.getScaledInstance(itemSize, itemSize, java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(newimg);
    }

    public List<LaunchableApp> getLaunchHistory() {
        List<LaunchableApp> list = new ArrayList<LaunchableApp>(history.size());
        for (LaunchableApp e : history) {
            list.add(0, e);
        }
        return list;
    }

    public LaunchableApp getCurrentApp() {
        return this.previousApp;
    }

    public void blackListAppFromHistory(LaunchableApp app) {
        appHistoryBlackList.add(app);
    }

    private class LimitedQueue<E> extends LinkedList<E> {

        private int limit;

        public LimitedQueue(int limit) {
            this.limit = limit;
        }

        @Override
        public boolean add(E o) {
            super.add(o);
            while (size() > limit) {
                super.remove();
            }
            return true;
        }

    }

}
