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

import org.apache.solr.client.solrj.SolrQuery;
import org.ut.biolab.medsavant.shared.query.QuerySortDirection;
import org.ut.biolab.medsavant.shared.query.parser.analysis.DepthFirstAdapter;
import org.ut.biolab.medsavant.shared.query.parser.node.AOrderbyItem;
import org.ut.biolab.medsavant.shared.query.parser.node.ASingleValuedAssociationField;
import org.ut.biolab.medsavant.shared.query.parser.node.TIdentificationVariable;
import org.ut.biolab.medsavant.shared.query.parser.node.TOrderDirection;

import java.util.Locale;

/**
 * Analyzer for the sorting fields.
 */
public class SortsAnalyzer extends DepthFirstAdapter {

    private SolrQuery.ORDER direction = SolrQuery.ORDER.asc;

    String field;

    @Override
    public void outAOrderbyItem(AOrderbyItem node) {
        TOrderDirection orderDirection = node.getOrderDirection();

        //if no order direction, keep the default one
        if (orderDirection != null) {
            String directionString = orderDirection.toString().toLowerCase(Locale.ROOT).trim();
            if (SolrQuery.ORDER.desc.toString().toLowerCase(Locale.ROOT).equals(directionString)) {
                direction = SolrQuery.ORDER.desc;
            }
        }
        super.outAOrderbyItem(node);
    }

    @Override
    public void inAOrderbyItem(AOrderbyItem node) {
        direction = SolrQuery.ORDER.asc;

        super.inAOrderbyItem(node);
    }

    @Override
    public void outASingleValuedAssociationField(ASingleValuedAssociationField node) {
        TIdentificationVariable tId = node.getIdentificationVariable();
        field = tId.toString();
        super.outASingleValuedAssociationField(node);
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public SolrQuery.ORDER getDirection() {
        return direction;
    }

    public void setDirection(SolrQuery.ORDER direction) {
        this.direction = direction;
    }
}
