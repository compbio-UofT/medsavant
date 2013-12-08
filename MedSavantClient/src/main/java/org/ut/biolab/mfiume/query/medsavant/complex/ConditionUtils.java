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
package org.ut.biolab.mfiume.query.medsavant.complex;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.InCondition;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.model.GenomicRegion;
import org.ut.biolab.medsavant.shared.model.Range;
import org.ut.biolab.medsavant.shared.model.RangeCondition;
import org.ut.biolab.medsavant.shared.util.BinaryConditionMS;
import org.ut.biolab.medsavant.shared.util.MiscUtils;

/**
 *
 * @author mfiume
 */
public class ConditionUtils {

    public static BinaryCondition TRUE_CONDITION = BinaryCondition.equalTo(1, 1);
    public static BinaryCondition FALSE_CONDITION = BinaryCondition.equalTo(1, 0);

    public static Condition getConditionsMatchingGenomicRegions(Collection<GenomicRegion> regions) throws SQLException, RemoteException {

        Map<String, List<Range>> rangeMap = GenomicRegion.mergeGenomicRegions(regions);

        Condition[] results;
        if (rangeMap.size() > 0) {
            results = new Condition[rangeMap.size()];
            int i = 0;
            for (String chrom : rangeMap.keySet()) {

                Condition[] tmp = new Condition[2];

                //add chrom condition
                tmp[0] = BinaryConditionMS.equalTo(
                        ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(BasicVariantColumns.CHROM),
                        chrom);

                //create range conditions
                List<Range> ranges = rangeMap.get(chrom);
                Condition[] rangeConditions = new Condition[ranges.size()];
                for (int j = 0; j < ranges.size(); j++) {
                    rangeConditions[j] = MiscUtils.getIntersectCondition(
                            (int)ranges.get(j).getMin(), 
                            (int)ranges.get(j).getMax(), 
                            ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(BasicVariantColumns.START_POSITION), 
                            ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(BasicVariantColumns.END_POSITION));
                    
                    /*rangeConditions[j] = new RangeCondition(
                            ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(BasicVariantColumns.POSITION),
                            (long) ranges.get(j).getMin(),
                            (long) ranges.get(j).getMax());*/
                }

                //add range conditions
                tmp[1] = ComboCondition.or(rangeConditions);

                results[i] = ComboCondition.and(tmp);

                i++;
            }
        } else {
            return FALSE_CONDITION;
        }

        return ComboCondition.or(results);
    }

    public static Condition getConditionsMatchingDNAIDs(Collection<String> dnaIDs) {
        if (dnaIDs.size() > 0) {
            if (dnaIDs.size() == 1) {
                return ComboCondition.or(
                        BinaryConditionMS.equalTo(
                        ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(BasicVariantColumns.DNA_ID), dnaIDs.iterator().next()));
            } else {
                return ComboCondition.or(
                        new InCondition(ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(BasicVariantColumns.DNA_ID), dnaIDs));
            }
        }
        return FALSE_CONDITION;
    }
}
