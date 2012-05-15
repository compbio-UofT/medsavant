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

package org.ut.biolab.medsavant.view.genetics.filter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Position;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.jidesoft.list.QuickListFilterField;
import com.jidesoft.swing.SearchableUtils;
import com.jidesoft.list.FilterableCheckBoxList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.MedSavantDatabase.DefaultpatientTableSchema;
import org.ut.biolab.medsavant.db.NonFatalDatabaseException;
import org.ut.biolab.medsavant.util.BinaryConditionMS;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.util.ChromosomeComparator;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.vcf.VariantRecord.VariantType;
import org.ut.biolab.medsavant.vcf.VariantRecord.Zygosity;
import org.ut.biolab.medsavant.view.component.Util.DataRetriever;
import org.ut.biolab.medsavant.view.dialog.IndeterminateProgressDialog;
import org.ut.biolab.medsavant.view.genetics.filter.FilterState.FilterType;
import org.ut.biolab.medsavant.view.genetics.filter.FilterUtils.Table;
import org.ut.biolab.medsavant.view.util.ViewUtil;


/**
 *
 * @author Andrew
 */
public class StringListFilterView extends FilterView {
    private static final Log LOG = LogFactory.getLog(StringListFilterView.class);

    /* Convenience Functions */
    public static FilterView createPatientFilterView(String tablename, String columnname, int queryId, String alias) throws SQLException, NonFatalDatabaseException, RemoteException {
        return new StringListFilterView(new JPanel(), tablename, columnname, queryId, alias, Table.PATIENT);
    }

    public static FilterView createVariantFilterView(String tablename, String columnname, int queryId, String alias) throws SQLException, NonFatalDatabaseException, RemoteException {
        return new StringListFilterView(new JPanel(), tablename, columnname, queryId, alias, Table.VARIANT);
    }
    private ListRetriever retriever;
    private FilterableCheckBoxList filterableList;

    public StringListFilterView(String tablename, String columnname, int queryId, String alias, Table whichTable) throws SQLException, RemoteException {
        this(new JPanel(), tablename, columnname, queryId, alias, whichTable);
    }

    public StringListFilterView(FilterState state, int queryId) throws SQLException, RemoteException {
        this(new JPanel(), FilterUtils.getTableName(Table.valueOf(state.getValues().get("table"))), state.getId(), queryId, state.getName(), Table.valueOf(state.getValues().get("table")));
        String values = state.getValues().get("values");
        if (values != null) {
            List<String> l = new ArrayList<String>();
            Collections.addAll(l, values.split(";;;"));
            applyFilter(l);
        }
    }

    /* StringListFilterView */
    private ActionListener al;
    private String columnname;
    private String alias;
    private Table whichTable;
    private List<String> appliedValues;

    public final void applyFilter(List<String> list) {

        ArrayList<Integer> indiciesOfItemsFromListInFilterableList = new ArrayList<Integer>();
        int[] indices = filterableList.getCheckBoxListSelectedIndices();
        for (int i : indices) {
            String option = filterableList.getModel().getElementAt(i).toString();
            if (list.contains(option)) {
                indiciesOfItemsFromListInFilterableList.add(i);
            }
        }

        int[] selectedIndicies = new int[indiciesOfItemsFromListInFilterableList.size()];


        for (int i = 0; i < selectedIndicies.length; i++) {
            selectedIndicies[i] = indiciesOfItemsFromListInFilterableList.get(i);
        }

        filterableList.setCheckBoxListSelectedIndices(selectedIndicies);

        al.actionPerformed(new ActionEvent(this, 0, null));
    }
    private int peekingPanelWidth = 370;

