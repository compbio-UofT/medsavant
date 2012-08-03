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

package org.ut.biolab.medsavant.view.genetics;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;

import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.jidesoft.grid.SortableTable;
import com.jidesoft.grid.TableModelWrapperUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.ResultController;
import org.ut.biolab.medsavant.db.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.TableSchema;
import org.ut.biolab.medsavant.filter.*;
import org.ut.biolab.medsavant.format.CustomField;
import org.ut.biolab.medsavant.format.AnnotationFormat;
import org.ut.biolab.medsavant.format.VariantFormat;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.GenomicRegion;
import org.ut.biolab.medsavant.model.Range;
import org.ut.biolab.medsavant.model.RegionSet;
import org.ut.biolab.medsavant.model.VariantComment;
import org.ut.biolab.medsavant.model.event.VariantSelectionChangedListener;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.reference.ReferenceController;
import org.ut.biolab.medsavant.region.RegionController;
import org.ut.biolab.medsavant.region.RegionSetFilterView;
import org.ut.biolab.medsavant.util.*;
import org.ut.biolab.medsavant.vcf.VariantRecord;
import org.ut.biolab.medsavant.view.component.SearchableTablePanel;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
public class TablePanel extends JLayeredPane {
    private static final Log LOG = LogFactory.getLog(TablePanel.class);
    private SearchableTablePanel tablePanel;
    private WaitPanel waitPanel;
    private boolean init = false;
    private boolean updateRequired = true;
    private final Object updateLock = new Object();
    private String pageName;
    private GridBagConstraints c;
    private Map<Integer, List<VariantComment>> starMap = new HashMap<Integer, List<VariantComment>>();
    private static List<VariantSelectionChangedListener> listeners = new ArrayList<VariantSelectionChangedListener>();


