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
import org.ut.biolab.medsavant.shared.query.parser.node.ASingleFromList;
import org.ut.biolab.medsavant.shared.query.parser.node.TInputParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * Collect the named parameter names.
 */
public class NamedParameterAnalyzer extends DepthFirstAdapter {

    private Map<String, Object> parameters;

    private String coreName;

    private static final String NAMED_PARAM_DELIM = ":";

    private static final String POSITIONAL_PARAM_DELIM = "?";

    @Override
    public void caseTInputParameter(TInputParameter node) {
        String inputParamString = node.toString();
        if (inputParamString.startsWith(NAMED_PARAM_DELIM) || inputParamString.startsWith(POSITIONAL_PARAM_DELIM)) {
            parameters.put(inputParamString.substring(1).trim(), "");
        }
    }

    @Override
    public void inASingleFromList(ASingleFromList node) {
        CoreAnalyzer coreAnalyzer = new CoreAnalyzer();
        node.apply(coreAnalyzer);

        coreName = coreAnalyzer.getCoreName();

        super.outASingleFromList(node);
    }

    public NamedParameterAnalyzer() {
        parameters = new HashMap<String, Object>();
    }

    public NamedParameterAnalyzer(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(HashMap<String, Object> parameters) {
        this.parameters = parameters;
    }

    public String getCoreName() {
        return coreName;
    }

    public void setCoreName(String coreName) {
        this.coreName = coreName;
    }
}
