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

import java.awt.BorderLayout;
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
import org.ut.biolab.medsavant.db.TableSchema;
import org.ut.biolab.medsavant.filter.*;
import org.ut.biolab.medsavant.format.BasicVariantColumns;
import org.ut.biolab.medsavant.format.CustomField;
import org.ut.biolab.medsavant.format.AnnotationFormat;
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
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
public class TablePanel extends JLayeredPane implements BasicVariantColumns {

    private static final Log LOG = LogFactory.getLog(TablePanel.class);


    private WaitPanel waitPanel;
    private boolean tableInitialized = false;
    private boolean updateRequired = true;
    private final Object updateLock = new Object();
    private String pageName;
    private GridBagConstraints c;
    private Map<Integer, List<VariantComment>> starMap = new HashMap<Integer, List<VariantComment>>();
    private static List<VariantSelectionChangedListener> listeners = new ArrayList<VariantSelectionChangedListener>();
    private final JPanel activePanel;

    private final JPanel summaryContainer;
    private final JPanel tableContainer;
    private SearchableTablePanel searchableTablePanel;

    private final MedSavantWorker tableInitializer;

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

        summaryContainer = new JPanel();
        summaryContainer.setLayout(new BorderLayout());

        tableContainer = new JPanel();
        tableContainer.setLayout(new BorderLayout());

        activePanel = new JPanel();
        activePanel.setLayout(new BorderLayout());
        add(activePanel, c, JLayeredPane.DEFAULT_LAYER);

        waitPanel = new WaitPanel("Generating List View");

        add(waitPanel, c, JLayeredPane.MODAL_LAYER);

        showWaitCard();

