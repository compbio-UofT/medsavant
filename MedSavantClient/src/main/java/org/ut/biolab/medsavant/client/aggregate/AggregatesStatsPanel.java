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

package org.ut.biolab.medsavant.client.aggregate;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.ut.biolab.medsavant.client.cohort.CohortAggregatePanel;
import org.ut.biolab.medsavant.client.ontology.OntologyAggregatePanel;
import org.ut.biolab.medsavant.client.patient.FamilyAggregatePanel;
import org.ut.biolab.medsavant.client.region.RegionListAggregatePanel;
import org.ut.biolab.medsavant.client.util.ThreadController;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author Nirvana Nursimulu
 */
public class AggregatesStatsPanel extends JPanel {

    private JComboBox generatorCombo;
    private JPanel toolBarPanel;
    private final String pageName;

    public AggregatesStatsPanel(String pageName) {
        this.pageName = pageName;
        setLayout(new BorderLayout());
        initToolBar();
        updateRegionStats();
    }

    private void updateRegionStats() {

        ThreadController.getInstance().cancelWorkers(pageName);

        removeAll();
        add(toolBarPanel, BorderLayout.NORTH);

        AggregatePanelGenerator gen = (AggregatePanelGenerator)generatorCombo.getSelectedItem();
        AggregatePanel p = gen.getPanel();
        p.recalculate();

        add(p);
        updateUI();
    }

    private void initToolBar() {

        toolBarPanel = ViewUtil.getSubBannerPanel("Aggregate variants by");
        //toolBarPanel.setLayout(new BoxLayout(toolBarPanel, BoxLayout.X_AXIS));

        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        toolBarPanel.add(ViewUtil.clear(bar));

        toolBarPanel.add(Box.createHorizontalGlue());

        generatorCombo = new JComboBox();
        generatorCombo.setPreferredSize(new Dimension(300,23));

        generatorCombo.addItem(new AggregatePanelGenerator() {
            @Override
            public String toString() { return "Region List"; }

            @Override
            public AggregatePanel generatePanel() { return new RegionListAggregatePanel(pageName); }
        });
        generatorCombo.addItem(new AggregatePanelGenerator() {
            @Override
            public String toString() { return "Family"; }

            @Override
            public AggregatePanel generatePanel() { return new FamilyAggregatePanel(pageName); }
        });
        generatorCombo.addItem(new AggregatePanelGenerator() {
            @Override
            public String toString() { return "Ontology"; }

            @Override
            public AggregatePanel generatePanel() { return new OntologyAggregatePanel(pageName); }
        });

        generatorCombo.addItem(new AggregatePanelGenerator() {
            @Override
            public String toString() { return "Cohort"; }

            @Override
            public AggregatePanel generatePanel() { return new CohortAggregatePanel(pageName); }
        });
        generatorCombo.setSelectedIndex(0);

        generatorCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateRegionStats();
            }
        });

        bar.add(generatorCombo);

        add(toolBarPanel, BorderLayout.NORTH);
    }

    public void update() {
        for (int i = 0; i < generatorCombo.getItemCount(); i++) {
            ((AggregatePanelGenerator)generatorCombo.getItemAt(i)).setUpdateRequired(true);
        }
        ((AggregatePanelGenerator)generatorCombo.getSelectedItem()).updateIfRequired();
    }
}
