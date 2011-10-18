/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.manage;

import com.jidesoft.utils.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.ut.biolab.medsavant.db.model.RegionSet;
import org.ut.biolab.medsavant.db.util.query.RegionQueryUtil;
import org.ut.biolab.medsavant.view.patients.DetailedView;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class IntervalDetailedView extends DetailedView {

    private int limitNumberOfRegionsShown = 500;
    
    //private List<String> fieldNames;
    //private List<Object> fieldValues;
    private RegionDetailsSW sw;
    private final JPanel content;
    private final JPanel details;
    private RegionSet regionSet;
    
    private int numRegionsInRegionList;

    @Override
    public void setMultipleSelections(List<Vector> selectedRows) {
        //System.err.println("Multiple selections of regions not supported yet!");
    }
    
    private class RegionDetailsSW extends SwingWorker {
        private final RegionSet regionSet;
        private final int limit;

        public RegionDetailsSW(RegionSet regionSet, int limit) {
            this.regionSet = regionSet;
            this.limit = limit;
        }
        
        @Override
        protected Object doInBackground() throws Exception {
            //numRegionsInRegionList = QueryUtil.getNumRegionsInRegionSet(regionName);
            //List<Vector> regionList = QueryUtil.getRegionNamesInRegionSet(regionName,limit);
            numRegionsInRegionList = RegionQueryUtil.getNumberRegions(regionSet.getId());
            List<String> regionList = RegionQueryUtil.getRegionNamesInRegionSet(regionSet.getId(), limit);
            return regionList;
        }
        
        @Override
        protected void done() {
            try {
                List<String> result = (List<String>) get();
                setRegionList(result);
                
            } catch (Exception ex) {
                return;
            }
        }
        
    }

    public synchronized void setRegionList(List<String> regions) {

        details.removeAll();
        
        details.setLayout(new BorderLayout());
        //.setLayout(new BoxLayout(details,BoxLayout.Y_AXIS));
        
        JPanel h1 = ViewUtil.getClearPanel();
        h1.setLayout(new BoxLayout(h1,BoxLayout.Y_AXIS));
        
        h1.add(ViewUtil.getKeyValuePairPanel("Regions in set", ViewUtil.numToString(numRegionsInRegionList)));
        
        if (numRegionsInRegionList != regions.size()) {
            JLabel l = new JLabel("Showing first " + regions.size());
            l.setForeground(Color.white);
            h1.add(ViewUtil.getLeftAlignedComponent(l));
        }
        
        details.add(h1,BorderLayout.NORTH);
        
        DefaultListModel lm = new DefaultListModel();
        for (String region : regions) {
            //JLabel l = new JLabel(v.get(1).toString()); 
            //l.setForeground(Color.white);
            lm.addElement(region);
        }
        JList list = (JList) ViewUtil.clear(new JList(lm));
        list.setBackground(ViewUtil.getDetailsBackgroundColor());
        list.setForeground(Color.white);
        JScrollPane jsp = ViewUtil.getClearBorderlessJSP(list);
        details.add(jsp, BorderLayout.CENTER);
        //list.setOpaque(false);

        details.updateUI();
    }
    
    public IntervalDetailedView() {
        //fieldNames = MedSavantDatabase.getInstance().getSubjectTableSchema().getFieldAliases();
        
        content = this.getContentPanel();
        
        details = ViewUtil.getClearPanel();
        
        JButton deleteButton = new JButton("Delete region list");
        deleteButton.setOpaque(false);
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(regionSet != null){
                    int result = JOptionPane.showConfirmDialog(
                            null,
                            "Are you sure you want to delete " + regionSet.getName() + "?\nThis cannot be undone.",
                            "Confirm", 
                            JOptionPane.YES_NO_OPTION);
                    if (result != JOptionPane.YES_OPTION) return;
                    try {
                        RegionQueryUtil.removeRegionList(regionSet.getId());
                    } catch (SQLException ex) {
                        Logger.getLogger(IntervalDetailedView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    parent.refresh();
                }
            }
        });
        
        this.addBottomComponent(deleteButton);
        
        content.setLayout(new BorderLayout());
        
        content.add(details,BorderLayout.CENTER);
    }
    
    @Override
    public void setSelectedItem(Vector item) {
        
        
        RegionSet regionList = (RegionSet) item.get(0);
        setTitle(regionList.getName());
        this.regionSet = regionList;
        
        details.removeAll();
        details.updateUI();
        
        if (sw != null) {
            sw.cancel(true);
        }
        sw = new RegionDetailsSW(regionList,limitNumberOfRegionsShown);
        sw.execute();
    }
    
}
