/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.view.component;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;


public class HoverButton extends JButton {

    private static final Color UNSELECTED_TEXT_COLOR = Color.GRAY;

    private boolean over;
    private Font f;
    String face = "Arial";
    int size = 15;
    int style = Font.PLAIN;
    private int sidePadding = 2;
    private int totalWidth;
    private int topPadding = 1;
    private int totalHeight;
    private Color textColor = Color.white;

    public void setForeColor(Color c) {
        this.textColor = c;
    }

    public void setFontSize(int size) {
        this.size = size;
        resetFont();

    }

    public void setFontStyle(int style) {
        this.style = style;
        resetFont();
    }

    public HoverButton(String s) {
        super(s);

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

        resize();
    }

    private void resize() {

        String title = getText();

        Graphics2D g2d = (Graphics2D)getGraphics();
        if (g2d == null) {
            return;
        }
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setFont(f);

        int width = g2d.getFontMetrics().stringWidth(title);
        int height = g2d.getFontMetrics().getAscent();

        totalWidth = width + sidePadding * 2;
        totalHeight = height + topPadding * 2;

        setPreferredSize(new Dimension(totalWidth, totalHeight));
        setMinimumSize(new Dimension(totalWidth, totalHeight));
        setMaximumSize(new Dimension(totalWidth, totalHeight));

        invalidate();
    }

    @Override
    public void paintComponent(Graphics g) {

        String title = this.getText();

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setFont(f);

        int height = g2d.getFontMetrics().getAscent();

        if (over || this.isSelected()) {
            g2d.setColor(textColor);
        } else {
            g2d.setColor(UNSELECTED_TEXT_COLOR);
        }


        g2d.drawString(title, 8, (getHeight() + height) / 2 - 2);

    }

    private void resetFont() {
        f = new Font(face, style, size);
    }

    public void setSelectedColor(Color c) {
        this.textColor = c;
        this.repaint();
    }
}
