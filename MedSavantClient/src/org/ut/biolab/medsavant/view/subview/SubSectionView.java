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
    private boolean updateRequired = true;

    public void setUpdateRequired(boolean required){
        this.updateRequired = required;
    }

    public boolean isUpdateRequired(){
        return this.updateRequired;
    }

    public SubSectionView(SectionView parent) {
        this.parent = parent;
    }

    public abstract String getName();

    public abstract JPanel getView(boolean update);

    public Component[] getSubSectionMenuComponents() { return null; }

    public SectionView getParent() {
        return this.parent;
    }

    public abstract void viewDidLoad();
    public abstract void viewDidUnload();
}
