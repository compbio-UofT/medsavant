package org.ut.biolab.medsavant.shared.query.parser;

import org.ut.biolab.medsavant.shared.query.parser.node.Start;

/**
 * Information form a query.
 */
public class QueryContext {

    private Start tree;

    public Start getTree() {
        return tree;
    }

    public void setTree(Start tree) {
        this.tree = tree;
    }
}
