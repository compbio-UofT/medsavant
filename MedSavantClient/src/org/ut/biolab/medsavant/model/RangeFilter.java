/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.ut.biolab.medsavant.model;

import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.TableSchema;
import org.ut.biolab.medsavant.util.BinaryConditionMS;

/**
 *
 * @author AndrewBrook
 */
public abstract class RangeFilter extends QueryFilter {

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
        DbColumn posCol = table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_POSITION);
        DbColumn chrCol = table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM);
        Object[] chrs = ranges.getChrs();
        int pos = 0;
        for (Object o : chrs) {
            String chrName = (String)o;
            List<Range> rangesInChr = ranges.getRanges(chrName);
            for(Range r : rangesInChr) {
                try {
                    Condition posCondition = MedSavantClient.QueryUtilAdapter.getRangeCondition(posCol, r);
                    Condition chrCondition = BinaryConditionMS.equalTo(chrCol, chrName);
                    conditions[pos] = ComboCondition.and(posCondition, chrCondition);
                    pos++;
                } catch (RemoteException ex) {
                    Logger.getLogger(RangeFilter.class.getName()).log(Level.SEVERE, null, ex);
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
