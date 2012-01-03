/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import com.healthmarketscience.sqlbuilder.Condition;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultpatientTableSchema;
import org.ut.biolab.medsavant.db.util.shared.BinaryConditionMS;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.util.MiscUtils;
import org.ut.biolab.medsavant.view.genetics.filter.FilterState.FilterType;
import org.ut.biolab.medsavant.view.genetics.filter.FilterUtils.Table;
import org.ut.biolab.medsavant.view.util.ChromosomeComparator;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author Andrew
 */
public class StringListFilterView extends FilterView {
    
    /* Convenience Functions */
    
    public static FilterView createPatientFilterView(String tablename, String columnname, int queryId, String alias) throws SQLException, NonFatalDatabaseException, RemoteException {
        return new StringListFilterView(new JPanel(), tablename, columnname, queryId, alias, Table.PATIENT);
    }
    
    public static FilterView createVariantFilterView(String tablename, String columnname, int queryId, String alias) throws SQLException, NonFatalDatabaseException, RemoteException {
        return new StringListFilterView(new JPanel(), tablename, columnname, queryId, alias, Table.VARIANT);
    }
    
    public StringListFilterView(String tablename, String columnname, int queryId, String alias, Table whichTable) throws SQLException, RemoteException {
        this(new JPanel(), tablename, columnname, queryId, alias, whichTable);
    }
    
    public StringListFilterView(FilterState state, int queryId) throws SQLException, RemoteException {
        this(new JPanel(), FilterUtils.getTableName(Table.valueOf(state.getValues().get("table"))), state.getId(), queryId, state.getName(), Table.valueOf(state.getValues().get("table")));
        String values = state.getValues().get("values");
        if(values != null){
            List<String> l = new ArrayList<String>();
            Collections.addAll(l, values.split(";;;"));
            applyFilter(l);
        }
    }
       
    /* StringListFilterView */
    
    private List<JCheckBox> boxes;
    private ActionListener al;
    private String columnname;
    private String alias;
    private Table whichTable;
    private List<String> appliedValues;
    
    public void applyFilter(List<String> list){
        for(JCheckBox box : boxes){
            box.setSelected(list.contains(box.getText()));
        }
        al.actionPerformed(new ActionEvent(this, 0, null));
    }   
    
    private StringListFilterView(JComponent container, String tablename, final String columnname, int queryId, final String alias, final Table whichTable) throws SQLException, RemoteException {
        super(alias, container, queryId);
        
        this.columnname = columnname;
        this.alias = alias;
        this.whichTable = whichTable;
        
        final List<String> uniq;

        if (columnname.equals(DefaultVariantTableSchema.COLUMNNAME_OF_AC)) {
            uniq = new ArrayList<String>();
            uniq.addAll(Arrays.asList(
                    new String[]{
                        "1","2"
                    }));
        } else if (columnname.equals(DefaultVariantTableSchema.COLUMNNAME_OF_AF)) {
            uniq = new ArrayList<String>();
            uniq.addAll(Arrays.asList(
                    new String[]{
                        "0.50","1.00"
                    }));
        } else if (columnname.equals(DefaultVariantTableSchema.COLUMNNAME_OF_REF)
                || columnname.equals(DefaultVariantTableSchema.COLUMNNAME_OF_ALT)) {
            uniq = new ArrayList<String>();
            uniq.addAll(Arrays.asList(
                    new String[]{
                        "A","C","G","T"
                    }));
        } else if (columnname.equals(DefaultpatientTableSchema.COLUMNNAME_OF_GENDER)){
            uniq = new ArrayList<String>();
            uniq.addAll(Arrays.asList(
                    new String[]{
                        MiscUtils.GENDER_MALE, MiscUtils.GENDER_FEMALE, MiscUtils.GENDER_UNKNOWN
                    }));
        } else {
            uniq = MedSavantClient.VariantQueryUtilAdapter.getDistinctValuesForColumn(LoginController.sessionId, tablename, columnname);
        }

        if (columnname.equals(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM)) {
            Collections.sort(uniq,new ChromosomeComparator());
        }

        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        JPanel bottomContainer = new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));
        bottomContainer.setMaximumSize(new Dimension(10000,30));

        final JButton applyButton = new JButton("Apply");
        applyButton.setEnabled(false);
        boxes = new ArrayList<JCheckBox>();
        
        al = new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                
                applyButton.setEnabled(false);

                final List<String> acceptableValues = new ArrayList<String>();
                for (JCheckBox b : boxes) {
                    if (b.isSelected()) {
                        if(columnname.equals(DefaultpatientTableSchema.COLUMNNAME_OF_GENDER)){
                            acceptableValues.add(Integer.toString(MiscUtils.stringToGender(b.getText())));
                        } else {
                            acceptableValues.add(b.getText());
                        }
                    }
                }
                appliedValues = acceptableValues;

                Filter f = new QueryFilter() {

                    @Override
                    public Condition[] getConditions() {                       
                        if(whichTable == Table.VARIANT){
                            Condition[] results = new Condition[acceptableValues.size()];
                            int i = 0;
                            for (String s : acceptableValues) {
                                results[i++] = BinaryConditionMS.equalTo(ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(columnname), s);
                            }
                            return results;
                        } else if (whichTable == Table.PATIENT) {
                            try {
                                List<String> individuals = MedSavantClient.PatientQueryUtilAdapter.getDNAIdsForStringList(LoginController.sessionId, ProjectController.getInstance().getCurrentPatientTableSchema(), acceptableValues, columnname);

                                Condition[] results = new Condition[individuals.size()];
                                int i = 0; 
                                for(String ind : individuals){
                                    results[i++] = BinaryConditionMS.equalTo(ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID), ind);
                                }
                                return results;

                            } catch (NonFatalDatabaseException ex) {
                                Logger.getLogger(StringListFilterView.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (SQLException ex) {
                                MiscUtils.checkSQLException(ex);
                                Logger.getLogger(StringListFilterView.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (RemoteException ex) {
                                Logger.getLogger(StringListFilterView.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        return new Condition[0];   
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
                FilterController.addFilter(f, getQueryId());

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
        
        //force left alignment
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(Box.createRigidArea(new Dimension(5,5)));
        p.add(Box.createHorizontalGlue());
        container.add(p);
              
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
        
    }

    @Override
    public FilterState saveState() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("table", whichTable.toString());
        if(appliedValues != null && !appliedValues.isEmpty()){
            String values = "";
            for(int i = 0; i < appliedValues.size(); i++){
                values += appliedValues.get(i);
                if(i != appliedValues.size()-1){
                    values += ";;;";
                }
            }
            map.put("values", values);         
        }
        return new FilterState(FilterType.STRING, alias, columnname, map);
    }

}
