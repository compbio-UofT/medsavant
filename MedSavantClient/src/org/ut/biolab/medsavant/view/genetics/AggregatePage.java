/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.ut.biolab.medsavant.view.genetics;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JPanel;

import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.reference.ReferenceController;
import org.ut.biolab.medsavant.controller.ThreadController;
import org.ut.biolab.medsavant.listener.ReferenceListener;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;


/**
 *
 * @author mfiume
 */
public class AggregatePage extends SubSectionView implements FiltersChangedListener, ReferenceListener {

    private JPanel panel;
    private AggregatesStatsPanel asp;
    private boolean isLoaded = false;

    public AggregatePage(SectionView parent) {
        super(parent);
        FilterController.addFilterListener(this);
        ReferenceController.getInstance().addReferenceListener(this);
    }

    @Override
    public String getName() {
        return "Enrichment";
    }

    @Override
    public JPanel getView(boolean update) {
        if (panel == null) {
            panel = new JPanel();
            panel.setLayout(new BorderLayout());

            asp = new AggregatesStatsPanel(getName());
            panel.add(asp, BorderLayout.CENTER);
        }
        if (asp != null) {
            asp.update(update, isLoaded);
        }
        return panel;
    }

    public Component[] getSubSectionMenuComponents() {
//        Component[] cs = new Component[1];
//        JButton addButton = new JButton("Add Region Statistics");
//        addButton.addActionListener(new ActionListener() {
//
//            public void actionPerformed(ActionEvent e) {
//                rsc.addRegionStats();
//            }
//        });
//        cs[0] = addButton;
//        return cs;
        return null;
    }

    @Override
    public void viewDidLoad() {
        isLoaded = true;
        if (asp != null) {
            asp.update(false, isLoaded);
        }
    }

    @Override
    public void viewDidUnload() {
        isLoaded = false;
        ThreadController.getInstance().cancelWorkers(getName());
    }

    @Override
    public void filtersChanged() {
        ThreadController.getInstance().cancelWorkers(getName());
        if (asp != null) {
            asp.update(true, isLoaded);
        }
    }

    @Override
    public void referenceAdded(String name) {
    }

    @Override
    public void referenceRemoved(String name) {
    }

    @Override
    public void referenceChanged(String name) {
        if (asp != null) {
            asp.update(true, isLoaded);
        }
    }
}
