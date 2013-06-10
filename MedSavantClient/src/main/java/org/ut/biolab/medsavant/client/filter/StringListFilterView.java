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
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord.VariantType;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord.Zygosity;

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

        boolean needsWorker = false;

        if (bool) {
            setAvailableValues(Arrays.asList("True", "False"));
        } else if (colName.equals(AC.getColumnName())) {
            setAvailableValues(Arrays.asList("1", "2"));
        } else if (colName.equals(AF.getColumnName())) {
            setAvailableValues(Arrays.asList("0.50", "1.00"));
        } else if (colName.equals(REF.getColumnName()) || colName.equals(ALT.getColumnName())) {
            setAvailableValues(Arrays.asList("A", "C", "G", "T"));
        } else if (colName.equals(VARIANT_TYPE.getColumnName())) {
            setAvailableValues(Arrays.asList(VariantType.SNP.toString(), VariantType.Insertion.toString(), VariantType.Deletion.toString(), VariantType.Various.toString(), VariantType.Unknown.toString()));
        } else if (colName.equals(ZYGOSITY.getColumnName())) {
            setAvailableValues(Arrays.asList(Zygosity.HomoRef.toString(), Zygosity.HomoAlt.toString(), Zygosity.Hetero.toString(), Zygosity.HeteroTriallelic.toString(), Zygosity.Missing.toString()));
        } else if (colName.equals(GENDER.getColumnName())) {
            setAvailableValues(Arrays.asList(ClientMiscUtils.GENDER_MALE, ClientMiscUtils.GENDER_FEMALE, ClientMiscUtils.GENDER_UNKNOWN));
        } else {

            needsWorker = true;

            new MedSavantWorker<Void>("FilterView") {
                @Override
                protected void showProgress(double fract) {
                }

                @Override
                protected void showSuccess(Void result) {
                    initContentPanel();
                }

                @Override
                protected Void doInBackground() throws Exception {
                    setAvailableValues(MedSavantClient.DBUtils.getDistinctValuesForColumn(LoginController.getInstance().getSessionID(), whichTable.getName(), columnName, allowInexactMatch, useCache));
                    if (columnName.equals(CHROM.getColumnName())) {
                        Collections.sort(getAvailableValues(), new ChromosomeComparator());
                    }
                    return null;
                }
            }.execute();
        }

        if (!needsWorker) {
            initContentPanel();
        }
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
            if (columnName.equals(GENDER.getColumnName())) {
                appliedValues.add(Integer.toString(ClientMiscUtils.stringToGender(getAvailableValues().get(i))));
            } else {
                appliedValues.add(getAvailableValues().get(i));
            }
        }

        if (appliedValues.size() == getAvailableValues().size()) {
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
                        return new Condition[]{
                                    BinaryConditionMS.equalTo(ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(columnName), appliedValues.get(0))
                                };
                    } else {
                        return new Condition[]{
                                    new InCondition(ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(columnName), appliedValues)
                                };
                    }
                } else if (whichTable == WhichTable.PATIENT) {
                    try {
                        return getDNAIDCondition(MedSavantClient.PatientManager.getDNAIDsForStringList(LoginController.getInstance().getSessionID(),
                                ProjectController.getInstance().getCurrentPatientTableSchema(), appliedValues, columnName,
                                allowInexactMatch));
                    } catch (SessionExpiredException ex) {
                        MedSavantExceptionHandler.handleSessionExpiredException(ex);
                        return null;
                    }
                }
            }

            return FALSE_CONDITION;
        }
    }
}
