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
