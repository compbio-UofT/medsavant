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

import com.healthmarketscience.sqlbuilder.Condition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.shared.model.Cohort;
import org.ut.biolab.medsavant.client.query.SearchConditionItem;
import org.ut.biolab.medsavant.client.query.medsavant.MedSavantConditionViewGenerator;
import org.ut.biolab.medsavant.client.query.value.StringConditionValueGenerator;
import org.ut.biolab.medsavant.client.query.value.encode.StringConditionEncoder;
import org.ut.biolab.medsavant.client.query.view.SearchConditionItemView;
import org.ut.biolab.medsavant.client.query.view.StringSearchConditionEditorView;

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
        return ConditionUtils.getConditionsMatchingDNAIDs(MedSavantClient.CohortManager.getDNAIDsForCohorts(LoginController.getSessionID(), ProjectController.getInstance().getCurrentProjectID(), cohortNames));
    }

    @Override
    public StringSearchConditionEditorView getViewGeneratorForItem(SearchConditionItem item) {
         StringSearchConditionEditorView editor = new StringSearchConditionEditorView(item, new StringConditionValueGenerator() {
            @Override
            public List<String> getStringValues() {
                List<String> vals = new ArrayList<String>();
                try {

                    List<Cohort> cohorts = Arrays.asList(MedSavantClient.CohortManager.getCohorts(LoginController.getSessionID(), ProjectController.getInstance().getCurrentProjectID()));
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
