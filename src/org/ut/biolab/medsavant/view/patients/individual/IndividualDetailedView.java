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
import java.util.List;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.db.DBUtil;
import org.ut.biolab.medsavant.db.MedSavantDatabase;
import org.ut.biolab.medsavant.db.QueryUtil;
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
    private String[] patientIds;
    
    private class IndividualDetailsSQ extends SwingWorker {
        private final String pid;

        public IndividualDetailsSQ(String pid) {
            this.pid = pid;
        }
        
        @Override
        protected Object doInBackground() throws Exception {
            List<Vector> fieldValues = QueryUtil.getPatientRecord(pid);
            return fieldValues;
        }
        
        @Override
        protected void done() {
            try {
                List<Vector> result = (List<Vector>) get();
                setPatientInformation(result);
                
            } catch (Exception ex) {
                return;
            }
        }
        
    }

    public synchronized void setPatientInformation(List<Vector> result) {
        Vector firstMatch = result.get(0);
        String[][] values = new String[fieldNames.size()][2];
        for (int i = 0; i < fieldNames.size(); i++) {
            values[i][0] = fieldNames.get(i);
            values[i][1] = firstMatch.get(i).toString();
        }
        
        details.removeAll();
        details.setLayout(new BoxLayout(details,BoxLayout.Y_AXIS));
        
        details.add(ViewUtil.getKeyValuePairPanel(values));
        
        details.add(Box.createVerticalGlue());
        
        details.updateUI();
    }
    
    public IndividualDetailedView() {
        fieldNames = MedSavantDatabase.getInstance().getSubjectTableSchema().getFieldAliases();
        
        content = this.getContentPanel();
        
        details = ViewUtil.getClearPanel();
        menu = ViewUtil.getButtonPanel();
        
        menu.add(addIndividualButton());
        menu.add(deleteIndividualButton());
        
        content.setLayout(new BorderLayout());
        
        content.add(ViewUtil.getClearBorderedJSP(details),BorderLayout.CENTER);
        content.add(menu,BorderLayout.SOUTH);
    }
    
    @Override
    public void setSelectedItem(Vector item) {
        String patientId = (String) item.get(0);
        setTitle(patientId);
        
        details.removeAll();
        details.updateUI();
        
        if (sw != null) {
            sw.cancel(true);
        }
        sw = new IndividualDetailsSQ(patientId);
        sw.execute();
    }
    
    @Override
    public void setMultipleSelections(Vector[] items){
        patientIds = new String[items.length];
        for(int i = 0; i < items.length; i++){
            patientIds[i] = (String) items[i].get(0);
        }
    }
    
    private JButton addIndividualButton(){
        JButton button = new JButton("Add individual(s) to cohort");
        button.setBackground(ViewUtil.getDetailsBackgroundColor());
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(patientIds != null && patientIds.length > 0){
                    DBUtil.addIndividualToCohort(patientIds); 
                }                   
            }
        }); 
        return button;
    }
    
    private JButton deleteIndividualButton(){
        JButton button = new JButton("Delete individual(s)");
        button.setBackground(ViewUtil.getDetailsBackgroundColor());
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(patientIds != null && patientIds.length > 0){
                    DBUtil.deleteIndividual(patientIds);     
                    parent.refresh();
                }
            }
        }); 
        return button;
    }
    
}
