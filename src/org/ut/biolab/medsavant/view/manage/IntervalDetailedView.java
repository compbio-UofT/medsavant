/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.manage;

import com.jidesoft.utils.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;
import java.util.Vector;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.ut.biolab.medsavant.olddb.QueryUtil;
import org.ut.biolab.medsavant.olddb.table.GeneListViewTableSchema;
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
    
    private int numRegionsInRegionList;

    @Override
    public void setMultipleSelections(List<Vector> selectedRows) {
        System.err.println("Multiple selections of regions not supported yet!");
    }
    
    private class RegionDetailsSW extends SwingWorker {
        private final String regionName;
        private final int limit;

        public RegionDetailsSW(String regionName, int limit) {
            this.regionName = regionName;
            this.limit = limit;
        }
        
        @Override
        protected Object doInBackground() throws Exception {
            numRegionsInRegionList = QueryUtil.getNumRegionsInRegionSet(regionName);
            List<Vector> regionList = QueryUtil.getRegionsInRegionSet(regionName,limit);
            return regionList;
        }
        
        @Override
        protected void done() {
            try {
                List<Vector> result = (List<Vector>) get();
                setRegionList(result);
                
            } catch (Exception ex) {
                return;
            }
        }
        
    }

    public synchronized void setRegionList(List<Vector> regions) {

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
        for (Vector v : regions) {
            //JLabel l = new JLabel(v.get(1).toString()); 
            //l.setForeground(Color.white);
            lm.addElement((String) v.get(GeneListViewTableSchema.INDEX_DESCRIPTION-1));
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
        
        this.addBottomComponent(new JButton("Delete region list"));
        
        content.setLayout(new BorderLayout());
        
        content.add(details,BorderLayout.CENTER);
    }
    
    @Override
    public void setSelectedItem(Vector item) {
        String regionListName = (String) item.get(0);
        setTitle(regionListName);
        
        details.removeAll();
        details.updateUI();
        
        if (sw != null) {
            sw.cancel(true);
        }
        sw = new RegionDetailsSW(regionListName,limitNumberOfRegionsShown);
        sw.execute();
    }
    
}
