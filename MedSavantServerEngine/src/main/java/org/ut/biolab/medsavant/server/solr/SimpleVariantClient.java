package org.ut.biolab.medsavant.server.solr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.*;
import java.net.URLEncoder;
import java.util.Random;

public class SimpleVariantClient {

    private static HttpClient httpClient = new DefaultHttpClient();

    private static final String SOLR_HOST = "http://localhost:8983/solr/";

    private static final String CORE = "variant";

    private static final String SEARCH_HANDLER = "/select";

    private static final Log LOG = LogFactory.getLog(SimpleVariantClient.class);

    public long generateAndExecuteSimpleQuery() {

        String id = getRandomParameter(VariantTestConstants.ids);
        String dna_id = getRandomParameter(VariantTestConstants.dna_ids);
        String alt = getRandomParameter(VariantTestConstants.alts);
        String chrom = getRandomParameter(VariantTestConstants.chroms);
        String ref = getRandomParameter(VariantTestConstants.refs);
        String zygosity = getRandomParameter(VariantTestConstants.zygosities);

        SimpleSolrQuery simpleSolrQuery = new SimpleSolrQuery();
        simpleSolrQuery.addQueryTerm("id", id);
        simpleSolrQuery.addQueryTerm("dna_id", dna_id);
        simpleSolrQuery.addQueryTerm("alt", alt);
        simpleSolrQuery.addQueryTerm("chrom", chrom);
        simpleSolrQuery.addQueryTerm("ref", ref);
        simpleSolrQuery.addFilterQueryTerm("zygosity", zygosity);

        return executeQueryForTime(simpleSolrQuery);
    }


    /**
     * Execute the query and return the content of the HTTP response as a String.
     * @param query
     * @return
     */
    public String executeQuery(SimpleSolrQuery query) {

        String queryString = query.getFullSolrQuery();

        HttpUriRequest httpGet = new HttpGet(SOLR_HOST + CORE + SEARCH_HANDLER + "?" + queryString);

        HttpResponse httpResponse = null;
        String responseString = null;

        try {
            httpResponse = httpClient.execute(httpGet);

            responseString = getResponseContentAsString(httpResponse);

        } catch (IOException e) {
            LOG.error("Failed to execute the Query", e);
        }

        return responseString;
    }

    /**
     * Return the time in milliseconds needed to execute the query.
     * @param query
     * @return          -1 if there was an error or a positive number representing the number of milliseconds needed to
     *                  execute the query if no error occured.
     */
    public long executeQueryForTime(SimpleSolrQuery query) {

        String queryString = query.getFullSolrQuery();

        String safeURL = SOLR_HOST + CORE + SEARCH_HANDLER + "?" + queryString;

        //FIXME this is ugly
        try {
            safeURL = URLEncoder.encode(safeURL , "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error("Failed to safely encode request string");
        }

        HttpUriRequest httpGet = new HttpGet("(" + safeURL + ")");

        HttpResponse httpResponse = null;
        String responseString = null;

        long start = System.currentTimeMillis();
        long end;
        long duration = -1;

        try {

            httpResponse = httpClient.execute(httpGet);


            getResponseContentAsString(httpResponse);

            end = System.currentTimeMillis();
            duration = start - end;

        } catch (IOException e) {
            LOG.error("Failed to execute the Query", e);
        }

        return duration;
    }


    /**
     * Get the content of a HttpResponse object as a String.
     *
     * @param httpResponse  A HttpResponse object.
     * @return              String representation of the content.
     */
    private static String getResponseContentAsString(HttpResponse httpResponse) {

        InputStream httpResponseInputStream = null;

        String contentString = null;

        try {
            httpResponseInputStream = httpResponse.getEntity().getContent();

            contentString =  inputStreamToString(httpResponseInputStream);
        } catch (IOException e) {
            LOG.error("Failed to retrieve content of HTTP response", e);
        }

        return contentString;
    }

    /**
     * Convert an input stream to a String representation.
     *
     * @param inputStream   The InputStream
     * @return              The String representation of the input stream.
     * @throws IOException
     */
    private static String inputStreamToString(InputStream inputStream) throws IOException {

        String line;

        StringBuilder sb = new StringBuilder();

        BufferedReader br  = new BufferedReader( new InputStreamReader(inputStream));
        while ( (line = br.readLine() ) != null) {
            sb.append(line);
        }

        return sb.toString();
    }

    private String getRandomParameter(String[] vector) {
        Random random = new Random();

        int index = random.nextInt(vector.length);

        return vector[index];
    }





}
