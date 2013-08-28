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
package org.ut.biolab.medsavant.shared.query.parser;

import org.ut.biolab.medsavant.shared.query.parser.node.Start;

import java.util.List;
import java.util.Map;

/**
 * Information form a query.
 */
public class QueryContext {

    private Start tree;

    private String coreName;

    private Map<String, Object> parameters;

    private List<String> groupByTerms;

    private Map<String, String> aggregates;

    private Map<String, Map<String, String>> updateFields;

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public String getCoreName() {
        return coreName;
    }

    public void setCoreName(String coreName) {
        this.coreName = coreName;
    }

    public Start getTree() {
        return tree;
    }

    public void setTree(Start tree) {
        this.tree = tree;
    }

    public List<String> addGroupByTerm(String term) {
        groupByTerms.add(term);
        return groupByTerms;
    }

    public List<String> getGroupByTerms() {
        return groupByTerms;
    }

    public void setGroupByTerms(List<String> groupByTerms) {
        this.groupByTerms = groupByTerms;
    }

    public Map<String, String> getAggregates() {
        return aggregates;
    }

    public void setAggregates(Map<String, String> aggregates) {
        this.aggregates = aggregates;
    }

    public Map<String, Map<String, String>> getUpdateFields() {
        return updateFields;
    }

    public void setUpdateFields(Map<String, Map<String, String>> updateFields) {
        this.updateFields = updateFields;
    }
}
