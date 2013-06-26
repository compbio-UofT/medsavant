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

import org.ut.biolab.medsavant.server.solr.service.SolrVCFUploader;

import java.io.File;

/**
 * Test parsing of vcf file and indexing to Solr.
 *
 * @author Bogdan Vancea
 */
public class UploaderTest {

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
