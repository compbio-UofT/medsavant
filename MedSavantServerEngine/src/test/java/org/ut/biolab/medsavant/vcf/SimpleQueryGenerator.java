package org.ut.biolab.medsavant.vcf;

import org.ut.biolab.medsavant.server.solr.SimpleSolrQuery;
import org.ut.biolab.medsavant.server.solr.service.VariantData;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Generate simple querries
 */
public class SimpleQueryGenerator {

    public SimpleSolrQuery generate(int qNumber) {

        SimpleSolrQuery simpleSolrQuery = new SimpleSolrQuery();

        Map<String, String> params = generateRandomParams();
        int i = 0;

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (i < qNumber) {
                simpleSolrQuery.addQueryTerm(entry.getKey(), entry.getValue());
            } else {
                simpleSolrQuery.addFilterQueryTerm(entry.getKey(), entry.getValue());
            }

            i++;
        }

        return simpleSolrQuery;
    }


    public Map<String, String> generateRandomParams() {
        String id = getRandomParameter(VariantTestConstants.ids);
        String dna_id = getRandomParameter(VariantTestConstants.dna_ids);
        String alt = getRandomParameter(VariantTestConstants.alts);
        String chrom = getRandomParameter(VariantTestConstants.chroms);
        String ref = getRandomParameter(VariantTestConstants.refs);
        String zygosity = getRandomParameter(VariantTestConstants.zygosities);

        Map<String, String> randomParams = new HashMap<String, String>();

        randomParams.put(VariantData.ID, id);
        randomParams.put(VariantData.DNA_ID, dna_id);
        randomParams.put(VariantData.ALT, alt);
        randomParams.put(VariantData.CHROM, chrom);
        randomParams.put(VariantData.REF, ref);
        randomParams.put(VariantData.ZYGOSITY, zygosity);

        return randomParams;
    }


    private String getRandomParameter(String[] vector) {
        Random random = new Random();

        int index = random.nextInt(vector.length);

        return vector[index];
    }


}
