/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.TreeMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.ut.biolab.medsavant.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.view.genetics.aggregates.AggregatePanelGenerator;
import org.ut.biolab.medsavant.view.genetics.aggregates.GOsubPanel;
import org.ut.biolab.medsavant.view.genetics.aggregates.GeneListPanelGenerator;
import org.ut.biolab.medsavant.view.genetics.aggregates.HPOsubPanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;



/**
 *
 * @author Nirvana Nursimulu
 */
public class AggregatesStatsPanel extends JPanel implements FiltersChangedListener{
    
    private JPanel toolBarPanel;
    private String currentRegionStat;
    
    /**
     * List containing the name of region stats the user can view.
     */
    private TreeMap<String, AggregatePanelGenerator> panelMap = new TreeMap<String, AggregatePanelGenerator>();

    
    public AggregatesStatsPanel(){
        this.setLayout(new BorderLayout());
        addPanels();
        initToolBar();
        updateRegionStats();
    }
    
    private void addPanels() {
        //addPanel(new GOsubPanel());
        //addPanel(new HPOsubPanel());
        // Add your panel here.
        addPanel(new OntologyPanelGenerator());
        addPanel(new GeneListPanelGenerator());
    }
    
    private void addPanel(AggregatePanelGenerator p) {
        panelMap.put(p.getName(), p);
    }
    
    private void updateRegionStats(){
        
//        System.out.println("Entered updateRegionStats");
//        System.out.println("About to stop all region stats");
        stopAll(currentRegionStat);
//        System.out.println("Getting from dictionary...");
        AggregatePanelGenerator panelObj = panelMap.get(currentRegionStat);
//        System.out.println("Gotten object!");
        // TODO: figure out why UI is frozen at all after the last line has been executed.
        
        this.removeAll();
//        System.out.println("Was able to remove all...");
        // When UI froze, NN noticed that the above line was not printed?! Is that right?
        this.add(toolBarPanel, BorderLayout.NORTH);
//        System.out.println("Able to add...");
        
        this.add(panelObj.getPanel()); 
//        System.out.println("So, now we've added the object: " + currentRegionStat);
        this.updateUI();
    }    
    
    private void initToolBar(){
        
        toolBarPanel = ViewUtil.getSubBannerPanel("Aggregate variants by");
        //toolBarPanel.setLayout(new BoxLayout(toolBarPanel, BoxLayout.X_AXIS));

        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        toolBarPanel.add(ViewUtil.clear(bar));
        
        toolBarPanel.add(Box.createHorizontalGlue());
        
//        bar.setFloatable(false);        
        JComboBox b = new JComboBox();
        
        for (String regionStatsName: panelMap.keySet()){
            b.addItem(regionStatsName);
        }
        
        setCurrentRegionStats(panelMap.firstKey());
        
        b.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                String regionStatsName = (String) cb.getSelectedItem();
                setCurrentRegionStats(regionStatsName);
                
//                System.out.println("setCurrentRegionStats  " + regionStatsName);
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

    public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {
        stopAll(panelMap.firstKey());
//        ((AggregatePanelGenerator)panelMap.firstEntry()).setUpdate(true);
    }
    
    /**
     * Stop updating all panels except for this panel.
     * @param exceptThisPanel 
     */
    private void stopAll(String exceptThisPanel){        
        // Set all panels to not be updated.
        for (String panelName: panelMap.keySet()){
            if (!panelName.equals(exceptThisPanel)){
                panelMap.get(panelName).setUpdate(false);
            }
            else{
                panelMap.get(panelName).setUpdate(true);
            }
        }
    }
      
}
