package org.ut.biolab.mfiume.query.medsavant;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.filter.WhichTable;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.util.ChromosomeComparator;
import org.ut.biolab.mfiume.query.value.StringConditionValueGenerator;

/**
 *
 * @author mfiume
 */
public class MedSavantDatabaseStringConditionValueGenerator extends StringConditionValueGenerator implements BasicPatientColumns, BasicVariantColumns  {

    private final CustomField field;
    private final WhichTable whichTable;

    public MedSavantDatabaseStringConditionValueGenerator(CustomField field, WhichTable whichTable) {
        this.field = field;
        this.whichTable = whichTable;
    }

    @Override
    public List<String> getStringValues() {

        String columnName = field.getColumnName();
        boolean useCache = false;

        try {

            boolean allowInexactMatch = columnName.equals(PHENOTYPES.getColumnName());
            List<String> results = MedSavantClient.DBUtils.getDistinctValuesForColumn(LoginController.getInstance().getSessionID(), whichTable.getName(), columnName, allowInexactMatch, useCache);

            if (columnName.equals(CHROM.getColumnName())) {
                Collections.sort(results, new ChromosomeComparator());
            }

            return results;
        } catch (Exception ex) {
            Logger.getLogger(MedSavantDatabaseStringConditionValueGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }

        List<String> ar = new ArrayList<String>();
        ar.add("Error");
        return ar;
    }
}
