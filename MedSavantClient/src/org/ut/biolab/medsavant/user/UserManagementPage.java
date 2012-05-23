/*
 *    Copyright 2011-2012 University of Toronto
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

package org.ut.biolab.medsavant.user;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.JPanel;

import com.jidesoft.utils.SwingWorker;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.ThreadController;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.UserLevel;
import org.ut.biolab.medsavant.user.UserController.UserListener;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.view.component.CollapsiblePanel;
import org.ut.biolab.medsavant.view.dialog.IndeterminateProgressDialog;
import org.ut.biolab.medsavant.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.view.list.DetailedView;
import org.ut.biolab.medsavant.view.list.SimpleDetailedListModel;
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

    @Override
    public void userAdded(String name) {
        panel.refresh();
    }

    @Override
    public void userRemoved(String name) {
        panel.refresh();
    }

    @Override
    public void userChanged(String name) {
        panel.refresh();
    }

    private SplitScreenView panel;

    public UserManagementPage(SectionView parent) {
        super(parent);
        UserController.getInstance().addUserListener(this);
    }

    @Override
    public String getName() {
        return "Users";
    }

    @Override
    public JPanel getView(boolean update) {
        if (panel == null) {
            setPanel();
        }
        return panel;
    }

    public void setPanel() {
        panel = new SplitScreenView(
                new SimpleDetailedListModel("User") {
                    @Override
                    public List getData() throws Exception {
                        return UserController.getInstance().getUserNames();
                    }
                },
                new UserDetailedView(),
                new UserDetailedListEditor());
    }

    @Override
    public Component[] getSubSectionMenuComponents() {
        return new Component[0];
    }

    @Override
    public void viewDidLoad() {
    }

    @Override
    public void viewDidUnload() {
        ThreadController.getInstance().cancelWorkers(getName());
    }

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
            NewUserDialog npd = new NewUserDialog();
            npd.setVisible(true);
        }

        @Override
        public void deleteItems(final List<Object[]> items) {
            int result;

            String name = null;
            if (items.size() == 1) {
                name = (String)items.get(0)[0];
                result = DialogUtils.askYesNo("Confirm", "<html>Are you sure you want to remove <i>" + name + "</i>?<br>This cannot be undone.</html>");
            } else {
                result = DialogUtils.askYesNo("Confirm", "Are you sure you want to remove these " + items.size() + " users?\nThis cannot be undone.");
            }

            if (result == DialogUtils.YES) {
                String title = "Removing Users";
                String message = items.size() + " users being removed.  Please wait.";
                if (name != null) {
                    title = "Removing User";
                    message = "<html>User <i>" + name + "</i> being removed.  Please wait.</html>";
                }
                new IndeterminateProgressDialog(title, message) {
                    @Override
                    public void run() {
                        int numCouldntRemove = 0;
                        String name = null;
                        try {
                            for (Object[] v: items) {
                                name = (String)v[0];
                                UserController.getInstance().removeUser(name);
                            }
                        } catch (Throwable ex) {
                            numCouldntRemove++;
                            ClientMiscUtils.reportError("Error removing user \"" + name + "\": %s", ex);
                        }
                        if (numCouldntRemove != items.size()) {
                            DialogUtils.displayMessage("Successfully removed " + (items.size() - numCouldntRemove) + " user(s)");
                        }
                    }
                }.setVisible(true);
            }
        }
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
                    infoList.add(ClientMiscUtils.userLevelToString(level));
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
}
