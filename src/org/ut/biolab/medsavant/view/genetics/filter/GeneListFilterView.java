/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import org.ut.biolab.medsavant.db.model.GenomicRegion;
import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.model.RangeCondition;
import org.ut.biolab.medsavant.db.model.RegionSet;
import org.ut.biolab.medsavant.db.util.query.RegionQueryUtil;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.log.ClientLogger;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultVariantTableSchema;

/**
 *
 * @author mfiume
 */
class GeneListFilterView {

    public static final String FILTER_NAME = "Gene List";
    public static final String FILTER_ID = "gene_list";
    private static final String GENELIST_NONE = "None";

    static FilterView getFilterView(int queryId) {
        return new FilterView(FILTER_NAME, getContentPanel(queryId));
    }
    
    private static List<RegionSet> getDefaultValues() {
        try {
            return RegionQueryUtil.getRegionSets();
        } catch (Exception ex){
            Logger.getLogger(GeneListFilterView.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private static JComponent getContentPanel(final int queryId) {

        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.setMaximumSize(new Dimension(1000,80));

        final JComboBox b = new JComboBox();
        b.setMaximumSize(new Dimension(1000,30));

        b.addItem(GENELIST_NONE);
        List<RegionSet> geneLists = getDefaultValues();
        for (RegionSet set : geneLists) {
            b.addItem(set);
        }
        
        final JButton applyButton = new JButton("Apply");
        applyButton.setEnabled(false);

        ActionListener al = new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                applyButton.setEnabled(false);

                Filter f = new QueryFilter() {

                    @Override
                    public Condition[] getConditions() {

                        if(b.getSelectedItem().equals(GENELIST_NONE)){
                            return new Condition[0];
                        }

                        RegionSet regionSet = (RegionSet) b.getSelectedItem();

                        try {

                            List<GenomicRegion> regions = RegionQueryUtil.getRegionsInRegionSet(regionSet.getId());
                            
                            Condition[] results = new Condition[regions.size()];
                            int i = 0;
                            for (GenomicRegion gr : regions) {
                                Condition[] tmp = new Condition[2];
                                
                                tmp[0] = BinaryCondition.equalTo(
                                        ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM), 
                                        gr.getChrom());

                                tmp[1] = new RangeCondition(
                                        ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_POSITION), 
                                        (long)gr.getRange().getMin(), 
                                        (long)gr.getRange().getMax());
                                                                
                                results[i] = ComboCondition.and(tmp);

                                i++;
                            }

                            return results;

                        } catch (Exception ex) {
                            ex.printStackTrace();
                            return null;
                        }
                    }

                    @Override
                    public String getName() {
                        return FILTER_NAME;
                    }


                    @Override
                    public String getId() {
                        return FILTER_ID;
                    }
                };
                ClientLogger.log(ClientLogger.class,"Adding filter: " + f.getName());
                FilterController.addFilter(f, queryId); //TODO
            }
        };
        applyButton.addActionListener(al);

        b.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                applyButton.setEnabled(true);
            }
        });



        JPanel bottomContainer = new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));
        bottomContainer.setMaximumSize(new Dimension(1000,30));

        bottomContainer.add(Box.createHorizontalGlue());
        bottomContainer.add(applyButton);


        p.add(b, BorderLayout.CENTER);
        p.add(bottomContainer, BorderLayout.SOUTH);

        //al.actionPerformed(null);
        return p;
    }
}
