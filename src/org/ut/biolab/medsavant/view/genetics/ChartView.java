/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class ChartView extends JPanel {

    public ChartView() {
        initGUI();
    }

    private void initGUI() {
        this.setLayout(new BorderLayout());
        initToolBar();
        initCards();
    }

    private void initToolBar() {
        JPanel toolbar = ViewUtil.getTertiaryBannerPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));

        ButtonGroup rg = new ButtonGroup();

        JRadioButton b1 = new JRadioButton("All");
        JRadioButton b2 = new JRadioButton("Cohort");
        
        rg.add(b1);
        rg.add(b2);
        
        toolbar.add(Box.createHorizontalGlue());
        
        toolbar.add(b1);
        toolbar.add(b2);
        
        toolbar.add(Box.createHorizontalGlue());

        
        b1.setSelected(true);

        this.add(toolbar, BorderLayout.NORTH);
    }

    private void initCards() {
        initCohortCard();
    }

    private void initCohortCard() {
        JPanel h1 = new JPanel();
        h1.setLayout(new GridLayout(1,1));
        
        h1.add(new SummaryChart(), BorderLayout.NORTH);
        
        this.add(h1,BorderLayout.CENTER);
    }
}
