/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.patients.individual;

import java.awt.Component;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.ThreadController;
import org.ut.biolab.medsavant.view.list.SplitScreenView;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;

/**
 *
 * @author mfiume
 */
public class IndividualsPage extends SubSectionView {
    private SplitScreenView view;

    public IndividualsPage(SectionView parent) { super(parent); }

    public String getName() {
        return "Individuals";
    }

    public JPanel getView(boolean update) {
        view = new SplitScreenView(
                new IndividualListModel(),
                new IndividualDetailedView(),
                new IndividualDetailEditor());
        return view;
    }

    public Component[] getSubSectionMenuComponents() {
        Component[] result = new Component[0];
        //result[0] = getAddPatientsButton();
        return result;
    }

    @Override
    public void viewDidLoad() {
    }

    @Override
    public void viewDidUnload() {
        ThreadController.getInstance().cancelWorkers(getName());
    }

}
