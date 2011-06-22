/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.patients;

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
public class PatientsSubView extends SubView {

    @Override
    public String getName() {
        return "Patients";
    }

    @Override
    public Page[] getPages() {
        Page[] pages = new Page[2];
        pages[0] = new IndividualsPage();
        pages[1] = new CohortsPage();
        return pages;
    }

    @Override
    public JPanel[] getPersistentPanels() {
        return null;
    }

}
