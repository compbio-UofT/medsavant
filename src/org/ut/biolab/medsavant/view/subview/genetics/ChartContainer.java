/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.subview.genetics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class ChartContainer extends JPanel {

    private JPanel container;

    public ChartContainer() {
        this.setLayout(new BorderLayout());
        JPanel title = ViewUtil.getBannerPanel();
        title.add(Box.createHorizontalGlue());
        JButton addButton = new JButton("Add chart");
        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JPanel p = new ChartPanel();
                p.setPreferredSize(new Dimension(800, 230));
                p.setMaximumSize(new Dimension(800, 230));
                p.setBorder(ViewUtil.getTinyLineBorder());
                addChart(p);
            }
        });
        title.add(addButton);
        this.add(title, BorderLayout.NORTH);
        container = ViewUtil.createClearPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        JScrollPane sp = (JScrollPane) ViewUtil.clear(new JScrollPane(container));
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.getViewport().setOpaque(false);
        this.add(sp, BorderLayout.CENTER);
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        GradientPaint p = new GradientPaint(0, 0, Color.darkGray, 0, this.getHeight(), Color.black);
        g2.setPaint(p);
        g2.fillRect(0, 0, this.getWidth(), this.getHeight());
    }

    private void addChart(Component p) {
        container.add(p);
    }
}
