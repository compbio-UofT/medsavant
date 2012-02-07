/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.genetics;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.jidesoft.grid.SortableTable;
import com.jidesoft.grid.TableModelWrapperUtils;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.sql.SQLException;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLayeredPane;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.controller.ResultController;
import org.ut.biolab.medsavant.db.format.CustomField;
import org.ut.biolab.medsavant.db.format.AnnotationFormat;
import org.ut.biolab.medsavant.controller.ThreadController;
import org.ut.biolab.medsavant.db.util.shared.BinaryConditionMS;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.VariantStarredTableSchema;
import org.ut.biolab.medsavant.db.format.VariantFormat;
import org.ut.biolab.medsavant.db.model.StarredVariant;
import org.ut.biolab.medsavant.db.settings.Settings;
import org.ut.biolab.medsavant.db.util.shared.DBUtil;
import org.ut.biolab.medsavant.db.util.shared.MiscUtils;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.view.component.SearchableTablePanel;
import org.ut.biolab.medsavant.view.component.Util.DataRetriever;
import org.ut.biolab.medsavant.view.genetics.filter.FilterUtils;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
class TablePanel extends JLayeredPane {

    private SearchableTablePanel tablePanel;
    private WaitPanel waitPanel;

    private boolean init = false;
    private boolean updateRequired = true;
    private final Object updateLock = new Object();
    private String pageName;

    private GridBagConstraints c;

    private Map<Integer, List<StarredVariant>> starMap = new HashMap<Integer, List<StarredVariant>>();

