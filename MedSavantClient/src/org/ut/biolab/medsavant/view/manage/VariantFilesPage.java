/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.manage;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.controller.ThreadController;
import org.ut.biolab.medsavant.db.model.SimpleVariantFile;
import org.ut.biolab.medsavant.listener.ReferenceListener;
import org.ut.biolab.medsavant.view.ViewController;
import org.ut.biolab.medsavant.view.component.CollapsiblePanel;
import org.ut.biolab.medsavant.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.view.list.DetailedListModel;
import org.ut.biolab.medsavant.view.list.DetailedView;
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
    
    public VariantFilesPage(SectionView parent){
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
                new VariantFilesListModel(),
                new VariantFilesDetailedView(),
                new VariantFilesDetailedListEditor());
    }

    @Override
    public void referenceAdded(String name) {}

    @Override
    public void referenceRemoved(String name) {}

    @Override
    public void referenceChanged(String prnameojectName) {
        if(isLoaded) {
            update();
        } else {
            updateRequired = true;
        }
    }
    
    public void update(){
        panel.refresh();
    }

    
    /*
     * VARIANT FILES LIST MODEL
     */
    private static class VariantFilesListModel implements DetailedListModel {

        private List<String> cnames;
        private List<Class> cclasses;
        private List<Integer> chidden;

        public VariantFilesListModel() {}

        public List<Object[]> getList(int limit) throws Exception {
            List<SimpleVariantFile> files = MedSavantClient.VariantQueryUtilAdapter.getUploadedFiles(
                    LoginController.sessionId, 
                    ProjectController.getInstance().getCurrentProjectId(), 
                    ReferenceController.getInstance().getCurrentReferenceId());
            List<Object[]> result = new ArrayList<Object[]>();
            for(SimpleVariantFile svf : files){
                result.add(new Object[]{svf});
            }
            return result;
        }

        public List<String> getColumnNames() {
            if (cnames == null) {
                cnames = new ArrayList<String>();
                cnames.add("Variant File");
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


    /*
     * VARIANT FILES DETAILED VIEW
     */
    private static class VariantFilesDetailedView extends DetailedView {

        private final JPanel details;
        private final JPanel content;
        private SimpleVariantFile file;
        private DetailsSW sw;
        //private List<String> fieldNames;
        private CollapsiblePanel infoPanel;

        public VariantFilesDetailedView() {
            
            //fieldNames = new ArrayList<String>();
            //fieldNames.add("User Level");
        
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
            file = (SimpleVariantFile) item[0];
            setTitle(file.toString());

            details.removeAll();
            details.updateUI();

            if (sw != null) {
                sw.cancel(true);
            }
            sw = new DetailsSW(file);
            sw.execute();
        }

        @Override
        public void setRightClick(MouseEvent e) {
            //nothing yet
        }
        
        public synchronized void setFileInfoList(List<String[]> info) {
            
            details.removeAll();
            
            ViewUtil.setBoxYLayout(details);
            
            String[][] values = new String[info.size()][2];
            for(int i = 0; i < info.size(); i++){
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
                    return MedSavantClient.VariantQueryUtilAdapter.getTagsForUpload(LoginController.sessionId, file.getId());
                } catch (SQLException ex) {
                    return null;
                }
            }
            
            @Override
            protected void done() {
                List<String[]> infoList = null;
                try {
                    infoList = (List<String[]>) get();
                    infoList.add(0, new String[]{"Upload ID", Integer.toString(file.getId())});
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
            if (items.isEmpty()) {
                setTitle("");
            } else {
                setTitle("Multiple uploads (" + items.size() + ")");
            }
            details.removeAll();
            details.updateUI();
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
            return false;
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
            //TODO
        }
    }
    
}