    private StringListFilterView(final JComponent container, final String tablename, final String columnname, int queryId, final String alias, final Table whichTable) throws SQLException, RemoteException {
        super(alias, container, queryId);

        this.columnname = columnname;
        this.alias = alias;
        this.whichTable = whichTable;

        List<String> uniq = new ArrayList<String>();
        if (columnname.equals(DefaultVariantTableSchema.COLUMNNAME_OF_AC)) {
            uniq.addAll(Arrays.asList(
                    new String[]{
                        "1", "2"
                    }));
        } else if (columnname.equals(DefaultVariantTableSchema.COLUMNNAME_OF_AF)) {
            uniq.addAll(Arrays.asList(
                    new String[]{
                        "0.50", "1.00"
                    }));
        } else if (columnname.equals(DefaultVariantTableSchema.COLUMNNAME_OF_REF)
                || columnname.equals(DefaultVariantTableSchema.COLUMNNAME_OF_ALT)) {
            uniq.addAll(Arrays.asList(
                    new String[]{
                        "A", "C", "G", "T"
                    }));
        } else if (columnname.equals(DefaultVariantTableSchema.COLUMNNAME_OF_VARIANT_TYPE)) {
            uniq.addAll(Arrays.asList(
                    new String[]{
                        VariantType.SNP.toString(), VariantType.Insertion.toString(), VariantType.Deletion.toString(), VariantType.Various.toString(), VariantType.Unknown.toString()
                    }));
        } else if (columnname.equals(DefaultVariantTableSchema.COLUMNNAME_OF_ZYGOSITY)) {
            uniq.addAll(Arrays.asList(
                    new String[]{
                        Zygosity.HomoRef.toString(), Zygosity.HomoAlt.toString(), Zygosity.Hetero.toString(), Zygosity.HeteroTriallelic.toString()
                    }));
        } else if (columnname.equals(DefaultpatientTableSchema.COLUMNNAME_OF_GENDER)) {
            uniq.addAll(Arrays.asList(
                    new String[]{
                        ClientMiscUtils.GENDER_MALE, ClientMiscUtils.GENDER_FEMALE, ClientMiscUtils.GENDER_UNKNOWN
                    }));
        } else {
            final IndeterminateProgressDialog dialog = new IndeterminateProgressDialog(
                    "Generating List",
                    "<HTML>Determining distinct values for field. <BR>This may take a few minutes the first time.</HTML>",
                    true);
            Thread t = new Thread() {

                @Override
                public void run() {
                    try {
                        initHelper(container, MedSavantClient.VariantQueryUtilAdapter.getDistinctValuesForColumn(LoginController.sessionId, tablename, columnname, true));
                        dialog.close();
                    } catch (SQLException ex) {
                        LOG.error("Error getting distinct values for " + tablename + "." + columnname, ex);
                    } catch (RemoteException ex) {
                        LOG.error("Error getting distinct values for " + tablename + "." + columnname, ex);
                    }
                }
            };
            t.start();
            dialog.setVisible(true);
            return;

        }
        initHelper(container, uniq);
    }

    public static class ListRetriever implements DataRetriever {

        private List<Object[]> optionsObjects;

        public ListRetriever(List<String> options) {
            optionsObjects = new ArrayList<Object[]>(options.size());

            //JCheckBox b = new JCheckBox();

            for (String s : options) {
                optionsObjects.add(new Object[]{new Boolean(false), s});
            }
        }

        @Override
        public List<Object[]> retrieve(int start, int limit) {
            System.err.println("Retriveing data from " + start + " to " + (start + limit));

            start = Math.max(0, start);
            int end = Math.min(start + limit, getTotalNum());

            return optionsObjects.subList(start, end);
        }

        @Override
        public int getTotalNum() {
            return optionsObjects.size();
        }

        @Override
        public void retrievalComplete() {
        }
    }

