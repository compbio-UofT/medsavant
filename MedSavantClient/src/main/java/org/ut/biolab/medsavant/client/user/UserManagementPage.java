/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.user;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.CollapsiblePanes;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.shared.model.UserLevel;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.dialog.ProgressDialog;
import org.ut.biolab.medsavant.client.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.client.view.list.DetailedView;
import org.ut.biolab.medsavant.client.view.list.SimpleDetailedListModel;
import org.ut.biolab.medsavant.client.view.list.SplitScreenView;
import org.ut.biolab.medsavant.client.view.app.MultiSectionApp;
import org.ut.biolab.medsavant.client.view.app.AppSubSection;
import org.ut.biolab.medsavant.client.view.component.BlockingPanel;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class UserManagementPage extends AppSubSection implements Listener<UserEvent> {

    private SplitScreenView view;

    public UserManagementPage(MultiSectionApp parent) {
        super(parent, "Users");
        UserController.getInstance().addListener(this);
    }

    @Override
    public JPanel getView() {
        if (view == null) {
            view = new SplitScreenView(
                    new SimpleDetailedListModel<String>("User") {
                        @Override
                        public String[] getData() throws Exception {
                            return UserController.getInstance().getUserNames();
                        }
                    },
                    new UserDetailedView(),
                    new UserDetailedListEditor());
        }
        return view;
    }

    @Override
    public Component[] getSubSectionMenuComponents() {
        return new Component[0];
    }

    @Override
    public void handleEvent(UserEvent evt) {
        view.refresh();
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
                new ProgressDialog(title, message) {

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
                            this.dispose();
                        }
                        this.dispose();
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
        private final BlockingPanel blockingPanel;

        public UserDetailedView() {
            super(pageName);

            JPanel viewContainer = (JPanel) ViewUtil.clear(this.getContentPanel());
            viewContainer.setLayout(new BorderLayout());

            JPanel infoContainer = ViewUtil.getClearPanel();
            ViewUtil.applyVerticalBoxLayout(infoContainer);

            blockingPanel = new BlockingPanel("No user selected",ViewUtil.getClearBorderlessScrollPane(infoContainer));
            viewContainer.add(blockingPanel, BorderLayout.CENTER);

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
            
            blockingPanel.block();
        }

        @Override
        public void setSelectedItem(Object[] item) {
            
            if (item.length == 0) {
                blockingPanel.block();
                return;
            }
            
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
            blockingPanel.unblock();
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
                return MedSavantClient.UserManager.getUserLevel(LoginController.getSessionID(), userName);
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
