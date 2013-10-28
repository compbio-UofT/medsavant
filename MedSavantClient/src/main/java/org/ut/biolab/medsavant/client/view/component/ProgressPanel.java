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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Path2D;
import javax.swing.JPanel;

/**
 *
 * @author mfiume
 */
public class ProgressPanel extends JPanel implements ComponentListener {

    private static final int preferredHeight = 5;
    private int preferredWidth = 300;
    private static final int padding = 0;
    private int width_bar;
    private static final int height_bar = 20;//preferredHeight - 2 * padding;
    private boolean animating = false;
    private int speed = 300;
    private int maxValue = 100;
    private long currentValue;
    private long lastValue;
    private double animatingValue;
    private boolean drawLastIndicator = false;
    private boolean drawShadow = true;

    public ProgressPanel() {

        this.setDoubleBuffered(true);
        this.setBackground(Color.pink);

        this.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
        this.currentValue = 100;
        this.lastValue = currentValue;
        this.repaint();
        this.addComponentListener(this);
    }

    public void setMaxValue(int value) {
        this.maxValue = value;
        this.repaint();
    }

    public void animateToValue(final int val) {

        if (val > this.maxValue) {
            if (this.currentValue != this.maxValue) {
                this.animateToValue(this.maxValue);
            }
            return;
        }

        if (val < 0) {
            if (this.currentValue != 0) {
                this.animateToValue(0);
            }
            return;
        }

        final ProgressPanel ap = this;

        Thread t = new Thread() {

            public void run() {

                if (ap == null || ap.getWidth() == 0) {
                    return;
                }

                ap.animating = true;

                ap.animatingValue = ap.currentValue;

                boolean forward = val > currentValue;

                int it = 0;
                while (true) {

                    it++;

                    if (forward) {
                        ap.animatingValue = ap.animatingValue + maxValue/ap.getWidth();
                    } else {
                        ap.animatingValue = ap.animatingValue - maxValue/ap.getWidth();
                    }

                    if ((forward && (ap.animatingValue >= val)) || (!forward && (ap.animatingValue <= val))) {
                        break;
                    }

                    ap.repaint();


                    try {
                        Thread.sleep(1000 / speed);
                    } catch (InterruptedException ex) {
                    }

                }

                ap.animating = false;
                ap.lastValue = ap.currentValue;
                ap.currentValue = val;
                ap.repaint();
            }
        };

        t.run();
    }
    Color innerColor = new Color(72, 181, 249);//new Color(103,162,209);
    Color borderColor = new Color(150,150,150);

    int shadowWidth = 3;
    int rounding = 10;

    public void draw(Graphics g) {
        drawPanel(g);
        //drawLegend();
    }

    @Override
    public void paintComponent(Graphics g) {
        draw(g);
    }

    private long getCurrentValue() {
        return this.currentValue;
    }

    @Override
    public void componentResized(ComponentEvent ce) {
        preferredWidth = this.getWidth();
        this.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
        this.draw(this.getGraphics());
    }

    @Override
    public void componentMoved(ComponentEvent ce) {
    }

    @Override
    public void componentShown(ComponentEvent ce) {
        //this.drawLegend();
        this.draw(this.getGraphics());
    }

    @Override
    public void componentHidden(ComponentEvent ce) {
    }
    boolean legendDrawn = false;

    private void drawPanel(Graphics g) {
        width_bar = preferredWidth - 2 * padding;

        Rectangle.Double fullRect = new Rectangle.Double(padding, padding, width_bar, height_bar);

        Graphics2D g2 = (Graphics2D) g;

        if (g2 == null) {
            return;
        }

        //g2.clearRect(0, padding, padding, height_bar);
        //g2.clearRect(width_bar, padding, padding, height_bar);

        //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int startx = padding;
        int starty = padding;

        double width = 0;
        double height = height_bar;

        try {
            if (this.animating) {
                width = this.animatingValue * width_bar / this.maxValue;
            } else {
                width = this.currentValue * width_bar / this.maxValue;
            }
        } catch (ArithmeticException ex){
            //divide by 0
            width = 0;
        }

        Rectangle.Double r2 = new Rectangle.Double(startx, starty, width, height);

        g2.setColor(Color.white);
        g2.fill(fullRect);
        g2.setColor(innerColor);
        g2.fill(r2);
        g2.setColor(borderColor);
        g2.draw(r2);

        Stroke s = g2.getStroke();

        if (drawLastIndicator) {

            double lastwidth = startx + this.lastValue * width_bar / this.maxValue;
            Rectangle.Double line = new Rectangle.Double(lastwidth, starty, 0.1, height);
            g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 10.0f, new float[]{4.0f}, 0.0f));
            g2.setColor(Color.darkGray);
            g2.draw(line);

        }

        g2.setStroke(s);

        g2.setColor(Color.gray);
        g2.draw(fullRect);

        if (drawShadow) {

            int sw = shadowWidth;
            for (int i = sw; i >= 0; i -= 1) {
                g2.setColor(new Color(10, 10, 10, 110 - (i * 110 / sw)));
                //g2.setStroke(new BasicStroke(i));
                g2.drawLine(startx, starty + i, width_bar + padding, starty + i);
            }
        }

        g2.dispose();
    }
    int arrowwidth = 8;
    int arrowheight = 10;
    int yinset = arrowwidth - 1;

    private void clearForIndicator(int width, int startx, int starty) {
        Graphics2D g = (Graphics2D) this.getGraphics();
        g.clearRect(startx - arrowwidth / 2, starty - arrowheight + yinset, width + arrowwidth, arrowheight - yinset);
    }

    private void drawIndicator(double x, double y, Color c) {
        Graphics2D g = (Graphics2D) this.getGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(c);

        Path2D.Double p = new Path2D.Double();
        p.moveTo(x, yinset + y);
        p.lineTo(x, yinset + y - arrowheight);
        p.lineTo(x + arrowwidth / 2, yinset + y - arrowheight);
        p.closePath();

        g.fill(p);

        p = new Path2D.Double();
        p.moveTo(x, yinset + y);
        p.lineTo(x, yinset + y - arrowheight);
        p.lineTo(x - arrowwidth / 2, yinset + y - arrowheight);
        p.closePath();

        g.fill(p);
        //p.lineTo(x, y);

    }

    public void setToValue(int val) {
        this.currentValue = val;
        this.repaint();
    }
}
