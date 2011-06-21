/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics;

import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
class CohortPanel extends JPanel {

    public CohortPanel() {
        this.setName("Cohorts");
        this.setBackground(ViewUtil.getMenuColor());
        this.add(new JLabel("Settings go here"));
    }
    
}
