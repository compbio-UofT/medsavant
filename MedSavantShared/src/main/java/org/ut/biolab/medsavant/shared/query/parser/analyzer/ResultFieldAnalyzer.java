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
package org.ut.biolab.medsavant.shared.query.parser.analyzer;

import org.ut.biolab.medsavant.shared.query.parser.analysis.DepthFirstAdapter;
import org.ut.biolab.medsavant.shared.query.parser.node.*;

import java.util.*;

/**
 *  Parse the result fields for the query.
 */
public class ResultFieldAnalyzer extends DepthFirstAdapter{

    private List<String> field;

    private Map<String, String> aggregates;

    public ResultFieldAnalyzer() {
        this.field = new ArrayList<String>();
        this.aggregates = new HashMap<String, String>();
    }

    public ResultFieldAnalyzer(List<String> field) {
        this.field = field;
    }

    @Override
    public void caseAIddotSingleValuedAssociationPathExpression(AIddotSingleValuedAssociationPathExpression node) {

        PSingleValuedAssociationField fieldValue =  node.getSingleValuedAssociationField();
        if (fieldValue != null) {
            field.add(fieldValue.toString());
        }
    }

    @Override
    public void caseACountAggregateExpression(ACountAggregateExpression node) {

        String count = node.getCount().toString().toLowerCase(Locale.ROOT).trim();

        EntityPropertyAnalyzer epa = new EntityPropertyAnalyzer();
        node.apply(epa);
        String entity = epa.getProperty();

        aggregates.put(count, entity);
    }

    public List<String> getField() {
        return field;
    }

    public void setField(List<String> field) {
        this.field = field;
    }

    public boolean hasAggregates() {
        return !aggregates.isEmpty();
    }

    public Map<String, String> getAggregates() {
        return aggregates;
    }

    public void setAggregates(Map<String, String> aggregates) {
        this.aggregates = aggregates;
    }
}
