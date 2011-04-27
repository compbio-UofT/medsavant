/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.gadget.filter;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.jidesoft.pane.FloorTabbedPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.db.DB;
import org.ut.biolab.medsavant.db.PatientTable;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;


/**
 *
 * @author mfiume
 */
public class FilterPanel extends JPanel {
    
    private FilterController mfc;
    private FloorTabbedPane filterViewTabbedPanel;
    private final ArrayList<FilterView> filterViews;

    public FilterPanel() {
        this.setLayout(new BorderLayout());
        this.setBackground(Color.black);
        filterViews = new ArrayList<FilterView>();
        initGUI();
    }


    public void paintComponent(Graphics g) {

        GradientPaint p = new GradientPaint(0,0,Color.darkGray,0,40,Color.black);
        ((Graphics2D)g).setPaint(p);

        g.fillRect(0, 0, this.getWidth(), this.getHeight());

    }

    private void initGUI() {
        mfc = new FilterController();

        filterViewTabbedPanel = new FloorTabbedPane();
        filterViewTabbedPanel.setPreferredSize(new Dimension(300,450));
        filterViewTabbedPanel.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                //updateCurrentFilterViewIndex();

            }
        });

        this.add(this.filterViewTabbedPanel,BorderLayout.CENTER);

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
        filterViewTabbedPanel.addTab(view.getTitle(),view.getComponent());
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

    void listenToComponent(final JRadioButton c) {
        c.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                //setApplyButtonEnabled(true);
            }

        });
    }

}
