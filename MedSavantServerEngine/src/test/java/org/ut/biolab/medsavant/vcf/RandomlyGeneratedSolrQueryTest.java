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
package org.ut.biolab.medsavant.vcf;

import org.apache.solr.common.SolrDocumentList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ut.biolab.medsavant.server.solr.SimpleSolrQuery;
import org.ut.biolab.medsavant.server.solr.exception.InitializationException;
import org.ut.biolab.medsavant.server.solr.service.VariantData;
import org.ut.biolab.medsavant.server.solr.service.VariantService;

import java.util.Random;

public class RandomlyGeneratedSolrQueryTest {

    private VariantService variantService;

    @Before
    public void initialize() throws InitializationException {
        variantService = new VariantService();
        variantService.initialize();
    }

    @Test
    public void testMoreComplexQueries() {

        int iterations = 100;

        long sum = 0;

        for (int i = 0; i <iterations; i++) {
            long duration = generateAndExecuteSimpleQuery();
            sum += duration;
        }

        sum /= iterations;
        System.out.println("Average time " + sum);
        Assert.assertTrue(sum > 0);
    }

    @Test
    public void testSimpleQuery() {

        SimpleSolrQuery solrQuery = generateSimpleQuery();

        SolrDocumentList solrDocumentList = variantService.search(solrQuery);

        Assert.assertNotNull(solrDocumentList);
    }

    public long generateAndExecuteSimpleQuery() {

        SimpleSolrQuery simpleSolrQuery = generateSimpleQuery();

        long start = System.currentTimeMillis();
        long duration;

        SolrDocumentList solrDocumentList = variantService.search(simpleSolrQuery);

        long end = System.currentTimeMillis();
        duration = end - start;

        System.out.println("Duration: " + duration + " " + solrDocumentList.getNumFound());

        return duration;
    }

    public SimpleSolrQuery generateSimpleQuery() {
        String id = getRandomParameter(VariantTestConstants.ids);
        String dna_id = getRandomParameter(VariantTestConstants.dna_ids);
        String alt = getRandomParameter(VariantTestConstants.alts);
        String chrom = getRandomParameter(VariantTestConstants.chroms);
        String ref = getRandomParameter(VariantTestConstants.refs);
        String zygosity = getRandomParameter(VariantTestConstants.zygosities);

        SimpleSolrQuery simpleSolrQuery = new SimpleSolrQuery();
        simpleSolrQuery.addQueryTerm(VariantData.ID, id);
        simpleSolrQuery.addQueryTerm(VariantData.DNA_ID, dna_id);
        simpleSolrQuery.addQueryTerm(VariantData.ALT, alt);
        simpleSolrQuery.addQueryTerm(VariantData.CHROM, chrom);
        simpleSolrQuery.addQueryTerm(VariantData.REF, ref);
        simpleSolrQuery.addFilterQueryTerm(VariantData.ZYGOSITY, zygosity);

        return simpleSolrQuery;
    }

    private String getRandomParameter(String[] vector) {
        Random random = new Random();

        int index = random.nextInt(vector.length);

        return vector[index];
    }

}
