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
package org.ut.biolab.medsavant.client.query.medsavant.complex;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.region.RegionController;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.model.GenomicRegion;
import org.ut.biolab.medsavant.shared.model.Range;
import org.ut.biolab.medsavant.shared.model.RangeCondition;
import org.ut.biolab.medsavant.shared.model.RegionSet;
import org.ut.biolab.medsavant.shared.util.BinaryConditionMS;
import org.ut.biolab.medsavant.shared.util.MiscUtils;
import org.ut.biolab.medsavant.client.query.SearchConditionItem;
import org.ut.biolab.medsavant.client.query.medsavant.MedSavantConditionViewGenerator;
import org.ut.biolab.medsavant.client.query.value.StringConditionValueGenerator;
import org.ut.biolab.medsavant.client.query.value.encode.StringConditionEncoder;
import org.ut.biolab.medsavant.client.query.view.StringSearchConditionEditorView;

/**
 *
 * @author mfiume
 */
public class TagConditionGenerator implements ComprehensiveConditionGenerator {

    private boolean alreadyInitialized;
    private ArrayList<String> acceptableValues;
    private HashMap<String, RegionSet> termNameToTermObjectMap;

    @Override
    public String getName() {
        return "Tag";
    }

    @Override
    public Condition getConditionsFromEncoding(String encoding) throws Exception {

        init();

        List<String> termNames = StringConditionEncoder.unencodeConditions(encoding);
        List<RegionSet> appliedTerms = new ArrayList<RegionSet>(termNames.size());
        for (String termName : termNames) {
            appliedTerms.add(termNameToTermObjectMap.get(termName));
        }

        return ComboCondition.or(getConditions(RegionController.getInstance().getRegionsInSets(appliedTerms)));
    }

    @Override
    public StringSearchConditionEditorView getViewGeneratorForItem(SearchConditionItem item) {
        StringSearchConditionEditorView editor = new StringSearchConditionEditorView(item, new StringConditionValueGenerator() {
            @Override
            public List<String> getStringValues() {
                init();
                return acceptableValues;
            }
        });
        return editor;
    }

    private void init() {
        if (alreadyInitialized) {
            return;
        }
        acceptableValues = new ArrayList<String>();
        try {

            List<RegionSet> regionSets = RegionController.getInstance().getRegionSets();
            termNameToTermObjectMap = new HashMap<String, RegionSet>();
            for (RegionSet c : regionSets) {
                String setName = c.getName();
                acceptableValues.add(setName);
                termNameToTermObjectMap.put(setName, c);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        alreadyInitialized = true;
    }

    @Override
    public String category() {
        return MedSavantConditionViewGenerator.VARIANT_CONDITIONS;
    }

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
                        ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(BasicVariantColumns.CHROM),
                        chrom);

                //create range conditions
                List<Range> ranges = rangeMap.get(chrom);
                Condition[] rangeConditions = new Condition[ranges.size()];
                for (int j = 0; j < ranges.size(); j++) {
                    rangeConditions[j] = MiscUtils.getIntersectCondition(
                            (long) ranges.get(j).getMin(),
                            (long) ranges.get(j).getMax(),
                            ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(BasicVariantColumns.START_POSITION),
                            ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(BasicVariantColumns.END_POSITION));
                }

                //add range conditions
                tmp[1] = ComboCondition.or(rangeConditions);

                results[i] = ComboCondition.and(tmp);

                i++;
            }
        } else {
            results = new Condition[] {ConditionUtils.FALSE_CONDITION};
        }

        return results;
    }
}
