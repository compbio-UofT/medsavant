package org.ut.biolab.medsavant.shared.query.parser.analyzer;

import org.ut.biolab.medsavant.shared.query.parser.analysis.DepthFirstAdapter;
import org.ut.biolab.medsavant.shared.query.parser.node.TInputParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * Collect the named parameter names.
 */
public class NamedParameterAnalyzer extends DepthFirstAdapter {

    private Map<String, Object> parameters;

    private static final String NAMED_PARAM_DELIM = ":";

    private static final String POSITIONAL_PARAM_DELIM = "?";

    @Override
    public void caseTInputParameter(TInputParameter node) {
        String inputParamString = node.toString();
        if (inputParamString.startsWith(NAMED_PARAM_DELIM) || inputParamString.startsWith(POSITIONAL_PARAM_DELIM)) {
            parameters.put(inputParamString.substring(1).trim(), "");
        }
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
}