        tableInitializer = new MedSavantWorker(pageName) {

            @Override
            protected Object doInBackground() throws Exception {

                LOG.info("INITIALIZING TABLE BY THREAD");

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
                        if (!(af.getProgram().equals(AnnotationFormat.ANNOTATION_FORMAT_DEFAULT)
                                && !(field.getColumnName().equals(UPLOAD_ID.getColumnName()))
                                || field.getColumnName().equals(FILE_ID.getColumnName())
                                || field.getColumnName().equals(VARIANT_ID.getColumnName())
                                || field.getColumnName().equals(FILTER.getColumnName())
                                || field.getColumnName().equals(QUAL.getColumnName())
                                || field.getColumnName().equals(GT.getColumnName())
                                || field.getColumnName().equals(DBSNP_ID.getColumnName())
                                || field.getColumnName().equals(CUSTOM_INFO.getColumnName()))) {
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
                        LOG.info("Retrieving data for TablePanel");
                        try {
                            List<Object[]> result = ResultController.getInstance().getFilteredVariantRecords(start, limit, null);
                            //checkStarring(result);
                            return result;
                        } catch (Exception ex) {
                            LOG.error("Error retrieving data.", ex);
                            setActivePanel(true);
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
                        return result;
                    }

                    @Override
                    public void retrievalComplete() {
                        showShowCard();
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

                            int uploadID = (Integer)stp.getTable().getModel().getValueAt(rowToFetch, INDEX_OF_UPLOAD_ID);
                            int fileID = (Integer)stp.getTable().getModel().getValueAt(rowToFetch, INDEX_OF_FILE_ID);
                            int variantID = (Integer)stp.getTable().getModel().getValueAt(rowToFetch, INDEX_OF_VARIANT_ID);

                            DbColumn uIDcol = ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(UPLOAD_ID);
                            DbColumn fIDcol = ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(FILE_ID);
                            DbColumn vIDcol = ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(VARIANT_ID);

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
                                    (Integer)row[INDEX_OF_UPLOAD_ID],
                                    (Integer)row[INDEX_OF_FILE_ID],
                                    (Integer)row[INDEX_OF_VARIANT_ID],
                                    (Integer)ReferenceController.getInstance().getCurrentReferenceID(),
                                    (Integer)0, // pipeline ID
                                    (String)row[INDEX_OF_DNA_ID],
                                    (String)row[INDEX_OF_CHROM],
                                    (Integer)row[INDEX_OF_POSITION],
                                    (String)row[INDEX_OF_DBSNP_ID],
                                    (String)row[INDEX_OF_REF],
                                    (String)row[INDEX_OF_ALT],
                                    (Float)row[INDEX_OF_QUAL],
                                    (String)row[INDEX_OF_FILTER],
                                    (String)row[INDEX_OF_CUSTOM_INFO],
                                    new Object[]{});

                            String type = (String)row[INDEX_OF_VARIANT_TYPE];
                            String zygosity = (String)row[INDEX_OF_ZYGOSITY];
                            String genotype = (String)row[INDEX_OF_GT];

                            r.setType(VariantRecord.VariantType.valueOf(type));
                            r.setZygosity(VariantRecord.Zygosity.valueOf(zygosity));
                            r.setGenotype(genotype);

                            for (VariantSelectionChangedListener l: listeners) {
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

                        SortableTable table = searchableTablePanel.getTable();
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
                LOG.info("DONE INITING TABLE");
                tableContainer.removeAll();
                searchableTablePanel = (SearchableTablePanel)result;
                tableContainer.add(searchableTablePanel,BorderLayout.CENTER);
                tableContainer.updateUI();
                tableInitialized = true;
                updateRequired = true;
                updateTableIfRequired();
            }
        };
    }

    private void showWaitCard() {
        LOG.info("WAITING...");
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                waitPanel.setVisible(true);
                setLayer(waitPanel, JLayeredPane.MODAL_LAYER);
                waitPanel.repaint();
            }
        });
    }

    private void showShowCard() {
         LOG.info("SHOWING...");
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                waitPanel.setVisible(false);
                activePanel.repaint();
            }
        });

    }

    public boolean isInit() {
        return tableInitialized;
    }

    void setUpdateRequired(boolean b) {
        updateRequired = b;
    }

    public static void addVariantSelectionChangedListener(VariantSelectionChangedListener l) {
        listeners.add(l);
    }

    public void update() {
        LOG.info("Showing wait card");

        showWaitCard();

        boolean tableIsShowing = false;
        /*try {
            tableIsShowing = ResultController.getInstance().getFilteredVariantCount() < 1000000;
        } catch (Exception ex) {
        }
        * 
        */
        if (tableIsShowing) {
            updateRequired = true;
            updateTableIfRequired();
        } else {
            updateSummary();
            setActivePanel(false);
            showShowCard();
        }
    }

    private void updateSummary() {
        try {
            int numPassingVariants = ResultController.getInstance().getFilteredVariantCount();

            final JPanel blockPanel = new JPanel();
            ViewUtil.applyVerticalBoxLayout(blockPanel);


            JButton b = ViewUtil.getSoftButton("Load Spreadsheet");

            b.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {

                    showWaitCard();
                    setActivePanel(true);

                    if (!tableInitialized) {
                        tableInitializer.execute();
                    } else {
                        updateTableIfRequired();
                    }
                }
            });


            blockPanel.add(ViewUtil.centerHorizontally(new JLabel(ViewUtil.numToString(numPassingVariants) + " match your search conditions")));
            blockPanel.add(Box.createVerticalStrut(5));
            blockPanel.add(ViewUtil.centerHorizontally(b));

            JPanel centered = ViewUtil.getClearPanel();
            ViewUtil.applyVerticalBoxLayout(centered);
            centered.add(Box.createVerticalGlue());
            centered.add(blockPanel);
            centered.add(Box.createVerticalGlue());

            summaryContainer.removeAll();
            summaryContainer.add(ViewUtil.centerHorizontally(centered),BorderLayout.CENTER);

        } catch (Exception ex) {
            LOG.error(TablePanel.class, ex);
        }

    }

    public void updateTableIfRequired() {
        LOG.info("A");
        if (searchableTablePanel == null) {
            LOG.info("B");
            return;
        }
        LOG.info("C");
        synchronized (updateLock) {
            LOG.info("D");
            if (updateRequired) {
                LOG.info("E");
                searchableTablePanel.forceRefreshData();
            }
        }
        LOG.info("F");
    }

    private JPopupMenu createPopupSingle() {

        JPopupMenu menu = new JPopupMenu();

        TableModel model = searchableTablePanel.getTable().getModel();
        int r = TableModelWrapperUtils.getActualRowAt(model, searchableTablePanel.getTable().getSelectedRow());

        String chrom = (String)model.getValueAt(r, INDEX_OF_CHROM);
        int pos = (Integer)model.getValueAt(r, INDEX_OF_POSITION);
        String alt = (String)model.getValueAt(r, INDEX_OF_ALT);

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

    private void setActivePanel(boolean showTable) {

        final JPanel p = showTable ? this.tableContainer : this.summaryContainer;

        LOG.info("Setting panel to " + (showTable ? "this.searchableTablePanel" : "this.summaryPanel"));

        activePanel.removeAll();
        activePanel.add(p, BorderLayout.CENTER);
        activePanel.updateUI();


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
            FilterState chromState = StringListFilterView.wrapState(WhichTable.VARIANT, CHROM.getColumnName(), CHROM.getAlias(), Arrays.asList(chrom));

            FilterState posState = NumericFilterView.wrapState(WhichTable.VARIANT, POSITION.getColumnName(), POSITION.getAlias(), new Range(pos, pos), false);

            if (alt != null) {
                FilterState altState = StringListFilterView.wrapState(WhichTable.VARIANT, ALT.getColumnName(), ALT.getAlias(), Arrays.asList(alt));

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
                TableModel model = searchableTablePanel.getTable().getModel();
                int[] selRows = TableModelWrapperUtils.getActualRowsAt(model, searchableTablePanel.getTable().getSelectedRows(), false);
                for (int r : selRows) {
                    String chrom = (String) model.getValueAt(r, INDEX_OF_CHROM);
                    int pos = (Integer) model.getValueAt(r, INDEX_OF_POSITION);
                    regions.add(new GenomicRegion(null, chrom, pos, pos));
                }

                RegionSet r = RegionController.getInstance().createAdHocRegionSet("Selected Variant Positions", regions);
                GeneticsFilterPage.getSearchBar().loadFilters(RegionSetFilterView.wrapState(Arrays.asList(r)));
            }
        });
        menu.add(posItem);

        return menu;
    }

    /*
     * private void checkStarring(List<Object[]> variants) {
     *
     * List<Integer> selected = new ArrayList<Integer>(); starMap.clear();
     *
     * try { Set<StarredVariant> starred =
     * MedSavantClient.VariantManager.getStarredVariants(LoginController.sessionId,
     * ProjectController.getInstance().getCurrentProjectID(),
     * ReferenceController.getInstance().getCurrentReferenceID());
     *
     * for (int i = 0; i < variants.size(); i++) { Object[] row =
     * variants.get(i); StarredVariant current = new StarredVariant( (Integer)
     * row[DefaultVariantTableSchema.INDEX_OF_UPLOAD_ID], (Integer)
     * row[DefaultVariantTableSchema.INDEX_OF_FILE_ID], (Integer)
     * row[DefaultVariantTableSchema.INDEX_OF_VARIANT_ID], null, null, null); if
     * (starred.contains(current)) {
     *
     * selected.add(i); if (starMap.get(i) == null) { starMap.put(i, new
     * ArrayList<StarredVariant>()); }
     *
     * Object[] arr = starred.toArray(); for (Object a : arr) { StarredVariant
     * sv = (StarredVariant) a; if (sv.getUploadId() == current.getUploadId() &&
     * sv.getFileId() == current.getFileId() && sv.getVariantId() ==
     * current.getVariantId()) { starMap.get(i).add(sv); } }
     *
     * }
     * }
     *
     * } catch (Exception ex) { LOG.error("Error checking stars.", ex); }
     *
     * tablePanel.setSelectedRows(selected); }
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
