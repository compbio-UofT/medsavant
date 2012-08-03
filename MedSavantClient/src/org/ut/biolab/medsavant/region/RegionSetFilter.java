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

package org.ut.biolab.medsavant.region;

import org.ut.biolab.medsavant.filter.Filter;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;

import org.ut.biolab.medsavant.db.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.model.*;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.util.BinaryConditionMS;


/**
 * Filter which operates on a RegionSet or on an array of regions (from a gene or ontology).
 *
 * @author tarkvara
 */
public abstract class RegionSetFilter extends Filter {
    
    protected Condition[] getConditions(Collection<GenomicRegion> regions) throws SQLException, RemoteException {

        Map<String, List<Range>> rangeMap = GenomicRegion.mergeGenomicRegions(regions);
        
        Condition[] results;
        if (rangeMap.size() > 0) {
            results = new Condition[rangeMap.size()];
            int i = 0;
            for (String chrom : rangeMap.keySet()) {

                Condition[] tmp = new Condition[2];

                //add chrom condition
                tmp[0] = BinaryConditionMS.equalTo(
                        ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM),
                        chrom);

                //create range conditions
                List<Range> ranges = rangeMap.get(chrom);
                Condition[] rangeConditions = new Condition[ranges.size()];
                for (int j = 0; j < ranges.size(); j++) {
                    rangeConditions[j] = new RangeCondition(
                            ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_POSITION),
                            (long)ranges.get(j).getMin(),
                            (long)ranges.get(j).getMax());
                }

                //add range conditions
                tmp[1] = ComboCondition.or(rangeConditions);

                results[i] = ComboCondition.and(tmp);

                i++;
            }
        } else {
            results = FALSE_CONDITION;
        }

        return results;
    }
}
