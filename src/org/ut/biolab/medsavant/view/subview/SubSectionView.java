/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.subview;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public abstract class SubSectionView {

    private SectionView parent;

    
    public SubSectionView(SectionView parent) {
        this.parent = parent;
    }
    
    public abstract String getName();

    public abstract JPanel getView();

    public Component[] getBanner() { return null; }

    public SectionView getParent() {
        return this.parent;
    }
    
    public abstract void viewLoading();
    public abstract void viewDidUnload();
}
