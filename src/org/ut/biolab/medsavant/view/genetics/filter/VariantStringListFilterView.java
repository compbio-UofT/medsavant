/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.util.query.VariantQueryUtil;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.olddb.ConnectionController;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.view.util.ChromosomeComparator;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author AndrewBrook
 */
public class VariantStringListFilterView {
    
    //public static FilterView createFilterView(final TableSchema table, final String columnAlias) throws SQLException, NonFatalDatabaseException {
    public static FilterView createFilterView(String tablename, final String columnname, final int queryId, final String alias) throws SQLException, NonFatalDatabaseException {
    
        //DbColumn col = table.getDBColumn(columnAlias);
        Connection conn = ConnectionController.connect();
        //boolean isVariantTableSchema = table.getTable().getTableNameSQL().equals(VariantTableSchema.TABLE_NAME);

        final List<String> uniq;

        if (columnname.equals("ac")) {
            uniq = new ArrayList<String>();
            uniq.addAll(Arrays.asList(
                    new String[]{
                        "1","2"
                    }));
        } else if (columnname.equals("af")) {
            uniq = new ArrayList<String>();
            uniq.addAll(Arrays.asList(
                    new String[]{
                        "0.50","1.00"
                    }));
        } else if (columnname.equals("ref")
                || columnname.equals("alt")) {
            uniq = new ArrayList<String>();
            uniq.addAll(Arrays.asList(
                    new String[]{
                        "A","C","G","T"
                    }));
        } 
        else {
            //List<String> tmp = FilterCache.getDefaultValues(columnAlias);
            //if(tmp == null){
                //tmp = QueryUtil.getDistinctValuesForColumn(conn, table, col);
            //} else {
            //}
            //FilterCache.addDefaultValues(table.getTable().getTableNameSQL(), columnAlias, tmp);
            //uniq = tmp;
            uniq = VariantQueryUtil.getDistinctValuesForColumn(tablename, columnname);
        }

        if (columnname.equals("chrom")) {
            Collections.sort(uniq,new ChromosomeComparator());
        }

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        JPanel bottomContainer = new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));
        bottomContainer.setMaximumSize(new Dimension(10000,30));

        final JButton applyButton = new JButton("Apply");
        applyButton.setEnabled(false);
        final List<JCheckBox> boxes = new ArrayList<JCheckBox>();
        
        ActionListener al = new ActionListener(){

            public void actionPerformed(ActionEvent e) {

                applyButton.setEnabled(false);

                final List<String> acceptableValues = new ArrayList<String>();
                for (JCheckBox b : boxes) {
                    if (b.isSelected()) {
                        acceptableValues.add(b.getText());
                    }
                }

                //if (acceptableValues.size() == boxes.size()) {
                //    FilterController.removeFilter(columnname, queryId);
                //} else {
                    Filter f = new QueryFilter() {

                        @Override
                        public Condition[] getConditions() {
                            Condition[] results = new Condition[acceptableValues.size()];
                            int i = 0;
                            //DbColumn tempCol = MedSavantDatabase.getInstance().getVariantTableSchema().createTempColumn(table.getDBColumn(columnAlias));
                            for (String s : acceptableValues) {
                                //if(columnAlias.equals(VariantTableSchema.ALIAS_GT)){
                                //    results[i++] = BinaryCondition.equalTo(MedSavantDatabase.getInstance().getVariantTableSchema().getDBColumn(columnAlias), uniq.indexOf(s));
                                //} else {
                                    //results[i++] = BinaryCondition.equalTo(tempCol, s);                                           
                                //}
                                results[i++] = BinaryCondition.equalTo(new DbColumn(ProjectController.getInstance().getCurrentVariantTable(), columnname, "varchar", 1), s);
                            }
                            return results;
                        }

                        @Override
                        public String getName() {
                            return alias;
                        }

                        @Override
                        public String getId() {
                            return columnname;
                        }

                    };
                    //Filter f = new VariantRecordFilter(acceptableValues, fieldNum);
                    FilterController.addFilter(f, queryId);
                //}

                //TODO: why does this not work? Freezes GUI
                //apply.setEnabled(false);
            }
        };      
        applyButton.addActionListener(al);
        
        for (String s : uniq) {
            JCheckBox b = new JCheckBox(s);
            b.setSelected(true);
            b.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    AbstractButton abstractButton =
                            (AbstractButton) e.getSource();
                    ButtonModel buttonModel = abstractButton.getModel();
                    boolean pressed = buttonModel.isPressed();
                    if (pressed) {
                        applyButton.setEnabled(true);
                    }
                }
            });
            b.setAlignmentX(0F);
            b.setAlignmentY(0f);
            container.add(b);
            boxes.add(b);
        }

        JButton selectAll = ViewUtil.createHyperLinkButton("Select All");
        selectAll.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                for (JCheckBox c : boxes) {
                    c.setSelected(true);                   
                }
                applyButton.setEnabled(true);
            }
        });
        bottomContainer.add(selectAll);

        JButton selectNone = ViewUtil.createHyperLinkButton("Select None");

        selectNone.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                for (JCheckBox c : boxes) {
                    c.setSelected(false);
                    applyButton.setEnabled(true);
                }
            }
        });
        bottomContainer.add(selectNone);

        bottomContainer.add(Box.createHorizontalGlue());

        bottomContainer.add(applyButton);
        
        bottomContainer.add(Box.createRigidArea(new Dimension(5,30)));

        bottomContainer.setAlignmentX(0F);
        container.add(bottomContainer); 
        
        al.actionPerformed(null);        
        return new FilterView(alias, container);
    }
}
