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
package org.ut.biolab.medsavant.client.view.util;

import com.explodingpixels.macwidgets.HudWindow;
import com.explodingpixels.macwidgets.MacWidgetFactory;
import com.explodingpixels.macwidgets.SourceListStandardColorScheme;
import java.text.NumberFormat;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

import com.jidesoft.plaf.basic.ThemePainter;
import com.jidesoft.swing.JideButton;
import com.jidesoft.swing.JideSplitButton;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Locale;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import eu.hansolo.custom.SteelCheckBox;
import eu.hansolo.tools.ColorDef;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;

import org.ut.biolab.medsavant.shared.util.MiscUtils;
import org.ut.biolab.medsavant.client.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.client.view.component.ProgressWheel;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.savant.analytics.savantanalytics.AnalyticsAgent;

/**
 *
 * @author mfiume
 */
public final class ViewUtil {

    // detail colors
    public final static Color detailForeground = new Color(10, 10, 10);
    public final static Color detailSelectedBackground = new Color(92, 168, 229);

    // row colors
    public final static Color evenRowColor = new Color(250,250,250);
    public final static Color oddRowColor = new Color(242, 245, 249);

    private static final SourceListStandardColorScheme fColorScheme = new SourceListStandardColorScheme();

    // detail fonts
    public static Font detailFontPlain = new Font(getDefaultFontFamily(),Font.PLAIN,12);
    public static Font detailFontBold = new Font(getDefaultFontFamily(),Font.BOLD,12);

    public static Point getPositionRelativeTo(Component root, Component comp) {
        if (comp.equals(root)) {
            return new Point(0, 0);
        }
        Point pos = comp.getLocation();
        Point parentOff = getPositionRelativeTo(root, comp.getParent());
        return new Point(pos.x + parentOff.x, pos.y + parentOff.y);
    }

    public static JPanel getClearPanel() {
        return (JPanel) clear(new JPanel());
    }

    public static JComponent clear(JComponent c) {
        c.setOpaque(false);
        return c;
    }

    public static JButton createHyperLinkButton(String string) {
        JideButton b = new JideButton(string);
        b.setButtonStyle(JideButton.HYPERLINK_STYLE);
        return b;
    }

    public static Border getTinyBorder() {
        return new EmptyBorder(1, 1, 1, 1);
    }

    public static Border getSmallBorder() {
        return new EmptyBorder(3, 3, 3, 3);
    }

    public static Border getMediumBorder() {
        return new EmptyBorder(5, 5, 5, 5);
    }

    public static Border getBigBorder() {
        return new EmptyBorder(10, 10, 10, 10);
    }

    public static Border getMediumTopHeavyBorder() {
        return new EmptyBorder(10, 0, 10, 0);
    }

    public static Border getHugeBorder() {
        return new EmptyBorder(25, 25, 25, 25);
    }

    public static Font getBigTitleFont() {
        return new Font(getDefaultFontFamily(), Font.PLAIN, 18);
    }

    public static Font getMediumTitleFont() {
        return new Font(getDefaultFontFamily(), Font.BOLD, 13);
    }

    public static Font getSmallTitleFont() {
        return new Font(getDefaultFontFamily(), Font.PLAIN, 11);
    }

    public static Font getTinyTitleFont() {
        return new Font(getDefaultFontFamily(), Font.PLAIN, 9);
    }

