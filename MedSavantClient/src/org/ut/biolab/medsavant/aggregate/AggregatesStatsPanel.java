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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TreeMap;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.ut.biolab.medsavant.cohort.CohortPanelGenerator;
import org.ut.biolab.medsavant.ontology.OntologyPanelGenerator;
import org.ut.biolab.medsavant.patient.FamilyPanelGenerator;
import org.ut.biolab.medsavant.region.RegionListPanelGenerator;
import org.ut.biolab.medsavant.util.ThreadController;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author Nirvana Nursimulu
 */
public class AggregatesStatsPanel extends JPanel {
    
    private JPanel toolBarPanel;
    private String currentRegionStat;
    private final String pageName;
    
    /**
     * List containing the name of region stats the user can view.
     */
    private TreeMap<String, AggregatePanelGenerator> panelMap = new TreeMap<String, AggregatePanelGenerator>();

    
    public AggregatesStatsPanel(String pageName) {
        this.pageName = pageName;
        setLayout(new BorderLayout());
        addPanels();
        initToolBar();
        updateRegionStats();
    }
    
    private void addPanels() {
        // Add your panel here.
        addPanel(new OntologyPanelGenerator(pageName));
        addPanel(new RegionListPanelGenerator(pageName));
        addPanel(new CohortPanelGenerator(pageName));
        addPanel(new FamilyPanelGenerator(pageName));
    }
    
    private void addPanel(AggregatePanelGenerator p) {
        panelMap.put(p.getName(), p);
    }
    
    private void updateRegionStats() {
                  
        ThreadController.getInstance().cancelWorkers(pageName);            
      
        removeAll();
        add(toolBarPanel, BorderLayout.NORTH);

        AggregatePanelGenerator gen = panelMap.get(currentRegionStat);
        JPanel p = gen.getPanel();
        gen.run(false);
        
        add(p);
        validate();
    }    
    
    private void initToolBar() {
        
        toolBarPanel = ViewUtil.getSubBannerPanel("Aggregate variants by");
        //toolBarPanel.setLayout(new BoxLayout(toolBarPanel, BoxLayout.X_AXIS));

        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        toolBarPanel.add(ViewUtil.clear(bar));
        
        toolBarPanel.add(Box.createHorizontalGlue());
        
//        bar.setFloatable(false);        
        JComboBox b = new JComboBox();
        
        for (String regionStatsName: panelMap.keySet()) {
            b.addItem(regionStatsName);
        }
        
        setCurrentRegionStats(panelMap.firstKey());
        
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                String regionStatsName = (String) cb.getSelectedItem();
                setCurrentRegionStats(regionStatsName);
                updateRegionStats();
            }
        });
        
        bar.add(b);
//        bar.add(Box.createHorizontalStrut(5));
        this.add(toolBarPanel, BorderLayout.NORTH);   
        this.updateUI();
    }

    private void setCurrentRegionStats(String regionStatsName) {
        currentRegionStat = regionStatsName;
    }
    
    public void update(boolean update, boolean isLoaded) {
        if (update) {
            for(String key : panelMap.keySet()) {
                panelMap.get(key).setUpdateRequired(true);
            }
        }
        if (isLoaded && panelMap != null && currentRegionStat != null) {
            panelMap.get(currentRegionStat).run(update);
        }
    }
}
