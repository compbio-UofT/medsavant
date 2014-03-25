package org.ut.biolab.medsavant.client.view.dashboard;

import com.explodingpixels.macwidgets.MacButtonFactory;
import com.explodingpixels.macwidgets.MacPainterFactory;
import com.explodingpixels.macwidgets.MacWidgetFactory;
import com.explodingpixels.macwidgets.TriAreaComponent;
import com.explodingpixels.macwidgets.UnifiedToolBar;
import com.explodingpixels.painter.MacWidgetsPainter;
import java.awt.AlphaComposite;
import org.ut.biolab.medsavant.client.view.app.MenuFactory;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.component.NiceMenu;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.NavigationPanel;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.savant.analytics.savantanalytics.AnalyticsAgent;
import savant.util.swing.HyperlinkButton;
import org.apache.commons.httpclient.NameValuePair;
import org.ut.biolab.medsavant.client.view.component.StackableJPanelContainer;
import org.ut.biolab.medsavant.client.view.font.FontFactory;

/**
 *
 * @author mfiume
 */
public class Dashboard extends StackableJPanelContainer implements Listener<DashboardSection> {

    private static Log LOG = LogFactory.getLog(Dashboard.class);

    int appIconWidth = 128;

    private final ArrayList<DashboardSection> dashboardSections;
    private final JPanel dashLayer;
    private LaunchableApp previousApp;
    private final JPanel appLayer;

    //private NiceMenu appTopMenu;
    //private NiceMenu homeMenu;
    private final LimitedQueue<LaunchableApp> history;
    private final HashSet<LaunchableApp> appHistoryBlackList;

    private Image backgroundImage;
    private boolean transparentBackground = true;
    private TriAreaComponent homeToolbar;
    private TriAreaComponent appToolbar;

