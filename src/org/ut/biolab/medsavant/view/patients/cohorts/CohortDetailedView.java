/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.patients.cohorts;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.jidesoft.utils.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
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

    @Override
    public void setMultipleSelections(Vector[] selectedRows) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
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
        JList list = (JList) ViewUtil.clear(new JList(lm));
        list.setBackground(ViewUtil.getDetailsBackgroundColor());
        list.setForeground(Color.white);
        JScrollPane jsp = ViewUtil.getClearBorderedJSP(list);
        details.add(jsp, BorderLayout.CENTER);
        //list.setOpaque(false);

        details.updateUI();
    }
    
    public CohortDetailedView() {
        fieldNames = MedSavantDatabase.getInstance().getSubjectTableSchema().getFieldAliases();
        
        content = this.getContentPanel();
        
        details = ViewUtil.getClearPanel();
        menu = ViewUtil.getButtonPanel();
        
        menu.add(new JButton("Set default Case cohort"));
        menu.add(new JButton("Set default Control cohort"));
        menu.add(new JButton("Add individual(s) to cohort"));
        menu.add(new JButton("Remove individual(s) from cohort"));
        menu.add(new JButton("Delete cohort"));
        
        content.setLayout(new BorderLayout());
        
        content.add(details,BorderLayout.CENTER);
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
        sw = new CohortDetailsSW(patientId);
        sw.execute();
    }
    
}
