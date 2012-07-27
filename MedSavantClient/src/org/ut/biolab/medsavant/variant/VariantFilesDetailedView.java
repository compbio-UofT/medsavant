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

package org.ut.biolab.medsavant.variant;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.CollapsiblePanes;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.db.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.SimpleVariantFile;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.view.genetics.filter.FilterUtils;
import org.ut.biolab.medsavant.view.list.DetailedView;
import org.ut.biolab.medsavant.view.util.ViewUtil;


/**
 *
 * @author abrook
 */
class VariantFilesDetailedView extends DetailedView {

    private final JPanel details;
    private final JPanel content;
    private SimpleVariantFile[] files;
    private DetailsSW sw;
    private CollapsiblePane infoPanel;

    public VariantFilesDetailedView() {

        JPanel viewContainer = (JPanel) ViewUtil.clear(this.getContentPanel());
        viewContainer.setLayout(new BorderLayout());

        JPanel infoContainer = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(infoContainer);

        viewContainer.add(ViewUtil.getClearBorderlessScrollPane(infoContainer), BorderLayout.CENTER);

        CollapsiblePanes panes = new CollapsiblePanes();
        panes.setOpaque(false);

        infoPanel = new CollapsiblePane();
        infoPanel.setStyle(CollapsiblePane.TREE_STYLE);
        infoPanel.setCollapsible(false);
        panes.add(infoPanel);
        panes.addExpansion();

        infoContainer.add(panes);

        content = new JPanel();
        content.setLayout(new BorderLayout());
        infoPanel.setLayout(new BorderLayout());
        infoPanel.add(content,BorderLayout.CENTER);

        details = ViewUtil.getClearPanel();

        content.add(details);
    }

    @Override
    public void setSelectedItem(Object[] item) {
        files = new SimpleVariantFile[]{(SimpleVariantFile) item[0]};
        infoPanel.setTitle(files[0].getName());

        details.removeAll();
        details.updateUI();

        if (sw != null) {
            sw.cancel(true);
        }
        sw = new DetailsSW(files[0]);
        sw.execute();
    }

    @Override
    public void setRightClick(MouseEvent e) {
        JPopupMenu popup = createPopup(files);
        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    public synchronized void setFileInfoList(List<String[]> info) {

        details.removeAll();

        ViewUtil.setBoxYLayout(details);

        String[][] values = new String[info.size()][2];
        for (int i = 0; i < info.size(); i++) {
            values[i][0] = info.get(i)[0];
            values[i][1] = info.get(i)[1];
        }

        details.add(ViewUtil.getKeyValuePairList(values));

        details.updateUI();

    }

    private class DetailsSW extends SwingWorker {

        private SimpleVariantFile file;

        public DetailsSW(SimpleVariantFile file) {
            this.file = file;
        }

        @Override
        protected Object doInBackground() throws Exception {
            try {
                return MedSavantClient.VariantManager.getTagsForUpload(LoginController.sessionId, file.getUploadId());
            } catch (SQLException ex) {
                return null;
            }
        }

        @Override
        protected void done() {
            List<String[]> infoList = null;
            try {
                infoList = (List<String[]>) get();
                infoList.add(0, new String[]{"File Name", file.getName()});
                infoList.add(1, new String[]{"Upload ID", Integer.toString(file.getUploadId())});
                infoList.add(2, new String[]{"File ID", Integer.toString(file.getFileId())});
                //infoList.add(3, new String[]{"User", file.getUser()});
            } catch (InterruptedException ex) {
            } catch (ExecutionException ex) {
                VariantFilesPage.LOG.error("Error fetching variant details.", ex);
            }
            setFileInfoList(infoList);
        }
    }

    @Override
    public void setMultipleSelections(List<Object[]> items) {
        files = new SimpleVariantFile[items.size()];
        for (int i = 0; i < items.size(); i++) {
            files[i] = (SimpleVariantFile) (items.get(i)[0]);
        }

        if (items.isEmpty()) {
            infoPanel.setTitle("");
        } else {
            infoPanel.setTitle("Multiple uploads (" + items.size() + ")");
        }
        details.removeAll();
        details.updateUI();
    }

    private JPopupMenu createPopup(final SimpleVariantFile[] files) {
        JPopupMenu popupMenu = new JPopupMenu();

        if (ProjectController.getInstance().getCurrentVariantTableSchema() == null) {
            popupMenu.add(new JLabel("(You must choose a variant table before filtering)"));
        } else {

            //Filter by vcf file
            JMenuItem filter1Item = new JMenuItem("Filter by Variant File");
            filter1Item.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    Condition[] conditions = new Condition[files.length];
                    DbColumn upload = ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_UPLOAD_ID);
                    DbColumn file = ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_FILE_ID);
                    for (int i = 0; i < files.length; i++) {
                        conditions[i] = ComboCondition.and(
                                BinaryCondition.equalTo(upload, files[i].getUploadId()),
                                BinaryCondition.equalTo(file, files[i].getFileId()));
                    }

                    FilterUtils.createAndApplyGenericFixedFilter("Variant Files - Filter by File(s)", files.length + " Files(s)",
                            ComboCondition.or(conditions));
                }
            });
            popupMenu.add(filter1Item);
        }

        return popupMenu;
    }
}
