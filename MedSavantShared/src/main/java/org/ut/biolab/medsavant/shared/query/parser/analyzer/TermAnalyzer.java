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

import org.ut.biolab.medsavant.shared.model.solr.FieldMappings;
import org.ut.biolab.medsavant.shared.query.parser.QueryContext;
import org.ut.biolab.medsavant.shared.query.parser.analysis.DepthFirstAdapter;
import org.ut.biolab.medsavant.shared.query.parser.node.*;
import org.ut.biolab.medsavant.shared.query.parser.util.ParserUtil;

import java.util.Map;

/**
 * Analyzer which collects a term and term value from a WHERE clause.
 */
public class TermAnalyzer extends DepthFirstAdapter {

    private static final String TERM_VALUE_SEPARATOR = ":";

    private StringBuilder query;

    private String term;

    private String value;

    private QueryContext context;

    /**
     * Default constructor. Initializes the internal StringBuilder.
     */
    public TermAnalyzer() {
        query = new StringBuilder();
    }

    public TermAnalyzer(QueryContext context) {
        this();
        this.context = context;
    }

    @Override
    public void inASingleValuedAssociationField(ASingleValuedAssociationField node) {
        TIdentificationVariable tId = node.getIdentificationVariable();
        term = FieldMappings.mapToSolrField(tId.toString().trim(), context.getCoreName());
        query.append(term.trim() + TERM_VALUE_SEPARATOR);
        super.outASingleValuedAssociationField(node);
    }

    @Override
    public void caseAComparisonExpression(AComparisonExpression node) {

        TComparisonOperator tComparisonOperator = node.getComparisonOperator();
        String value = getValueToken(node.getComparisonExpressionRightOperand().toString().trim());
        if (ParserUtil.equal(tComparisonOperator.toString())) {
            node.getComparisonExpressionRightOperand().apply(this);
        } else if (ParserUtil.greaterThan(tComparisonOperator.toString())) {
            query.append("[" + value + " TO *]");
        } else if (ParserUtil.lessThan(tComparisonOperator.toString())) {
            query.append("[* TO " + value + " ]");
        }
    }

    @Override
    public void inAMathComparisonExpressionRightOperand(AMathComparisonExpressionRightOperand node) {
        value = node.toString();

        query.append(getValueToken(value));

        super.outAMathComparisonExpressionRightOperand(node);
    }

    @Override
    public void caseAConditionalExpression(AConditionalExpression node) {
        TOr or = node.getOr();

        if (or != null) {
            PConditionalExpression conditionalExpression = node.getConditionalExpression();
            conditionalExpression.apply(this);

            query.append(spacedString(or.toString()));

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

            query.append(spacedString(and.toString()));

            PConditionalFactor conditionalFactor = node.getConditionalFactor();
            conditionalFactor.apply(this);
        }
    }

    @Override
    public void caseABetweenExpression(ABetweenExpression node) {

        String start = node.getA().toString().trim();
        String end = node.getB().toString().trim();

        query.append("[" + start + " TO " + end + "]");
    }

    public boolean isNamedParamter(String parameter) {
        return  parameter.startsWith(":") ? true : false;
    }

    public String parseNamedParameter(String nameParameter) {
        return nameParameter.substring(1).trim();
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

    private String getValueToken(String value ) {
        String parsedValue = null;

        if (isNamedParamter(value) && context.getParameters().containsKey(parseNamedParameter(value))) {
            parsedValue = String.valueOf(context.getParameters().get(parseNamedParameter(value)));
        } else {
            parsedValue = value;
        }

        return parsedValue;
    }

    public String getQuery() {
        return query.toString();
    }

    private String spacedString(String str) {
        return " " + str.trim() + " ";
    }


}
