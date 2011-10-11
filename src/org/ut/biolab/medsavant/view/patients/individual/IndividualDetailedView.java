/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.patients.individual;

import com.jidesoft.utils.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.util.Cohort;
import org.ut.biolab.medsavant.db.util.query.CohortQueryUtil;
import org.ut.biolab.medsavant.db.util.query.PatientQueryUtil;
import org.ut.biolab.medsavant.olddb.DBUtil;
import org.ut.biolab.medsavant.olddb.MedSavantDatabase;
import org.ut.biolab.medsavant.olddb.QueryUtil;
import org.ut.biolab.medsavant.view.dialog.ComboForm;
import org.ut.biolab.medsavant.view.patients.DetailedView;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class IndividualDetailedView extends DetailedView {

    private List<String> fieldNames;
    //private List<Object> fieldValues;
    private IndividualDetailsSQ sw;
    private final JPanel content;
    private final JPanel details;
    private final JPanel menu;
    private int[] patientIds;
    
    private class IndividualDetailsSQ extends SwingWorker {
        private final int pid;

        public IndividualDetailsSQ(int pid) {
            this.pid = pid;
        }
        
        @Override
        protected Object doInBackground() throws Exception {
            Vector fieldValues = PatientQueryUtil.getPatientRecord(ProjectController.getInstance().getCurrentProjectId(), pid);
            return fieldValues;
        }
        
        @Override
        protected void done() {
            try {
                Vector result = (Vector) get();
                setPatientInformation(result);
                
            } catch (Exception ex) {
                return;
            }
        }
        
    }

    public synchronized void setPatientInformation(Vector result) {
        String[][] values = new String[fieldNames.size()][2];
        for (int i = 0; i < fieldNames.size(); i++) {
            values[i][0] = fieldNames.get(i);
            values[i][1] = "";
            if(result.get(i) != null)
                values[i][1] = result.get(i).toString();
        }
        
        details.removeAll();
        details.setLayout(new BoxLayout(details,BoxLayout.Y_AXIS));
        
        details.add(ViewUtil.getKeyValuePairPanel(values));
        
        details.add(Box.createVerticalGlue());
        
        details.updateUI();
    }
    
    public IndividualDetailedView() {
        
        //fieldNames = MedSavantDatabase.getInstance().getPatientTableSchema().getFieldAliases();
        try {
            fieldNames = PatientQueryUtil.getPatientFieldAliases(ProjectController.getInstance().getCurrentProjectId());
        } catch (SQLException ex) {
            Logger.getLogger(IndividualDetailedView.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        content = this.getContentPanel();
        
        details = ViewUtil.getClearPanel();
        menu = ViewUtil.getButtonPanel();
        
        menu.add(addIndividualsButton());
        menu.add(deleteIndividualsButton());
        menu.setVisible(false);
        
        content.setLayout(new BorderLayout());
        
        content.add(ViewUtil.getClearBorderlessJSP(details),BorderLayout.CENTER);
        content.add(menu,BorderLayout.SOUTH);
    }
    
    @Override
    public void setSelectedItem(Vector item) {
        int patientId = (Integer) item.get(0);
        setTitle(Integer.toString(patientId));
        
        details.removeAll();
        details.updateUI();
        
        if (sw != null) {
            sw.cancel(true);
        }
        sw = new IndividualDetailsSQ(patientId);
        sw.execute();
        
        if(menu != null) menu.setVisible(true);
    }
    
    @Override
    public void setMultipleSelections(List<Vector> items){
        patientIds = new int[items.size()];
        for(int i = 0; i < items.size(); i++){
            patientIds[i] = (Integer) items.get(i).get(0);
        }
    }
    
    private JButton addIndividualsButton(){
        JButton button = new JButton("Add individual(s) to cohort");
        button.setBackground(ViewUtil.getDetailsBackgroundColor());
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(patientIds != null && patientIds.length > 0){
                    try {    
                        List<Cohort> cohorts = CohortQueryUtil.getCohorts(ProjectController.getInstance().getCurrentProjectId());                    
                        ComboForm form = new ComboForm(cohorts.toArray(), "Select Cohort", "Select which cohort to add to:");
                        Cohort selected = (Cohort) form.getSelectedValue();
                        if (selected == null) {
                            return;
                        }                        
                        CohortQueryUtil.addPatientsToCohort(patientIds, selected.getId());                            
                    } catch (SQLException ex) {
                        Logger.getLogger(IndividualDetailedView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    parent.refresh();                   
                }                   
            }
        }); 
        return button;
    }
    
    private JButton deleteIndividualsButton(){
        JButton button = new JButton("Delete individual(s)");
        button.setBackground(ViewUtil.getDetailsBackgroundColor());
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(patientIds != null && patientIds.length > 0){
                    try {
                        PatientQueryUtil.removePatient(ProjectController.getInstance().getCurrentProjectId(), patientIds);
                    } catch (SQLException ex) {
                        Logger.getLogger(IndividualDetailedView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    parent.refresh();
                }
            }
        }); 
        return button;
    }
    
}