    public static JPanel getTertiaryBannerPanel() {
        JPanel p = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                GradientPaint p = new GradientPaint(0, 0, Color.white, 0, 40, Color.white);
                ((Graphics2D) g).setPaint(p);
                g.fillRect(0, 0, this.getWidth(), this.getHeight());
            }
        };

        p.setBorder(ViewUtil.getSmallBorder());

        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));

        return p;
    }

    public static JPanel getSecondaryBannerPanel() {
        JPanel p = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                GradientPaint p = new GradientPaint(0, 0, Color.white, 0, 40, Color.lightGray);
                ((Graphics2D) g).setPaint(p);
                g.fillRect(0, 0, this.getWidth(), this.getHeight());
            }
        };

        p.setBorder(ViewUtil.getMediumBorder());

        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));

        return p;
    }

    public static Component getMediumSeparator() {
        return Box.createRigidArea(new Dimension(5, 1));
    }

    public static Component getSmallVerticalSeparator() {
        return Box.createRigidArea(new Dimension(1, 2));
    }

    public static Border getTinyLineBorder() {
        return new LineBorder(Color.lightGray, 1);
    }

    public static Border getThickLeftLineBorder() {
        return BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 4, 0, 0, Color.lightGray), BorderFactory.createEmptyBorder(0, 2, 0, 0));
    }

    public static Border getTopLineBorder() {
        return BorderFactory.createMatteBorder(1, 0, 0, 0, Color.lightGray);
    }

    public static Border getRightLineBorder() {
        return BorderFactory.createMatteBorder(0, 0, 0, 1, Color.lightGray);
    }

    public static Border getBottomLineBorder() {
        return BorderFactory.createMatteBorder(0, 0, 1, 0, Color.lightGray);
    }

    public static Border getSideLineBorder() {
        return BorderFactory.createMatteBorder(0, 1, 0, 1, Color.lightGray);
    }

    public static Color getTertiaryMenuColor() {
        return new Color(220, 220, 220);
    }

    public static Color getSecondaryMenuColor() {
        return new Color(214,221,230);
    }

    public static Color getSemiBlackColor() {
        return new Color(64, 64, 64);
    }

    public static JLabel getTitleLabel(String string) {
        JLabel l = new JLabel(string);
        l.setFont(new Font(getDefaultFontFamily(), Font.PLAIN, 14));
        l.setForeground(Color.black);
        return l;
    }

    public static String getDefaultFontFamily() {
        return "Helvetica Neue";
    }

    public static JPanel alignLeft(Component c) {
        JPanel aligned = ViewUtil.getClearPanel();
        aligned.setLayout(new BoxLayout(aligned, BoxLayout.X_AXIS));
        aligned.add(c);
        aligned.add(Box.createHorizontalGlue());
        return aligned;
    }

    public static JPanel alignRight(Component c) {
        JPanel aligned = ViewUtil.getClearPanel();
        aligned.setLayout(new BoxLayout(aligned, BoxLayout.X_AXIS));
        aligned.add(Box.createHorizontalGlue());
        aligned.add(c);
        return aligned;
    }

    public static JScrollPane getClearBorderedScrollPane(Container c) {
        JScrollPane jsp = new JScrollPane(c);
        ViewUtil.clear(jsp);
        ViewUtil.clear(jsp.getViewport());
        jsp.setBorder(ViewUtil.getTinyLineBorder());
        return jsp;
    }

    public static JScrollPane getClearBorderlessScrollPane(Container c) {
        JScrollPane jsp = new JScrollPane(c);
        ViewUtil.clear(jsp);
        ViewUtil.clear(jsp.getViewport());
        jsp.setBorder(null);
        return jsp;
    }

    public static Color getDetailsBackgroundColor() {
        return new Color(40, 40, 40);
    }

    public static Component getDialogLabel(String string) {
        JLabel l = new JLabel(string);
        l.setFont(new Font("Tahoma", Font.PLAIN, 14));
        return l;
    }

    public static Component getDialogLabel(String string, Color fontColor) {
        Component l = getDialogLabel(string);
        if (fontColor != null) {
            l.setForeground(fontColor);
        }
        return l;
    }

    public static JPanel getSubBannerPanel(String title) {
        JPanel p = new JPanel();
        setBoxXLayout(p);
        p.setBackground(new Color(245, 245, 245));
        p.setBorder(BorderFactory.createTitledBorder(title));
        return p;
    }

    public static void setBoxXLayout(JPanel p) {
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    }

    public static void setBoxYLayout(JPanel p) {
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    }

    public static JPanel getMessagePanelBig(String string) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(Box.createVerticalGlue());
        p.add(ViewUtil.getCenterAlignedComponent(getTitleLabel(string)));
        p.add(Box.createVerticalGlue());
        return p;
    }

    public static void applyVerticalBoxLayout(Container p) {
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    }

    public static void applyHorizontalBoxLayout(Container p) {
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    }

    public static JPanel centerHorizontally(JComponent c) {
        JPanel p = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(p);
        p.add(Box.createHorizontalGlue());
        p.add(c);
        p.add(Box.createHorizontalGlue());
        return p;
    }

    public static JButton getSoftButton(String string) {
        JButton b = new JButton(string);
        b.putClientProperty("JButton.buttonType", "segmentedRoundRect");
        b.putClientProperty("JButton.segmentPosition", "only");
        b.setFocusable(false);
        return b;
    }

    public static JComponent makeSmall(JComponent c) {
        c.putClientProperty("JComponent.sizeVariant", "small");
        return c;
    }

    public static JComponent makeMini(JComponent c) {
        c.putClientProperty("JComponent.sizeVariant", "mini");
        return c;
    }

    public static JToggleButton getMenuToggleButton(String title) { //, int num) {

        JToggleButton button = new JToggleButton(title);

        if (MiscUtils.MAC) {
            button.putClientProperty("JButton.buttonType", "textured");
        }

        return button;
    }

    public static JToggleButton getSoftToggleButton(String string) {
        JToggleButton b = new JToggleButton(string);
        b.setFocusable(false);
        b.putClientProperty("JButton.buttonType", "segmentedRoundRect");
        b.putClientProperty("JButton.segmentPosition", "only");
        return b;
    }

    public static JButton getTexturedButton(ImageIcon icon) {
        JButton button = new JButton(icon);
        ViewUtil.makeSmall(button);
        button.setFocusable(false);
        button.putClientProperty("JButton.buttonType", "textured");
        //button.putClientProperty( "JButton.segmentPosition", "only" );
        return button;
    }

    public static JButton getTexturedButton(String s) {
        JButton button = new JButton(s);
        button.setFocusable(false);
        button.putClientProperty("JButton.buttonType", "textured");
        return button;
    }

    public static BufferedImage makeRoundedCorner(BufferedImage image, int cornerRadius) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = output.createGraphics();

        // This is what we want, but it only does hard-clipping, i.e. aliasing
        // g2.setClip(new RoundRectangle2D ...)
        // so instead fake soft-clipping by first drawing the desired clip shape
        // in fully opaque white with antialiasing enabled...
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));

        // ... then compositing the image on top,
        // using the white shape from above as alpha source
        g2.setComposite(AlphaComposite.SrcAtop);
        g2.drawImage(image, 0, 0, null);

        g2.dispose();

        return output;
    }

    public static BufferedImage darkenImage(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = output.createGraphics();

        g2.drawImage(image, 0, 0, null);
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fill(new Rectangle(0, 0, w, h));

        g2.dispose();

        return output;
    }
    
    public static JButton getIconButton(ImageIcon icon) {
        return getIconButton(icon,30);
    }

    public static JButton getIconButton(ImageIcon icon, int cornerRadius) {

        BufferedImage unselectedImage = makeRoundedCorner(makeBufferedImageFromIcon(icon), cornerRadius);
        BufferedImage selectedImage = makeRoundedCorner(darkenImage(makeBufferedImageFromIcon(icon)), cornerRadius);

        final JButton button = new JButton(new ImageIcon(unselectedImage));
        button.setPressedIcon(new ImageIcon(selectedImage));

        button.setFocusable(false);
        button.setContentAreaFilled(false);
        button.setBorder(null);

        //ViewUtil.makeSmall(button);
        return button;
    }

    public static JToggleButton getTogglableIconButton(ImageIcon icon) {

        final ImageIcon selectedIcon = icon;
        //final ImageIcon unselectedIcon = new AlphaImageIcon(icon, 0.3F);
        final ImageIcon unselectedIcon = new ImageIcon(GrayFilter.createDisabledImage(icon.getImage()));

        final JToggleButton button = new JToggleButton(icon);
        button.setFocusable(false);
        button.setContentAreaFilled(false);
        button.setBorder(null);
        //ViewUtil.makeSmall(button);

        final Runnable setSelected = new Runnable() {
            @Override
            public void run() {
                button.setIcon(selectedIcon);
                button.setFocusable(false);
                button.setContentAreaFilled(false);
                button.setBorder(null);
            }
        };

        final Runnable setUnselected = new Runnable() {
            @Override
            public void run() {
                button.setIcon(unselectedIcon);
                button.setFocusable(false);
                button.setContentAreaFilled(false);
                button.setBorder(null);
            }
        };

        button.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent ce) {
                if (button.getModel().isSelected()) {
                    setSelected.run();
                } else {
                    setUnselected.run();
                }
            }
        });

        setUnselected.run();

        return button;
    }

    public static JToggleButton getTexturedToggleButton(ImageIcon icon) {
        JToggleButton button = new JToggleButton(icon);
        button.setFocusable(false);
        ViewUtil.makeSmall(button);
        button.putClientProperty("JButton.buttonType", "textured");
        return button;
    }

    public static JToggleButton getTexturedToggleButton(String s) {
        JToggleButton button = new JToggleButton(s);
        button.setFocusable(false);
        button.putClientProperty("JButton.buttonType", "textured");
        return button;
    }

    public static JButton getTexturedButton(String s, ImageIcon icon) {
        JButton button = new JButton(s, icon);
        ViewUtil.makeSmall(button);
        button.setFocusable(false);
        button.setVerticalTextPosition(SwingConstants.CENTER);
        button.setHorizontalTextPosition(SwingConstants.LEFT);
        button.putClientProperty("JButton.buttonType", "textured");
        return button;
    }

    public static Color getAlternateRowColor() {
        return new Color(242, 245, 249);
    }

    public static JComponent subTextComponent(JComponent c, String subtext) {
        return subTextComponent(c, subtext, 14);
    }

    public static JComponent subTextComponent(JComponent c, String subtext, int fontSize) {
        int width = c.getPreferredSize().width;
        JPanel p = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(p);
        p.add(ViewUtil.centerHorizontally(c));
        JLabel s = new JLabel(subtext);
        ViewUtil.setFontSize(s, fontSize);
        s.setForeground(Color.darkGray);
        ViewUtil.makeSmall(s);
        p.add(ViewUtil.centerHorizontally(ViewUtil.clear(s)));

        FontMetrics fm = s.getFontMetrics(s.getFont());
        p.setMaximumSize(new Dimension(Math.max(width, fm.stringWidth(subtext)), 23 + c.getPreferredSize().height));
        return p;
    }

    public static void fixSize(JComponent c, Dimension d) {
        c.setMaximumSize(d);
        c.setPreferredSize(d);
        c.setMaximumSize(d);
        c.revalidate();
    }

    public static JLabel getErrorLabel(String msg) {
        JLabel l = new JLabel(msg);
        l.setForeground(Color.red);
        return l;
    }

    public static ProgressWheel getIndeterminateProgressBar() {
        ProgressWheel w = new ProgressWheel();
        return w;
    }

    public static void setFontSize(JLabel label, int i) {
        Font f = label.getFont();
        Font newFont = new Font(f.getFamily(), f.getStyle(), i);
        label.setFont(newFont);
    }

    public static JComponent horizontallyAlignComponents(Component[] component) {
        JPanel p = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(p);
        for (Component c : component) {
            p.add(c);
        }
        p.add(Box.createHorizontalGlue());
        return p;
    }

    public static void shortenLabelToLength(JLabel label, int length) {
        String text = label.getText();
        if (text.length() > length) {
            label.setText(ViewUtil.ellipsize(text, length));
            label.setToolTipText(text);
        }
    }

    public static JDialog getHUD(Component parent, String title, String text) {
        return getHUD(parent, title, text, false);
    }

    public static JDialog getHUD(Component parent, String title, String text, boolean modal) {
        return getHUD(parent, title, text, false, modal);
    }

    public static double parseDoubleFromFormattedString(String s) {
        NumberFormat format = NumberFormat.getInstance(Locale.getDefault());
        try {
            Number number = format.parse(s);
            double d = number.doubleValue();
            return d;
        } catch (Exception w) {
            return 0;
        }
    }

    public static JDialog getHUD(Component parent, String title, String text, boolean hideCloseIcon, boolean modal) {
        int width = 300;

        JLabel l = new JLabel();
        String labelText = String.format("<html><div WIDTH=%d>%s</div><html>", width, text);
        l.setText(labelText);

        l.setOpaque(false);

        final JDialog d;
        JComponent contentPane = null;

        if (ClientMiscUtils.LINUX) {
            d = new JDialog(MedSavantFrame.getInstance(), title, false);
            contentPane = d.getRootPane();
            d.setResizable(false);
            d.setUndecorated(true);
        } else {
            final HudWindow hud = new HudWindow(title);
            contentPane = hud.getContentPane();
            d = hud.getJDialog();
            d.setModal(modal);
            l.setForeground(Color.white);
            if (hideCloseIcon) {
                hud.hideCloseButton();
            }
        }

        contentPane.setBorder(ViewUtil.getMediumBorder());
        contentPane.setLayout(new BorderLayout());
        contentPane.add(l, BorderLayout.CENTER);

        if (ClientMiscUtils.LINUX) {
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(getMediumTitleFont());
            contentPane.add(titleLabel, BorderLayout.NORTH);
        }

        d.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        d.pack();

        d.setLocationRelativeTo(parent);

        d.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent fe) {
            }

            @Override
            public void focusLost(FocusEvent fe) {
                d.setVisible(false);
            }
        });
        return d;
    }

    public static JButton getHelpButton(final String title, final String helpText) {
        return getHelpButton(title, helpText, false);
    }

    public static JButton getHelpButton(final String title, final String helpText, final boolean modal) {

        final JButton helpButton = new JButton("?");
        ViewUtil.makeSmall(helpButton);
        helpButton.setFocusable(false);
        if (MiscUtils.MAC) {
            helpButton.putClientProperty("JButton.buttonType", "help");
            helpButton.setText("");
        }
        /*helpButton.setToolTipText("<html>Type a search condition into the search box, e.g. \"Chromosome\".<br>"
         + "Press Enter / Return to accept the selected condition name.<br>"
         + "You\'ll then be prompted to specify parameters for this condition</html>");*/

        helpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JDialog d = getHUD(helpButton, title, helpText, modal);
                d.setVisible(true);
                try {
                    AnalyticsAgent.log(title + " help button pressed");
                } catch (Exception e) {
                }

            }
        });
        return helpButton;
    }

    private static boolean isJava7() {
        return System.getProperty("java.version").startsWith("1.7");
    }

    private static BufferedImage makeBufferedImageFromIcon(ImageIcon icon) {
        BufferedImage bufferedImage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
        bufferedImage.getGraphics().drawImage(icon.getImage(), 0, 0, null);
        return bufferedImage;
    }

    public static JLabel getSettingsHeaderLabel(String name) {
        JLabel l = new JLabel(name);
        l.setFont(ViewUtil.getMediumTitleFont());
        return l;
    }

    public static Component getSettingsHelpLabel(String name) {
        JLabel l = new JLabel(name);
        l.setFont(ViewUtil.getSmallTitleFont());
        l.setForeground(new Color(150, 150, 150));
        return l;
    }

    public static JLabel getLargeGrayLabel(String n) {
        JLabel l = new JLabel(n);
        l.setFont(ViewUtil.getBigTitleFont());
        l.setForeground(ViewUtil.getSemiBlackColor());
        return l;
    }

    public static Color getPrimaryMenuColor() {
        return new Color(221,221,221);
    }

    public static Color getSubtleTitleColor() {
        return new Color(114,114,114);
    }

    public static JButton getRefreshButton() {
        return new JButton("Refresh");
    }

    public static JButton getConfigureButton() {
        return ViewUtil.getIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.CONFIGURE));
    }

    public static void ellipsizeLabel(JLabel label, int width) {
        Font f = label.getFont();
        FontMetrics fm = label.getFontMetrics(f);
        
        if (fm.stringWidth(label.getText()) <= width) {
            return;
        }
        
        label.setToolTipText(label.getText());
        
        while (fm.stringWidth(label.getText()) > width) {
            String text = label.getText().replace("...", "");
            text = text.substring(0, text.length()-1);
            text = text.trim();
            text = text + "...";
            label.setText(text);
        }
    }

    public static void adjustForegroundColorOnMouseover(final JComponent l, int amount) {
        final Color originalColor = l.getForeground();
        final Color newColor = new Color(Math.min(originalColor.getRed() + amount, 255), Math.min(originalColor.getBlue() + amount, 255), Math.min(originalColor.getGreen() + amount, 255));
        l.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                l.setForeground(newColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                l.setForeground(originalColor);
            }
        });
    }

    public static Font getBigInputFont() {
        return new Font(getDefaultFontFamily(), Font.PLAIN, 16);
    }

    public static SteelCheckBox getSwitchCheckBox() {
        return getSwitchCheckBox(" ");
    }
    public static SteelCheckBox getSwitchCheckBox(String text) {
        SteelCheckBox cb = new SteelCheckBox();
        //cb.setSelectedColor(ColorDef.JUG_GREEN);
        //cb.setColored(true);
        cb.setText(text);
        return cb;
    }

    private static class DetailListCellRenderer extends JLabel implements ListCellRenderer {

        public DetailListCellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            setText(value.toString());

            this.setBorder(ViewUtil.getSmallBorder());

            if (isSelected) {
                setBackground(ViewUtil.detailSelectedBackground);

                //setForeground(list.getSelectionForeground());
                setForeground(ViewUtil.detailForeground);
            } else {
                if (index % 2 == 0) {
                    setBackground(ViewUtil.evenRowColor);
                } else {
                    setBackground(ViewUtil.oddRowColor);
                }
                setForeground(ViewUtil.detailForeground);
            }
            return this;
        }
    }

    public static String numToString(float num) {
        return NumberFormat.getInstance().format(num);
    }

    public static String numToString(double num) {
        return NumberFormat.getInstance().format(num);
    }

    public static String numToString(int num) {
        return NumberFormat.getInstance().format(num);
    }

    public static String numToString(long num) {
        return NumberFormat.getInstance().format(num);
    }

    public static JLabel getGrayLabel(String label) {
        JLabel l = new JLabel(label);
        l.setForeground(Color.darkGray);
        l.setFont(new Font("Arial", Font.PLAIN, 18));
        return l;
    }

    public static JLabel getDetailTitleLabel(String label) {
        JLabel l = new JLabel(label);
        l.setForeground(Color.black);
        l.setFont(new Font(l.getFont().getFamily(), Font.PLAIN, 17));
        return l;
    }

    public static JLabel getEmphasizedLabel(String s) {
        JLabel sc = new JLabel(s);
        sc.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD,11.0f));
        JLabel l = MacWidgetFactory.makeEmphasizedLabel(sc,
                fColorScheme.getCategoryTextColor(),
                fColorScheme.getCategoryTextColor(),
                fColorScheme.getCategoryTextShadowColor());
        return l;
    }

    public static JLabel getEmphasizedSemiBlackLabel(String s) {
        JLabel sc = new JLabel(s);
        sc.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD,11.0f));
        JLabel l = MacWidgetFactory.makeEmphasizedLabel(sc,
                getSemiBlackColor(),
                getSemiBlackColor(),
                new Color(255,255,255,0));
        return l;
    }
    
    public static JLabel getSubtleHeaderLabel(String s) {
        JLabel sc = new JLabel(s);
        sc.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD,11.0f));
        JLabel l = MacWidgetFactory.makeEmphasizedLabel(sc,
                ViewUtil.getSubtleTitleColor(),
                ViewUtil.getSubtleTitleColor(),
                fColorScheme.getCategoryTextShadowColor());
        return l;
    }

    public static JPanel getKeyValuePairPanel(String key, String val) {
        JLabel keyl = new JLabel(key + ": ");
        keyl.setFont(new Font(keyl.getFont().getFamily(), Font.BOLD, keyl.getFont().getSize()));
        keyl.setForeground(Color.darkGray);

        JLabel value = new JLabel(val);
        value.setFont(new Font(keyl.getFont().getFamily(), Font.PLAIN, keyl.getFont().getSize()));
        value.setForeground(Color.black);

        JPanel h1 = ViewUtil.getClearPanel();
        h1.setLayout(new BoxLayout(h1, BoxLayout.X_AXIS));
        h1.add(keyl);
        h1.add(value);
        h1.add(Box.createHorizontalGlue());

        return h1;
    }

    public static JPanel getKeyValuePairPanelListItem(String key, String val, boolean dark) {
        return getKeyValuePairPanelListItem(key, val, dark, true);
    }

    public static JPanel getKeyValuePairPanelListItem(String key, String val, boolean dark, boolean keyBold) {
        JLabel keyl = new JLabel(key);
        keyl.setFont(new Font(keyl.getFont().getFamily(), (keyBold ? Font.BOLD : Font.PLAIN), keyl.getFont().getSize()));
        keyl.setForeground(detailForeground);

        JLabel value = new JLabel(val);
        value.setFont(new Font(keyl.getFont().getFamily(), Font.PLAIN, keyl.getFont().getSize()));
        value.setForeground(detailForeground);

        JPanel h1 = new JPanel();
        h1.setLayout(new BoxLayout(h1, BoxLayout.X_AXIS));
        h1.setBorder(ViewUtil.getSmallBorder());
        if (dark) {
            h1.setBackground(evenRowColor);
        } else {
            h1.setBackground(oddRowColor);
        }

        //h1.setBorder(ViewUtil.getBottomBorder());
        h1.add(keyl);
        h1.add(Box.createHorizontalGlue());
        h1.add(value);

        return h1;
    }

    public static JPanel getKeyValuePairList(String[][] keyPairs) {

        KeyValuePairPanel kvp = new KeyValuePairPanel();
        for (int i = 0; i < keyPairs.length; i++) {
            kvp.addKey(keyPairs[i][0]);
            kvp.setValue(keyPairs[i][0], keyPairs[i][1]);
        }
        return kvp;
    }

    public static JPanel getKeyList(String[] keys) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        for (int i = 0; i < keys.length; i++) {
            p.add(getKeyValuePairPanelListItem(keys[i], "", i % 2 == 0, false));
        }
        return p;
    }

    public static JPanel getButtonPanel() {
        JPanel p = ViewUtil.getClearPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        Dimension d = new Dimension(999, 35);
        p.setBorder(null);
        p.setMaximumSize(d);
        p.setMinimumSize(d);
        p.setPreferredSize(d);
        p.add(Box.createHorizontalGlue());
        return p;
    }

    public static JPanel getLeftAlignedComponent(Component c) {
        JPanel p = ViewUtil.getClearPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(c);
        p.add(Box.createHorizontalGlue());
        return p;
    }

    public static JPanel getCenterAlignedComponent(Component c) {
        JPanel p = ViewUtil.getClearPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(Box.createHorizontalGlue());
        p.add(c);
        p.add(Box.createHorizontalGlue());
        return p;
    }

    public static Color getColor(int colorIndex) {
        ColorScheme cs = DefaultColorScheme.getInstance();
        return cs.getColor(colorIndex);

    }

    public static Color getColor(int colorIndex, int of) {
        ColorScheme cs = DefaultColorScheme.getInstance();
        return cs.getColor(colorIndex, of);

    }

    /*
     * Use this to create an icon button. JLabel is used instead of JButton
     * for proper display on Windows. MouseListeners can be added as usual.
     */
    public static JLabel createIconButton(ImageIcon i) {
        JLabel b = new JLabel();
        b.setBorder(null);
        b.setOpaque(false);
        b.setPreferredSize(new Dimension(i.getIconWidth(), i.getIconHeight()));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setIcon(i);
        return b;
    }

    public static JLabel createLabelButton(String text) {
        JLabel b = new JLabel(text);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }
    private final static String NON_THIN = "[^iIl1\\.,']";

    private static int textWidth(String str) {
        return (int) (str.length() - str.replaceAll(NON_THIN, "").length() / 2);
    }

    public static String ellipsize(String text, int max) {

        if (textWidth(text) <= max) {
            return text;
        }

        // Start by chopping off at the word before max
        // This is an over-approximation due to thin-characters...
        int end = text.lastIndexOf(' ', max - 3);

        // Just one long word. Chop it off.
        if (end == -1) {
            return text.substring(0, max - 3) + "...";
        }

        // Step forward as long as textWidth allows.
        int newEnd = end;
        do {
            end = newEnd;
            newEnd = text.indexOf(' ', end + 1);

            // No more spaces.
            if (newEnd == -1) {
                newEnd = text.length();
            }

        } while (textWidth(text.substring(0, newEnd) + "...") < max);

        return text.substring(0, end) + "...";
    }

    public static void positionButtonAlone(JComponent c) {
        positionButton(c, "only");
    }

    public static void positionButtonFirst(JComponent c) {
        positionButton(c, "first");
    }

    public static void positionButtonMiddle(JComponent c) {
        positionButton(c, "middle");
    }

    public static void positionButtonLast(JComponent c) {
        positionButton(c, "last");
    }

    private static void positionButton(JComponent c, String position) {
        if (MiscUtils.MAC && (c instanceof JButton || c instanceof JToggleButton)) {
            c.putClientProperty("JButton.segmentPosition", position);
        }
    }
	
	/**
	 * Get the sidebar color.
	 * @return the sidebar color
	 */
	public static Color getSidebarColor() {
		return fColorScheme.getActiveBackgroundColor();
	}
	
}
