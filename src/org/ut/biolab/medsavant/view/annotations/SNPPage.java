/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.annotations;

import java.awt.Component;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.subview.SubSectionView;

/**
 *
 * @author mfiume
 */
public class SNPPage implements SubSectionView {

    public String getName() {
        return "SNPs";
    }

    public JPanel getView() {
        return new JPanel();
    }

    public Component getBanner() {
        return new JPanel();
    }
    
}
