/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.subview.SubSectionView;

/**
 *
 * @author mfiume
 */
public class GeneticsRegionsPage extends SubSectionView {

    public GeneticsRegionsPage(SectionView parent) { super(parent); }

    
    public String getName() {
        return "Regions";
    }

    public JPanel getView() {
        return new JPanel();
    }

}
