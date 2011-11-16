/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.manage;

import org.ut.biolab.medsavant.view.dialog.NewUserDialog;
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
import org.ut.biolab.medsavant.controller.ThreadController;
import org.ut.biolab.medsavant.controller.UserController;
import org.ut.biolab.medsavant.controller.UserController.UserListener;
import org.ut.biolab.medsavant.view.MainFrame;
import org.ut.biolab.medsavant.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.view.list.DetailedListModel;
import org.ut.biolab.medsavant.view.list.DetailedView;
import org.ut.biolab.medsavant.view.list.SplitScreenView;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class UserManagementPage extends SubSectionView implements UserListener {

    private static class UserDetailedListEditor extends DetailedListEditor {

        @Override
        public boolean doesImplementAdding() { return true; }
        
        @Override
        public boolean doesImplementDeleting() { return true; }
        
        @Override
        public void addItems() {
            NewUserDialog npd = new NewUserDialog(MainFrame.getInstance(),true);
            npd.setVisible(true);
        }

        @Override
        public void editItems(Object[] results) {
        }

        @Override
        public void deleteItems(List<Object[]> results) {
            int nameIndex = 0;
            
            int result;
            
            if (results.size() == 1) {
                String name = (String) results.get(0)[nameIndex];
                result = JOptionPane.showConfirmDialog(MainFrame.getInstance(), 
                             "Are you sure you want to remove " + name + "?\nThis cannot be undone.",
                             "Confirm", JOptionPane.YES_NO_OPTION);
            } else {
                result = JOptionPane.showConfirmDialog(MainFrame.getInstance(), 
                             "Are you sure you want to remove these " + results.size() + " users?\nThis cannot be undone.",
                             "Confirm", JOptionPane.YES_NO_OPTION);
            }
            
            if (result == JOptionPane.YES_OPTION) {
                for (Object[] v : results) {
                    String name = (String) v[nameIndex];
                    UserController.getInstance().removeUser(name);
                }
                
                DialogUtils.displayMessage("Successfully removed " + results.size() + " user(s)");
            }
        }
    }

    public void userAdded(String name) {
        panel.refresh();
    }

    public void userRemoved(String name) {
        panel.refresh();
    }

    public void userChanged(String name) {
        panel.refresh();
    }

     private static class UserDetailedView extends DetailedView  {
        private final JPanel details;
        private final JPanel content;
        private String name;
        private DetailsSW sw;

        public UserDetailedView() {
        
        content = this.getContentPanel();
        
        details = ViewUtil.getClearPanel();
        
        content.setLayout(new BorderLayout());
        
        content.add(details,BorderLayout.CENTER);
    }
        
    @Override
    public void setSelectedItem(Object[] item) {
        name = (String) item[0];
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
    public void setMultipleSelections(List<Object[]> items){
        setTitle("Multiple users (" + items.size() + ")");
        details.removeAll();
        details.updateUI();
    }
    
    }

    private static class UserListModel implements DetailedListModel {
        private ArrayList<String> cnames;
        private ArrayList<Class> cclasses;
        private ArrayList<Integer> chidden;

        public UserListModel() {
        }

        public List<Object[]> getList(int limit) throws Exception {
            List<String> projects = UserController.getInstance().getUserNames();
            List<Object[]> projectVector = new ArrayList<Object[]>();
            for (String p : projects) {
                Object[] oarr = new Object[1];
                oarr[0] = p;
                projectVector.add(oarr);
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
                new UserListModel(), 
                new UserDetailedView(),
                new UserDetailedListEditor());
    }
    
    @Override
    public Component[] getBanner() {
        Component[] result = new Component[0];
        //result[0] = getAddButton();
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
    public void viewDidLoad() {
    }

    @Override
    public void viewDidUnload() {
        ThreadController.getInstance().cancelWorkers(getName());
    }
    
    
    
}
