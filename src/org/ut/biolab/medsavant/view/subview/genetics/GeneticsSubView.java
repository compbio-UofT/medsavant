/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.subview.genetics;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import medsavant.exception.AccessDeniedDatabaseException;
import org.ut.biolab.medsavant.view.filter.FilterPanel;
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
    public JPanel getPersistentPanel() {
        try {
            //TODO: account for exception in filter panel instead
            return new FilterPanel();
        } catch (AccessDeniedDatabaseException ex) {
            return null;
        }
    }

    

}
