/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.util.PeekingPanel;
import org.ut.biolab.medsavant.view.genetics.filter.FilterPanel;
import org.ut.biolab.medsavant.view.genetics.filter.FilterProgressPanel;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;

/**
 *
 * @author mfiume
 */
public class GeneticsFilterPage extends SubSectionView {

    private JPanel view;
    private FilterPanel fp;
    
    public GeneticsFilterPage(SectionView parent) {
        super(parent);
    }

    public String getName() {
        return "Filter";
    }

    public JPanel getView(boolean update) {
        if (view == null || update) {
            view = new JPanel();
            view.setLayout(new BorderLayout());
            fp = new FilterPanel();
            view.add(fp,BorderLayout.CENTER);
            view.add(new PeekingPanel("History", BorderLayout.WEST, new FilterProgressPanel(), true), BorderLayout.EAST);
        } else {
            fp.refreshSubPanels();
        }

        return view;
    }

    @Override
    public void viewDidLoad() {
    }

    @Override
    public void viewDidUnload() {
    }
}
