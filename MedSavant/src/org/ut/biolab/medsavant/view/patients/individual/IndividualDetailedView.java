/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.patients.individual;

import com.jidesoft.utils.SwingWorker;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.model.Cohort;
import org.ut.biolab.medsavant.db.util.query.CohortQueryUtil;
import org.ut.biolab.medsavant.db.util.query.PatientQueryUtil;
import org.ut.biolab.medsavant.log.ClientLogger;
import org.ut.biolab.medsavant.view.component.CollapsablePanel;
import org.ut.biolab.medsavant.view.dialog.ComboForm;
import org.ut.biolab.medsavant.view.list.DetailedView;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class IndividualDetailedView extends DetailedView {

    private List<String> fieldNames;
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
            Object[] fieldValues = PatientQueryUtil.getPatientRecord(ProjectController.getInstance().getCurrentProjectId(), pid);
            return fieldValues;
        }
        
        @Override
        protected void done() {
            try {
                Object[] result = (Object[]) get();
                setPatientInformation(result);
            } catch (CancellationException ex){
                
            } catch (Exception ex) {
                ClientLogger.log(IndividualDetailedView.class, ex.getLocalizedMessage());
                return;
            }
        }
    }

    public synchronized void setPatientInformation(Object[] result) {

        String[][] values = new String[fieldNames.size()][2];
        for (int i = 0; i < fieldNames.size(); i++) {
            values[i][0] = fieldNames.get(i);
            values[i][1] = "";
            if(result[i] != null)
                values[i][1] = result[i].toString();
        }
        
        details.removeAll();
        details.setLayout(new BoxLayout(details,BoxLayout.Y_AXIS));
        
        details.add(ViewUtil.getKeyValuePairList(values));
        
        details.add(Box.createVerticalGlue());
        
        details.updateUI();
    }
    
    public IndividualDetailedView() {
        
        try {
            fieldNames = PatientQueryUtil.getPatientFieldAliases(ProjectController.getInstance().getCurrentProjectId());
        } catch (SQLException ex) {
            ClientLogger.log(IndividualDetailedView.class,ex.getLocalizedMessage(),Level.SEVERE);
        }
        
        JPanel viewContainer = (JPanel) ViewUtil.clear(this.getContentPanel());
        viewContainer.setLayout(new BorderLayout());
        
        JPanel infoContainer = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(infoContainer);
        
        viewContainer.add(ViewUtil.getClearBorderlessJSP(infoContainer),BorderLayout.CENTER);
        
        CollapsablePanel cp = new CollapsablePanel("Patient Information");
        infoContainer.add(cp);
        infoContainer.add(Box.createVerticalGlue());
        
        content = cp.getContentPane();
        
        details = ViewUtil.getClearPanel();
        menu = ViewUtil.getClearPanel();
        
        menu.add(addIndividualsButton());
        menu.setVisible(false);
        content.setLayout(new BorderLayout());
        
        content.add(details,BorderLayout.CENTER);
        
        this.addBottomComponent(menu);
    }
    
    @Override
    public void setSelectedItem(Object[] item) {
        int patientId = (Integer) item[0];
        patientIds = new int[1];
        patientIds[0] = patientId;
        String hospitalId = (String) item[3];
        
        setTitle(hospitalId);
        
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
    public void setMultipleSelections(List<Object[]> items){
        patientIds = new int[items.size()];
        for(int i = 0; i < items.size(); i++){
            patientIds[i] = (Integer) items.get(i)[0];
        }
        setTitle("Multiple individuals (" + items.size() + ")");
        details.removeAll();
        details.updateUI();
    }
    
    private JButton addIndividualsButton(){
        JButton button = new JButton("Add individual(s) to cohort");
        button.setBackground(ViewUtil.getDetailsBackgroundColor());
        button.setOpaque(false);
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
    
}
