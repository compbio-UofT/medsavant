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

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.*;

import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.InCondition;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.db.DefaultPatientTableSchema;
import org.ut.biolab.medsavant.db.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.util.ChromosomeComparator;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.vcf.VariantRecord.VariantType;
import org.ut.biolab.medsavant.vcf.VariantRecord.Zygosity;
import org.ut.biolab.medsavant.view.dialog.IndeterminateProgressDialog;
import org.ut.biolab.medsavant.view.genetics.filter.FilterUtils.WhichTable;


/**
 *
 * @author Andrew
 */
public class StringListFilterView extends TabularFilterView {

    private final WhichTable whichTable;
    private final String columnName;
    private final String alias;

    public StringListFilterView(FilterState state, int queryID) throws SQLException, RemoteException {
        this(WhichTable.valueOf(state.getValues().get("table")), state.getFilterID(), queryID, state.getName());
        String values = state.getValues().get("values");
        if (values != null) {
            List<String> l = new ArrayList<String>();
            Collections.addAll(l, values.split(";;;"));
            applyFilter(l);
        }
    }

    public StringListFilterView(WhichTable t, String colName, int queryID, String alias) throws SQLException, RemoteException {
        this(t, colName, queryID, alias, false);
    }

    protected StringListFilterView(WhichTable t, String colName, int queryID, String alias, boolean bool) throws SQLException, RemoteException {
        super(alias, queryID);

        this.whichTable = t;
        this.columnName = colName;
        this.alias = alias;

        if (bool) {
            availableValues = Arrays.asList("True", "False");
        } else if (colName.equals(DefaultVariantTableSchema.COLUMNNAME_OF_AC)) {
            availableValues = Arrays.asList("1", "2");
        } else if (colName.equals(DefaultVariantTableSchema.COLUMNNAME_OF_AF)) {
            availableValues = Arrays.asList("0.50", "1.00");
        } else if (colName.equals(DefaultVariantTableSchema.COLUMNNAME_OF_REF) || colName.equals(DefaultVariantTableSchema.COLUMNNAME_OF_ALT)) {
            availableValues = Arrays.asList("A", "C", "G", "T");
        } else if (colName.equals(DefaultVariantTableSchema.COLUMNNAME_OF_VARIANT_TYPE)) {
            availableValues = Arrays.asList(VariantType.SNP.toString(), VariantType.Insertion.toString(), VariantType.Deletion.toString(), VariantType.Various.toString(), VariantType.Unknown.toString());
        } else if (colName.equals(DefaultVariantTableSchema.COLUMNNAME_OF_ZYGOSITY)) {
            availableValues = Arrays.asList(Zygosity.HomoRef.toString(), Zygosity.HomoAlt.toString(), Zygosity.Hetero.toString(), Zygosity.HeteroTriallelic.toString());
        } else if (colName.equals(DefaultPatientTableSchema.COLUMNNAME_OF_GENDER)) {
            availableValues = Arrays.asList(ClientMiscUtils.GENDER_MALE, ClientMiscUtils.GENDER_FEMALE, ClientMiscUtils.GENDER_UNKNOWN);
        } else {
            new IndeterminateProgressDialog("Generating List", "<html>Determining distinct values for field.<br>This may take a few minutes the first time.</html>") {
                @Override
                public void run() {
                    try {
                        availableValues = MedSavantClient.VariantManager.getDistinctValuesForColumn(LoginController.sessionId, whichTable.getName(), columnName, true);
                        if (columnName.equals(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM)) {
                            Collections.sort(availableValues, new ChromosomeComparator());
                        }
                        initContentPanel();
                    } catch (Throwable ex) {
                        ClientMiscUtils.reportError(String.format("Error getting distinct values for %s.%s: %%s", whichTable, columnName), ex);
                    }
                }
            }.setVisible(true);
            return;

        }
        initContentPanel();
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
        return new FilterState(Filter.Type.STRING, alias, columnName, map);
    }

    @Override
    protected void applyFilter() {
        applyButton.setEnabled(false);

        appliedValues = new ArrayList<String>();

        int[] indices = filterableList.getCheckBoxListSelectedIndices();
        for (int i : indices) {
            if (columnName.equals(DefaultPatientTableSchema.COLUMNNAME_OF_GENDER)){
                appliedValues.add(Integer.toString(ClientMiscUtils.stringToGender(filterableList.getModel().getElementAt(i).toString())));
            } else {
                appliedValues.add(filterableList.getModel().getElementAt(i).toString());
            }
        }

        if (appliedValues.size() == availableValues.size()) {
            FilterController.getInstance().removeFilter(columnName, queryID);
            return;
        }

        FilterController.getInstance().addFilter(new QueryFilter() {

            @Override
            public Condition[] getConditions() throws SQLException, RemoteException {
                if (appliedValues.size() > 0) {
                    if (whichTable == WhichTable.VARIANT) {
                        return new Condition[] {
                            new InCondition(ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(columnName), appliedValues)
                        };
                    } else if (whichTable == WhichTable.PATIENT) {
                        return getDNAIDCondition(MedSavantClient.PatientManager.getDNAIDsForStringList(LoginController.sessionId, ProjectController.getInstance().getCurrentPatientTableSchema(), appliedValues, columnName));
                    }
                }
                return FALSE_CONDITION;
            }

            @Override
            public String getName() {
                return alias;
            }

            @Override
            public String getID() {
                return columnName;
            }
        }, queryID);
    }

}
