package org.ut.biolab.mfiume.query.medsavant.complex;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.UnaryCondition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JPopupMenu;
import org.ut.biolab.medsavant.client.filter.WhichTable;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.mfiume.query.SearchConditionItem;
import org.ut.biolab.mfiume.query.medsavant.MedSavantConditionViewGenerator;
import org.ut.biolab.mfiume.query.medsavant.MedSavantDatabaseNumberConditionValueGenerator;
import org.ut.biolab.mfiume.query.medsavant.MedSavantDatabaseStringConditionValueGenerator;
import org.ut.biolab.mfiume.query.value.StringConditionValueGenerator;
import org.ut.biolab.mfiume.query.value.encode.NumericConditionEncoder;
import org.ut.biolab.mfiume.query.value.encode.StringConditionEncoder;
import org.ut.biolab.mfiume.query.view.NumberSearchConditionEditorView;
import org.ut.biolab.mfiume.query.view.SearchConditionEditorView;
import org.ut.biolab.mfiume.query.view.StringSearchConditionEditorView;

/**
 *
 * @author mfiume
 */
public class VariantConditionGenerator implements ComprehensiveConditionGenerator {

    boolean allowInexactMatch;
    private final String columnName;
    private final String alias;
    private final CustomField field;
    private List<String> columnsToForceStringView = Arrays.asList(
            new String[]{
                BasicVariantColumns.AC.getColumnName(),
                BasicVariantColumns.AN.getColumnName(),
                BasicVariantColumns.UPLOAD_ID.getColumnName(),
                BasicVariantColumns.FILE_ID.getColumnName()});
    private final HashMap<String, Map> columnNameToRemapMap;

    private class VariantStringConditionEditorView extends StringSearchConditionEditorView{
        public VariantStringConditionEditorView(SearchConditionItem i, StringConditionValueGenerator vg) {
            super(i, vg);
        }              
        
    }
    
    public VariantConditionGenerator(String alias, CustomField field) {
        this.columnName = field.getColumnName();
        this.alias = alias; // field.getAlias();
        this.field = field;

        columnNameToRemapMap = new HashMap<String, Map>();

        // create gender remap
        TreeMap<String, String> zygosityRemap = new TreeMap<String, String>();
        zygosityRemap.put("Homozygous Reference", "HomoRef");
        zygosityRemap.put("Homozygous Alternate", "HomoAlt");
        zygosityRemap.put("Heterozygous", "Hetero");
        zygosityRemap.put("Heterozygous (Triallelic)", "HeteroTriallelic");
        zygosityRemap.put("Missing", "Missing");

        columnNameToRemapMap.put(BasicVariantColumns.ZYGOSITY.getColumnName(), zygosityRemap);
    }


    @Override
    public String getName() {
        return alias;
    }

    @Override
    public String category() {
        return MedSavantConditionViewGenerator.VARIANT_CONDITIONS;
    }

    @Override
    public Condition getConditionsFromEncoding(String encoding) throws Exception {



        if (columnsToForceStringView.contains(columnName)) {
            return generateStringConditionForVariantDatabaseField(encoding);
        }


        switch (field.getColumnType()) {
            case INTEGER:
            case FLOAT:
            case DECIMAL:
                return generateNumericConditionForVariantDatabaseField(encoding);
        }

        return generateStringConditionForVariantDatabaseField(encoding);

    }

    @Override
    public SearchConditionEditorView getViewGeneratorForItem(SearchConditionItem item) {
        return generateViewFromDatabaseField(item);
    }

    private SearchConditionEditorView generateViewFromDatabaseField(SearchConditionItem item) {

        if (columnsToForceStringView.contains(columnName)) {
            return generateStringViewFromDatabaseField(item);
        }

        switch (field.getColumnType()) {
            case INTEGER:
            case FLOAT:
            case DECIMAL:
                return generateNumericViewFromDatabaseField(item);
        }

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

        StringSearchConditionEditorView editor = new VariantStringConditionEditorView(item, valueGenerator);
        return editor;
    }
    private static final WhichTable whichTable = WhichTable.VARIANT;

    private NumberSearchConditionEditorView generateNumericViewFromDatabaseField(SearchConditionItem item) {

        NumberSearchConditionEditorView editor = new NumberSearchConditionEditorView(item, new MedSavantDatabaseNumberConditionValueGenerator(field, whichTable));
        return editor;
    }

    private Condition generateStringConditionForVariantDatabaseField(String encoding) {

        DbColumn col = ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(field.getColumnName());

        if (StringConditionEncoder.encodesNull(encoding)) {
            return ComboCondition.or(UnaryCondition.isNull(col),BinaryCondition.equalTo(col, ""));
        } else if (StringConditionEncoder.encodesNotNull(encoding)) {
            return ComboCondition.and(UnaryCondition.isNotNull(col),BinaryCondition.notEqualTo(col, ""));
        }

        /*if (StringConditionEncoder.encodesAll(encoding)) {
         return BinaryCondition.equalTo(1, 1);
         } else if (StringConditionEncoder.encodesNone(encoding)) {
         return BinaryCondition.equalTo(1, 0);
         }*/

        List<String> selected = StringConditionEncoder.unencodeConditions(encoding);


        if (columnNameToRemapMap.containsKey(columnName)) {
            selected = remapValues(selected, columnNameToRemapMap.get(columnName));
        }


        if (selected.isEmpty()) {
            return BinaryCondition.equalTo(1, 0); // always false
        }

        Condition[] conditions = new Condition[selected.size()];
        int i = 0;
        for (String select : selected) {
            conditions[i++] = BinaryCondition.equalTo(col, select);
        }
        return ComboCondition.or(conditions);
    }

    private Condition generateVariantConditionForDatabaseField(MedSavantConditionViewGenerator.DatabaseFieldStruct s, String encoding) {
        String colName = field.getColumnName();

        if (columnsToForceStringView.contains(colName)) {
            return generateStringConditionForVariantDatabaseField(encoding);
        }

        switch (field.getColumnType()) {
            case INTEGER:
            case FLOAT:
            case DECIMAL:
                return generateNumericConditionForVariantDatabaseField(encoding);
        }

        return generateStringConditionForVariantDatabaseField(encoding);
    }

    private Condition generateNumericConditionForVariantDatabaseField(String encoding) {

        double[] selected = NumericConditionEncoder.unencodeConditions(encoding);
        DbColumn col = ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(field.getColumnName());

        if (selected[0] == selected[1]) {
            return BinaryCondition.equalTo(col, selected[0]);
        } else {
            return ComboCondition.and(
                    BinaryCondition.greaterThan(col, selected[0], true),
                    BinaryCondition.lessThan(col, selected[1], true));
        }
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
