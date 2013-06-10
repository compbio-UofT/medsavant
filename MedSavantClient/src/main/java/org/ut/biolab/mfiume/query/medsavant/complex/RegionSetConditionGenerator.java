package org.ut.biolab.mfiume.query.medsavant.complex;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.region.RegionController;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.model.Cohort;
import org.ut.biolab.medsavant.shared.model.GenomicRegion;
import org.ut.biolab.medsavant.shared.model.OntologyTerm;
import org.ut.biolab.medsavant.shared.model.Range;
import org.ut.biolab.medsavant.shared.model.RangeCondition;
import org.ut.biolab.medsavant.shared.model.RegionSet;
import org.ut.biolab.medsavant.shared.util.BinaryConditionMS;
import org.ut.biolab.mfiume.query.SearchConditionItem;
import org.ut.biolab.mfiume.query.medsavant.MedSavantConditionViewGenerator;
import org.ut.biolab.mfiume.query.value.StringConditionValueGenerator;
import org.ut.biolab.mfiume.query.value.encode.StringConditionEncoder;
import org.ut.biolab.mfiume.query.view.SearchConditionItemView;
import org.ut.biolab.mfiume.query.view.StringSearchConditionEditorView;

/**
 *
 * @author mfiume
 */
public class RegionSetConditionGenerator implements ComprehensiveConditionGenerator {

    private boolean alreadyInitialized;
    private ArrayList<String> acceptableValues;
    private HashMap<String, RegionSet> termNameToTermObjectMap;

    @Override
    public String getName() {        
        return "Region Set";
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
    public SearchConditionItemView generateViewFromItem(SearchConditionItem item) {
        StringSearchConditionEditorView editor = new StringSearchConditionEditorView(item, new StringConditionValueGenerator() {
            @Override
            public List<String> getStringValues() {
                init();
                return acceptableValues;
            }
        });
        SearchConditionItemView view = new SearchConditionItemView(item, editor);
        return view;
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
        return MedSavantConditionViewGenerator.REGIONBASED_CONDITIONS;
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
                    rangeConditions[j] = new RangeCondition(
                            ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(BasicVariantColumns.POSITION),
                            (long)ranges.get(j).getMin(),
                            (long)ranges.get(j).getMax());
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
