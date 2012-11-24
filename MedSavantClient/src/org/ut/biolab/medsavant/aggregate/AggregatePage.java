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

    private JPanel view;
    private AggregatesStatsPanel statsPanel;

    public AggregatePage(SectionView parent) {
        super(parent, "Enrichment");
        FilterController.getInstance().addListener(new Listener<FilterEvent>() {
            @Override
            public void handleEvent(FilterEvent event) {
                ThreadController.getInstance().cancelWorkers(pageName);
                if (statsPanel != null) {
                    statsPanel.update();
                }
            }
        });
        ReferenceController.getInstance().addListener(new Listener<ReferenceEvent>() {
            @Override
            public void handleEvent(ReferenceEvent evt) {
                if (evt.getType() == ReferenceEvent.Type.CHANGED && statsPanel != null) {
                    statsPanel.update();
                }
            }
        });
    }

    @Override
    public JPanel getView() {
        if (view == null) {
            view = new JPanel();
            view.setLayout(new BorderLayout());

            statsPanel = new AggregatesStatsPanel(pageName);
            view.add(statsPanel, BorderLayout.CENTER);
        }
        if (statsPanel != null) {
            statsPanel.update();
        }
        return view;
    }

    @Override
    public Component[] getSubSectionMenuComponents() {
        return null;
    }
}
