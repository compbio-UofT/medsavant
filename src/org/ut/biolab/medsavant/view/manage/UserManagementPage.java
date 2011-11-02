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

package org.ut.biolab.medsavant.view.manage;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

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
    public void setSelectedItem(Object[] item) {
        name = (String)item[0];
        setTitle(name);
        
        details.removeAll();
        details.updateUI();
        
        if (sw != null) {
            sw.cancel(true);
        }
        sw = new DetailsSW(name);
        sw.execute();
    }

        
    // TODO: What the hell is DetailsSW doing?
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
    }

    }

    private static class ThisListModel implements DetailedListModel {
        private ArrayList<String> cnames;
        private ArrayList<Class> cclasses;
        private ArrayList<Integer> chidden;

        public ThisListModel() {
        }

        public List<Object[]> getList(int limit) throws Exception {
            List<String> projects = UserController.getInstance().getUserNames();
            List<Object[]> projectVector = new ArrayList<Object[]>();
            for (String p : projects) {
                projectVector.add(new Object[] { p });
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
    public void viewDidLoad() {
    }

    @Override
    public void viewDidUnload() {
    }
    
    
    
}
