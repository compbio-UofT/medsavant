/*
 *    Copyright 2011 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.view.patients.cohorts;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.model.Cohort;
import org.ut.biolab.medsavant.db.model.SimplePatient;
import org.ut.biolab.medsavant.db.util.query.CohortQueryUtil;
import org.ut.biolab.medsavant.view.dialog.IndeterminateProgressDialog;
import org.ut.biolab.medsavant.view.patients.DetailedView;
import org.ut.biolab.medsavant.view.util.ViewUtil;


/**
 *
 * @author mfiume
 */
public class CohortDetailedView extends DetailedView {
    private static final Logger LOG = Logger.getLogger(CohortDetailedView.class.getName());

    private CohortDetailsWorker sw;
    private final JPanel content;
    private final JPanel details;
    private final JPanel menu;
    private JList list;
    private Cohort cohort;
    private Cohort[] cohorts;
    
    private class CohortDetailsWorker extends SwingWorker<List<SimplePatient>, Integer> {

        public CohortDetailsWorker() {
        }
        
        @Override
        protected List<SimplePatient> doInBackground() throws Exception {
            return CohortQueryUtil.getIndividualsInCohort(ProjectController.getInstance().getCurrentProjectId(), cohort.getId());
        }
        
        @Override
        protected void done() {
            try {
                setPatientList(get());
            } catch (CancellationException x){
                
            } catch (Exception x) {
                // TODO: #90
                LOG.log(Level.SEVERE, null, x);
            }
        }
        
    }

    public synchronized void setPatientList(List<SimplePatient> patients) {

        details.removeAll();
        
        details.setLayout(new BorderLayout());
        //.setLayout(new BoxLayout(details,BoxLayout.Y_AXIS));
        
        details.add(ViewUtil.getKeyValuePairPanel("Patients in cohort", patients.size() + ""), BorderLayout.NORTH);
        DefaultListModel lm = new DefaultListModel();
        /*for (Vector v : patients) {
            JLabel l = new JLabel(v.get(CohortViewTableSchema.INDEX_HOSPITALID-1).toString()); l.setForeground(Color.white);
            //details.add(l);
            lm.addElement((String) v.get(CohortViewTableSchema.INDEX_HOSPITALID-1));
        }*/
        for(SimplePatient i : patients) {
            JLabel l = new JLabel(i.toString());
            l.setForeground(Color.white);
            details.add(l);
            lm.addElement(i);
        }
        list = (JList) ViewUtil.clear(new JList(lm));
        list.setBackground(ViewUtil.getDetailsBackgroundColor());
        list.setForeground(Color.white);
        JScrollPane jsp = ViewUtil.getClearBorderlessJSP(list);
        details.add(jsp, BorderLayout.CENTER);
        //list.setOpaque(false);

        details.updateUI();  
    }
    
    public CohortDetailedView() {
        content = this.getContentPanel();
        
        details = ViewUtil.getClearPanel();
        menu = ViewUtil.getButtonPanel();
        
        //menu.add(setDefaultCaseButton());
        //menu.add(setDefaultControlButton());
        menu.add(removeIndividualsButton());
        menu.add(deleteCohortButton());
        menu.setVisible(false);
        
        content.setLayout(new BorderLayout());
        
        content.add(details,BorderLayout.CENTER);
        content.add(menu,BorderLayout.SOUTH);
    }
    
    @Override
    public void setSelectedItem(Object[] item) {
        cohort = (Cohort)item[0];
        setTitle(cohort.getName());
        
        details.removeAll();
        details.updateUI();
        
        if (sw != null) {
            sw.cancel(true);
        }
        sw = new CohortDetailsWorker();
        sw.execute();
        
        if(menu != null) menu.setVisible(true);
    }
    
    @Override
    public void setMultipleSelections(List<Object[]> items){
        cohorts = new Cohort[items.size()];
        for(int i = 0; i < items.size(); i++){
            cohorts[i] = (Cohort)items.get(i)[0];
        }
    }
    
    private JButton removeIndividualsButton(){
        JButton button = new JButton("Remove individual(s) from cohort");
        button.setBackground(ViewUtil.getDetailsBackgroundColor());
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object[] selected = list.getSelectedValues();
                int[] patientIds = new int[selected.length];
                for(int i = 0; i < selected.length; i++){
                    //patientIds[i] = (Integer) selected[i];
                    patientIds[i] = ((SimplePatient) selected[i]).getId();
                }
                if(patientIds != null && patientIds.length > 0){
                    
                    try {
                        CohortQueryUtil.removePatientsFromCohort(patientIds, cohort.getId());
                    } catch (SQLException ex) {
                        Logger.getLogger(CohortDetailedView.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    sw = new CohortDetailsWorker();
                    sw.execute();
                }
            }
        }); 
        return button;
    }
    
    private JButton deleteCohortButton(){
        JButton button = new JButton("Delete cohort(s)");
        button.setBackground(ViewUtil.getDetailsBackgroundColor());
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(cohorts != null && cohorts.length > 0){
                    int result = JOptionPane.showConfirmDialog(
                            null,
                            "Are you sure you want to delete these cohorts?\nThis cannot be undone.",
                            "Confirm", 
                            JOptionPane.YES_NO_OPTION);
                    if (result != JOptionPane.YES_OPTION) return;
                    final IndeterminateProgressDialog dialog = new IndeterminateProgressDialog(
                            "Removing Cohort(s)", 
                            cohorts.length + " cohort(s) being removed. Please wait.", 
                            true);
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                CohortQueryUtil.removeCohorts(cohorts);
                            } catch (SQLException ex) {
                                Logger.getLogger(CohortDetailedView.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            parent.refresh();   
                            dialog.close();  
                        }
                    };
                    thread.start(); 
                    dialog.setVisible(true);
                }
            }
        }); 
        return button;
    }

    
}