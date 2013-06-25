package org.ut.biolab.medsavant.vcf;


import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ut.biolab.medsavant.server.solr.SimpleSolrQuery;
import org.ut.biolab.medsavant.server.solr.SimpleVariantClient;

public class SimpleVariantClientTest {

    private SimpleVariantClient simpleVariantClient;

    @Before
    public void initialize() {
        simpleVariantClient = new SimpleVariantClient();
    }

    @Test
    @Ignore
    public void testSimpleQuery() {

        SimpleSolrQuery solrQuery = new SimpleSolrQuery();

        solrQuery.addQueryTerm("id", "rs*");
        solrQuery.addFilterQueryTerm("dna_id", "NA*");

        String responseString = simpleVariantClient.executeQuery(solrQuery);

        //FIXME maybe replace this with something more conclusive?
        Assert.assertNotNull(responseString);
    }

    @Test
    @Ignore
    public void testSimpleQueryForTime() {

        SimpleSolrQuery solrQuery = new SimpleSolrQuery();

        solrQuery.addQueryTerm("id", "rs*");
        solrQuery.addFilterQueryTerm("dna_id", "NA*");

        long duration = simpleVariantClient.executeQueryForTime(solrQuery);

        //FIXME maybe replace this with something more conclusive?
        Assert.assertTrue( duration > 0);
    }

    @Test
    @Ignore
    public void testMoreComplexQueries() {

        int iterations = 100;

        long sum = 0;

        for (int i = 0; i <iterations; i++) {
            long duration = simpleVariantClient.generateAndExecuteSimpleQuery();
            sum += duration;
            System.out.println(duration);
        }

        sum /= iterations;

        System.out.println(sum);

        Assert.assertTrue(sum > 0);
    }




}
