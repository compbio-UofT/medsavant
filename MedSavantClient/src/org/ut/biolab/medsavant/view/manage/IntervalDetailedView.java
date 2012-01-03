/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.manage;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.model.GenomicRegion;
import org.ut.biolab.medsavant.db.model.Range;
import org.ut.biolab.medsavant.db.model.RangeCondition;
import org.ut.biolab.medsavant.db.model.RegionSet;
import org.ut.biolab.medsavant.db.util.shared.BinaryConditionMS;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.view.genetics.filter.FilterPanelSubItem;
import org.ut.biolab.medsavant.view.genetics.filter.FilterUtils;
import org.ut.biolab.medsavant.view.list.DetailedView;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class IntervalDetailedView extends DetailedView {

    private int limitNumberOfRegionsShown = 500;

    private RegionDetailsSW sw;
    private final JPanel content;
    private final JPanel details;
    private int numRegionsInRegionList;
    private RegionSet regionSet;
    private static List<FilterPanelSubItem> filterPanels;

    @Override
    public void setMultipleSelections(List<Object[]> selectedRows) {

        //TODO: actually store them for possible deletion
        if (selectedRows.isEmpty()) {
                setTitle("");
            } else {
        setTitle("Multiple lists (" + selectedRows.size() + ")");
        }
        details.removeAll();
        details.updateUI();
    }

    @Override
    public void setRightClick(MouseEvent e) {
        JPopupMenu popup = createPopup(regionSet);
        popup.show(e.getComponent(), e.getX(), e.getY()); 
    }

    /*
    void removeSelectedRegionLists() {
        if(regionSet != null){
                    int result = JOptionPane.showConfirmDialog(
                            null,
                            "Are you sure you want to delete " + regionSet.getName() + "?\nThis cannot be undone.",
                            "Confirm",
                            JOptionPane.YES_NO_OPTION);
                    if (result != JOptionPane.YES_OPTION) return;
                    try {
                        MedSavantClient.RegionQueryUtilAdapter.removeRegionList(regionSet.getId());
                    } catch (SQLException ex) {
                        Logger.getLogger(IntervalDetailedView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    parent.refresh();
                }
    }
     *
     */

    private class RegionDetailsSW extends MedSavantWorker<List<String>> {
        private final RegionSet regionSet;
        private final int limit;

        public RegionDetailsSW(RegionSet regionSet, int limit) {
            super(getName());
            this.regionSet = regionSet;
            this.limit = limit;
        }

        protected List<String> doInBackground() throws Exception {
            //numRegionsInRegionList = QueryUtil.getNumRegionsInRegionSet(regionName);
            //List<Vector> regionList = QueryUtil.getRegionNamesInRegionSet(regionName,limit);
            numRegionsInRegionList = MedSavantClient.RegionQueryUtilAdapter.getNumberRegions(LoginController.sessionId, regionSet.getId());
            List<String> regionList = MedSavantClient.RegionQueryUtilAdapter.getRegionNamesInRegionSet(LoginController.sessionId, regionSet.getId(), limit);
            return regionList;
        }

        @Override
        protected void showProgress(double fraction) {
            //
        }

        @Override
        protected void showSuccess(List<String> result) {
            try {
                setRegionList(get());

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

        JList list = ViewUtil.getDetailList(lm);
        JScrollPane jsp = ViewUtil.getClearBorderlessJSP(list);
        details.add(jsp, BorderLayout.CENTER);
        //list.setOpaque(false);

        details.updateUI();
    }

    public IntervalDetailedView() {
        //fieldNames = MedSavantDatabase.getInstance().getSubjectTableSchema().getFieldAliases();

        content = this.getContentPanel();

        details = ViewUtil.getClearPanel();

        /*
        JButton deleteButton = new JButton("Delete region list");
        deleteButton.setOpaque(false);
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeSelectedRegionLists();
            }
        });

        this.addBottomComponent(deleteButton);
         *
         */

        content.setLayout(new BorderLayout());

        content.add(details,BorderLayout.CENTER);
    }

    @Override
    public void setSelectedItem(Object[] item) {

        RegionSet regionList = (RegionSet) item[0];
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
    
    private JPopupMenu createPopup(final RegionSet set){
        JPopupMenu popupMenu = new JPopupMenu();
        
        if(ProjectController.getInstance().getCurrentVariantTableSchema() == null){
            popupMenu.add(new JLabel("(You must choose a variant table before filtering)"));
        } else {

            //Filter by patient
            JMenuItem filter1Item = new JMenuItem("Filter by Region List");
            filter1Item.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    
                    try {
                        List<GenomicRegion> regions = MedSavantClient.RegionQueryUtilAdapter.getRegionsInRegionSet(LoginController.sessionId, set.getId());
                        Map<String, List<Range>> rangeMap = GenomicRegion.mergeGenomicRegions(regions);
                        Condition[] results = new Condition[rangeMap.size()];
                        int i = 0;
                        for(String chrom : rangeMap.keySet()){

                            Condition[] tmp = new Condition[2];

                            //add chrom condition
                            tmp[0] = BinaryConditionMS.equalTo(
                                    ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM), 
                                    chrom);

                            //create range conditions
                            List<Range> ranges = rangeMap.get(chrom);
                            Condition[] rangeConditions = new Condition[ranges.size()];
                            for(int j = 0; j < ranges.size(); j++){
                                rangeConditions[j] = new RangeCondition(
                                        ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_POSITION), 
                                        (long)ranges.get(j).getMin(), 
                                        (long)ranges.get(j).getMax());
                            }

                            //add range conditions
                            tmp[1] = ComboCondition.or(rangeConditions);

                            results[i] = ComboCondition.and(tmp);

                            i++;
                        }
                        
                        removeExistingFilters();
                        filterPanels = FilterUtils.createAndApplyGenericFixedFilter(
                                "Region Lists - Filter by List", 
                                regions.size() + " Region(s)", 
                                ComboCondition.or(results));
       
                    } catch (SQLException ex) {
                        Logger.getLogger(IntervalDetailedView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                }
            });
            popupMenu.add(filter1Item);          
        }

        return popupMenu;
    }
    
    private void removeExistingFilters(){
        if(filterPanels != null){
            for(FilterPanelSubItem panel : filterPanels){
                panel.removeThis();
            }
        }
    }

}
