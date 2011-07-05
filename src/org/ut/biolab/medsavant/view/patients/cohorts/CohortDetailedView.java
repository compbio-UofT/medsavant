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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.ut.biolab.medsavant.db.DBUtil;
import org.ut.biolab.medsavant.db.MedSavantDatabase;
import org.ut.biolab.medsavant.db.QueryUtil;
import org.ut.biolab.medsavant.view.patients.DetailedView;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class CohortDetailedView extends DetailedView {

    private List<String> fieldNames;
    //private List<Object> fieldValues;
    private CohortDetailsSW sw;
    private final JPanel content;
    private final JPanel details;
    private final JPanel menu;
    private String[] cohortNames;
    private JList list;
    private String cohortName;
    
    private class CohortDetailsSW extends SwingWorker {
        private final String cohortName;

        public CohortDetailsSW(String cohortName) {
            this.cohortName = cohortName;
        }
        
        @Override
        protected Object doInBackground() throws Exception {
            List<Vector> patientList = QueryUtil.getPatientsInCohort(cohortName);
            return patientList;
        }
        
        @Override
        protected void done() {
            try {
                List<Vector> result = (List<Vector>) get();
                setPatientList(result);
                
            } catch (Exception ex) {
                return;
            }
        }
        
    }

    public synchronized void setPatientList(List<Vector> patients) {

        details.removeAll();
        
        details.setLayout(new BorderLayout());
        //.setLayout(new BoxLayout(details,BoxLayout.Y_AXIS));
        
        details.add(ViewUtil.getKeyValuePairPanel("Patients in cohort", patients.size() + ""), BorderLayout.NORTH);
        DefaultListModel lm = new DefaultListModel();
        for (Vector v : patients) {
            JLabel l = new JLabel(v.get(2).toString()); l.setForeground(Color.white);
            //details.add(l);
            lm.addElement((String) v.get(2));
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
        fieldNames = MedSavantDatabase.getInstance().getSubjectTableSchema().getFieldAliases();
        
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
    public void setSelectedItem(Vector item) {
        cohortName = (String) item.get(0);
        setTitle(cohortName);
        
        details.removeAll();
        details.updateUI();
        
        if (sw != null) {
            sw.cancel(true);
        }
        sw = new CohortDetailsSW(cohortName);
        sw.execute();
        
        if(menu != null) menu.setVisible(true);
    }
    
    @Override
    public void setMultipleSelections(Vector[] items){
        cohortNames = new String[items.length];
        for(int i = 0; i < items.length; i++){
            cohortNames[i] = (String) items[i].get(0);
        }
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
    
    private JButton removeIndividualsButton(){
        JButton button = new JButton("Remove individual(s) from cohort");
        button.setBackground(ViewUtil.getDetailsBackgroundColor());
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object[] selected = list.getSelectedValues();
                String[] patientIds = new String[selected.length];
                for(int i = 0; i < selected.length; i++){
                    patientIds[i] = (String) selected[i];
                }
                if(patientIds != null && patientIds.length > 0){
                    DBUtil.removeIndividualsFromCohort(cohortName, patientIds);     
                    sw = new CohortDetailsSW(cohortName);
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
                if(cohortNames != null && cohortNames.length > 0){
                    DBUtil.deleteCohorts(cohortNames);     
                    parent.refresh();
                }
            }
        }); 
        return button;
    }
    

}