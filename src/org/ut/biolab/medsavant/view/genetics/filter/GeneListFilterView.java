/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import org.ut.biolab.medsavant.model.GenomicRegion;
import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.jidesoft.range.Range;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.olddb.MedSavantDatabase;
import org.ut.biolab.medsavant.olddb.QueryUtil;
import org.ut.biolab.medsavant.olddb.table.TableSchema;
import org.ut.biolab.medsavant.olddb.table.VariantTableSchema;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.model.record.VariantRecordModel;

/**
 *
 * @author mfiume
 */
class GeneListFilterView {

    private static final String FILTER_NAME = "Gene List";
    private static final String GENELIST_NONE = "None";

    static FilterView getFilterView() {
        return new FilterView(FILTER_NAME, getContentPanel());
    }
    
    private static List<String> getDefaultValues() {
        List<String> list = FilterCache.getDefaultValues(FILTER_NAME);
        if(list == null){
            try {
                list = QueryUtil.getDistinctGeneListNames();
            } catch (Exception ex) {
                ex.printStackTrace();
                Logger.getLogger(GeneListFilterView.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
        FilterCache.addDefaultValues(MedSavantDatabase.getInstance().getGeneListTableSchema().getTable().getTableNameSQL(), FILTER_NAME, list);
        return list;
    }   

    private static JComponent getContentPanel() {

        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());

        final JComboBox b = new JComboBox();

        b.addItem(GENELIST_NONE);
        List<String> geneListNames = getDefaultValues();
        for (String name : geneListNames) {
            b.addItem(name);
        }
        
        final JButton applyButton = new JButton("Apply");
        applyButton.setEnabled(false);

        applyButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                applyButton.setEnabled(false);

                if (((String) b.getSelectedItem()).equals(GENELIST_NONE)) {
                    FilterController.removeFilter(FILTER_NAME, 0); //TODO
                } else {
                    Filter f = new QueryFilter() {

                        @Override
                        public Condition[] getConditions() {

                            String geneListName = (String) b.getSelectedItem();
                            
                            TableSchema variantSchema = MedSavantDatabase.getInstance().getVariantTableSchema();
                            
                            try {

                                List<GenomicRegion> regions = QueryUtil.getGenomicRangesForRegionList(geneListName);

                                Condition[] results = new Condition[regions.size()];
                                int i = 0;
                                for (GenomicRegion gr : regions) {
                                    Condition[] tmp = new Condition[2];
                                    tmp[0] = BinaryCondition.equalTo(variantSchema.getDBColumn(VariantTableSchema.ALIAS_CHROM), gr.getChrom());
                                    tmp[1] = QueryUtil.getRangeCondition(variantSchema.getDBColumn(VariantTableSchema.ALIAS_POSITION), gr.getRange());
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
                            return FILTER_NAME;//TODO
                        }
                    };
                    //Filter f = new VariantRecordFilter(acceptableValues, fieldNum);
                    FilterController.addFilter(f, 0); //TODO
                }

                //TODO: why does this not work? Freezes GUI
                //apply.setEnabled(false);
            }
        });

        b.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                applyButton.setEnabled(true);
            }
        });



        JPanel bottomContainer = new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));

        bottomContainer.add(Box.createHorizontalGlue());
        bottomContainer.add(applyButton);

        /*
        addChangeListener(new ChangeListener() {
        
        public void stateChanged(ChangeEvent e) {
        applyButton.setEnabled(true);
        }
        });
         * 
         */

        p.add(b, BorderLayout.CENTER);
        p.add(bottomContainer, BorderLayout.SOUTH);

        return p;
    }
}
