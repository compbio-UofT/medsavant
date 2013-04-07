package org.ut.biolab.mfiume.query.medsavant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.filter.WhichTable;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.Range;
import org.ut.biolab.medsavant.shared.util.ChromosomeComparator;
import org.ut.biolab.mfiume.query.value.NumberConditionValueGenerator;

/**
 *
 * @author mfiume
 */
public class MedSavantDatabaseNumberConditionValueGenerator extends NumberConditionValueGenerator {

    private final CustomField field;
    private final WhichTable whichTable;

    public MedSavantDatabaseNumberConditionValueGenerator(CustomField field, WhichTable whichTable) {
        this.field = field;
        this.whichTable = whichTable;
    }

    @Override
    public double[] getExtremeNumericValues() {

        String columnName = field.getColumnName();
        try {
            Range r = MedSavantClient.DBUtils.getExtremeValuesForColumn(LoginController.getInstance().getSessionID(), whichTable.getName(), columnName);
            return new double[] { r.getMin(), r.getMax() } ;
        } catch (Exception ex) {
            Logger.getLogger(MedSavantDatabaseStringConditionValueGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new double[]{0, 0};
    }
}