    public Dashboard() {

        history = new LimitedQueue<LaunchableApp>(11);
        appHistoryBlackList = new HashSet<LaunchableApp>();

        this.setDoubleBuffered(true);

        this.setBackground(Color.white);

        //cardLayout = new CardLayout();
        //this.setLayout(cardLayout);
        dashboardSections = new ArrayList<DashboardSection>();

        appLayer = new JPanel();
        appLayer.setBackground(Color.white);
        appLayer.setLayout(new BorderLayout());

        this.push(appLayer);

        dashLayer = new JPanel();/* {
         public void paintComponent(Graphics g) {

         if (backgroundImage != null) {

         //System.out.println("Drawing image for dash background");
         int width = this.getWidth();
         int height = this.getHeight();

         BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g2 = resizedImage.createGraphics();
         g2.drawImage(backgroundImage, 0, 0, width, height, null);

         //Image scaled = backgroundImage.getScaledInstance(this.getWidth(), this.getHeight(), Image.SCALE_SMOOTH);
         g.drawImage(resizedImage, 0, 0, null);

         g.setColor(new Color(255, 255, 255, 200));
         g.fillRect(0, 0, width, height);

         return;
         }

         if (transparentBackground) {

         int width = this.getWidth();
         int height = this.getHeight();

         g.setColor(new Color(255, 255, 255, 240));
         g.fillRect(0, 0, width, height);

         }
         }
         };*/

        dashLayer.setBackground(Color.white);

        this.push(dashLayer);

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
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });
    }

    public void setBackgroundImage(Image im) {
        this.backgroundImage = im;
        dashLayer.repaint();
    }

    public void addDashboardSection(DashboardSection s) {
        this.dashboardSections.add(s);
    }

    private void relayout() {

        dashLayer.removeAll();

        JPanel middlePane = ViewUtil.getClearPanel();

        int gapHorizontal = 30;
        int gapVertical = gapHorizontal;
        int topAndBottomInsets = 100;

        int widthOfContainer = this.getParent().getSize().width;

        int centralWidth = Math.min((int) Math.round(widthOfContainer * 0.75), 1200); // TODO: centralize width for other apps to use

        int numIconsPerRow = (centralWidth + gapHorizontal) / (appIconWidth + gapHorizontal);

        int leftInset = widthOfContainer / 2 - (numIconsPerRow * (appIconWidth + gapHorizontal)) / 2;
        int rightInset = 0;

        middlePane.setLayout(new MigLayout(String.format("gapy %d, insets %d %d %d %d", 0, topAndBottomInsets, leftInset, topAndBottomInsets, rightInset)));

        dashLayer.setOpaque(true);

        dashLayer.setLayout(new BorderLayout());

        for (DashboardSection s : this.dashboardSections) {

            if (!s.isEnabled()) {
                continue;
            }

            if (s.getApps().isEmpty()) {
                continue;
            }

            if (!s.getName().equals("Apps")) {
                JLabel l = ViewUtil.getSubtleHeaderLabel(s.getName().toUpperCase());
                middlePane.add(l, "wrap, center");
            }

            JPanel appPlaceholder = ViewUtil.getClearPanel();

            MigLayout layout = new MigLayout(String.format("gapx %d, gapy %d, wrap %d, insets 0", gapHorizontal, gapVertical, numIconsPerRow));
            appPlaceholder.setLayout(layout);
            for (final LaunchableApp launcher : s.getApps()) {
                try {
                    appPlaceholder.add(getRepresentationForLauncher(this, launcher, appIconWidth));
                } catch (Exception e) {
                    LOG.error("Error creating launcher for app " + launcher.toString(), e);
                    e.printStackTrace();
                }
            }
            middlePane.add(appPlaceholder, String.format("wrap, gapy 5 %d", gapVertical));
        }

        JScrollPane p = ViewUtil.getClearBorderlessScrollPane(middlePane);
        p.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        MenuFactory.generateMenu(); // initialize the Apps in the menus

        JPanel bottomDisclaimer = ViewUtil.getClearPanel();
        bottomDisclaimer.setLayout(new MigLayout("gapx 10, fillx, insets 8"));

        //JLabel copy = new JLabel("Developed at University of Toronto");
        //copy.setForeground(ViewUtil.getSubtleTitleColor());
        //bottomDisclaimer.add(copy);
        JComponent feedback = ViewUtil.createHyperlinkButton("Send Feedback", ViewUtil.getMedSavantBlueColor(), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                try {
                    URI uri = URI.create(MedSavantFrame.FEEDBACK_URI);
                    Desktop.getDesktop().mail(uri);
                } catch (Exception ex) {
                }
            }
        });
        bottomDisclaimer.add(feedback, "split, right");

        JComponent userguide = ViewUtil.createHyperlinkButton("User Guide", ViewUtil.getMedSavantBlueColor(), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                try {
                    URI uri = URI.create(MedSavantFrame.USERGUIDE_URI);
                    Desktop.getDesktop().browse(uri);
                } catch (Exception ex) {
                }
            }
        });
        bottomDisclaimer.add(userguide, "right");

        homeToolbar = getToolBar("Home");
        homeToolbar.addComponentToRight(MedSavantFrame.getInstance().getNotificationPanel().generateMenuButton());
        homeToolbar.addComponentToRight((JComponent) Box.createHorizontalStrut(5));
        
        dashLayer.add(homeToolbar.getComponent(), BorderLayout.NORTH);

        dashLayer.add(p, BorderLayout.CENTER);
        dashLayer.add(bottomDisclaimer, BorderLayout.SOUTH);
        dashLayer.updateUI();
    }

    public BufferedImage createBlurredImage(JPanel panel) {
        System.out.println("Blurring image");

        int w = panel.getWidth();
        int h = panel.getHeight();
        BufferedImage sourceImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sourceImage.createGraphics();
        panel.paint(g);

        return createBlurredImage(sourceImage);
    }

    public BufferedImage createBlurredImage(BufferedImage sourceImage) {

        // Create a buffered image from the source image with a format that's compatible with the screen
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();

        GraphicsDevice graphicsDevice = graphicsEnvironment.getDefaultScreenDevice();

        GraphicsConfiguration graphicsConfiguration = graphicsDevice.getDefaultConfiguration();

        // If the source image has no alpha info use Transparency.OPAQUE instead
        BufferedImage image = graphicsConfiguration.createCompatibleImage(sourceImage.getWidth(null), sourceImage.getHeight(null), Transparency.BITMASK);

        // Copy image to buffered image
        Graphics graphics = image.createGraphics();

        // Paint the image onto the buffered image
        graphics.drawImage(sourceImage, 0, 0, null);

        graphics.dispose();

        float[] matrix = {
            1 / 16f, 1 / 8f, 1 / 16f,
            1 / 8f, 1 / 4f, 1 / 8f,
            1 / 16f, 1 / 8f, 1 / 16f,};

        BufferedImageOp op = new ConvolveOp(new Kernel(3, 3, matrix));

        return op.filter(image, null);
    }

    public void goHome() {

        if (previousApp != null) {
            previousApp.viewWillUnload();
        }

        this.dashLayer.setVisible(true);

        if (previousApp != null) {
            previousApp.viewDidUnload();
        }

        previousApp = null;
    }

    public void launchApp(LaunchableApp app) {

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

        appToolbar = getToolBar(app.getName());
        appToolbar.addComponentToLeft((JComponent) Box.createHorizontalStrut(5));
        appToolbar.addComponentToLeft(getHomeButton());
        appToolbar.addComponentToRight(MedSavantFrame.getInstance().getNotificationPanel().generateMenuButton());
        appToolbar.addComponentToRight((JComponent) Box.createHorizontalStrut(5));

        appLayer.add(appToolbar.getComponent(), BorderLayout.NORTH);
        appLayer.add(p, BorderLayout.CENTER);

        this.dashLayer.setVisible(false);

        if (previousApp != null) {
            previousApp.viewDidUnload();
        }
        app.viewDidLoad();
        AnalyticsAgent.log(new NameValuePair("app-launched", app.getName()));
        previousApp = app;

        appLayer.updateUI();
    }

    public static JPanel getRepresentationForLauncher(String name, ImageIcon icon, int iconWidth, ActionListener actionListener) {
        return getRepresentationForLauncher(name, icon, iconWidth, actionListener, false);
    }

    public static JPanel getRepresentationForLauncher(String name, ImageIcon icon, int iconWidth, ActionListener actionListener, boolean disableButton) {
        JPanel p = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(p);

        JButton button = ViewUtil.getIconButton(new ImageIcon(getScaledInstance(icon.getImage(), iconWidth, iconWidth, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true)));

        button.addActionListener(actionListener);

        p.add(ViewUtil.centerHorizontally(button));
        p.add(Box.createVerticalStrut(3));

        if (iconWidth <= 64) {
            JLabel title = ViewUtil.getGrayLabel(name);
            title.setFont(new Font(ViewUtil.getDefaultFontFamily(), disableButton ? Font.BOLD : Font.PLAIN, 12));
            ViewUtil.ellipsizeLabel(title, iconWidth);
            p.add(ViewUtil.centerHorizontally(title));
            //button.setEnabled(!disableButton); // disable if selected

            //button.setToolTipText(name);
        } else {
            JLabel title = ViewUtil.getGrayLabel(name);
            ViewUtil.ellipsizeLabel(title, iconWidth);
            title.setFont(new Font(ViewUtil.getDefaultFontFamily(), disableButton ? Font.BOLD : Font.PLAIN, 15));
            p.add(ViewUtil.centerHorizontally(title));
        }

        return p;
    }

    public static JPanel getRepresentationForLauncher(final Dashboard d, final LaunchableApp launcher, int iconWidth) {

        return getRepresentationForLauncher(launcher.getName(), launcher.getIcon(), iconWidth, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                d.launchApp(launcher);
            }
        });
    }

    public static Image getScaledInstance(Image img,
            int targetWidth,
            int targetHeight,
            Object hint,
            boolean higherQuality) {
        int type = BufferedImage.TYPE_INT_ARGB;
        Image ret = img;
        int w, h;
        if (higherQuality) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = new ImageIcon(img).getIconWidth();
            h = new ImageIcon(img).getIconHeight();
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }

        do {
            if (higherQuality && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w != targetWidth || h != targetHeight);

        return ret;
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

    @Override
    public void handleEvent(DashboardSection event) {
        relayout();
    }

    private JComponent getHomeButton() {

        final ActionListener goHomeActionListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                goHome();
            }
        };

        
        JButton goHome = new JButton(IconFactory.getInstance().getIcon(IconFactory.ICON_ROOT + "home-20.png"));
        goHome.setFocusable(false);
        goHome.putClientProperty("JButton.buttonType", "textured");

        goHome.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                goHomeActionListener.actionPerformed(null);
            }

        });

        return goHome;
    }

    private JComponent getLogoutButton() {

        final ActionListener signOutActionListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                MedSavantFrame.getInstance().requestLogoutAndRestart();
            }
        };

        JButton signOut = new JButton(IconFactory.getInstance().getIcon(IconFactory.ICON_ROOT + "exit-20.png"));
        signOut.setFocusable(false);
        signOut.putClientProperty("JButton.buttonType", "textured");

        signOut.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                signOutActionListener.actionPerformed(null);
            }

        });

        return signOut;
    }

    private TriAreaComponent getToolBar(String name) {
                
        TriAreaComponent bar = new TriAreaComponent(10);
        bar.setBackgroundPainter(new MacWidgetsPainter<Component>() {

            @Override
            public void paint(Graphics2D gd, Component t, int i, int i1) {
                gd.setColor(new Color(221,221,221));
                gd.fillRect(0, 0, t.getWidth(), t.getHeight());
               
            }
        });
        bar.getComponent().setBorder(ViewUtil.getBottomLineBorder());
        JLabel title = MacWidgetFactory.makeEmphasizedLabel(new JLabel(name),new Color(64,64,64), new Color(64,64,64), new Color(230,230,230));
        title.setFont(title.getFont().deriveFont(Font.PLAIN, 20));
        //title.setFont(FontFactory.getMenuTitleFont());
        bar.addComponentToCenter(title);
        
        return bar;
    }

    private JComponent pushUp(JComponent c) {
        JPanel p = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(p);
        p.add(c);
        p.add(Box.createVerticalGlue());
        return p;
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
