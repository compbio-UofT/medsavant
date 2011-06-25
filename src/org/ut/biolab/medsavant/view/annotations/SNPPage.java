/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.annotations;

import java.awt.Component;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;

/**
 *
 * @author mfiume
 */
public class SNPPage extends SubSectionView {

    public SNPPage(SectionView parent) { super(parent); }

    
    public String getName() {
        return "SNPs";
    }

    public JPanel getView() {
        return new JPanel();
    }
    
}
