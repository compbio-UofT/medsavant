package org.ut.biolab.medsavant.shared.query.parser.analyzer;

import org.ut.biolab.medsavant.shared.query.parser.analysis.DepthFirstAdapter;
import org.ut.biolab.medsavant.shared.query.parser.node.*;

/**
 * Analyzer which collects a term and term value from a WHERE clause.
 */
public class TermAnalyzer extends DepthFirstAdapter {

    String term;
    String value;

    @Override
    public void outASingleValuedAssociationField(ASingleValuedAssociationField node) {
        TIdentificationVariable tId = node.getIdentificationVariable();
        term = tId.toString();
        super.outASingleValuedAssociationField(node);
    }

    @Override
    public void outAMathComparisonExpressionRightOperand(AMathComparisonExpressionRightOperand node) {
        value = node.toString();
        super.outAMathComparisonExpressionRightOperand(node);
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
