package org.ut.biolab.medsavant.shared.query.parser;

import org.ut.biolab.medsavant.shared.query.parser.node.Start;

import java.util.ArrayList;
import java.util.List;

/**
 * Information form a query.
 */
public class QueryContext {

    private Start tree;

    private String coreName;

    private List<String> resultFieldNames;

    public QueryContext() {
        resultFieldNames = new ArrayList<String>();
    }

    public void addResultField(String resultField) {
        resultFieldNames.add(resultField);
    }

    public void clearResultFieldNames() {
        resultFieldNames.clear();
    }

    public List<String> getResultFieldNames() {
        return resultFieldNames;
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

    public void setResultFieldNames(List<String> resultFieldNames) {
        this.resultFieldNames = resultFieldNames;
    }
}
