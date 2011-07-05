/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics;

import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;

/**
 *
 * @author mfiume
 */
public class GeneticsTestPage extends SubSectionView {

    public GeneticsTestPage(SectionView parent) { super(parent); }

    
    public String getName() {
        return "Tests";
    }

    public JPanel getView() {
        return new JPanel();
    }

}
