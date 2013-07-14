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
package org.ut.biolab.medsavant.shared.vcf;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ut.biolab.medsavant.shared.query.SimpleSolrQuery;


public class SolrQueryTest {

    private SimpleSolrQuery solrQuery;

    @Before
    public void initializeTest() {
        solrQuery = new SimpleSolrQuery();
    }

    @Test
    public void testQueryTerms() {

        String correctQueryResult = "q=id:rs* AND dna_id:NA*";

        solrQuery.addQueryTerm("id", "rs*");
        solrQuery.addQueryTerm("dna_id", "NA*");

        String fullSolrQuery = solrQuery.toString().toString();

        Assert.assertEquals(fullSolrQuery, correctQueryResult);

    }

    @Test
    public void testFilterQueryTerms() {

        String correctQueryResult = "fq=id:rs* AND dna_id:NA*";

        solrQuery.addFilterQueryTerm("id", "rs*");
        solrQuery.addFilterQueryTerm("dna_id", "NA*");

        String fullSolrQuery = solrQuery.toSolrParams().toString();

        Assert.assertEquals(fullSolrQuery, correctQueryResult);

    }

}
