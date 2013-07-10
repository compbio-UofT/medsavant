package org.ut.biolab.mfiume.query.medsavant;

import org.ut.biolab.mfiume.query.medsavant.complex.CohortConditionGenerator;
import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.InCondition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.awt.BorderLayout;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.cohort.CohortFilterView;
import org.ut.biolab.medsavant.client.filter.FilterHolder;
import org.ut.biolab.medsavant.client.filter.NumericFilterView;
import org.ut.biolab.medsavant.client.filter.StringListFilterView;
import org.ut.biolab.medsavant.client.filter.TagFilterView;
import org.ut.biolab.medsavant.client.filter.WhichTable;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.shared.db.ColumnType;
import org.ut.biolab.medsavant.shared.format.AnnotationFormat;
import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.Cohort;
import org.ut.biolab.medsavant.shared.model.OntologyType;
import org.ut.biolab.medsavant.shared.util.BinaryConditionMS;
import org.ut.biolab.mfiume.query.ConditionViewGenerator;
import org.ut.biolab.mfiume.query.SearchConditionItem;
import org.ut.biolab.mfiume.query.medsavant.complex.ComprehensiveConditionGenerator;
import org.ut.biolab.mfiume.query.medsavant.complex.OntologyConditionGenerator;
import org.ut.biolab.mfiume.query.medsavant.complex.PatientConditionGenerator;
import org.ut.biolab.mfiume.query.medsavant.complex.RegionSetConditionGenerator;
import org.ut.biolab.mfiume.query.medsavant.complex.TagConditionGenerator;
import org.ut.biolab.mfiume.query.medsavant.complex.VariantConditionGenerator;
import org.ut.biolab.mfiume.query.value.DatabaseConditionGenerator;
import org.ut.biolab.mfiume.query.value.encode.StringConditionEncoder;
import org.ut.biolab.mfiume.query.value.StringConditionValueGenerator;
import org.ut.biolab.mfiume.query.value.encode.NumericConditionEncoder;
import org.ut.biolab.mfiume.query.view.NumberSearchConditionEditorView;
import org.ut.biolab.mfiume.query.view.SearchConditionItemView;
import org.ut.biolab.mfiume.query.view.StringSearchConditionEditorView;

/**
 *
 * @author mfiume
 */
public class MedSavantConditionViewGenerator implements ConditionViewGenerator {

    private static final Log LOG = LogFactory.getLog(MedSavantConditionViewGenerator.class);
    public static String REGIONBASED_CONDITIONS = "Region and Ontology Conditions";
    private final HashMap<String, DatabaseFieldStruct> itemToCustomFieldMap;
    private final HashMap<SearchConditionItem, DatabaseConditionGenerator> itemToConditionGeneratorMap;

    private static MedSavantConditionViewGenerator instance;
    private TreeMap<String, List<String>> allowedMap;
    private Map<String, ComprehensiveConditionGenerator> conditionGenerators;
    //private Map<String, ComprehensiveConditionGenerator> patientConditionGenerators;
    //private Map<String, ComprehensiveConditionGenerator> variantConditionGenerators;
    public static final String PATIENT_CONDITIONS = "Patient Conditions";
    public static final String VARIANT_CONDITIONS = "Variant Conditions";

