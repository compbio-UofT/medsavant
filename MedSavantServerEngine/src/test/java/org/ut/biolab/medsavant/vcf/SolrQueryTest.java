package org.ut.biolab.medsavant.vcf;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ut.biolab.medsavant.server.solr.SimpleSolrQuery;


public class SolrQueryTest {

    private SimpleSolrQuery solrQuery;

    @Before
    public void initializeTest() {
        solrQuery = new SimpleSolrQuery();
    }

    @Test
    public void testSolrQuery() {

        String correctQueryResult = "q=*:*&fq=id:rs* AND dna_id:NA*";

        solrQuery.addQueryTerm("*", "*");
        solrQuery.addFilterQueryTerm("id","rs*");
        solrQuery.addFilterQueryTerm("dna_id", "NA*");

        String fullSolrQuery = solrQuery.getFullSolrQuery();

        Assert.assertEquals(fullSolrQuery, correctQueryResult);

    }

    @Test
    public void testQueryTemrs() {

        String correctQueryResult = "q=id:rs* AND dna_id:NA*";

        solrQuery.addQueryTerm("id", "rs*");
        solrQuery.addQueryTerm("dna_id", "NA*");

        String fullSolrQuery = solrQuery.getNormalQuery();

        Assert.assertEquals(fullSolrQuery, correctQueryResult);

    }

    @Test
    public void testFilterQueryTemrs() {

        String correctQueryResult = "fq=id:rs* AND dna_id:NA*";

        solrQuery.addFilterQueryTerm("id", "rs*");
        solrQuery.addFilterQueryTerm("dna_id", "NA*");

        String fullSolrQuery = solrQuery.getFilterQuery();

        Assert.assertEquals(fullSolrQuery, correctQueryResult);

    }

}
