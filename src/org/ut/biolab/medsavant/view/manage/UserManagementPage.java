/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.manage;

import com.jidesoft.utils.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.UserController;
import org.ut.biolab.medsavant.controller.UserController.UserListener;
import org.ut.biolab.medsavant.view.MainFrame;
import org.ut.biolab.medsavant.view.patients.DetailedListModel;
import org.ut.biolab.medsavant.view.patients.DetailedView;
import org.ut.biolab.medsavant.view.patients.SplitScreenView;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class UserManagementPage extends SubSectionView implements UserListener {

    public void userAdded(String name) {
        panel.refresh();
    }

    public void userRemoved(String name) {
        panel.refresh();
    }

    public void userChanged(String name) {
        panel.refresh();
    }

     private static class ThisDetailedView extends DetailedView  {
        private final JPanel details;
        private final JPanel content;
        private String name;
        private DetailsSW sw;

        public ThisDetailedView() {
        
        content = this.getContentPanel();
        
        details = ViewUtil.getClearPanel();
        
        this.addBottomComponent(deleteButton());

        
        content.setLayout(new BorderLayout());
        
        content.add(details,BorderLayout.CENTER);
    }
        
        public final JButton deleteButton() {
            JButton b = new JButton("Delete User");
            b.setOpaque(false);
            b.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    
                    int result = JOptionPane.showConfirmDialog(MainFrame.getInstance(), 
                             "Are you sure you want to delete " + name + "?\nThis cannot be undone.",
                             "Confirm", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        UserController.getInstance().removeUser(name);
                    }
                }
                
            });
            return b;
        }
    
    @Override
    public void setSelectedItem(Vector item) {
        name = (String) item.get(0);
        setTitle(name);
        
        details.removeAll();
        details.updateUI();
        
        if (sw != null) {
            sw.cancel(true);
        }
        sw = new DetailsSW(name);
        sw.execute();
    }

        
    
    private static class DetailsSW extends SwingWorker {

        public DetailsSW(String projectName) {
        }

            @Override
            protected Object doInBackground() throws Exception {
                return null;
            }

    }
    
    @Override
    public void setMultipleSelections(List<Vector> items){
    }
    
    }

    private static class ThisListModel implements DetailedListModel {
        private ArrayList<String> cnames;
        private ArrayList<Class> cclasses;
        private ArrayList<Integer> chidden;

        public ThisListModel() {
        }

        public List<Vector> getList(int limit) throws Exception {
            List<String> projects = UserController.getInstance().getUserNames();
            List<Vector> projectVector = new ArrayList<Vector>();
            for (String p : projects) {
                Vector v = new Vector();
                v.add(p);
                projectVector.add(v);
            }
            return projectVector;
        }

        public List<String> getColumnNames() {
            if (cnames == null) {
                cnames = new ArrayList<String>();
                cnames.add("User");
            }
            return cnames;
        }

        public List<Class> getColumnClasses() {
            if (cclasses == null) {
                cclasses = new ArrayList<Class>();
                cclasses.add(String.class);
            }
            return cclasses;
        }

        public List<Integer> getHiddenColumns() {
            if (chidden == null) {
                chidden = new ArrayList<Integer>();
            }
            return chidden;
        }
    }

    private SplitScreenView panel;

    public UserManagementPage(SectionView parent) { 
        super(parent);
        UserController.getInstance().addUserListener(this);
    }

    public String getName() {
        return "Users";
    }

    public JPanel getView(boolean update) {
        if (panel == null) {
            setPanel();
        }
        return panel;
    }
    
    public void setPanel() { 
        panel = new SplitScreenView(
                new ThisListModel(), 
                new ThisDetailedView());
    }
    
    @Override
    public Component[] getBanner() {
        Component[] result = new Component[1];
        result[0] = getAddButton();
        return result;
    }
    
    private JButton getAddButton(){
        JButton button = new JButton("Add User");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                NewUserDialog npd = new NewUserDialog(MainFrame.getInstance(),true);
                npd.setVisible(true);
            }
        });
        return button;
    }
    
    @Override
    public void viewLoading() {
    }

    @Override
    public void viewDidUnload() {
    }
    
    
    
}
