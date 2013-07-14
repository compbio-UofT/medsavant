package org.ut.biolab.medsavant.shared.query.parser.analyzer;

import org.ut.biolab.medsavant.shared.query.parser.analysis.DepthFirstAdapter;
import org.ut.biolab.medsavant.shared.query.parser.node.*;

/**
 * Analyzer which collects a term and term value from a WHERE clause.
 */
public class TermAnalyzer extends DepthFirstAdapter {

    private static final String TERM_VALUE_SEPARATOR = ":";

    private StringBuilder query;

    /**
     * Default constructor. Initializes the internal StringBuilder.
     */
    public TermAnalyzer() {
        query = new StringBuilder();
    }

    @Override
    public void inASingleValuedAssociationField(ASingleValuedAssociationField node) {
        TIdentificationVariable tId = node.getIdentificationVariable();
        String term = tId.toString();

        query.append(term.trim() + TERM_VALUE_SEPARATOR);
        super.outASingleValuedAssociationField(node);
    }

    @Override
    public void inAMathComparisonExpressionRightOperand(AMathComparisonExpressionRightOperand node) {
        String value = node.toString();
        query.append(value);
        super.outAMathComparisonExpressionRightOperand(node);
    }

    @Override
    public void caseAConditionalExpression(AConditionalExpression node) {
        TOr or = node.getOr();

        if (or != null) {
            PConditionalExpression conditionalExpression = node.getConditionalExpression();
            conditionalExpression.apply(this);

            query.append(or.toString());

            PConditionalTerm conditionalTerm = node.getConditionalTerm();
            conditionalTerm.apply(this);
        }

    }

    @Override
    public void caseAConditionalTerm(AConditionalTerm node) {
        TAnd and = node.getAnd();

        if (and != null) {

            PConditionalTerm conditionalTerm = node.getConditionalTerm();
            conditionalTerm.apply(this);

            query.append(and.toString());

            PConditionalFactor conditionalFactor = node.getConditionalFactor();
            conditionalFactor.apply(this);

        }

    }

    @Override
    public void inALeftBracketProd(ALeftBracketProd node) {
        query.append(node.getLeftBracket().toString().trim());
        super.outALeftBracketProd(node);
    }

    @Override
    public void inARightBracketProd(ARightBracketProd node) {
        query.append(node.getRightBracket().toString().trim());
        super.outARightBracketProd(node);
    }

    public String getQuery() {
        return query.toString();
    }
}
