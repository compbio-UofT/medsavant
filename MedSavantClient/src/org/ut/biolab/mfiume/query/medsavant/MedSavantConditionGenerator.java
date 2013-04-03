package org.ut.biolab.mfiume.query.medsavant;

import java.awt.BorderLayout;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.client.cohort.CohortFilterView;
import org.ut.biolab.medsavant.client.filter.FilterHolder;
import org.ut.biolab.medsavant.client.filter.NumericFilterView;
import org.ut.biolab.medsavant.client.filter.StringListFilterView;
import org.ut.biolab.medsavant.client.filter.TagFilterView;
import org.ut.biolab.medsavant.client.filter.WhichTable;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.shared.db.ColumnType;
import org.ut.biolab.medsavant.shared.format.AnnotationFormat;
import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.mfiume.query.ConditionViewGenerator;
import org.ut.biolab.mfiume.query.SearchConditionItem;
import org.ut.biolab.mfiume.query.value.DefaultStringConditionValueGenerator;
import org.ut.biolab.mfiume.query.view.SearchConditionItemView;
import org.ut.biolab.mfiume.query.view.StringSearchConditionEditorView;

/**
 *
 * @author mfiume
 */
public class MedSavantConditionGenerator implements ConditionViewGenerator {

    private final HashMap<String, DatabaseFieldStruct> itemToCustomFieldMap;

    public MedSavantConditionGenerator() {
        itemToCustomFieldMap = new HashMap<String,DatabaseFieldStruct>();
    }

    @Override
    public SearchConditionItemView generateViewForItem(SearchConditionItem item) {

        if (itemToCustomFieldMap.containsKey(item.getName())) {
            DatabaseFieldStruct s = itemToCustomFieldMap.get(item.getName());
            s.setItem(item);
            return generateViewFromDatabaseField(s);
        }

        throw new UnsupportedOperationException("No view for item " + item.getName());
    }

    @Override
    public Map<String, List<String>> getAllowableItemNames() {

        List<String> patientItems = new ArrayList<String>();

        List<String> variantItems = new ArrayList<String>();

        try {
            patientItems.add("Cohort");

            // Add from patient table
            for (CustomField field : ProjectController.getInstance().getCurrentPatientFormat()) {
                if (field.isFilterable() && isFilterable(field.getColumnType())) {
                    patientItems.add(field.getAlias());
                }
            }

            // Add from variant table
            AnnotationFormat[] afs = ProjectController.getInstance().getCurrentAnnotationFormats();
            for (AnnotationFormat af : afs) {
                for (CustomField field : af.getCustomFields()) {
                    if (field.isFilterable() && isFilterable(field.getColumnType())) {
                        //catHolders.add(new FieldFilterHolder(field, WhichTable.VARIANT, queryID));
                        String name = field.getAlias();
                        variantItems.add(field.getAlias());
                        itemToCustomFieldMap.put(name, new DatabaseFieldStruct(field,WhichTable.VARIANT));
                    }
                }

                // force Tag filter into default variant conditions
                if (af.getProgram().equals(AnnotationFormat.ANNOTATION_FORMAT_DEFAULT)) {
                    //tag filter
                    //catHolders.add(new SimpleFilterHolder(TagFilterView.class, queryID));
                    variantItems.add("Tag");
                }
            }

        } catch (Exception ex) {
            throw new UnsupportedOperationException("This should never happen");
        }

        Map<String, List<String>> allowedMap = new TreeMap<String, List<String>>();
        allowedMap.put("Patient Conditions", patientItems);
        allowedMap.put("Variant Conditions", variantItems);

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

    private SearchConditionItemView generateViewFromDatabaseField(DatabaseFieldStruct s) {

        CustomField field = s.field;
        String colName = field.getColumnName();
        String alias = field.getAlias();
        switch (field.getColumnType()) {
            case INTEGER:
                if (!colName.equals(BasicPatientColumns.PATIENT_ID.getColumnName()) && !colName.equals(BasicPatientColumns.GENDER.getColumnName())) {
                    throw new UnsupportedOperationException("Cant generate int views yet");
                }
                break;
            case FLOAT:
            case DECIMAL:
                throw new UnsupportedOperationException("Cant generate decimal views yet");
        }

        return generateStringViewFromDatabaseField(s);

    }

    private SearchConditionItemView generateStringViewFromDatabaseField(DatabaseFieldStruct s) {

        WhichTable whichTable = s.whichTable;
        SearchConditionItem item = s.item;

        StringSearchConditionEditorView editor = new StringSearchConditionEditorView(item, new MedSavantDatabaseStringConditionValueGenerator(s.field,s.whichTable));
        SearchConditionItemView view = new SearchConditionItemView(item, editor);
        return view;
    }

    private static class DatabaseFieldStruct {
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
