package org.ut.biolab.medsavant.shared.query.parser.analyzer;

import org.ut.biolab.medsavant.shared.query.parser.QueryContext;
import org.ut.biolab.medsavant.shared.query.parser.analysis.DepthFirstAdapter;
import org.ut.biolab.medsavant.shared.query.parser.node.ASingleValuedAssociationField;

import java.util.List;

/**
 * Analyzer class that handles the group by aggregation terms.
 */
public class AggregateAnalyzer extends DepthFirstAdapter {

    private QueryContext queryContext;

    @Override
    public void caseASingleValuedAssociationField(ASingleValuedAssociationField node) {

        String aggregateField = node.toString().trim();
        queryContext.addGroupByTerm(aggregateField);

    }

    public AggregateAnalyzer(QueryContext queryContext) {
        this.queryContext = queryContext;
    }

    public QueryContext getQueryContext() {
        return queryContext;
    }

    public void setQueryContext(QueryContext queryContext) {
        this.queryContext = queryContext;
    }

}
