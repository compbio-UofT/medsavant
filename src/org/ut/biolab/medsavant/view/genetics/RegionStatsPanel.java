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
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
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
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.util.WaitPanel;



/**
 *
 * @author Nirvana Nursimulu
 */
public class RegionStatsPanel extends JPanel implements FiltersChangedListener{
    
    private JToolBar bar;
    private String currentRegionStat;
    private RegionStatsWorker rsw;
    private WaitPanel waitPanel;
    
    /**
     * List containing the name of region stats the user can view.
     */
    private static final String[] regionStatsNames = {"Gene Ontology", "Human Phenotype Ontology"};
    

    
    public RegionStatsPanel(){
        this.setLayout(new BorderLayout());

        initToolBar();
        updateRegionStats();
        FilterController.addFilterListener(this);
    }
    
    private void updateRegionStats(){
        
//        this.remove(bar);
        waitPanel = new WaitPanel("Getting region statistics");
        this.add(waitPanel, BorderLayout.CENTER);
        this.updateUI();
        
        // Kill any existing threads, if any.
        if (rsw != null && !rsw.isDone()){  rsw.cancel(true);     }
        
        rsw = new RegionStatsWorker(currentRegionStat, this, waitPanel);
        rsw.execute();
//        this.updateUI();
    }  
    
//    private void drawRegionStats(JComponent component){
//        // Remove the wait panel first.
//        this.remove(waitPanel);
//        this.add(component);
//    }    
    
    private void initToolBar(){
        
        JPanel toolBarPanel = ViewUtil.getBannerPanel();
        toolBarPanel.setBorder(ViewUtil.getMediumBorder());
        toolBarPanel.setLayout(new BoxLayout(toolBarPanel, BoxLayout.X_AXIS));

        toolBarPanel.add(Box.createHorizontalGlue());

        toolBarPanel.add(new JLabel("Region statistics for: "));      
        bar = new JToolBar();
        bar.setFloatable(false);
        toolBarPanel.add(bar);
        
//        bar.setFloatable(false);        
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
//        bar.add(Box.createHorizontalStrut(5));
        this.add(toolBarPanel, BorderLayout.NORTH);   
        this.updateUI();
    }

    private void setCurrentRegionStats(String regionStatsName) {
        currentRegionStat = regionStatsName;
    }
    
    
    public class RegionStatsWorker extends SwingWorker{
        
        private String regionStatsName;
        private JPanel panel;
        private JPanel waitPanel;
        
        public RegionStatsWorker(String regionStatsName, JPanel panel, WaitPanel waitPanel){
            this.regionStatsName = regionStatsName;
            this.panel = panel;
            this.waitPanel = waitPanel;
        }
        
        // TODO: make this more general if possible, for future purposes.
        private JComponent getRegionStatsFor(String regionStatsName){
            
                JComponent component = null;
                if (regionStatsName.equals("Gene Ontology")){  
                    // TODO: change this approach: what if the GO tree is never loaded?
                    while (!FilterObjectStorer.containsObjectWithName(GOFilter.NAME_TREE))
                        ;
                    Object o = FilterObjectStorer.getObject(GOFilter.NAME_TREE);
                    component = ConstructJTree.getTree((Tree)o, true, false);
//                    System.out.println("Gene Ontology tree constructed for Regions stats.");
                }
                else if (regionStatsName.equals("Human Phenotype Ontology")){
                    // TODO: change this approach: what if the HPO tree is never loaded?
                    while (!FilterObjectStorer.containsObjectWithName(HPOFilter.NAME_TREE))
                        ;
                    Object o = FilterObjectStorer.getObject(HPOFilter.NAME_TREE);
                    component = ConstructJTree.getTree((Tree)o, false, false);
//                    System.out.println("HPO tree constructed for Region stats");
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
//                System.out.println("Beginning of done");
                JScrollPane scrollPane = new JScrollPane((JComponent)get());

            // Remove the wait panel first.
            panel.remove(waitPanel);
            panel.add(scrollPane);
            panel.updateUI();
        
//                System.out.println("Ending of done");
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
