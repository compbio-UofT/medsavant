package org.ut.biolab.medsavant.shared.query.parser.analyzer;

import org.ut.biolab.medsavant.shared.query.parser.analysis.DepthFirstAdapter;
import org.ut.biolab.medsavant.shared.query.parser.node.ASingleValuedAssociationField;
import org.ut.biolab.medsavant.shared.query.parser.node.TIdentificationVariable;

import java.util.ArrayList;
import java.util.List;

/**
 *  Parse the result fields for the query.
 */
public class ResultFieldAnalyzer extends DepthFirstAdapter{

    private List<String> field;

    public ResultFieldAnalyzer() {
        this.field = new ArrayList<String>();
    }

    public ResultFieldAnalyzer(List<String> field) {
        this.field = field;
    }

    @Override
    public void outASingleValuedAssociationField(ASingleValuedAssociationField node) {

        TIdentificationVariable tId =  node.getIdentificationVariable();
        if (tId != null) {
            field.add(tId.toString());
        }

        super.outASingleValuedAssociationField(node);
    }

    public List<String> getField() {
        return field;
    }

    public void setField(List<String> field) {
        this.field = field;
    }
}
