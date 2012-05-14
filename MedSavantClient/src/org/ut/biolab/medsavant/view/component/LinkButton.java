package org.ut.biolab.medsavant.view.component;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JComponent;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.util.ViewUtil;

public class LinkButton extends JButton {

    private boolean over;
    private Font f;
    String face = "Arial";
    int size = 10;
    int style = Font.PLAIN;
    private final Image image;
    private int totalWidth;
    private int totalHeight;

    public void setForeColor(Color c) {
        this.textColor = c;
    }

    public void setFontSize(int size) {
        this.size = size;
        resetFont();
    }

    private boolean isImage() {
        return image != null;
    }

    public void setFontStyle(int style) {
        this.style = style;
        resetFont();
    }

    public LinkButton(Image i) {
        this.image = i;
        this.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public LinkButton(String s) {
        super(s);

        image = null;

        resetFont();

        this.setOpaque(false);
        final JComponent instance = this;
        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {

                over = true;
                instance.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                over = false;
                instance.repaint();
            }
        });
        this.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    Color textColorUnselected = Color.white;
    Color textColor = textColorUnselected;
    Color bgColor = new Color(120, 120, 120);
    Color disabledBGColor = new Color(210, 210, 210);
    int sidePadding = 3;
    int topPadding = 2;
    int padding = 3;
    int iconWidth = 9;

    private void resize() {

        if (isImage()) {

            totalWidth = iconWidth + sidePadding * 2;
            totalHeight = iconWidth + topPadding * 2;

            this.setPreferredSize(new Dimension(totalWidth, totalHeight));

        } else {
            String title = this.getText();

            Graphics2D g2d = (Graphics2D) this.getGraphics();
            if (g2d == null) { return; }
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.setFont(f);

            int width = g2d.getFontMetrics().stringWidth(title);
            int height = g2d.getFontMetrics().getAscent();


            totalWidth = width + sidePadding * 2;
            totalHeight = height + topPadding * 2;

            if (this.isEnabled()) {
                totalWidth += iconWidth + sidePadding;
            }

            this.setPreferredSize(new Dimension(totalWidth, totalHeight));
        }
    }

    @Override
    public void paintComponent(Graphics g) {

        if (totalWidth == 0) { this.resize(); }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (!this.isEnabled()) {
            g2d.setColor(disabledBGColor);
        } else {
            g2d.setColor(bgColor);
        }

        g2d.fillRoundRect(0, 0, totalWidth, totalHeight, 5, 5);

        if (isImage()) {
            g2d.drawImage(
                        image,
                        sidePadding,
                        topPadding,
                        null);
        } else {
            String title = this.getText();

            g2d.setFont(f);

            int width = g2d.getFontMetrics().stringWidth(title);
            int height = g2d.getFontMetrics().getAscent();

            if (this.isEnabled()) {
                totalWidth += iconWidth + sidePadding;
            }

            if (this.isEnabled()) {
                g2d.drawImage(
                        IconFactory.getInstance().getIcon(IconFactory.StandardIcon.LINKOUT).getImage(),
                        width + sidePadding + 3,
                        2,
                        null);
            }

            if (over || this.isSelected()) {
                g2d.setColor(textColor);
            } else {
                g2d.setColor(textColorUnselected);
            }


            g2d.drawString(title, sidePadding, topPadding / 2 + height - 1);
        }

    }

    private void resetFont() {
        f = new Font(face, style, size);
    }

    @Override
    public void setEnabled(boolean b) {
        super.setEnabled(b);
        resize();
        this.invalidate();
    }
}
