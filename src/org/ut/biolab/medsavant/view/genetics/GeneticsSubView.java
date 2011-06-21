/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.genetics;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import medsavant.exception.AccessDeniedDatabaseException;
import org.ut.biolab.medsavant.view.genetics.filter.FilterPanel;
import org.ut.biolab.medsavant.view.subview.Page;
import org.ut.biolab.medsavant.view.subview.SubView;

/**
 *
 * @author mfiume
 */
public class GeneticsSubView extends SubView {

    @Override
    public String getName() {
        return "Genetics";
    }

    @Override
    public Page[] getPages() {
        Page[] pages = new Page[2];
        pages[0] = new GeneticsSummerizePage();
        pages[1] = new GeneticsListPage();
        return pages;
    }

    @Override
    public JPanel[] getPersistentPanels() {
        try {
            JPanel[] panels = new JPanel[2];
            panels[0] = new CohortPanel();
            panels[1] = new FilterPanel();
            //TODO: account for exception in filter panel instead
            return panels;
        } catch (AccessDeniedDatabaseException ex) {
            return null;
        }
    }

    

}
