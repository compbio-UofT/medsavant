/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.view.genetics.filter.GOFilter;
import org.ut.biolab.medsavant.view.genetics.filter.HPOFilter;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.ConstructJTree;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.Tree;
import org.ut.biolab.medsavant.view.genetics.storer.FilterObjectStorer;
import org.ut.biolab.medsavant.view.util.WaitPanel;



/**
 *
 * @author Nirvana Nursimulu
 */
public class RegionStatsPanel extends JPanel implements FiltersChangedListener{
    
    private JToolBar bar;
    private String currentRegionStat;
    private RegionStatsWorker rsw;
    
    /**
     * List containing the name of region stats the user can view.
     */
    private static final String[] regionStatsNames = {"Gene Ontology", "Human Phenotype Ontology"};
    
    /**
     * Maps the name of a region stats to its corresponding component (eg, JTree).
     */
    private HashMap<String, JComponent> regionStatToComp;

    
    public RegionStatsPanel(){
        this.setLayout(new BorderLayout());
        regionStatToComp = new HashMap<String, JComponent>();
        
        for (String regionStatsName: regionStatsNames){
            // At the beginning, no component has been created.
            regionStatToComp.put(regionStatsName, null);
        }
        initToolBar();
        updateRegionStats();
        FilterController.addFilterListener(this);
    }
    
    private void updateRegionStats(){
        
        this.removeAll();
        this.add(new WaitPanel("Getting region statistics"), BorderLayout.CENTER);
        this.updateUI();
        
        // Kill any existing threads, if any.
        if (rsw != null && !rsw.isDone()){  rsw.cancel(true);     }
        
        rsw = new RegionStatsWorker(currentRegionStat);
        rsw.execute();
    }  
    
    private synchronized void drawRegionStats(JComponent component){
        this.add(component);
    }    
    
    private void initToolBar(){
        bar = new JToolBar();
        bar.setFloatable(false);        
        JComboBox b = new JComboBox();
        
        for (String regionStatsName: regionStatsNames){
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
        bar.add(Box.createHorizontalStrut(5));
        this.add(bar, BorderLayout.NORTH);        
    }

    private void setCurrentRegionStats(String regionStatsName) {
        currentRegionStat = regionStatsName;
    }
    
    
    public class RegionStatsWorker extends SwingWorker{
        
        private String regionStatsName;
        
        public RegionStatsWorker(String regionStatsName){
            this.regionStatsName = regionStatsName;
        }
        
        // TODO: make this more general if possible, for future purposes.
        private JComponent getRegionStatsFor(String regionStatsName){
            
            // Get the component that is associated with this region stats.
            JComponent component = regionStatToComp.get(regionStatsName);
            
            // If the component hasn't been made yet, make it, and put into the
            // dictionary, so that it can be accessed later on.
            if (component == null){
                
                if (regionStatsName.equals("Gene Ontology")){                   
                    while (!FilterObjectStorer.containsObjectWithName(GOFilter.NAME_TREE))
                        ;
                    Object o = FilterObjectStorer.getObject(GOFilter.NAME_TREE);
                    component = ConstructJTree.getTree((Tree)o, true, false);
//                    System.out.println("dun dun dun");
                }
                else if (regionStatsName.equals("Human Phenotype Ontology")){
                    while (!FilterObjectStorer.containsObjectWithName(HPOFilter.NAME_TREE))
                        ;
                    Object o = FilterObjectStorer.getObject(HPOFilter.NAME_TREE);
                    component = ConstructJTree.getTree((Tree)o, false, false);
//                    System.out.println("dun dun dun");
                }
                
                regionStatToComp.put(regionStatsName, (JTree)component);
            }
            if (regionStatsName.equals("Gene Ontology") || 
                    regionStatsName.equals("Human Phenotype Ontology")){
             
                // Do something smart here with threads and what-not.
            }
            return component;
        }

        @Override
        protected Object doInBackground() throws Exception {
            return getRegionStatsFor(regionStatsName);
        }
        
        protected void done(){
            try {
                JScrollPane scrollPane = new JScrollPane((JComponent)get());
                drawRegionStats(scrollPane);
            } catch (Exception ex){
                ex.printStackTrace();
                Logger.getLogger(RegionStatsPanel.class.getName()).log(Level.SEVERE, null, ex);                
            }
        }
        
    }
    
    public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {
        updateRegionStats();
    }    
    
    
}