    private MedSavantConditionViewGenerator() {
        itemToCustomFieldMap = new HashMap<String, DatabaseFieldStruct>();
        itemToConditionGeneratorMap = new HashMap<SearchConditionItem, DatabaseConditionGenerator>();

        conditionGenerators = new HashMap<String, ComprehensiveConditionGenerator>();
        //patient
        try {
            for (CustomField field : ProjectController.getInstance().getCurrentPatientFormat()) {
                if (field.isFilterable() && isFilterable(field.getColumnType())) {
                    String name = field.getAlias();
                    ComprehensiveConditionGenerator patientFieldCondition = new PatientConditionGenerator(field);
                    conditionGenerators.put(name, patientFieldCondition);
                }
            }
        } catch (Exception e) {
            LOG.error(e);
        }

        // variant
        try {
            AnnotationFormat[] afs = ProjectController.getInstance().getCurrentAnnotationFormats();
            for (AnnotationFormat af : afs) {
                for (CustomField field : af.getCustomFields()) {
                    if (field.isFilterable() && isFilterable(field.getColumnType())) {
                        //catHolders.add(new FieldFilterHolder(field, WhichTable.VARIANT, queryID));

                        String program = af.getProgram();
                        program = (program.equals("VCF Conditions") || program.equals("Standard Variant Conditions")) ? "" : " - " + program;

                        String name = field.getAlias() + program;
                        ComprehensiveConditionGenerator variantFieldCondition = new VariantConditionGenerator(name,field);
                        conditionGenerators.put(name, variantFieldCondition);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e);
        }

        // other
        ComprehensiveConditionGenerator cohort = new CohortConditionGenerator();
        conditionGenerators.put(cohort.getName(), cohort);

        ComprehensiveConditionGenerator regions = new RegionSetConditionGenerator();
        conditionGenerators.put(regions.getName(), regions);

        ComprehensiveConditionGenerator tag = new TagConditionGenerator();
        conditionGenerators.put(tag.getName(), tag);

        ComprehensiveConditionGenerator go = new OntologyConditionGenerator(OntologyType.GO);
        conditionGenerators.put(go.getName(), go);

        ComprehensiveConditionGenerator hpo = new OntologyConditionGenerator(OntologyType.HPO);
        conditionGenerators.put(hpo.getName(), hpo);

        ComprehensiveConditionGenerator omim = new OntologyConditionGenerator(OntologyType.OMIM);
        conditionGenerators.put(omim.getName(), omim);

        init();
    }

    public static MedSavantConditionViewGenerator getInstance() {
        if (instance == null) {
            instance = new MedSavantConditionViewGenerator();
        }
        return instance;
    }

    @Override
    public SearchConditionItemView generateViewForItem(SearchConditionItem item) {

        String conditionName = item.getName();

        if (conditionGenerators.containsKey(conditionName)) {
            return conditionGenerators.get(conditionName).generateViewFromItem(item);
        }

        throw new UnsupportedOperationException("No view for item " + item.getName());
    }

    @Override
    public Condition generateConditionForItem(SearchConditionItem item) throws Exception {

        DatabaseConditionGenerator cg = itemToConditionGeneratorMap.get(item);


        String conditionName = item.getName();

        // non basic conditions
        if (conditionGenerators.containsKey(conditionName)) {
            return conditionGenerators.get(conditionName).getConditionsFromEncoding(item.getSearchConditionEncoding());
        }


        /*
         if (itemToCustomFieldMap.containsKey(conditionName) || cg == null) {
         DatabaseFieldStruct s = itemToCustomFieldMap.get(item.getName());
         String encoding = item.getSearchConditionEncoding();

         if (s.whichTable == WhichTable.PATIENT) {
         // TODO
         } else if (s.whichTable == WhichTable.VARIANT) {
         return generateVariantConditionForDatabaseField(s, encoding);
         }
         }*/

        throw new UnsupportedOperationException("No condition generator for " + item.getName());
    }

    public final void init() {
        allowedMap = new TreeMap<String, List<String>>();
        addMap(conditionGenerators);
    }

    @Override
    public Map<String, List<String>> getAllowableItemNames() {
        return allowedMap;
    }

    private boolean isFilterable(ColumnType type) {
        switch (type) {
            case INTEGER:
            case FLOAT:
            case DECIMAL:
            case BOOLEAN:
            case VARCHAR:
                return true;
            default:
                return false;
        }
    }

    private void addMap(Map<String, ComprehensiveConditionGenerator> conditionNameToGeneratorMap) {
        for (String name : conditionNameToGeneratorMap.keySet()) {
            ComprehensiveConditionGenerator gen = conditionNameToGeneratorMap.get(name);
            if (allowedMap.containsKey(gen.category())) {
                allowedMap.get(gen.category()).add(gen.getName());
            } else {
                ArrayList<String> arr = new ArrayList<String>();
                arr.add(gen.getName());
                allowedMap.put(gen.category(), arr);
            }
        }
    }

    public static class DatabaseFieldStruct {

        private final CustomField field;
        private final WhichTable whichTable;
        private SearchConditionItem item;

        public DatabaseFieldStruct(CustomField field, WhichTable whichTable) {
            this.field = field;
            this.whichTable = whichTable;
        }

        public void setItem(SearchConditionItem item) {
            this.item = item;
        }
    }
}
