/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.query.medsavant.complex;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.filter.WhichTable;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.client.query.SearchConditionItem;
import org.ut.biolab.medsavant.client.query.medsavant.MedSavantConditionViewGenerator;
import org.ut.biolab.medsavant.client.query.medsavant.MedSavantDatabaseNumberConditionValueGenerator;
import org.ut.biolab.medsavant.client.query.medsavant.MedSavantDatabaseStringConditionValueGenerator;
import org.ut.biolab.medsavant.client.query.value.StringConditionValueGenerator;
import org.ut.biolab.medsavant.client.query.value.encode.NumericConditionEncoder;
import org.ut.biolab.medsavant.client.query.value.encode.StringConditionEncoder;
import org.ut.biolab.medsavant.client.query.view.NumberSearchConditionEditorView;
import org.ut.biolab.medsavant.client.query.view.SearchConditionEditorView;
import org.ut.biolab.medsavant.client.query.view.SearchConditionEditorView.ConditionRestorationException;
import org.ut.biolab.medsavant.client.query.view.SearchConditionItemView;
import org.ut.biolab.medsavant.client.query.view.StringSearchConditionEditorView;

/**
 *
 * @author mfiume
 */
public class PatientConditionGenerator implements ComprehensiveConditionGenerator {

    boolean allowInexactMatch;
    private final String columnName;
    private final String alias;
    private final CustomField field;
    private final HashMap<String, Map> columnNameToRemapMap;

    public PatientConditionGenerator(CustomField field) {
        this.columnName = field.getColumnName();
        this.alias = field.getAlias();
        allowInexactMatch = columnName.equals(BasicPatientColumns.PHENOTYPES.getColumnName());

        columnNameToRemapMap = new HashMap<String, Map>();

        // create gender remap
        TreeMap<String, String> genderRemap = new TreeMap<String, String>();
        genderRemap.put("Male", "1");
        genderRemap.put("Female", "0");
        genderRemap.put("Unspecified", "2");

        columnNameToRemapMap.put(BasicPatientColumns.GENDER.getColumnName(), genderRemap);

        // create gender remap
        TreeMap<String, String> affectedRemap = new TreeMap<String, String>();
        affectedRemap.put("Yes", "1");
        affectedRemap.put("No", "0");

        columnNameToRemapMap.put(BasicPatientColumns.AFFECTED.getColumnName(), affectedRemap);

        this.field = field;
    }

    @Override
    public String getName() {
        return alias;
    }

    @Override
    public String category() {
        return MedSavantConditionViewGenerator.PATIENT_CONDITIONS;
    }

    @Override
    public Condition getConditionsFromEncoding(String encoding) throws Exception {

        List<String> appliedValues = StringConditionEncoder.unencodeConditions(encoding);

        if (columnNameToRemapMap.containsKey(columnName)) {
            appliedValues = remapValues(appliedValues, columnNameToRemapMap.get(columnName));
        }

        if (appliedValues.isEmpty()) {
            return ConditionUtils.FALSE_CONDITION;
        }

        return ConditionUtils.getConditionsMatchingDNAIDs(
                MedSavantClient.PatientManager.getDNAIDsForStringList(
                LoginController.getSessionID(),
                ProjectController.getInstance().getCurrentPatientTableSchema(),
                appliedValues,
                columnName,
                allowInexactMatch));
    }

    @Override
    public StringSearchConditionEditorView getViewGeneratorForItem(SearchConditionItem item) {
        return generateViewFromDatabaseField(item);
    }

    private StringSearchConditionEditorView generateViewFromDatabaseField(SearchConditionItem item) {

        if (columnNameToRemapMap.containsKey(columnName)) {
            return generateStringViewWithPresetValues(item, columnNameToRemapMap.get(columnName).keySet());
        }

        // all patient fields get treated as strings
        return generateStringViewFromDatabaseField(item);
    }

    private StringSearchConditionEditorView generateStringViewFromDatabaseField(SearchConditionItem item) {

        StringConditionValueGenerator valueGenerator;
        String colName = field.getColumnName();

        /**
         * Exceptions
         */
        if (colName.equals(BasicVariantColumns.ALT.getColumnName())
                || colName.equals(BasicVariantColumns.REF.getColumnName())
                || colName.equals(BasicVariantColumns.AA.getColumnName())) {
            valueGenerator = new StringConditionValueGenerator() {
                @Override
                public List<String> getStringValues() {
                    return Arrays.asList(new String[]{"A", "C", "G", "T"});
                }
            };

        } else if (colName.equals(BasicVariantColumns.VARIANT_TYPE.getColumnName())) {
            valueGenerator = new StringConditionValueGenerator() {
                @Override
                public List<String> getStringValues() {
                    return Arrays.asList(new String[]{"SNP", "Deletion", "Insertion", "Unknown", "Multiple"});
                }
            };

        } else if (colName.equals(BasicVariantColumns.ZYGOSITY.getColumnName())) {
            valueGenerator = new StringConditionValueGenerator() {
                @Override
                public List<String> getStringValues() {
                    return Arrays.asList(new String[]{"Homozygous Reference", "Homozygous Alternate", "Heterozygous", "Heterozygous (Triallelic)", "Missing"});
                }
            };

        } else if (colName.equals(BasicVariantColumns.DBSNP_ID.getColumnName())) {
            valueGenerator = null;

            /**
             * The normal case
             */
        } else {
            valueGenerator = new MedSavantDatabaseStringConditionValueGenerator(field, whichTable);
        }

        StringSearchConditionEditorView editor = new StringSearchConditionEditorView(item, valueGenerator);
        return editor;
    }
    private static final WhichTable whichTable = WhichTable.PATIENT;

    private SearchConditionItemView generateFloatViewFromDatabaseField(SearchConditionItem item) {

        NumberSearchConditionEditorView editor = new NumberSearchConditionEditorView(item, new MedSavantDatabaseNumberConditionValueGenerator(field, whichTable));
        SearchConditionItemView view = new SearchConditionItemView(item, editor);
        return view;
    }

    private Condition generateStringConditionForVariantDatabaseField(MedSavantConditionViewGenerator.DatabaseFieldStruct s, String encoding) {

        /*if (StringConditionEncoder.encodesAll(encoding)) {
         return BinaryCondition.equalTo(1, 1);
         } else if (StringConditionEncoder.encodesNone(encoding)) {
         return BinaryCondition.equalTo(1, 0);
         }*/

        List<String> selected = StringConditionEncoder.unencodeConditions(encoding);
        if (selected.isEmpty()) {
            return BinaryCondition.equalTo(1, 0); // always false
        }
        DbColumn col = ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(field.getColumnName());
        Condition[] conditions = new Condition[selected.size()];
        int i = 0;
        for (String select : selected) {
            conditions[i++] = BinaryCondition.equalTo(col, select);
        }
        return ComboCondition.or(conditions);
    }    

    private StringSearchConditionEditorView generateStringViewWithPresetValues(SearchConditionItem item, final Set<String> values) {
        StringConditionValueGenerator vg = new StringConditionValueGenerator() {
            @Override
            public List<String> getStringValues() {
                return new ArrayList<String>(values);
            }
        };
        StringSearchConditionEditorView editor = new StringSearchConditionEditorView(item, vg);
        return editor;
    }

    private List<String> remapValues(List<String> appliedValues, Map<String, String> remap) {
        List<String> remappedAppliedValues = new ArrayList<String>();
        for (String s : appliedValues) {
            System.out.println("Remapping " + s + " to " + remap.get(s));
            remappedAppliedValues.add(remap.get(s));
        }
        return remappedAppliedValues;
    }
}
