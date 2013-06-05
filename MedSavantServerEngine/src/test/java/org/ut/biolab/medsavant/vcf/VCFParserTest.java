package org.ut.biolab.medsavant.vcf;


import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ut.biolab.medsavant.server.solr.exception.InitializationException;
import org.ut.biolab.medsavant.server.solr.service.AbstractSolrService;
import org.ut.biolab.medsavant.server.solr.service.SolrVCFUploader;
import org.ut.biolab.medsavant.server.solr.service.VCFService;
import org.ut.biolab.medsavant.server.vcf.VCFParser;

import java.io.File;
import java.io.IOException;

public class VCFParserTest {

    SolrVCFUploader solrVCFUploader;

    AbstractSolrService solrVariantService;

    @Before
    public void initializeTestData() throws InitializationException {
        solrVCFUploader = new SolrVCFUploader();

        solrVariantService = new VCFService();
        solrVariantService.initialize();
    }

    @Test
    public void testIndexDataToSolr() {

        File input = new File("input.vcf");

        long sizeBefore = solrVariantService.search("*:*").getNumFound();

        SolrVCFUploader solrVCFUploader = new SolrVCFUploader();

        long variantsIndexed = solrVCFUploader.processAndIndex(input);

        long sizeAfter = solrVariantService.search("*:*").getNumFound();

        Assert.assertEquals(sizeAfter, sizeBefore + variantsIndexed);
    }

    @Test
    public void testIndexDataToSolrUsingMedSavantParser() throws IOException {

        File input = new File("input.vcf");

        long sizeBefore = solrVariantService.search("*:*").getNumFound();

        long variantIndexer = VCFParser.parseVariantsAndUploadToSolr(input);

        long variantsIndexed = solrVCFUploader.processAndIndex(input);

        long sizeAfter = solrVariantService.search("*:*").getNumFound();

        Assert.assertEquals(sizeAfter, sizeBefore + variantsIndexed);
    }



}
