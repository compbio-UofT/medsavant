/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.patients.cohorts;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.jidesoft.utils.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.model.Cohort;
import org.ut.biolab.medsavant.db.model.SimplePatient;
import org.ut.biolab.medsavant.db.util.query.CohortQueryUtil;
import org.ut.biolab.medsavant.view.component.CollapsablePanel;
import org.ut.biolab.medsavant.view.list.DetailedView;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class CohortDetailedView extends DetailedView {

    //private List<Object> fieldValues;
    private CohortDetailsSW sw;
    private final JPanel content;
    private final JPanel details;
    private final JPanel menu;
    //private String[] cohortNames;
    private JList list;
    private Cohort cohort;
    private Cohort[] cohorts;
    private final CollapsablePanel membersPane;


    private class CohortDetailsSW extends SwingWorker {

        private final Cohort cohort;

        public CohortDetailsSW(Cohort cohort) {
            this.cohort = cohort;
        }

        @Override
        protected Object doInBackground() throws Exception {
            List<SimplePatient> patientList = CohortQueryUtil.getIndividualsInCohort(ProjectController.getInstance().getCurrentProjectId(), cohort.getId());
            return patientList;
        }

        @Override
        protected void done() {
            try {
                List<SimplePatient> result = (List<SimplePatient>) get();
                setPatientList(result);

            } catch (Exception ex) {
                return;
            }
        }
    }

    public synchronized void setPatientList(List<SimplePatient> patients) {

        details.removeAll();

        details.setLayout(new BorderLayout());
        //.setLayout(new BoxLayout(details,BoxLayout.Y_AXIS));

        DefaultListModel lm = new DefaultListModel();
        /*for (Vector v : patients) {
        JLabel l = new JLabel(v.get(CohortViewTableSchema.INDEX_HOSPITALID-1).toString()); l.setForeground(Color.white);
        //details.add(l);
        lm.addElement((String) v.get(CohortViewTableSchema.INDEX_HOSPITALID-1));
        }*/
        for (SimplePatient i : patients) {
            JLabel l = new JLabel(i.toString());
            l.setForeground(Color.white);
            details.add(l);
            lm.addElement(i);
        }
        list = ViewUtil.getDetailList(lm);

        membersPane.setDescription(ViewUtil.numToString(patients.size()));

        JScrollPane jsp = ViewUtil.getClearBorderlessJSP(list);
        details.add(jsp, BorderLayout.CENTER);
        //list.setOpaque(false);

        details.updateUI();
    }

    public CohortDetailedView() {
        
        JPanel viewContainer = (JPanel) ViewUtil.clear(this.getContentPanel());
        viewContainer.setLayout(new BorderLayout());
        
        JPanel infoContainer = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(infoContainer);
        
        viewContainer.add(ViewUtil.getClearBorderlessJSP(infoContainer),BorderLayout.CENTER);
        
        membersPane = new CollapsablePanel("Members");
        infoContainer.add(membersPane);
        infoContainer.add(Box.createVerticalGlue());
        
        content = membersPane.getContentPane();
        
        details = ViewUtil.getClearPanel();
        menu = ViewUtil.getClearPanel();// ViewUtil.getButtonPanel();

        //menu.add(setDefaultCaseButton());
        //menu.add(setDefaultControlButton());
        menu.add(removeIndividualsButton());
        //menu.add(deleteCohortButton());
        menu.setVisible(false);

        content.setLayout(new BorderLayout());

        content.add(details, BorderLayout.CENTER);
        this.addBottomComponent(menu);
    }

    @Override
    public void setSelectedItem(Object[] item) {
        cohort = ((Cohort) item[0]);
        setTitle(cohort.getName());

        details.removeAll();
        details.updateUI();

        if (sw != null) {
            sw.cancel(true);
        }
        sw = new CohortDetailsSW(cohort);
        sw.execute();

        if (menu != null) {
            menu.setVisible(true);
        }
    }

    @Override
    public void setMultipleSelections(List<Object[]> items) {
        cohorts = new Cohort[items.size()];
        for (int i = 0; i < items.size(); i++) {
            cohorts[i] = (Cohort) items.get(i)[0];
        }
        setTitle("Multiple cohorts (" + items.size() + ")");
        details.removeAll();
        details.updateUI();
    }

    /*
    private JButton setDefaultCaseButton(){
    JButton button = new JButton("Set default Case cohort");
    button.setBackground(ViewUtil.getDetailsBackgroundColor());
    button.addActionListener(new ActionListener() {
    public void actionPerformed(ActionEvent e) {
    //TODO
    }
    }); 
    return button;
    }
    
    private JButton setDefaultControlButton(){
    JButton button = new JButton("Set default Control cohort");
    button.setBackground(ViewUtil.getDetailsBackgroundColor());
    button.addActionListener(new ActionListener() {
    public void actionPerformed(ActionEvent e) {
    //TODO
    }
    }); 
    return button;
    }
     * 
     */
    private JButton removeIndividualsButton() {
        JButton button = new JButton("Remove individual(s) from cohort");
        button.setBackground(ViewUtil.getDetailsBackgroundColor());
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Object[] selected = list.getSelectedValues();
                int[] patientIds = new int[selected.length];
                for (int i = 0; i < selected.length; i++) {
                    //patientIds[i] = (Integer) selected[i];
                    patientIds[i] = ((SimplePatient) selected[i]).getId();
                }
                if (patientIds != null && patientIds.length > 0) {

                    try {
                        CohortQueryUtil.removePatientsFromCohort(patientIds, cohort.getId());
                    } catch (SQLException ex) {
                        Logger.getLogger(CohortDetailedView.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    sw = new CohortDetailsSW(cohort);
                    sw.execute();
                }
            }
        });
        return button;
    }

}
