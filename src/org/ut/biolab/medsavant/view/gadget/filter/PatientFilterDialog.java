/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.gadget.filter;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.ut.biolab.medsavant.db.DB;
import org.ut.biolab.medsavant.db.PatientTable;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;

/**
 *
 * @author mfiume
 */
class PatientFilterDialog extends FilterDialog {

    PatientFilterDialog() {
        super(null,"Patient Filter");
        List<FilterView> filterViews = getPatientFilterViews();
        addFilterViews(filterViews);
    }

    public void reset() {
    }

    public Filter getFilter() {
        return null;
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
        container.add(new JRadioButton("Male"));
        final JRadioButton buttonFemale = new JRadioButton("Female");
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
        container.add(new JRadioButton("10-20"));
        container.add(new JRadioButton("20-30"));
        container.add(new JRadioButton("old"));

        return new FilterView(title,container,null);
    }
}
