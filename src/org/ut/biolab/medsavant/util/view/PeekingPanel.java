/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.util.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class PeekingPanel extends JPanel {
    
    private final JPanel panel;
    private boolean isExpanded;
    private final JLabel title;
    private final String titleString;
    private final JPanel titlePanel;
    private Color tabColor = ViewUtil.getDarkColor();

    public PeekingPanel(String label, String borderLayoutPosition, JPanel panel, boolean isExpanded) {
        this(label, borderLayoutPosition, panel, isExpanded, 300);
    }

    public void setTabColor(Color c) {
        this.tabColor = c;
    }


    public PeekingPanel(String label, String borderLayoutPosition, JPanel panel, boolean isExpanded, int size) {

        final boolean isVertical = borderLayoutPosition.equals(BorderLayout.EAST) || borderLayoutPosition.equals(BorderLayout.WEST);
        
        this.setLayout(new BorderLayout());
        this.panel = panel;

        if (isVertical) {
            panel.setPreferredSize(new Dimension(size,999));
        } else {
            panel.setPreferredSize(new Dimension(999,size));
        }
        titlePanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                //g.setColor(tabColor);
                //g.fillRect(0, 0, titlePanel.getWidth(), titlePanel.getHeight());

                if (isVertical) {
                    GradientPaint p = new GradientPaint(0,0,Color.darkGray,40,0,Color.black);
                    ((Graphics2D)g).setPaint(p);
                    g.fillRect(0, 0, titlePanel.getWidth(), titlePanel.getHeight());
                } else {
                    GradientPaint p = new GradientPaint(0,0,Color.darkGray,0,40,Color.black);
                    ((Graphics2D)g).setPaint(p);
                    g.fillRect(0, 0, titlePanel.getWidth(), titlePanel.getHeight());
                }
            }
        };
        titlePanel.setBorder(ViewUtil.getTinyBorder());
        if (isVertical) {
            titlePanel.setLayout(new BoxLayout(titlePanel,BoxLayout.Y_AXIS));
        } else {
            titlePanel.setLayout(new BoxLayout(titlePanel,BoxLayout.X_AXIS));
        }
        titlePanel.add(Box.createHorizontalGlue());
       // titlePanel.setBackground(Color.black); //new Color(1,73,98));//

        this.titleString = label.toUpperCase();
        title = new JLabel(titleString);
        title.setFont(ViewUtil.getSmallTitleFont());
        title.setForeground(Color.white);
        if (borderLayoutPosition.equals(BorderLayout.EAST)) {
            title.setUI(new VerticalLabelUI(true));
        } else if (borderLayoutPosition.equals(BorderLayout.WEST)) {
            title.setUI(new VerticalLabelUI(false));
        }
        titlePanel.add(title);

        titlePanel.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                toggleExpanded();
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }

        });

        this.add(titlePanel, borderLayoutPosition);
        this.add(panel, BorderLayout.CENTER);

        setExpanded(isExpanded);
    }

    public void setExpanded(boolean expanded) {
        this.isExpanded = expanded;
        String s = this.isExpanded ? " HIDE " + titleString : " SHOW " + titleString;
        this.title.setText(s);
        panel.setVisible(isExpanded);
    }

    public void toggleExpanded() {
        setExpanded(!this.isExpanded);
    }

}
