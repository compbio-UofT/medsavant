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
package org.ut.biolab.medsavant.view.manage;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.controller.ThreadController;
import org.ut.biolab.medsavant.db.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.model.SimpleVariantFile;
import org.ut.biolab.medsavant.listener.ReferenceListener;
import org.ut.biolab.medsavant.view.ViewController;
import org.ut.biolab.medsavant.view.component.CollapsiblePanel;
import org.ut.biolab.medsavant.view.genetics.filter.FilterPanelSubItem;
import org.ut.biolab.medsavant.view.genetics.filter.FilterUtils;
import org.ut.biolab.medsavant.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.view.list.DetailedView;
import org.ut.biolab.medsavant.view.list.SimpleDetailedListModel;
import org.ut.biolab.medsavant.view.list.SplitScreenView;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author Andrew
 */
public class VariantFilesPage extends SubSectionView implements ReferenceListener {
    
    private SplitScreenView panel;
    private boolean isLoaded = false;
    private boolean updateRequired = false;
    private boolean showPeekOnUnload = false;
    
    public VariantFilesPage(SectionView parent) {
        super(parent);
        ReferenceController.getInstance().addReferenceListener(this);
    }

    @Override
    public String getName() {
        return "Variant Files";
    }

    @Override
    public JPanel getView(boolean update) {
        if (panel == null || updateRequired) {
            setPanel();
        } 
        return panel;
    }

    @Override
    public void viewDidLoad() {
        isLoaded = true;
        showPeekOnUnload = ViewController.getInstance().isPeekRightShown();
        ViewController.getInstance().setPeekRightShown(false);
    }

    @Override
    public void viewDidUnload() {
        isLoaded = false;
        ViewController.getInstance().setPeekRightShown(showPeekOnUnload);
        ThreadController.getInstance().cancelWorkers(getName());
    }
    
    public void setPanel() {
        panel = new SplitScreenView(
                new SimpleDetailedListModel("Variant File") {
                    @Override
                    public List getData() throws Exception {
                        return MedSavantClient.VariantQueryUtilAdapter.getUploadedFiles(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectId(), ReferenceController.getInstance().getCurrentReferenceId());
                    }
                },
                new VariantFilesDetailedView(),
                new VariantFilesDetailedListEditor());
    }

    @Override
    public void referenceAdded(String name) {}

    @Override
    public void referenceRemoved(String name) {}

    @Override
    public void referenceChanged(String prnameojectName) {
        if (isLoaded) {
            update();
        } else {
            updateRequired = true;
        }
    }
    
    public void update() {
        panel.refresh();
    }

    
    /*
     * VARIANT FILES DETAILED VIEW
     */
    private static class VariantFilesDetailedView extends DetailedView {

        private final JPanel details;
        private final JPanel content;
        private SimpleVariantFile[] files;
        private DetailsSW sw;
        private CollapsiblePanel infoPanel;
        private static List<FilterPanelSubItem> filterPanels;

        public VariantFilesDetailedView() {

            JPanel viewContainer = (JPanel) ViewUtil.clear(this.getContentPanel());
            viewContainer.setLayout(new BorderLayout());

            JPanel infoContainer = ViewUtil.getClearPanel();
            ViewUtil.applyVerticalBoxLayout(infoContainer);

            viewContainer.add(ViewUtil.getClearBorderlessJSP(infoContainer), BorderLayout.CENTER);

            infoPanel = new CollapsiblePanel("Upload Information");
            infoContainer.add(infoPanel);
            infoContainer.add(Box.createVerticalGlue());

            content = infoPanel.getContentPane();

            details = ViewUtil.getClearPanel();

            content.add(details);
        }

        @Override
        public void setSelectedItem(Object[] item) {
            files = new SimpleVariantFile[]{(SimpleVariantFile) item[0]};
            setTitle(files[0].getName());

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
                    return MedSavantClient.VariantQueryUtilAdapter.getTagsForUpload(LoginController.sessionId, file.getUploadId());
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
                    Logger.getLogger(VariantFilesPage.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(VariantFilesPage.class.getName()).log(Level.SEVERE, null, ex);
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
                setTitle("");
            } else {
                setTitle("Multiple uploads (" + items.size() + ")");
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

                        removeExistingFilters();
                        filterPanels = FilterUtils.createAndApplyGenericFixedFilter(
                                "Variant Files - Filter by File(s)",
                                files.length + " Files(s)",
                                ComboCondition.or(conditions));
                    }
                });
                popupMenu.add(filter1Item);
            }

            return popupMenu;
        }
        
        private void removeExistingFilters() {
            if (filterPanels != null) {
                for (FilterPanelSubItem panel : filterPanels) {
                    panel.removeThis();
                }
            }
        }
        
    }
    
    
    
    /*
     * VARIANT FILES DETAILED LIST EDITOR
     */
    private static class VariantFilesDetailedListEditor extends DetailedListEditor {

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
            new ImportVariantsWizard();
        }

        @Override
        public void editItems(Object[] results) {
        }

        @Override
        public void deleteItems(List<Object[]> results) {
            List<SimpleVariantFile> files = new ArrayList<SimpleVariantFile>();
            for (Object[] f : results) {
                files.add((SimpleVariantFile)f[0]);
            }
            new RemoveVariantsWizard(files);
        }
    }
    
}
