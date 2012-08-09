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
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.CollapsiblePanes;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.api.Listener;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.UserLevel;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.util.MedSavantWorker;
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
public class UserManagementPage extends SubSectionView implements Listener<UserEvent> {

    private SplitScreenView panel;

    public UserManagementPage(SectionView parent) {
        super(parent, "Users");
        UserController.getInstance().addListener(this);
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
                new SimpleDetailedListModel<String>("User") {
                    @Override
                    public String[] getData() throws Exception {
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
    public void handleEvent(UserEvent evt) {
        panel.refresh();
    }

    private class UserDetailedListEditor extends DetailedListEditor {

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
                name = (String) items.get(0)[0];
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

                    int numCouldntRemove = 0;

                    @Override
                    public void run() {
                        String name = null;
                        try {
                            for (Object[] v : items) {
                                name = (String) v[0];
                                UserController.getInstance().removeUser(name);
                            }
                            setVisible(false);
                            if (numCouldntRemove != items.size()) {
                                DialogUtils.displayMessage("Successfully removed " + (items.size() - numCouldntRemove) + " user(s)");
                            }
                        } catch (Throwable ex) {
                            setVisible(false);
                            numCouldntRemove++;
                            ClientMiscUtils.reportError("Error removing user \"" + name + "\": %s", ex);
                        }
                    }
                }.setVisible(true);
            }
        }
    }

    private class UserDetailedView extends DetailedView {

        private final String[] FIELD_NAMES = new String[]{"User Level"};
        private final JPanel details;
        private final JPanel content;
        private String name;
        private DetailsWorker worker;
        private CollapsiblePane infoPanel;

        public UserDetailedView() {
            super(pageName);

            JPanel viewContainer = (JPanel) ViewUtil.clear(this.getContentPanel());
            viewContainer.setLayout(new BorderLayout());

            JPanel infoContainer = ViewUtil.getClearPanel();
            ViewUtil.applyVerticalBoxLayout(infoContainer);

            viewContainer.add(ViewUtil.getClearBorderlessScrollPane(infoContainer), BorderLayout.CENTER);

            CollapsiblePanes panes = new CollapsiblePanes();
            panes.setOpaque(false);
            infoContainer.add(panes);

            infoPanel = new CollapsiblePane();
            infoPanel.setStyle(CollapsiblePane.TREE_STYLE);

            infoPanel.setCollapsible(false);
            panes.add(infoPanel);
            panes.addExpansion();

            content = new JPanel();
            content.setLayout(new BorderLayout());
            infoPanel.setLayout(new BorderLayout());
            infoPanel.add(content, BorderLayout.CENTER);

            details = ViewUtil.getClearPanel();

            //content.setLayout(new BorderLayout());

            //content.add(details, BorderLayout.CENTER);
            content.add(details);
        }

        @Override
        public void setSelectedItem(Object[] item) {
            name = (String) item[0];
            infoPanel.setTitle(name);

            details.removeAll();
            details.updateUI();

            if (worker != null) {
                worker.cancel(true);
            }
            worker = new DetailsWorker(name);
            worker.execute();
        }

        @Override
        public JPopupMenu createPopup() {
            return null;    //nothing yet
        }

        public synchronized void setUserInfoList(String[] info) {

            details.removeAll();

            details.setLayout(new BorderLayout());
            ViewUtil.setBoxYLayout(details);

            String[][] values = new String[FIELD_NAMES.length][2];
            for (int i = 0; i < FIELD_NAMES.length; i++) {
                values[i][0] = FIELD_NAMES[i];
                values[i][1] = info[0];
            }
            details.add(ViewUtil.getKeyValuePairList(values));

            details.updateUI();

        }

        @Override
        public void setMultipleSelections(List<Object[]> items) {
            if (items.isEmpty()) {
                infoPanel.setTitle("");
            } else {
                infoPanel.setTitle("Multiple users (" + items.size() + ")");
            }
            details.removeAll();
            details.updateUI();
        }

        private class DetailsWorker extends MedSavantWorker<UserLevel> {

            private String userName;

            public DetailsWorker(String userName) {
                super(pageName);
                this.userName = userName;
            }

            @Override
            protected UserLevel doInBackground() throws Exception {
                return MedSavantClient.UserManager.getUserLevel(LoginController.sessionId, userName);
            }

            @Override
            protected void showProgress(double frac) {
            }

            @Override
            protected void showSuccess(UserLevel lev) {
                setUserInfoList(new String[]{lev.toString()});
            }
        }
    }
}