    public TablePanel(String page) {

        this.pageName = page;
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

        add(waitPanel, c, JLayeredPane.MODAL_LAYER);

        showWaitCard();

        MedSavantWorker worker = new MedSavantWorker(pageName) {

            @Override
            protected Object doInBackground() throws Exception {

                List<String> fieldNames = new ArrayList<String>();
                final List<Class> fieldClasses = new ArrayList<Class>();
                List<Integer> hiddenColumns = new ArrayList<Integer>();

                AnnotationFormat[] afs = ProjectController.getInstance().getCurrentAnnotationFormats();
                for (AnnotationFormat af : afs) {
                    for (CustomField field : af.getCustomFields()) {
                        fieldNames.add(field.getAlias());
                        switch (field.getColumnType()) {
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

                        //only show some vcf fields
                        if (!(af.getProgram().equals(VariantFormat.ANNOTATION_FORMAT_DEFAULT)
                                && !(field.getColumnName().equals(DefaultVariantTableSchema.COLUMNNAME_OF_UPLOAD_ID)
                                || field.getColumnName().equals(DefaultVariantTableSchema.COLUMNNAME_OF_FILE_ID)
                                || field.getColumnName().equals(DefaultVariantTableSchema.COLUMNNAME_OF_VARIANT_ID)
                                || field.getColumnName().equals(DefaultVariantTableSchema.COLUMNNAME_OF_FILTER)
                                || field.getColumnName().equals(DefaultVariantTableSchema.COLUMNNAME_OF_QUAL)
                                || field.getColumnName().equals(DefaultVariantTableSchema.COLUMNNAME_OF_GT)
                                || field.getColumnName().equals(DefaultVariantTableSchema.COLUMNNAME_OF_DBSNP_ID)
                                || field.getColumnName().equals(DefaultVariantTableSchema.COLUMNNAME_OF_CUSTOM_INFO)))) {
                            //|| af.getProgram().equals(VariantFormat.ANNOTATION_FORMAT_CUSTOM_VCF))) {
                            hiddenColumns.add(fieldNames.size() - 1);
                        }
                    }
                }
                if (isCancelled()) {
                    throw new InterruptedException();
                }

                final DataRetriever<Object[]> retriever = new DataRetriever<Object[]>() {

                    @Override
                    public List<Object[]> retrieve(int start, int limit) {
                        showWaitCard();
                        try {
                            List<Object[]> result = ResultController.getInstance().getFilteredVariantRecords(start, limit, null);
                            //checkStarring(result);
                            showShowCard();
                            return result;
                        } catch (Exception ex) {
                            LOG.error("Error retrieving data.", ex);
                            showShowCard();
                            return null;
                        }
                    }

                    @Override
                    public int getTotalNum() {
                        showWaitCard();
                        int result = 0;
                        try {
                            result = ResultController.getInstance().getFilteredVariantCount();
                        } catch (Exception ex) {
                            LOG.error("Error getting total number.", ex);
                        }
                        showShowCard();
                        return result;
                    }

                    @Override
                    public void retrievalComplete() {
                        synchronized (updateLock) {
                            updateRequired = false;
                        }
                    }
                };

                final SearchableTablePanel stp = new SearchableTablePanel(pageName, fieldNames.toArray(new String[0]), fieldClasses.toArray(new Class[0]), MiscUtils.toIntArray(hiddenColumns), 1000, retriever) {

                    @Override
                    public String getToolTip(int actualRow) {
                        if (starMap.get(actualRow) != null && !starMap.get(actualRow).isEmpty()) {
                            String s = "<HTML>";
                            List<VariantComment> starred = starMap.get(actualRow);
                            for (int i = 0; i < starred.size(); i++) {
                                VariantComment current = starred.get(i);
                                s += "\"" + ClientMiscUtils.addBreaksToString(current.getDescription(), 100) + "\"<BR>";
                                s += "- " + current.getUser() + ", " + current.getTimestamp().toString();
                                if (i != starred.size() - 1) {
                                    s += "<BR>----------<BR>";
                                }
                            }
                            s += "</HTML>";
                            return s;
                        }
                        return null;
                    }
                };

                stp.getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent e) {

                        if (e.getValueIsAdjusting()) {
                            return;
                        }

                        int[] selRows = stp.getTable().getSelectedRows();
                        if (selRows.length > 0) {
                            int rowToFetch = selRows[0];

                            int uploadID = (Integer) stp.getTable().getModel().getValueAt(rowToFetch, DefaultVariantTableSchema.INDEX_OF_UPLOAD_ID);
                            int fileID = (Integer) stp.getTable().getModel().getValueAt(rowToFetch, DefaultVariantTableSchema.INDEX_OF_FILE_ID);
                            int variantID = (Integer) stp.getTable().getModel().getValueAt(rowToFetch, DefaultVariantTableSchema.INDEX_OF_VARIANT_ID);

                            DbColumn uIDcol = ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_UPLOAD_ID);
                            DbColumn fIDcol = ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_FILE_ID);
                            DbColumn vIDcol = ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_VARIANT_ID);

                            Condition[][] conditions = new Condition[1][3];
                            conditions[0][0] = BinaryConditionMS.equalTo(uIDcol, uploadID);
                            conditions[0][1] = BinaryConditionMS.equalTo(fIDcol, fileID);
                            conditions[0][2] = BinaryConditionMS.equalTo(vIDcol, variantID);


                            List<Object[]> rows;
                            try {
                                rows = MedSavantClient.VariantManager.getVariants(
                                        LoginController.sessionId,
                                        ProjectController.getInstance().getCurrentProjectID(),
                                        ReferenceController.getInstance().getCurrentReferenceID(),
                                        conditions,
                                        0, 1);

                            } catch (Exception ex) {
                                DialogUtils.displayError("Error", "There was a problem retriving variant results");
                                return;
                            }

                            Object[] row = rows.get(0);

                            VariantRecord r = new VariantRecord(
                                    (Integer)   row[DefaultVariantTableSchema.INDEX_OF_UPLOAD_ID],
                                    (Integer)   row[DefaultVariantTableSchema.INDEX_OF_FILE_ID],
                                    (Integer)   row[DefaultVariantTableSchema.INDEX_OF_VARIANT_ID],
                                    (Integer)   ReferenceController.getInstance().getCurrentReferenceID(),
                                    (Integer)   0, // pipeline ID
                                    (String)    row[DefaultVariantTableSchema.INDEX_OF_DNA_ID],
                                    (String)    row[DefaultVariantTableSchema.INDEX_OF_CHROM],
                                    (Integer)   row[DefaultVariantTableSchema.INDEX_OF_POSITION], // TODO: this should be a long
                                    (String)    row[DefaultVariantTableSchema.INDEX_OF_DBSNP_ID],
                                    (String)    row[DefaultVariantTableSchema.INDEX_OF_REF],
                                    (String)    row[DefaultVariantTableSchema.INDEX_OF_ALT],
                                    (Float)     row[DefaultVariantTableSchema.INDEX_OF_QUAL],
                                    (String)    row[DefaultVariantTableSchema.INDEX_OF_FILTER],
                                    (String)    row[DefaultVariantTableSchema.INDEX_OF_CUSTOM_INFO],
                                    new Object[]{});

                            String type = (String)row[DefaultVariantTableSchema.INDEX_OF_VARIANT_TYPE];
                            String zygosity = (String)row[DefaultVariantTableSchema.INDEX_OF_ZYGOSITY];
                            String genotype = ((String)row[DefaultVariantTableSchema.INDEX_OF_GT]);

                            r.setType(VariantRecord.VariantType.valueOf(type));
                            r.setZygosity(VariantRecord.Zygosity.valueOf(zygosity));
                            r.setGenotype(genotype);

                            for (VariantSelectionChangedListener l : listeners) {
                                l.variantSelectionChanged(r);
                            }
                        }
                    }
                });

                stp.setExportButtonVisible(false);

                stp.getTable().addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseReleased(MouseEvent e) {

                        //check for right click
                        if (!SwingUtilities.isRightMouseButton(e)) {
                            return;
                        }

                        SortableTable table = tablePanel.getTable();
                        int numSelected = table.getSelectedRows().length;
                        if (numSelected == 1) {
                            int r = table.rowAtPoint(e.getPoint());
                            if (r < 0 || r >= table.getRowCount()) {
                                return;
                            }
                            JPopupMenu popup = createPopupSingle();
                            popup.show(e.getComponent(), e.getX(), e.getY());
                        } else if (numSelected > 1) {
                            JPopupMenu popup = createPopupMultiple();
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
                tablePanel = (SearchableTablePanel) result;
                remove(tablePanel);
                add(tablePanel, c, JLayeredPane.DEFAULT_LAYER);
                showShowCard();
                updateIfRequired();
                init = true;
            }
        };
        worker.execute();

    }

    private void showWaitCard() {
        waitPanel.setVisible(true);
        setLayer(waitPanel, JLayeredPane.MODAL_LAYER);
        waitPanel.repaint();
    }

    private void showShowCard() {
        waitPanel.setVisible(false);
    }

    public boolean isInit() {
        return init;
    }

    void setUpdateRequired(boolean b) {
        updateRequired = b;
    }

    public static void addVariantSelectionChangedListener(VariantSelectionChangedListener l) {
        listeners.add(l);
    }

    public void updateIfRequired() {
        if (tablePanel == null) {
            return;
        }
        synchronized (updateLock) {
            if (updateRequired) {
                tablePanel.forceRefreshData();
            }
        }
    }

    private JPopupMenu createPopupSingle() {

        JPopupMenu menu = new JPopupMenu();

        TableModel model = tablePanel.getTable().getModel();
        int r = TableModelWrapperUtils.getActualRowAt(model, tablePanel.getTable().getSelectedRow());

        String chrom = (String)model.getValueAt(r, DefaultVariantTableSchema.INDEX_OF_CHROM);
        int pos = (Integer)model.getValueAt(r, DefaultVariantTableSchema.INDEX_OF_POSITION);
        String alt = (String)model.getValueAt(r, DefaultVariantTableSchema.INDEX_OF_ALT);

        //Filter by position
        JMenuItem posItem = new JMenuItem("Filter by Position");
        posItem.addActionListener(new PopupActionListener(chrom, pos, null));
        menu.add(posItem);

        //Filter by position and alt
        JMenuItem posAndAltItem = new JMenuItem("Filter by Position and Alt");
        posAndAltItem.addActionListener(new PopupActionListener(chrom, pos, alt));
        menu.add(posAndAltItem);

        return menu;
    }

    private class PopupActionListener implements ActionListener {
        private final String chrom;
        private final int pos;
        private final String alt;

        PopupActionListener(String chrom, int pos, String alt) {
            this.chrom = chrom;
            this.pos = pos;
            this.alt = alt;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ThreadController.getInstance().cancelWorkers(pageName);

            TableSchema schema = ProjectController.getInstance().getCurrentVariantTableSchema();
            DbColumn chromColumn = schema.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM);
            FilterState chromState = StringListFilterView.wrapState(WhichTable.VARIANT, chromColumn.getName(), VariantFormat.ALIAS_OF_CHROM, Arrays.asList(chrom));

            DbColumn posColumn = schema.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_POSITION);
            FilterState posState = NumericFilterView.wrapState(WhichTable.VARIANT, posColumn.getName(), VariantFormat.ALIAS_OF_POSITION, new Range(pos, pos), false);

            if (alt != null) {
                DbColumn altColumn = schema.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_ALT);
                FilterState altState = StringListFilterView.wrapState(WhichTable.VARIANT, altColumn.getName(), VariantFormat.ALIAS_OF_ALT, Arrays.asList(alt));

                GeneticsFilterPage.getSearchBar().loadFilters(chromState, posState, altState);
            } else {
                GeneticsFilterPage.getSearchBar().loadFilters(chromState, posState);                    
            }
        }
    }

    private JPopupMenu createPopupMultiple() {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem posItem = new JMenuItem("Filter by Selected Positions");
        posItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                ThreadController.getInstance().cancelWorkers(pageName);

                List<GenomicRegion> regions = new ArrayList<GenomicRegion>();
                TableModel model = tablePanel.getTable().getModel();
                int[] selRows = TableModelWrapperUtils.getActualRowsAt(model, tablePanel.getTable().getSelectedRows(), false);
                for (int r: selRows) {
                    String chrom = (String)model.getValueAt(r, DefaultVariantTableSchema.INDEX_OF_CHROM);
                    int pos = (Integer)model.getValueAt(r, DefaultVariantTableSchema.INDEX_OF_POSITION);
                    regions.add(new GenomicRegion(null, chrom, pos, pos));
                }
                
                RegionSet r = RegionController.getInstance().createAdHocRegionSet(regions);
                GeneticsFilterPage.getSearchBar().loadFilters(RegionSetFilterView.wrapState(Arrays.asList(r)));
            }
            
        });
        menu.add(posItem);

        return menu;
    }

