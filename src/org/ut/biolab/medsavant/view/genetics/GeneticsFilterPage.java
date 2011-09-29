/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics;

import java.awt.BorderLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.util.view.PeekingPanel;
import org.ut.biolab.medsavant.view.genetics.filter.FilterPanel;
import org.ut.biolab.medsavant.view.genetics.filter.FilterProgressPanel;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class GeneticsFilterPage extends SubSectionView {

    private JPanel view;

    public GeneticsFilterPage(SectionView parent) {
        super(parent);
    }

    public String getName() {
        return "Filter";
    }

    public JPanel getView(boolean update) {
        if (view == null || update) {
            try {
                view = new JPanel();
                view.setLayout(new BorderLayout());
                view.add(new FilterPanel(),BorderLayout.CENTER);
                view.add(new PeekingPanel("History", BorderLayout.WEST, new FilterProgressPanel(), true), BorderLayout.EAST);
            } catch (NonFatalDatabaseException ex) {
                view = ViewUtil.getMessagePanel("Error connecting to database");
                ex.printStackTrace();
            }
        }

        return view;
    }

    @Override
    public void viewLoading() {
    }

    @Override
    public void viewDidUnload() {
    }
}
