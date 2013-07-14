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
package org.ut.biolab.medsavant.server.vcf;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ut.biolab.medsavant.shared.query.SimpleSolrQuery;
import org.ut.biolab.medsavant.shared.solr.exception.InitializationException;
import org.ut.biolab.medsavant.shared.solr.service.AbstractSolrService;
import org.ut.biolab.medsavant.shared.solr.service.VariantService;
import org.ut.biolab.medsavant.server.vcf.VCFParser;

import java.io.File;
import java.io.IOException;

public class VCFParserTest {

    SolrVCFUploader solrVCFUploader;

    AbstractSolrService solrVariantService;

    @Before
    public void initializeTestData() throws InitializationException {
        solrVCFUploader = new SolrVCFUploader();

        solrVariantService = new VariantService();
        solrVariantService.initialize();
    }

    @Test
    public void testIndexDataToSolr() {

        File input = new File("input2.vcf");

        SimpleSolrQuery simpleSolrQuery = new SimpleSolrQuery();
        simpleSolrQuery.addQueryTerm("*", "*");

        long sizeBefore = solrVariantService.search(simpleSolrQuery).getNumFound();

        SolrVCFUploader solrVCFUploader = new SolrVCFUploader();

        long variantsIndexed = solrVCFUploader.processAndIndex(input);

        long sizeAfter = solrVariantService.search(simpleSolrQuery).getNumFound();

        Assert.assertEquals(sizeAfter, sizeBefore + variantsIndexed);
    }

    @Test
    public void testIndexDataToSolrUsingMedSavantParser() throws IOException {

        File input = new File("input.vcf");

        SimpleSolrQuery simpleSolrQuery = new SimpleSolrQuery();
        simpleSolrQuery.addQueryTerm("*", "*");

        long sizeBefore = solrVariantService.search(simpleSolrQuery).getNumFound();

        VCFParser.parseVariantsAndUploadToSolr(input);

        long variantsIndexed = solrVCFUploader.processAndIndex(input);

        long sizeAfter = solrVariantService.search(simpleSolrQuery).getNumFound();

        Assert.assertEquals(sizeAfter, sizeBefore + variantsIndexed);
    }
}
