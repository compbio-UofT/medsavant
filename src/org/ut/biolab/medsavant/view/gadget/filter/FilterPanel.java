/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.gadget.filter;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.jidesoft.pane.CollapsiblePane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.FilterGenerator;
import org.ut.biolab.medsavant.db.DB;
import org.ut.biolab.medsavant.db.PatientTable;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;


/**
 *
 * @author mfiume
 */
public class FilterPanel extends JPanel {


    private final ArrayList<FilterView> filterViews;
    private JPanel contentPanel;

    public FilterPanel() {
        this.setLayout(new BorderLayout());
        filterViews = new ArrayList<FilterView>();
        initGUI();
    }

    private void initGUI() {
        
        contentPanel = new JPanel();
        contentPanel.setBackground(Color.white);
        //contentPanel.setLayout(new GridBagLayout(0,1));
        //contentPanel.add(Box.createGlue());
        //contentPanel.setLayout(new BoxLayout(contentPanel,BoxLayout.Y_AXIS));
        this.add(contentPanel,BorderLayout.CENTER);

        List<FilterView> fv = getPatientFilterViews();
        addFilterViews(fv);
    }

    public void addFilterViews(List<FilterView> filterViews) {
        for (FilterView view : filterViews) {
            addFilterView(view);
        }
    }
    
    private void addFilterView(FilterView view) {
        filterViews.add(view);
        CollapsiblePane cp = new CollapsiblePane(view.getTitle());
        try {
            cp.setCollapsed(true);
            cp.setContentAreaFilled(false);
            cp.setCollapsedPercentage(0);
        } catch (PropertyVetoException ex) {
        }
        cp.setContentPane(view.getComponent());
        this.contentPanel.add(cp);
    }


    private List<FilterView> getPatientFilterViews() {
        List<FilterView> views = new ArrayList<FilterView>();

        views.add(getGenderFilterView());
        views.add(getAgeFilterView());

        return views;
    }

    private FilterView getGenderFilterView() {
        String title = "Gender";

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container,BoxLayout.Y_AXIS));

        container.add(new JLabel("Patients are:"));
        container.add(new JCheckBox("Male"));
        final JCheckBox buttonFemale = new JCheckBox("Female");
        container.add(buttonFemale);
        listenToComponent(buttonFemale);

        FilterGenerator fg = new FilterGenerator() {
            public Filter generateFilter() {
                QueryFilter qf = new QueryFilter() {
                    @Override
                    public List<Condition> getConditions() {
                        List<Condition> c = new ArrayList<Condition>();
                        String value = "male";
                        if (buttonFemale.isSelected()) { value = "female"; }
                        c.add(BinaryCondition.equalTo(DB.getInstance().patientTable.getColumn(PatientTable.COL_GENDER), value));
                        return c;
                    }
                };
                return qf;
            }
        };

        return new FilterView(title,container,fg);
    }

    private FilterView getAgeFilterView() {
        String title = "Age";

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container,BoxLayout.Y_AXIS));

        container.add(new JLabel("Patients are:"));
        container.add(new JCheckBox("10-20"));
        container.add(new JCheckBox("20-30"));
        container.add(new JCheckBox("old"));

        return new FilterView(title,container,null);
    }

    void listenToComponent(final JCheckBox c) {
        c.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                //setApplyButtonEnabled(true);
            }

        });
    }

}
