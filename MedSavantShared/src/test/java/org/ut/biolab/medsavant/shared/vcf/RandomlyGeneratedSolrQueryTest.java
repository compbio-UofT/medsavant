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

import org.apache.solr.common.SolrDocumentList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.ut.biolab.medsavant.shared.solr.exception.InitializationException;
import org.ut.biolab.medsavant.shared.solr.service.VariantService;

import java.util.Arrays;
import java.util.Collection;

@RunWith(value = Parameterized.class)
public class RandomlyGeneratedSolrQueryTest {

    private VariantService variantService;

    private int qTermsNumber;

    public RandomlyGeneratedSolrQueryTest(int qTermsNumber) {
        this.qTermsNumber = qTermsNumber;
    }

    @Before
    public void initialize() throws InitializationException {
        variantService = new VariantService();
        variantService.initialize();
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][] { { 0 }, { 1 }, { 2 }, { 3 }, { 4 }, { 5 }, { 6 } };
        return Arrays.asList(data);
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

    public long generateAndExecuteSimpleQuery() {

        SimpleQueryGenerator simpleQueryGenerator = new SimpleQueryGenerator();

        long start = System.currentTimeMillis();
        long duration;

        SolrDocumentList solrDocumentList = variantService.search(simpleQueryGenerator.generate(qTermsNumber));

        long end = System.currentTimeMillis();
        duration = end - start;

        return duration;
    }

}
