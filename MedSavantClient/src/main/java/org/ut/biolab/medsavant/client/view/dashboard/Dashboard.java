/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 * 
 * All rights reserved. No warranty, explicit or implicit, provided.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
 * FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.ut.biolab.medsavant.client.view.dashboard;

import com.explodingpixels.macwidgets.MacWidgetFactory;
import com.explodingpixels.macwidgets.TriAreaComponent;
import com.explodingpixels.painter.MacWidgetsPainter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import mfiume.component.transition.JTransitionPanel;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.savant.analytics.savantanalytics.AnalyticsAgent;
import org.apache.commons.httpclient.NameValuePair;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.view.component.StackableJPanelContainer;
import org.ut.biolab.medsavant.client.view.images.ImagePanel;
import org.ut.biolab.medsavant.shared.util.WebResources;

/**
 *
 * @author mfiume
 */
public class Dashboard extends StackableJPanelContainer implements Listener<DashboardSection> {

    private static Log LOG = LogFactory.getLog(Dashboard.class);

    int appIconWidth = 128;

    private final ArrayList<DashboardSection> dashboardSections;

    private final JTransitionPanel transitionCanvas;
    private final JPanel dashLayer;
    private LaunchableApp previousApp;
    private final JPanel appLayer;

    private final HashSet<LaunchableApp> appHistoryBlackList;

    private Image backgroundImage;
    private boolean transparentBackground = true;
    private TriAreaComponent homeToolbar;
    private TriAreaComponent appToolbar;
    private LaunchHistory history;

    public Dashboard() {

        history = new LaunchHistory();
        appHistoryBlackList = new HashSet<LaunchableApp>();

        this.setDoubleBuffered(true);

        this.setBackground(ViewUtil.getDefaultBackgroundColor());

        //cardLayout = new CardLayout();
        //this.setLayout(cardLayout);
        dashboardSections = new ArrayList<DashboardSection>();

        appLayer = new JPanel();
        appLayer.setBackground(this.getBackground());
        appLayer.setLayout(new BorderLayout());

        //this.push(appLayer);
        dashLayer = new JPanel() {
            public void paintComponent(Graphics g) {

                super.paintComponent(g);
                if (backgroundImage != null) {

                    int width = getWidth();
                    int height = getHeight();
                    int imageW = backgroundImage.getWidth(this);
                    int imageH = backgroundImage.getHeight(this);

                    // Tile the image to fill our area.  
                    for (int x = 0; x < width; x += imageW) {
                        for (int y = 0; y < height; y += imageH) {
                            g.drawImage(backgroundImage, x, y, this);
                        }
                    }
                    return;
                }
            }
        };

        dashLayer.setBackground(this.getBackground());

        //this.push(dashLayer);
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

        transitionCanvas = new JTransitionPanel();
        transitionCanvas.push(dashLayer, JTransitionPanel.TransitionType.NONE, null);
        this.push(transitionCanvas);
    }

