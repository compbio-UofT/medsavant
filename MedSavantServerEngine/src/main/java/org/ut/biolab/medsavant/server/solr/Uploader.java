package org.ut.biolab.medsavant.server.solr;

import org.ut.biolab.medsavant.server.solr.service.SolrVCFUploader;

import java.io.File;

/**
 * Test parsing of vcf file and indexing to Solr.
 */
public class Uploader {

    public static void main(String[] args) {

        String vcfFile;

        if (args.length > 0) {
            vcfFile = args[0];
        } else {
            vcfFile =  "input4.vcf";
        }

        File input = new File(vcfFile);

        SolrVCFUploader solrVCFUploader = new SolrVCFUploader();

        long variantsIndexed = solrVCFUploader.processAndIndex(input);

        System.out.println("Indexed " + variantsIndexed + " variants");
    }
}
