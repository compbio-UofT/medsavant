/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.client.view.component;

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

import org.ut.biolab.medsavant.client.view.images.IconFactory;


public class LinkButton extends JButton {

    private static final String face = "Arial";
    private final Image image;
    private final Color bgcolor = Color.LIGHT_GRAY;
    private final Color textColorUnselected = Color.BLACK;

    //Color bgColor = new Color(50, 50, 50);
    private final Color disabledBGColor = new Color(210, 210, 210);
    private final int sidePadding = 3;
    private final int topPadding = 2;
    private final int iconWidth = 9;

    private Color textColor = textColorUnselected;
    private Font font;
    private int fontSize = 10;
    private int fontStyle = Font.PLAIN;
    private int totalWidth;
    private int totalHeight;

    private boolean over;

    public LinkButton(Image i) {
        image = i;
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        resize();
    }

    public LinkButton(String s) {
        super(s);

        image = null;

        resetFont();

        setOpaque(false);
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                over = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                over = false;
                repaint();
            }
        });
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public void setForeColor(Color c) {
        textColor = c;
    }

    public void setFontSize(int size) {
        fontSize = size;
        resetFont();
    }

    private boolean isImage() {
        return image != null;
    }

    public void setFontStyle(int style) {
        fontStyle = style;
        resetFont();
    }

    private void resize() {

        if (isImage()) {
            totalWidth = iconWidth + sidePadding * 2;
            totalHeight = iconWidth + topPadding * 2;


        } else {
            String title = getText();

            Graphics2D g2d = (Graphics2D) this.getGraphics();
            if (g2d == null) {
                return;
            }
            //g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.setFont(font);

            int width = g2d.getFontMetrics().stringWidth(title);
            int height = g2d.getFontMetrics().getAscent();

            totalWidth = width + sidePadding * 2;
            totalHeight = height + topPadding * 2;

            if (isEnabled()) {
                totalWidth += iconWidth + sidePadding;
            }
        }

        this.setPreferredSize(new Dimension(totalWidth, totalHeight));
        this.setMinimumSize(new Dimension(totalWidth, totalHeight));
        this.setMaximumSize(new Dimension(totalWidth, totalHeight));

        invalidate();
    }

    @Override
    public void paintComponent(Graphics g) {

        if (totalWidth == 0) {
            resize();
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (!isEnabled()) {
            g2d.setColor(disabledBGColor);
            g2d.fillRoundRect(0, 0, totalWidth, totalHeight, 5, 5);
        } else {
            g2d.setColor(bgcolor);
            g2d.fillRoundRect(0, 0, totalWidth, totalHeight, 5, 5);
            //PaintUtil.paintDarkMenu(g, this);
        }

        if (isImage()) {
            g2d.drawImage(
                    image,
                    sidePadding,
                    topPadding,
                    null);
        } else {
            String title = getText();

            g2d.setFont(font);

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

            if (over || isSelected()) {
                g2d.setColor(textColor);
            } else {
                g2d.setColor(textColorUnselected);
            }


            g2d.drawString(title, sidePadding, topPadding / 2 + height - 1);
        }

    }

    private void resetFont() {
        font = new Font(face, fontStyle, fontSize);
    }

    @Override
    public void setEnabled(boolean b) {
        super.setEnabled(b);
        resize();
        invalidate();
    }
}
