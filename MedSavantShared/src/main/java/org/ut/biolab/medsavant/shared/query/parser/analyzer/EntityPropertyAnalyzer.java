package org.ut.biolab.medsavant.shared.query.parser.analyzer;

import org.ut.biolab.medsavant.shared.model.solr.FieldMappings;
import org.ut.biolab.medsavant.shared.query.parser.analysis.DepthFirstAdapter;
import org.ut.biolab.medsavant.shared.query.parser.node.AIddotSingleValuedAssociationPathExpression;
import org.ut.biolab.medsavant.shared.query.parser.node.ASingleValuedAssociationField;
import org.ut.biolab.medsavant.shared.query.parser.node.ASingleValuedAssociationFieldSingleValuedAssociationPathExpression;

import java.util.Locale;

/**
 * Extract the path from an expression of the form id . property
 */
public class EntityPropertyAnalyzer extends DepthFirstAdapter {

    private String property;

    @Override
    public void caseASingleValuedAssociationFieldSingleValuedAssociationPathExpression(ASingleValuedAssociationFieldSingleValuedAssociationPathExpression node) {
        property = "";
    }

    @Override
    public void caseAIddotSingleValuedAssociationPathExpression(AIddotSingleValuedAssociationPathExpression node) {
        property = node.getSingleValuedAssociationField().toString().trim().toLowerCase(Locale.ROOT);

    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }
}