/*
    private void checkStarring(List<Object[]> variants) {

        List<Integer> selected = new ArrayList<Integer>();
        starMap.clear();

        try {
            Set<StarredVariant> starred = MedSavantClient.VariantManager.getStarredVariants(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectID(), ReferenceController.getInstance().getCurrentReferenceID());

            for (int i = 0; i < variants.size(); i++) {
                Object[] row = variants.get(i);
                StarredVariant current = new StarredVariant(
                        (Integer) row[DefaultVariantTableSchema.INDEX_OF_UPLOAD_ID],
                        (Integer) row[DefaultVariantTableSchema.INDEX_OF_FILE_ID],
                        (Integer) row[DefaultVariantTableSchema.INDEX_OF_VARIANT_ID],
                        null,
                        null,
                        null);
                if (starred.contains(current)) {

                    selected.add(i);
                    if (starMap.get(i) == null) {
                        starMap.put(i, new ArrayList<StarredVariant>());
                    }

                    Object[] arr = starred.toArray();
                    for (Object a : arr) {
                        StarredVariant sv = (StarredVariant) a;
                        if (sv.getUploadId() == current.getUploadId() && sv.getFileId() == current.getFileId() && sv.getVariantId() == current.getVariantId()) {
                            starMap.get(i).add(sv);
                        }
                    }

                }
            }

        } catch (Exception ex) {
            LOG.error("Error checking stars.", ex);
        }

        tablePanel.setSelectedRows(selected);
    }
*/
    private boolean isStarredByUser(int row) {
        if (!starMap.containsKey(row)) {
            return false;
        }
        List<VariantComment> starred = starMap.get(row);
        for (VariantComment current : starred) {
            if (current.getUser().equals(LoginController.getInstance().getUserName())) {
                return true;
            }
        }
        return false;
    }

    private void removeStarForUser(int row) {
        List<VariantComment> list = starMap.get(row);
        int index = -1;
        for (int i = 0; i < list.size(); i++) {
            VariantComment current = list.get(i);
            if (current.getUser().equals(LoginController.getInstance().getUserName())) {
                index = i;
            }
        }
        if (index != -1) {
            list.remove(index);
        }
    }
}
