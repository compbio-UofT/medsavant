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
package org.ut.biolab.medsavant.client.filter;

import java.rmi.RemoteException;
import java.util.List;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.model.Range;
import org.ut.biolab.medsavant.client.model.RangeSet;
import org.ut.biolab.medsavant.shared.util.BinaryConditionMS;

/**
 *
 * @author AndrewBrook
 */
public abstract class RangeFilter extends Filter {
    private static final Log LOG = LogFactory.getLog(RangeFilter.class);

    private RangeSet ranges;

    public RangeFilter(RangeSet ranges) {
        super();
        this.ranges = ranges;
    }

    public RangeFilter() {
        super();
    }

    public RangeSet getRangeSet() {
        return ranges;
    }

    @Override
    public Condition[] getConditions() {
        Condition[] conditions = new Condition[ranges.getSize()];
        TableSchema table = ProjectController.getInstance().getCurrentVariantTableSchema();
        DbColumn posCol = table.getDBColumn(BasicVariantColumns.POSITION);
        DbColumn chrCol = table.getDBColumn(BasicVariantColumns.CHROM);
        Object[] chrs = ranges.getChrs();
        int pos = 0;
        for (Object o : chrs) {
            String chrName = (String)o;
            List<Range> rangesInChr = ranges.getRanges(chrName);
            for(Range r : rangesInChr) {
                try {
                    Condition posCondition = MedSavantClient.DBUtils.getRangeCondition(posCol, r);
                    Condition chrCondition = BinaryConditionMS.equalTo(chrCol, chrName);
                    conditions[pos] = ComboCondition.and(posCondition, chrCondition);
                    pos++;
                } catch (RemoteException ex) {
                    LOG.error("Error getting range condition.", ex);
                }

            }
        }
        return conditions;
    }

    public void merge(RangeSet newRanges) {
        if (ranges == null) {
            ranges = newRanges;
        } else {
            ranges.merge(newRanges);
        }
    }

}
