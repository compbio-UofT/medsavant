package org.ut.biolab.medsavant.shared.util;

import java.net.URL;

/**
 *
 * @author mfiume
 */
public class WebResources {

    /*
     * Website URLs
     */
    //p/assets
    //p/medsavant
    //scripts/logUsageStats.cgi
    //learn
    //serve
    
    //Email address
    private static final String EMAIL_DOMAIN ="genomesavant.com";
    public static final URL FEEDBACK_URL = NetworkUtils.getKnownGoodURL("mailto:feedback@"+EMAIL_DOMAIN+"?subject=MedSavant%20Feedback");
        
    private static final URL BASE_URL = NetworkUtils.getKnownGoodURL("http://compbio.cs.toronto.edu/savant/data/dropbox/medsavant");            
    
    //Links to the website.
    public static final URL WEB_URL = NetworkUtils.getKnownGoodURL("http://genomesavant.com/p/medsavant");    
    public static final URL USERGUIDE_URL = NetworkUtils.getKnownGoodURL(WEB_URL, "learn");

    //Links to xml files. 
    private static final URL XML_BASE = NetworkUtils.getKnownGoodURL(BASE_URL, "xml");
    public static final URL ANNOTATION_DIRECTORY_URL = NetworkUtils.getKnownGoodURL(XML_BASE, "annotationDirectory.xml");
    public static final URL CLIENT_SERVER_VERSION_COMPATIBILITY_URL = NetworkUtils.getKnownGoodURL(XML_BASE, "client_server_compatibility.xml");
    public static final URL DATABASE_SERVER_VERSION_COMPATIBILITY_URL = NetworkUtils.getKnownGoodURL(XML_BASE, "db_server_compatibility.xml");
    public static final URL APPSDK_CLIENT_VERSION_COMPATIBILITY_URL = NetworkUtils.getKnownGoodURL(XML_BASE, "xml/appsdk_client_compatibility.xml");
    public static final String[] PLUGIN_REPOSITORY_URLS
            = new String[]{NetworkUtils.getKnownGoodURL(XML_BASE, "pluginDirectory.xml").toString()};
    
    //Links to data files.    
    private static final URL DATA_BASE = NetworkUtils.getKnownGoodURL(BASE_URL, "data");
    private static final URL HG18_BASE = NetworkUtils.getKnownGoodURL(DATA_BASE, "hg18");
    private static final URL HG19_BASE = NetworkUtils.getKnownGoodURL(DATA_BASE, "hg19");
    private static final URL ONTOLOGY_BASE = NetworkUtils.getKnownGoodURL(DATA_BASE, "ontology");    
    public static final URL GENEMANIA_DATA_URL = NetworkUtils.getKnownGoodURL(DATA_BASE, "gmdata.zip");    
    public static final URL REFGENE_HG19_URL = NetworkUtils.getKnownGoodURL(HG19_BASE, "refGene.txt.gz");
    public static final URL JANNOVAR_HG19_SERFILE_URL = NetworkUtils.getKnownGoodURL(HG19_BASE, "jannovar/refseq_hg19.ser");    
    public static final URL REFGENE_HG18_URL = NetworkUtils.getKnownGoodURL(HG18_BASE, "hg18.refGene.gz");
    public static final URL GO_OBO_URL = NetworkUtils.getKnownGoodURL(ONTOLOGY_BASE, "gene_ontology.1_2.obo");
    public static final URL HPO_OBO_URL = NetworkUtils.getKnownGoodURL(ONTOLOGY_BASE, "human-phenotype-ontology.obo");
    public static final URL OMIM_OBO_URL = NetworkUtils.getKnownGoodURL(ONTOLOGY_BASE,  "omim.obo");
    public static final URL GO_TO_GENES_URL = NetworkUtils.getKnownGoodURL(ONTOLOGY_BASE, "gene_association.goa_human.gz");
    public static final URL HPO_TO_GENES_URL = NetworkUtils.getKnownGoodURL(ONTOLOGY_BASE, "phenotype_to_genes.txt");
    public static final URL OMIM_TO_HPO_URL = NetworkUtils.getKnownGoodURL(ONTOLOGY_BASE, "phenotype_annotation.tab");
    
    //Links to scripts
    private static final URL SCRIPT_BASE = NetworkUtils.getKnownGoodURL(BASE_URL, "scripts");
    public static final URL LOG_USAGE_STATS_URL = NetworkUtils.getKnownGoodURL(SCRIPT_BASE, "logUsageStats.cgi");
    public static final URL BUGREPORT_URL = NetworkUtils.getKnownGoodURL(SCRIPT_BASE, "bugreport-post.php");
    public static final URL FEEDBACK_FORM_URL = NetworkUtils.getKnownGoodURL(SCRIPT_BASE, "feedbackreport-post.php");
 
    //MISC.
    public static final URL SAVANT_ROOTREQUEST_URL = NetworkUtils.getKnownGoodURL(BASE_URL+"savant");
       
    
    //public_base: genomesavant.com/medsavant
    //server_base: genomesvant.com/serve    
    //userguide: public_base, learn
    //genemania: server_base, genemania
    //compatibility: server_base, compatibility
    //logusage: server_base, logging
    //bugreport: public_base, bugreport
    //feedbackform: public_base, feedback
    //refgene: server_base, refgene/hg18
    //jannovar: server_base, jannovar/hg19
    //savant: savant_base, medsavant
    
}
