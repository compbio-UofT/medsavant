/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TreeMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.ut.biolab.medsavant.view.genetics.aggregates.GOsubPanel;
import org.ut.biolab.medsavant.view.genetics.aggregates.HPOsubPanel;
import org.ut.biolab.medsavant.view.genetics.aggregates.OntologySubPanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;



/**
 *
 * @author Nirvana Nursimulu
 */
public class RegionStatsPanel extends JPanel{
    
    private JPanel toolBarPanel;
    private String currentRegionStat;
    
    /**
     * List containing the name of region stats the user can view.
     */
    private static final String[] regionStatsNames = {"Gene Ontology", "Human Phenotype Ontology"};
    private TreeMap<String, JPanel> panelMap = new TreeMap<String, JPanel>();

    
    public RegionStatsPanel(){
        this.setLayout(new BorderLayout());
        addPanels();
        initToolBar();
        updateRegionStats();
    }
    
    private void addPanels() {
        addPanel(new GOsubPanel());
        addPanel(new HPOsubPanel());
        // Add your panel here.
    }
    
    private void addPanel(JPanel p) {
        panelMap.put(p.getName(), p);
    }
    
    private void updateRegionStats(){
        
        this.removeAll();
        this.add(toolBarPanel, BorderLayout.NORTH);

        this.add(panelMap.get(currentRegionStat));    
        this.updateUI();
    }    
    
    private void initToolBar(){
        
        toolBarPanel = ViewUtil.getBannerPanel();
        toolBarPanel.setBorder(ViewUtil.getMediumBorder());
        toolBarPanel.setLayout(new BoxLayout(toolBarPanel, BoxLayout.X_AXIS));

        toolBarPanel.add(Box.createHorizontalGlue());

        toolBarPanel.add(new JLabel("Aggregate statistics by: "));      
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        toolBarPanel.add(ViewUtil.clear(bar));
        
        toolBarPanel.add(Box.createHorizontalGlue());
        
//        bar.setFloatable(false);        
        JComboBox b = new JComboBox();
        
        for (String regionStatsName: panelMap.keySet()){
            b.addItem(regionStatsName);
        }
        
        setCurrentRegionStats(regionStatsNames[0]);
        
        b.addActionListener(new ActionListener() {

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
      
}
