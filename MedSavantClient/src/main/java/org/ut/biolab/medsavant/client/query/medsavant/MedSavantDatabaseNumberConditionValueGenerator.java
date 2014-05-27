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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.filter.WhichTable;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.Range;
import org.ut.biolab.medsavant.shared.util.ChromosomeComparator;
import org.ut.biolab.medsavant.client.query.value.NumberConditionValueGenerator;

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
            Range r = MedSavantClient.DBUtils.getExtremeValuesForColumn(LoginController.getSessionID(), whichTable.getName(), columnName);
            return new double[] { r.getMin(), r.getMax() } ;
        } catch (Exception ex) {
            Logger.getLogger(MedSavantDatabaseStringConditionValueGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new double[]{0, 0};
    }
}
