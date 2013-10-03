package org.ut.biolab.mfiume.query.medsavant.complex;

import com.healthmarketscience.sqlbuilder.Condition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.shared.model.Cohort;
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
public class CohortConditionGenerator implements ComprehensiveConditionGenerator {

    @Override
    public String getName() {
        return "Cohort";
    }

    @Override
    public Condition getConditionsFromEncoding(String encoding) throws Exception {
        List<String> cohortNames = StringConditionEncoder.unencodeConditions(encoding);
        return ConditionUtils.getConditionsMatchingDNAIDs(MedSavantClient.CohortManager.getDNAIDsForCohorts(LoginController.getInstance().getSessionID(), ProjectController.getInstance().getCurrentProjectID(), cohortNames));
    }

    @Override
    public StringSearchConditionEditorView getViewGeneratorForItem(SearchConditionItem item) {
         StringSearchConditionEditorView editor = new StringSearchConditionEditorView(item, new StringConditionValueGenerator() {
            @Override
            public List<String> getStringValues() {
                List<String> vals = new ArrayList<String>();
                try {

                    List<Cohort> cohorts = Arrays.asList(MedSavantClient.CohortManager.getCohorts(LoginController.getInstance().getSessionID(), ProjectController.getInstance().getCurrentProjectID()));
                    for (Cohort c : cohorts) {
                        vals.add(c.getName());
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return vals;
            }
        });
        return editor;
    }

    @Override
    public String category() {
        return MedSavantConditionViewGenerator.PATIENT_CONDITIONS;
    }

}
