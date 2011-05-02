/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view;

import com.jidesoft.swing.JideSplitButton;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class SplitView extends JPanel {
    private final JPanel main;
    private final JPanel left;
    private final CardLayout cl;
    private final ButtonGroup bg;

    public SplitView() {
        left = new JPanel();
        left.setBorder(new EmptyBorder(10,10,10,10));
        left.setLayout(new BoxLayout(left,BoxLayout.Y_AXIS));
        left.setBackground(new Color(217,222,229));
        cl = new CardLayout();
        main = new JPanel(cl);
        main.setBackground(Color.gray);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                           left, main);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(200);
        this.setLayout(new BorderLayout());
        this.add(splitPane,BorderLayout.CENTER);
        bg = new ButtonGroup();
    }

    public void addSection(String sectionName) {
        if (left.getComponentCount() != 0) { left.add(Box.createVerticalStrut(10)); }
        JLabel l = new JLabel(sectionName.toUpperCase());
        l.setFont(new Font("Arial", Font.BOLD, 14));
        left.add(l);
    }

    public void addSubsection(final String subsectionName, JPanel card) {
        JRadioButton b = (JRadioButton) ViewUtil.clear(new JRadioButton(subsectionName));
        b.setFont(new Font("Arial", Font.PLAIN, 14));
        b.setForeground(Color.darkGray);
        b.setAlignmentX(0F);
        bg.add(b);
        left.add(b);
        main.add(card,subsectionName);
        b.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JRadioButton b2 = (JRadioButton) e.getSource();
                if (b2.isSelected()) {
                    cl.show(main, subsectionName);
                }
            }

        });
    }

}
