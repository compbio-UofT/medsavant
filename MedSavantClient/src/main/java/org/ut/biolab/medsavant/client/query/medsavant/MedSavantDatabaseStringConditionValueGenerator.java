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
package org.ut.biolab.medsavant.client.query.medsavant;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.filter.WhichTable;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.util.ChromosomeComparator;
import org.ut.biolab.medsavant.client.query.value.StringConditionValueGenerator;

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
            List<String> results = MedSavantClient.DBUtils.getDistinctValuesForColumn(LoginController.getSessionID(), whichTable.getName(), columnName, allowInexactMatch, useCache);

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
