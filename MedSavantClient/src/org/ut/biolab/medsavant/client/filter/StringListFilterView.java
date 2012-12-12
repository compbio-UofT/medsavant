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

package org.ut.biolab.medsavant.client.filter;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.*;

import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.InCondition;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.FilterStateAdapter;
import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.shared.model.ProgressStatus;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.shared.util.BinaryConditionMS;
import org.ut.biolab.medsavant.shared.util.ChromosomeComparator;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord.VariantType;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord.Zygosity;
import org.ut.biolab.medsavant.client.view.dialog.CancellableProgressDialog;


/**
 *
 * @author Andrew
 */
public class StringListFilterView extends TabularFilterView<String> implements BasicPatientColumns, BasicVariantColumns {

    private final WhichTable whichTable;
    private final String columnName;
    private final String alias;
    private final boolean allowInexactMatch;

    public StringListFilterView(FilterState state, int queryID) throws Exception {
        this(WhichTable.valueOf(state.getOneValue(FilterState.TABLE_ELEMENT)), state.getFilterID(), queryID, state.getName());
        List<String> values = state.getValues(FilterState.VALUE_ELEMENT);
        if (values != null) {
            setFilterValues(values);
        }
    }

    public StringListFilterView(WhichTable t, String colName, int queryID, String alias) throws Exception {
        this(t, colName, queryID, alias, false);
    }

    protected StringListFilterView(WhichTable t, String colName, int queryID, String alias, boolean bool) throws Exception {
        super(alias, queryID);

        this.whichTable = t;
        this.columnName = colName;
        this.alias = alias;

        allowInexactMatch = colName.equals(PHENOTYPES.getColumnName());

        // don't cache patient fields; they may change
        final boolean useCache = t == WhichTable.VARIANT;

        if (bool) {
            availableValues = Arrays.asList("True", "False");
        } else if (colName.equals(AC.getColumnName())) {
            availableValues = Arrays.asList("1", "2");
        } else if (colName.equals(AF.getColumnName())) {
            availableValues = Arrays.asList("0.50", "1.00");
        } else if (colName.equals(REF.getColumnName()) || colName.equals(ALT.getColumnName())) {
            availableValues = Arrays.asList("A", "C", "G", "T");
        } else if (colName.equals(VARIANT_TYPE.getColumnName())) {
            availableValues = Arrays.asList(VariantType.SNP.toString(), VariantType.Insertion.toString(), VariantType.Deletion.toString(), VariantType.Various.toString(), VariantType.Unknown.toString());
        } else if (colName.equals(ZYGOSITY.getColumnName())) {
            availableValues = Arrays.asList(Zygosity.HomoRef.toString(), Zygosity.HomoAlt.toString(), Zygosity.Hetero.toString(), Zygosity.HeteroTriallelic.toString());
        } else if (colName.equals(GENDER.getColumnName())) {
            availableValues = Arrays.asList(ClientMiscUtils.GENDER_MALE, ClientMiscUtils.GENDER_FEMALE, ClientMiscUtils.GENDER_UNKNOWN);
        } else {
            new CancellableProgressDialog("Generating List", "<html>Determining distinct values for field.<br>This may take a few minutes the first time.</html>") {
                @Override
                public void run() throws InterruptedException, SQLException, RemoteException {
                    availableValues = MedSavantClient.DBUtils.getDistinctValuesForColumn(LoginController.getInstance().getSessionID(), whichTable.getName(), columnName, allowInexactMatch ,useCache);
                    if (columnName.equals(CHROM.getColumnName())) {
                        Collections.sort(availableValues, new ChromosomeComparator());
                    }
                }

                @Override
                public ProgressStatus checkProgress() throws RemoteException {
                    return MedSavantClient.DBUtils.checkProgress(LoginController.getInstance().getSessionID(), cancelled);
                }
            }.showDialog();
        }
        initContentPanel();
    }

    public static FilterState wrapState(WhichTable t, String colName, String alias, Collection<String> applied) {
        FilterState state = new FilterState(Filter.Type.STRING, alias, colName);
        state.putOneValue(FilterState.TABLE_ELEMENT, t);
        state.putValues(FilterState.VALUE_ELEMENT, wrapValues(applied));
        return state;
    }

    @Override
    public FilterStateAdapter saveState() {
        return wrapState(whichTable, columnName, alias, appliedValues);
    }

    @Override
    protected void applyFilter() {
        applyButton.setEnabled(false);

        appliedValues = new ArrayList<String>();

        int[] indices = filterableList.getCheckBoxListSelectedIndices();
        for (int i : indices) {
            if (columnName.equals(GENDER.getColumnName())){
                appliedValues.add(Integer.toString(ClientMiscUtils.stringToGender(availableValues.get(i))));
            } else {
                appliedValues.add(availableValues.get(i));
            }
        }

        if (appliedValues.size() == availableValues.size()) {
            FilterController.getInstance().removeFilter(columnName, queryID);
            return;
        }

        FilterController.getInstance().addFilter(new StringListFilter(), queryID);
    }

    private class StringListFilter extends Filter {
        @Override
        public String getName() {
            return alias;
        }

        @Override
        public String getID() {
            return columnName;
        }

        @Override
        public Condition[] getConditions() throws SQLException, RemoteException {

            if (appliedValues.size() > 0) {
                if (whichTable == WhichTable.VARIANT) {
                    if (appliedValues.size() == 1) {
                        return new Condition[] {
                            BinaryConditionMS.equalTo(ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(columnName), appliedValues.get(0))
                        };
                    } else {
                        return new Condition[] {
                            new InCondition(ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(columnName), appliedValues)
                        };
                    }
                } else if (whichTable == WhichTable.PATIENT) {
                    return getDNAIDCondition(MedSavantClient.PatientManager.getDNAIDsForStringList(LoginController.getInstance().getSessionID(),
                                             ProjectController.getInstance().getCurrentPatientTableSchema(), appliedValues, columnName,
                                             allowInexactMatch));
                }
            }

            return FALSE_CONDITION;
        }
    }
}
