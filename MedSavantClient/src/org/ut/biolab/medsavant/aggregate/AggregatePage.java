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
package org.ut.biolab.medsavant.aggregate;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JPanel;

import org.ut.biolab.medsavant.api.Listener;
import org.ut.biolab.medsavant.filter.FilterController;
import org.ut.biolab.medsavant.filter.FilterEvent;
import org.ut.biolab.medsavant.reference.ReferenceController;
import org.ut.biolab.medsavant.util.ThreadController;
import org.ut.biolab.medsavant.reference.ReferenceEvent;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;


/**
 *
 * @author mfiume
 */
public class AggregatePage extends SubSectionView {

    private JPanel panel;
    private AggregatesStatsPanel statsPanel;
    private boolean isLoaded = false;

    public AggregatePage(SectionView parent) {
        super(parent);
        FilterController.getInstance().addListener(new Listener<FilterEvent>() {
            @Override
            public void handleEvent(FilterEvent event) {
                ThreadController.getInstance().cancelWorkers(getName());
                if (statsPanel != null) {
                    statsPanel.update(true, isLoaded);
                }
            }
        });
        ReferenceController.getInstance().addListener(new Listener<ReferenceEvent>() {
            @Override
            public void handleEvent(ReferenceEvent evt) {
                if (evt.getType() == ReferenceEvent.Type.CHANGED && statsPanel != null) {
                    statsPanel.update(true, isLoaded);
                }
            }
        });
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

            statsPanel = new AggregatesStatsPanel(getName());
            panel.add(statsPanel, BorderLayout.CENTER);
        }
        if (statsPanel != null) {
            statsPanel.update(update, isLoaded);
        }
        return panel;
    }

    @Override
    public Component[] getSubSectionMenuComponents() {
        return null;
    }

    @Override
    public void viewDidLoad() {
        super.viewDidLoad();
        isLoaded = true;
        if (statsPanel != null) {
            statsPanel.update(false, isLoaded);
        }
    }

    @Override
    public void viewDidUnload() {
        isLoaded = false;
        super.viewDidUnload();
    }
}
