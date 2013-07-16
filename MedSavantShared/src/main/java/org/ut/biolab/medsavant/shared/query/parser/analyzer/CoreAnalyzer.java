package org.ut.biolab.medsavant.shared.query.parser.analyzer;

import org.ut.biolab.medsavant.shared.query.parser.analysis.DepthFirstAdapter;
import org.ut.biolab.medsavant.shared.query.parser.node.ARangeVariableDeclaration;
import org.ut.biolab.medsavant.shared.query.parser.node.PAbstractSchemaName;

import java.util.Locale;

/**
 * Collect the core name from the query.
 */
public class CoreAnalyzer extends DepthFirstAdapter {

    private String coreName;

    @Override
    public void outARangeVariableDeclaration(ARangeVariableDeclaration node) {
        PAbstractSchemaName schemaName =  node.getAbstractSchemaName();

        if (schemaName != null) {
            coreName = schemaName.toString().trim().toLowerCase(Locale.ROOT);
        }

        super.outARangeVariableDeclaration(node);
    }

    public String getCoreName() {
        return coreName;
    }

    public void setCoreName(String coreName) {
        this.coreName = coreName;
    }
}