    public TablePanel(final String pageName) {

        this.pageName = pageName;
        //this.setLayout(new BorderLayout());
        this.setLayout(new GridBagLayout());

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;

        waitPanel = new WaitPanel("Generating List View");

        this.add(waitPanel, c, JLayeredPane.MODAL_LAYER);

        showWaitCard();

        final TablePanel instance = this;
        MedSavantWorker worker = new MedSavantWorker(pageName){
            @Override
            protected Object doInBackground() {

                List<String> fieldNames = new ArrayList<String>();
                final List<Class> fieldClasses = new ArrayList<Class>();
                List<Integer> hiddenColumns = new ArrayList<Integer>();

                AnnotationFormat[] afs = ProjectController.getInstance().getCurrentAnnotationFormats();
                for(AnnotationFormat af : afs){
                    for(CustomField field : af.getCustomFields()){
                        fieldNames.add(field.getAlias());
                        switch(field.getColumnType()){
                            case INTEGER:
                            case BOOLEAN:
                                fieldClasses.add(Integer.class);
                                break;
                            case FLOAT:
                            case DECIMAL:
                                fieldClasses.add(Double.class);
                                break;
                            case VARCHAR:
                            default:
                                fieldClasses.add(String.class);
                                break;
                        }

                        //only show vcf fields (except custom info)
                        if(!(af.getProgram().equals(VariantFormat.ANNOTATION_FORMAT_DEFAULT) &&
                                    !(field.getColumnName().equals(DefaultVariantTableSchema.COLUMNNAME_OF_UPLOAD_ID) ||
                                    field.getColumnName().equals(DefaultVariantTableSchema.COLUMNNAME_OF_FILE_ID) ||
                                    field.getColumnName().equals(DefaultVariantTableSchema.COLUMNNAME_OF_VARIANT_ID) ||
                                    field.getColumnName().equals(DefaultVariantTableSchema.COLUMNNAME_OF_CUSTOM_INFO)))){
                                //|| af.getProgram().equals(VariantFormat.ANNOTATION_FORMAT_CUSTOM_VCF))){
                            hiddenColumns.add(fieldNames.size()-1);
                        }
                    }
                }
                if(this.isThreadCancelled()) return null;

                DataRetriever retriever = new DataRetriever(){
                    public List<Object[]> retrieve(int start, int limit) {
                        showWaitCard();
                        try {
                            final List<Object[]> result = ResultController.getInstance().getFilteredVariantRecords(start, limit);
                            checkStarring(result);
                            showShowCard();
                            return result;
                        } catch (NonFatalDatabaseException ex) {
                            Logger.getLogger(TablePanel.class.getName()).log(Level.SEVERE, null, ex);
                            showShowCard();
                            return null;
                        }
                    }

                    public int getTotalNum() {
                        showWaitCard();
                        int result = 0;
                        try {
                            result = ResultController.getInstance().getNumFilteredVariants();
                        } catch (NonFatalDatabaseException ex) {
                            Logger.getLogger(TablePanel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        showShowCard();
                        return result;
                    }

                    public void retrievalComplete() {
                        synchronized (updateLock){
                            updateRequired = false;
                        }
                    }
                };

                SearchableTablePanel stp = new SearchableTablePanel(pageName, fieldNames, fieldClasses, hiddenColumns, 1000, retriever){
                    @Override
                    public String getToolTip(int actualRow){
                        if(starMap.get(actualRow) != null && !starMap.get(actualRow).isEmpty()){
                            String s = "<HTML>";
                            List<StarredVariant> starred = starMap.get(actualRow);
                            for(int i = 0; i < starred.size(); i++){
                                StarredVariant current = starred.get(i);
                                s += "\"" + MiscUtils.addBreaksToString(current.getDescription(), 100) + "\"<BR>";
                                s += "- " + current.getUser() + ", " + current.getTimestamp().toString();
                                if(i != starred.size()-1){
                                    s += "<BR>----------<BR>";
                                }
                            }
                            s += "</HTML>";
                            return s;
                        }
                        return null;
                    }
                };

                stp.getTable().addMouseListener(new MouseAdapter() {
                    public void mouseReleased(MouseEvent e) {

                        //check for right click
                        if(!SwingUtilities.isRightMouseButton(e)) return;

                        SortableTable table = tablePanel.getTable();
                        int numSelected = table.getSelectedRows().length;
                        if(numSelected == 1){
                            int r = table.rowAtPoint(e.getPoint());
                            if(r < 0 || r >= table.getRowCount()) return;
                            JPopupMenu popup = createPopupSingle(table, r);
                            popup.show(e.getComponent(), e.getX(), e.getY());
                        } else if(numSelected > 1){
                            JPopupMenu popup = createPopupMultiple(table);
                            popup.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                });

                return stp;
            }

            @Override
            protected void showProgress(double fraction) {
                //do nothing
            }

            @Override
            protected void showSuccess(Object result) {
                tablePanel = (SearchableTablePanel)result;
                instance.remove(tablePanel);
                instance.add(tablePanel, c, JLayeredPane.DEFAULT_LAYER);
                showShowCard();
                updateIfRequired();
                init = true;
            }

        };
        worker.execute();

    }

    private void showWaitCard() {
        waitPanel.setVisible(true);
        this.setLayer(waitPanel, JLayeredPane.MODAL_LAYER);
        waitPanel.repaint();
    }

    private void showShowCard() {
        waitPanel.setVisible(false);
    }

    public boolean isInit(){
        return init;
    }

    void setUpdateRequired(boolean b) {
        updateRequired = b;
    }

    public void updateIfRequired(){
        if(tablePanel == null) return;
        synchronized (updateLock){
            if(updateRequired){
                tablePanel.forceRefreshData();
            }
        }
    }

    private JPopupMenu createPopupSingle(SortableTable table, int r){

        table.setRowSelectionInterval(r, r);
        int row = TableModelWrapperUtils.getActualRowAt(table.getModel(), r);

        final String chrom = (String)table.getModel().getValueAt(r, DefaultVariantTableSchema.INDEX_OF_CHROM);
        final int position = (Integer)table.getModel().getValueAt(r, DefaultVariantTableSchema.INDEX_OF_POSITION);
        final String alt = (String)table.getModel().getValueAt(r, DefaultVariantTableSchema.INDEX_OF_ALT);


        JPopupMenu menu = new JPopupMenu();

        //star/unstar
        if(isStarredByUser(row)){
            menu.add(createUnstarVariantItem(row));
        } else {
            menu.add(createStarVariantsItem(table));
        }

        menu.add(new JSeparator());

        //Filter by position
        JMenuItem filter1Item = new JMenuItem("Filter by Position");
        filter1Item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                ThreadController.getInstance().cancelWorkers(pageName);

                Condition[] conditions = new Condition[2];
                conditions[0] = BinaryConditionMS.equalTo(ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM), chrom);
                conditions[1] = BinaryConditionMS.equalTo(ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_POSITION), position);
                FilterUtils.createAndApplyGenericFixedFilter(
                        "Table - Filter by Position",
                        "Chromosome: " + chrom + ", Position: " + position,
                        ComboCondition.and(conditions));
            }
        });
        menu.add(filter1Item);