    public void setBackground(Color c) {
        super.setBackground(c);
        if (dashLayer != null) {
            dashLayer.setBackground(c);
            dashLayer.repaint();
        }
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

        int topInset = 100;
        int bottomInsets = 20;

        int widthOfContainer = this.getParent().getSize().width;

        int centralWidth = Math.min((int) Math.round(widthOfContainer * 0.75), 1200); // TODO: centralize width for other apps to use

        int numIconsPerRow = (centralWidth + gapHorizontal) / (appIconWidth + gapHorizontal);

        int leftInset = widthOfContainer / 2 - (numIconsPerRow * (appIconWidth + gapHorizontal)) / 2;
        int rightInset = 0;

        middlePane.setLayout(new MigLayout(String.format("gapy %d, insets %d %d %d %d", 0, topInset, leftInset, bottomInsets, rightInset)));

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

        JPanel bottomDisclaimer = ViewUtil.getClearPanel();
        bottomDisclaimer.setLayout(new MigLayout("fillx, insets 2"));

        TriAreaComponent bottombar = new TriAreaComponent(10);
        bottombar.getComponent().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        ImagePanel logo = new ImagePanel("icon/logo/medsavant-icon-mini.png");

        bottombar.addComponentToLeft(logo);
       
        //JLabel copy = new JLabel("Developed at University of Toronto");
        //copy.setForeground(ViewUtil.getSubtleTitleColor());
        //bottombar.addComponentToLeft(copy);
        JComponent feedback = ViewUtil.createHyperlinkButton("Send Feedback", ViewUtil.getMedSavantBlueColor(), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                try {
//                //    URI uri = URI.create(MedSavantFrame.FEEDBACK_URI);
                    Desktop.getDesktop().mail(WebResources.FEEDBACK_URL.toURI());
                } catch (Exception ex) {
                }
            }
        });
        bottombar.addComponentToRight(feedback);

        JComponent userguide = ViewUtil.createHyperlinkButton("User Guide", ViewUtil.getMedSavantBlueColor(), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                try {                    
                    Desktop.getDesktop().browse(WebResources.USERGUIDE_URL.toURI());
                } catch (Exception ex) {
                }
            }
        });
        bottombar.addComponentToRight(userguide);

        bottomDisclaimer.add(bottombar.getComponent(), "width 100%");

        homeToolbar = getToolBar();
        //homeToolbar.addComponentToRight(MedSavantFrame.getInstance().getNotificationPanel().generateMenuButton());
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

        if (previousApp != null) {
            previousApp.viewDidUnload();
        }

        previousApp = null;
        
        transitionCanvas.push(dashLayer, JTransitionPanel.TransitionType.NONE, null);

        this.updateUI();
    }
    
    public void launchApp(String appName) {
        for (DashboardSection ds : this.dashboardSections) {
            for (LaunchableApp app : ds.getApps()) {
                if (appName.equals(app.getName())) {
                    launchApp(app);
                    return;
                }
            }
        }
    }

    public void launchApp(LaunchableApp app) {

        history.add(app);

        if (previousApp != null) {
            previousApp.viewWillUnload();
        }
        app.viewWillLoad();
        appLayer.removeAll();

        JPanel p = app.getView();

        appToolbar = getToolBar();
        appToolbar.addComponentToLeft((JComponent) Box.createHorizontalStrut(5));
        appToolbar.addComponentToLeft(getHomeButton());
        //appToolbar.addComponentToRight(MedSavantFrame.getInstance().getNotificationPanel().generateMenuButton());
        appToolbar.addComponentToRight((JComponent) Box.createHorizontalStrut(5));
        this.addTitleToBar(appToolbar, app.getName());

        appLayer.add(appToolbar.getComponent(), BorderLayout.NORTH);
        appLayer.add(p, BorderLayout.CENTER);

        if (previousApp != null) {
            previousApp.viewDidUnload();
        }
        app.viewDidLoad();
        AnalyticsAgent.log(new NameValuePair("app-launched", app.getName()));
        previousApp = app;

        appLayer.updateUI();
        
        //JTransitionPanel.TransitionType type = appToApp ? JTransitionPanel.TransitionType.SLIDE_RIGHT : JTransitionPanel.TransitionType.NONE;
        
        transitionCanvas.push(appLayer, JTransitionPanel.TransitionType.NONE, null);
    }

    public static JPanel getRepresentationForLauncher(String name, ImageIcon icon, int iconWidth, ActionListener actionListener) {
        return getRepresentationForLauncher(name, icon, iconWidth, actionListener, false);
    }

    public static JPanel getRepresentationForLauncher(String name, ImageIcon icon, int iconWidth, ActionListener actionListener, boolean disableButton) {
        JPanel p = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(p);

        JButton button = ViewUtil.getIconButton(new ImageIcon(ViewUtil.getScaledInstance(icon.getImage(), iconWidth, iconWidth, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true)));

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

    public List<LaunchableApp> getLaunchHistory() {
        return history.getRecentHistory();
    }

    public LaunchableApp getCurrentApp() {
        return this.previousApp;
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

    private TriAreaComponent getToolBar() {

        TriAreaComponent bar = new TriAreaComponent(5);
        bar.setBackgroundPainter(new MacWidgetsPainter<Component>() {

            @Override
            public void paint(Graphics2D gd, Component t, int i, int i1) {
                gd.setColor(ViewUtil.getPrimaryMenuColor());
                gd.fillRect(0, 0, t.getWidth(), t.getHeight());

            }
        });
        bar.getComponent().setBorder(ViewUtil.getBottomLineBorder());

        return bar;
    }

    private void addTitleToBar(TriAreaComponent bar, String name) {
        JLabel title = MacWidgetFactory.makeEmphasizedLabel(new JLabel(name), new Color(64, 64, 64), new Color(64, 64, 64), new Color(230, 230, 230));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14));
        //title.setFont(FontFactory.getMenuTitleFont());
        bar.addComponentToLeft(title);
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
