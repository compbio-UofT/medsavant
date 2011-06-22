/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.annotations;

import org.ut.biolab.medsavant.view.genetics.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.genetics.filter.FilterPanel;
import org.ut.biolab.medsavant.view.subview.Page;
import org.ut.biolab.medsavant.view.subview.SubView;

/**
 *
 * @author mfiume
 */
public class AnnotationsSubView extends SubView {

    @Override
    public String getName() {
        return "Annotations";
    }

    @Override
    public Page[] getPages() {
        Page[] pages = new Page[2];
        pages[0] = new GeneListsPage();
        pages[1] = new SNPPage();
        return pages;
    }

    @Override
    public JPanel[] getPersistentPanels() {
        return null;
    }

}
