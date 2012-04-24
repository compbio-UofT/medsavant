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
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ThreadController;
import org.ut.biolab.medsavant.controller.UserController;
import org.ut.biolab.medsavant.controller.UserController.UserListener;
import org.ut.biolab.medsavant.db.model.UserLevel;
import org.ut.biolab.medsavant.db.util.shared.MiscUtils;
import org.ut.biolab.medsavant.view.MedSavantFrame;
import org.ut.biolab.medsavant.view.component.CollapsiblePanel;
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
        public boolean doesImplementAdding() {
            return true;
        }

        @Override
        public boolean doesImplementDeleting() {
            return true;
        }

        @Override
        public void addItems() {
            NewUserDialog npd = new NewUserDialog(MedSavantFrame.getInstance(), true);
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
                result = JOptionPane.showConfirmDialog(MedSavantFrame.getInstance(),
                        "Are you sure you want to remove " + name + "?\nThis cannot be undone.",
                        "Confirm", JOptionPane.YES_NO_OPTION);
            } else {
                result = JOptionPane.showConfirmDialog(MedSavantFrame.getInstance(),
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

    private static class UserDetailedView extends DetailedView {

        private final JPanel details;
        private final JPanel content;
        private String name;
        private DetailsSW sw;
        private List<String> fieldNames;
        private CollapsiblePanel infoPanel;

        public UserDetailedView() {

            fieldNames = new ArrayList<String>();
            fieldNames.add("User Level");

            JPanel viewContainer = (JPanel) ViewUtil.clear(this.getContentPanel());
            viewContainer.setLayout(new BorderLayout());

            JPanel infoContainer = ViewUtil.getClearPanel();
            ViewUtil.applyVerticalBoxLayout(infoContainer);

            viewContainer.add(ViewUtil.getClearBorderlessJSP(infoContainer), BorderLayout.CENTER);

            infoPanel = new CollapsiblePanel("User Information");
            infoContainer.add(infoPanel);
            infoContainer.add(Box.createVerticalGlue());

            content = infoPanel.getContentPane();

            details = ViewUtil.getClearPanel();

            //content.setLayout(new BorderLayout());

            //content.add(details, BorderLayout.CENTER);
            content.add(details);
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

        @Override
        public void setRightClick(MouseEvent e) {
            //nothing yet
        }

        public synchronized void setUserInfoList(List<String> info) {

            details.removeAll();

            details.setLayout(new BorderLayout());
            ViewUtil.setBoxYLayout(details);

            String[][] values = new String[fieldNames.size()][2];
            for(int i = 0; i < fieldNames.size(); i++){
                values[i][0] = fieldNames.get(0);
                values[i][1] = info.get(0);
            }
            details.add(ViewUtil.getKeyValuePairList(values));

            details.updateUI();

        }

        private class DetailsSW extends SwingWorker {

            private String userName;

            public DetailsSW(String userName) {
                this.userName = userName;
            }

            @Override
            protected Object doInBackground() throws Exception {
                try {
                    return MedSavantClient.UserQueryUtilAdapter.getUserLevel(LoginController.sessionId, userName);
                } catch (SQLException ex) {
                    return null;
                }
            }

            @Override
            protected void done() {
                List<String> infoList = new ArrayList<String>();
                try {
                    UserLevel level = (UserLevel) get();
                    infoList.add(MiscUtils.userLevelToString(level));
                } catch (Exception ex){
                    infoList.add("Unknown");
                }
                setUserInfoList(infoList);
            }
        }

        @Override
        public void setMultipleSelections(List<Object[]> items) {
            if (items.isEmpty()) {
                setTitle("");
            } else {
                setTitle("Multiple users (" + items.size() + ")");
            }
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
    public Component[] getSubSectionMenuComponents() {
        Component[] result = new Component[0];
        //result[0] = getAddButton();
        return result;
    }

    private JButton getAddButton() {
        JButton button = new JButton("Add User");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                NewUserDialog npd = new NewUserDialog(MedSavantFrame.getInstance(), true);
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
