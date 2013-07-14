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
}
