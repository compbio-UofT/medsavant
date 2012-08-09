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

package org.ut.biolab.medsavant.view.component;

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
}