    private void initHelper(JComponent container, final List<String> uniq) {
        
        if(uniq == null){
            container.setLayout(new BorderLayout());
            container.setMaximumSize(new Dimension(1000, 80));
            container.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));     
            JTextArea label = new JTextArea("There are too many unique values to generate this list. You will not be able to filter on this column. ");
            label.setOpaque(false);
            label.setLineWrap(true);
            label.setWrapStyleWord(true);
            container.add(label);
            return;
        }

        if (columnname.equals(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM)) {
            Collections.sort(uniq, new ChromosomeComparator());
        }

        ViewUtil.applyVerticalBoxLayout(container);

        JPanel bottomContainer = new JPanel();
        ViewUtil.applyHorizontalBoxLayout(bottomContainer);
        bottomContainer.setBorder(ViewUtil.getSmallBorder());

        final JButton applyButton = new JButton("Apply");
        applyButton.setEnabled(false);

        JPanel optionContainer = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(optionContainer);
        optionContainer.setBorder(ViewUtil.getSmallBorder());


        final DefaultListModel model = new DefaultListModel();
        for (String option : uniq) {
            model.addElement(option);
        }

        final QuickListFilterField field = new QuickListFilterField(model);
        field.setHintText("Type here to filter options");
        optionContainer.add(field);

        field.setPreferredSize(new Dimension(250, 22));
        field.setMaximumSize(new Dimension(250, 22));

        filterableList = new FilterableCheckBoxList(field.getDisplayListModel()) {

            @Override
            public int getNextMatch(String prefix, int startIndex, Position.Bias bias) {
                return -1;
            }

            @Override
            public boolean isCheckBoxEnabled(int index) {
                return true;
            }
        };
        filterableList.getCheckBoxListSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        SearchableUtils.installSearchable(filterableList);

        filterableList.getCheckBoxListSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    applyButton.setEnabled(true);
                }
            }
        });

        setAllSelected(true);


        al = new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                applyButton.setEnabled(false);

                final List<String> acceptableValues = new ArrayList<String>();

                //List<String> options = new ArrayList<String>();
                int[] indices = filterableList.getCheckBoxListSelectedIndices();
                for (int i : indices) {
                    if (columnname.equals(DefaultpatientTableSchema.COLUMNNAME_OF_GENDER)){
                        acceptableValues.add(Integer.toString(ClientMiscUtils.stringToGender(filterableList.getModel().getElementAt(i).toString())));
                    } else {
                        acceptableValues.add(filterableList.getModel().getElementAt(i).toString());
                    }
                }

                appliedValues = acceptableValues;

                if (acceptableValues.size() == uniq.size()) {
                    FilterController.removeFilter(columnname, queryId);
                    return;
                }

                Filter f;

                // no conditions pass, provide false condition
                if (acceptableValues.isEmpty()) {
                    f = new QueryFilter() {

                        @Override
                        public Condition[] getConditions() {
                            // false condition
                            return new Condition[] { BinaryConditionMS.equalTo(0,1) };
                        }

                        @Override
                        public String getName() {
                            return alias;
                        }

                        @Override
                        public String getId() {
                            return columnname;
                        }
                    };

                // some conditions pass, provide conditions
                } else {


                    f = new QueryFilter() {

                        @Override
                        public Condition[] getConditions() {
                            if (whichTable == Table.VARIANT) {
                                Condition[] results = new Condition[acceptableValues.size()];
                                int i = 0;
                                for (String s : acceptableValues) {
                                    results[i++] = BinaryConditionMS.equalTo(ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(columnname), s);
                                }
                                return results;
                            } else if (whichTable == Table.PATIENT) {
                                try {
                                    List<String> individuals = MedSavantClient.PatientQueryUtilAdapter.getDNAIdsForStringList(LoginController.sessionId, ProjectController.getInstance().getCurrentPatientTableSchema(), acceptableValues, columnname);

                                    if(individuals.isEmpty()) return new Condition[] { BinaryCondition.equalTo(0, 1) };
                                    
                                    Condition[] results = new Condition[individuals.size()];
                                    int i = 0;
                                    for (String ind : individuals) {
                                        results[i++] = BinaryConditionMS.equalTo(ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID), ind);
                                    }
                                    return results;

                                } catch (Exception ex) {
                                    ClientMiscUtils.reportError("Error getting DNA IDs.", ex);
                                }
                            }
                            return new Condition[0];
                        }

                        @Override
                        public String getName() {
                            return alias;
                        }

                        @Override
                        public String getId() {
                            return columnname;
                        }
                    };
                }
                FilterController.addFilter(f, getQueryId());

                //TODO: why does this not work? Freezes GUI
                //apply.setEnabled(false);
            }
        };
        applyButton.addActionListener(al);

        JScrollPane jsp = new JScrollPane(filterableList);

        optionContainer.add(jsp);

        Dimension d = new Dimension(360, 200);
        optionContainer.setMaximumSize(d);
        optionContainer.setPreferredSize(d);

        JButton selectAll = ViewUtil.createHyperLinkButton("Select All");
        selectAll.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setAllSelected(true);
                applyButton.setEnabled(true);
            }
        });
        bottomContainer.add(selectAll);

        JButton selectNone = ViewUtil.createHyperLinkButton("Select None");

        selectNone.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setAllSelected(false);
                applyButton.setEnabled(true);
            }
        });
        bottomContainer.add(selectNone);

        bottomContainer.add(Box.createHorizontalGlue());

        bottomContainer.add(applyButton);

        container.add(optionContainer);
        container.add(bottomContainer);

    }

    private void setAllSelected(boolean b) {
        int[] selected;

        if (b) {
            selected = new int[filterableList.getModel().getSize()];
            for (int i = 0; i < selected.length; i++) {
                selected[i] = i;
            }
        } else {
            selected = new int[0];
        }
        filterableList.setCheckBoxListSelectedIndices(selected);
    }

    @Override
    public FilterState saveState() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("table", whichTable.toString());
        if (appliedValues != null && !appliedValues.isEmpty()) {
            String values = "";
            for (int i = 0; i < appliedValues.size(); i++) {
                values += appliedValues.get(i);
                if (i != appliedValues.size() - 1) {
                    values += ";;;";
                }
            }
            map.put("values", values);
        }
        return new FilterState(FilterType.STRING, alias, columnname, map);
    }
}
