package org.ut.biolab.medsavant.client.app;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.mfiume.app.api.AppInfoFetcher;
import org.ut.biolab.mfiume.app.AppInfo;

/**
 *
 * @author mfiume
 */
public class MedSavantAppFetcher implements AppInfoFetcher {

    @Override
    public List<AppInfo> fetchApplicationInformation(String search) throws Exception {

        AppInfo mendelAppInfo = new AppInfo(
                "Mendel",
                "1.0.0",
                "Analysis",
                "1.0.1",
                "Mendel adds the ability to perform case control analysis and to implement inheritance models.",
                "University of Toronto Biolab",
                "http://genomesavant.com",
                new URL("http://www.genomesavant.com/p/medsavant/serve/plugin/medsavant.mendel-1.0.0.jar"));

        AppInfo mendelAppInfo2 = new AppInfo(
                "Enrichment",
                "1.0.0",
                "Statistics",
                "1.0.1",
                "Enrichment adds the ability to aggregate variants by user-specified gene-lists, and terms in the Gene Ontology, Human Phenotype Ontology, and OMIM. Aggregation is a basic form of enrichment testing, and facilitates the process of identifying biological functions that are significantly affected within the sequenced population and to ultimately learn and understand the genetic mechanisms of their diseases.",
                "University of Toronto Biolab",
                "http://genomesavant.com",
                new URL("http://www.genomesavant.com/p/medsavant/serve/plugin/medsavant.enrichment-1.0.0.jar"));

        List<AppInfo> results = new ArrayList<AppInfo>();
        results.add(mendelAppInfo);
        results.add(mendelAppInfo2);
        return results;
    }
}
