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

package org.ut.biolab.medsavant.client.variant;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.*;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.CollapsiblePanes;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.shared.model.SimpleVariantFile;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.list.DetailedView;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;


/**
 *
 * @author abrook
 */
class VariantFilesDetailedView extends DetailedView implements BasicVariantColumns {

    private final JPanel details;
    private final JPanel content;
    private SimpleVariantFile[] files;
    private DetailsWorker detailsWorker;
    private CollapsiblePane infoPanel;

    public VariantFilesDetailedView(String page) {
        super(page);

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

        if (detailsWorker != null) {
            detailsWorker.cancel(true);
        }
        detailsWorker = new DetailsWorker(files[0]);
        detailsWorker.execute();
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

    private class DetailsWorker extends MedSavantWorker<List<String[]>> {

        private final SimpleVariantFile file;

        public DetailsWorker(SimpleVariantFile f) {
            super(getPageName());
            this.file = f;
        }

        @Override
        protected List<String[]> doInBackground() throws Exception {
            return MedSavantClient.VariantManager.getTagsForUpload(LoginController.sessionId, file.getUploadId());
        }

        protected void showProgress(double ignored) {
        }

        @Override
        protected void showSuccess(List<String[]> result) {
            result.add(0, new String[]{"File Name", file.getName()});
            result.add(1, new String[]{"Upload ID", Integer.toString(file.getUploadId())});
            result.add(2, new String[]{"File ID", Integer.toString(file.getFileId())});
            setFileInfoList(result);
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

    @Override
    public JPopupMenu createPopup() {
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
                    DbColumn upload = ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(UPLOAD_ID);
                    DbColumn file = ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(FILE_ID);
                    for (int i = 0; i < files.length; i++) {
                        conditions[i] = ComboCondition.and(
                                BinaryCondition.equalTo(upload, files[i].getUploadId()),
                                BinaryCondition.equalTo(file, files[i].getFileId()));
                    }

                    /* TODO:
                    FilterUtils.createAndApplyGenericFixedFilter("Variant Files - Filter by File(s)", files.length + " Files(s)",
                            ComboCondition.or(conditions));
                            */
                }
            });
            popupMenu.add(filter1Item);
        }

        return popupMenu;
    }
}
