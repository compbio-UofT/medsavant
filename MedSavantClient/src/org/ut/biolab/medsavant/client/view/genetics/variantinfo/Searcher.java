package org.ut.biolab.medsavant.client.view.genetics.variantinfo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

/**
 *
 * @author mfiume
 */
class Searcher {

    static String pubmedPrefix = "http://www.ncbi.nlm.nih.gov/pubmed?term=";
    static String googlePrefix = "http://www.google.com/search?hl=en&q=";
    static String googleScholarPrefix = "http://scholar.google.com/scholar?q=";

    static String charset = "UTF-8";

    static void searchGoogle(String search) throws UnsupportedEncodingException, MalformedURLException, IOException, URISyntaxException {
        URL url = new URL(googlePrefix + URLEncoder.encode(search, charset));
        java.awt.Desktop.getDesktop().browse(url.toURI());
    }

    static void searchGoogleScholar(String search) throws UnsupportedEncodingException, MalformedURLException, IOException, URISyntaxException {
        URL url = new URL(googleScholarPrefix + URLEncoder.encode(search, charset));
        java.awt.Desktop.getDesktop().browse(url.toURI());
    }

    static void searchPubmed(String search) throws UnsupportedEncodingException, MalformedURLException, IOException, URISyntaxException {
        URL url = new URL(pubmedPrefix + URLEncoder.encode(search, charset));
        java.awt.Desktop.getDesktop().browse(url.toURI());
    }
}
