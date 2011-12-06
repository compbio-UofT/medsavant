/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import com.healthmarketscience.sqlbuilder.Condition;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
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
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.util.BinaryConditionMS;
import org.ut.biolab.medsavant.db.util.query.PatientQueryUtil;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.view.genetics.filter.FilterState.FilterType;
import org.ut.biolab.medsavant.view.genetics.filter.FilterUtils.Table;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author Andrew
 */
public class BooleanFilterView extends FilterView{

    public static FilterView createVariantFilterView(String columnname, int queryId, String alias) throws SQLException, NonFatalDatabaseException {
        return new BooleanFilterView(new JPanel(), columnname, queryId, alias, Table.VARIANT);
    }
    
    public static FilterView createPatientFilterView(String columnname, int queryId, String alias) throws SQLException, NonFatalDatabaseException {
        return new BooleanFilterView(new JPanel(), columnname, queryId, alias, Table.PATIENT);
    }
    
    public BooleanFilterView(FilterState state, int queryId) throws SQLException {
        this(new JPanel(), state.getId(), queryId, state.getName(), Table.valueOf(state.getValues().get("table")));
        String values = state.getValues().get("values");
        if(values != null){
            List<String> l = new ArrayList<String>();
            Collections.addAll(l, values.split(";;;"));
            applyFilter(l);
        }
    }
    
    private ActionListener al;
    private List<JCheckBox> boxes;
    private String columnname;
    private String alias;
    private Table whichTable;
    private List<String> appliedValues;
    
    public void applyFilter(List<String> list){
        for(JCheckBox box : boxes){
            box.setSelected((box.getText().equals("True") && list.contains("1")) || (box.getText().equals("False") && list.contains("0")));
        }
        al.actionPerformed(new ActionEvent(this, 0, null));
    }
        
    private BooleanFilterView(final JPanel container, final String columnname, final int queryId, final String alias, final Table whichTable) throws SQLException {
        
        super(alias, container);
        
        this.columnname = columnname;
        this.alias = alias;
        this.whichTable = whichTable;
        
        List<String> uniq = new ArrayList<String>();
        uniq.add("True");
        uniq.add("False");

        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        JPanel bottomContainer = new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));

        final JButton applyButton = new JButton("Apply");
        applyButton.setEnabled(false);
        boxes = new ArrayList<JCheckBox>();

        al = new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {

                applyButton.setEnabled(false);

                final List<String> acceptableValues = new ArrayList<String>();

                if (boxes.get(0).isSelected()) {
                    acceptableValues.add("1");
                }
                if (boxes.get(1).isSelected()) {
                    acceptableValues.add("0");
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
                        } else if (whichTable == Table.PATIENT){
                            try {
                                List<String> individuals = PatientQueryUtil.getDNAIdsForStringList(ProjectController.getInstance().getCurrentPatientTableSchema(), acceptableValues, columnname);

                                Condition[] results = new Condition[individuals.size()];
                                int i = 0; 
                                for(String ind : individuals){
                                    results[i++] = BinaryConditionMS.equalTo(ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID), ind);
                                }
                                return results;

                            } catch (NonFatalDatabaseException ex) {
                                Logger.getLogger(StringListFilterView.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (SQLException ex) {
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
                FilterController.addFilter(f, queryId);

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

        bottomContainer.add(Box.createGlue());

        bottomContainer.add(applyButton);
        bottomContainer.setMaximumSize(new Dimension(10000,24));
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
        return new FilterState(FilterType.BOOLEAN, alias, columnname, map);
    }
}
