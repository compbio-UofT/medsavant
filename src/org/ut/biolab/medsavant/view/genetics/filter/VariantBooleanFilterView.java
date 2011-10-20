/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
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
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author AndrewBrook
 */
public class VariantBooleanFilterView {
    
    public static FilterView createFilterView(String tablename, final String columnname, final int queryId, final String alias) throws SQLException, NonFatalDatabaseException {
        
        List<String> uniq = new ArrayList<String>();
        uniq.add("True");
        uniq.add("False");

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        JPanel bottomContainer = new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));

        final JButton applyButton = new JButton("Apply");
        applyButton.setEnabled(false);
        final List<JCheckBox> boxes = new ArrayList<JCheckBox>();

        ActionListener al = new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {

                applyButton.setEnabled(false);

                final List<String> acceptableValues = new ArrayList<String>();

                if (boxes.get(0).isSelected()) {
                    acceptableValues.add("1");
                }
                if (boxes.get(1).isSelected()) {
                    acceptableValues.add("0");
                }

                Filter f = new QueryFilter() {

                    @Override
                    public Condition[] getConditions() {
                        Condition[] results = new Condition[acceptableValues.size()];
                        int i = 0;
                        for (String s : acceptableValues) {
                            results[i++] = BinaryCondition.equalTo(ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(columnname), s);
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

        bottomContainer.setAlignmentX(0F);
        container.add(bottomContainer);

        //al.actionPerformed(null);       
        return new FilterView(alias, container);
    }
    
}