        //Filter by position and alt
        JMenuItem filter2Item = new JMenuItem("Filter by Position and Alt");
        filter2Item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                ThreadController.getInstance().cancelWorkers(pageName);

                Condition[] conditions = new Condition[3];
                conditions[0] = BinaryConditionMS.equalTo(ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM), chrom);
                conditions[1] = BinaryConditionMS.equalTo(ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_POSITION), position);
                conditions[2] = BinaryConditionMS.equalTo(ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_ALT), alt);
                FilterUtils.createAndApplyGenericFixedFilter(
                        "Table - Filter by Position",
                        "Chromosome: " + chrom + ", Position: " + position + ", Alt: " + alt,
                        ComboCondition.and(conditions));
            }
        });
        menu.add(filter2Item);

        return menu;
    }

    private JPopupMenu createPopupMultiple(SortableTable table){
        JPopupMenu menu = new JPopupMenu();

        //Star variant(s)
        menu.add(createStarVariantsItem(table));

        return menu;
    }

    private JMenuItem createStarVariantsItem(final SortableTable table){

        final int[] selected = table.getSelectedRows();
        int[] actualSelected = new int[selected.length];
        for(int i = 0; i < selected.length; i++){
            actualSelected[i] = TableModelWrapperUtils.getActualRowAt(table.getModel(), selected[i]);
        }
        final int[] finalActualSelected = actualSelected;

        JMenuItem item = new JMenuItem("Mark Variant(s) as Important");
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                String description = "";
                while(true){
                    description = JOptionPane.showInputDialog("Add a description (500 char limit):", description.substring(0, Math.min(description.length(), 500)));
                    if(description == null) return;
                    if(description.length() <= 500) break;
                }

                List<StarredVariant> list = new ArrayList<StarredVariant>();
                for(int i = 0; i < finalActualSelected.length; i++){
                    int row = selected[i];
                    int actualRow = finalActualSelected[i];
                    StarredVariant sv = new StarredVariant(
                            (Integer)table.getModel().getValueAt(row, DefaultVariantTableSchema.INDEX_OF_UPLOAD_ID),
                            (Integer)table.getModel().getValueAt(row, DefaultVariantTableSchema.INDEX_OF_FILE_ID),
                            (Integer)table.getModel().getValueAt(row, DefaultVariantTableSchema.INDEX_OF_VARIANT_ID),
                            LoginController.getUsername(),
                            description,
                            DBUtil.getCurrentTimestamp());
                    list.add(sv);
                    if(!starMap.containsKey(actualRow)){
                        starMap.put(actualRow, new ArrayList<StarredVariant>());
                    }
                    removeStarForUser(actualRow);
                    starMap.get(actualRow).add(sv);
                }
                try {
                    int numStarred = MedSavantClient.VariantQueryUtilAdapter.addStarredVariants(
                            LoginController.sessionId,
                            ProjectController.getInstance().getCurrentProjectId(),
                            ReferenceController.getInstance().getCurrentReferenceId(),
                            list);
                    if(numStarred < list.size()){
                        JOptionPane.showMessageDialog(
                                null,
                                "<HTML>" + (list.size() - numStarred) + " out of " + list.size() + " variants were not marked. <BR>The total number of marked variants cannot exceed " + Settings.NUM_STARRED_ALLOWED + ".</HTML>",
                                "Out of Space",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(TablePanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (RemoteException ex) {
                    Logger.getLogger(TablePanel.class.getName()).log(Level.SEVERE, null, ex);
                }

                //add to view
                for(Integer i : finalActualSelected){
                    tablePanel.addSelectedRow(i);
                }
                tablePanel.repaint();
            }
        });

        return item;
    }

    private JMenuItem createUnstarVariantItem(final int row){

        JMenuItem item = new JMenuItem("Unmark");
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                StarredVariant sv = null;
                for(StarredVariant current : starMap.get(row)){
                    if(current.getUser().equals(LoginController.getUsername())){
                        sv = current;
                        break;
                    }
                }

                try {
                    MedSavantClient.VariantQueryUtilAdapter.unstarVariant(
                            LoginController.sessionId,
                            ProjectController.getInstance().getCurrentProjectId(),
                            ReferenceController.getInstance().getCurrentReferenceId(),
                            sv.getUploadId(),
                            sv.getFileId(),
                            sv.getVariantId(),
                            LoginController.getUsername());
                } catch (SQLException ex) {
                    Logger.getLogger(TablePanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (RemoteException ex) {
                    Logger.getLogger(TablePanel.class.getName()).log(Level.SEVERE, null, ex);
                }

                //remove from view
                List<StarredVariant> list = starMap.get(row);
                if(list.size() == 1){
                    tablePanel.removeSelectedRow(row);
                    tablePanel.repaint();
                }
                removeStarForUser(row);
            }
        });

        return item;
    }

    private void checkStarring(List<Object[]> variants){

        List<Integer> selected = new ArrayList<Integer>();
        starMap.clear();

        try {
            Set<StarredVariant> starred = MedSavantClient.VariantQueryUtilAdapter.getStarredVariants(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectId(), ReferenceController.getInstance().getCurrentReferenceId());

            for(int i = 0; i < variants.size(); i++){
                Object[] row = variants.get(i);
                StarredVariant current = new StarredVariant(
                        (Integer)row[DefaultVariantTableSchema.INDEX_OF_UPLOAD_ID],
                        (Integer)row[DefaultVariantTableSchema.INDEX_OF_FILE_ID],
                        (Integer)row[DefaultVariantTableSchema.INDEX_OF_VARIANT_ID],
                        null,
                        null,
                        null);
                if(starred.contains(current)){

                    selected.add(i);
                    if(starMap.get(i) == null){
                        starMap.put(i, new ArrayList<StarredVariant>());
                    }

                    Object[] arr = starred.toArray();
                    for(Object a : arr){
                        StarredVariant sv = (StarredVariant)a;
                        if(sv.getUploadId() == current.getUploadId() && sv.getFileId() == current.getFileId() && sv.getVariantId() == current.getVariantId()){
                            starMap.get(i).add(sv);
                        }
                    }

                }
            }

        } catch (Exception ex) {
            Logger.getLogger(TablePanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        tablePanel.setSelectedRows(selected);
    }

    private boolean isStarredByUser(int row){
        if(!starMap.containsKey(row)) return false;
        List<StarredVariant> starred = starMap.get(row);
        for(StarredVariant current : starred){
            if(current.getUser().equals(LoginController.getUsername())){
                return true;
            }
        }
        return false;
    }

    private void removeStarForUser(int row){
        List<StarredVariant> list = starMap.get(row);
        int index = -1;
        for(int i = 0; i < list.size(); i++){
            StarredVariant current = list.get(i);
            if(current.getUser().equals(LoginController.getUsername())){
                index = i;
            }
        }
        if(index != -1){
            list.remove(index);
        }
    }
}
