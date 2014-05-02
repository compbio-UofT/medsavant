/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.app.google.original;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.genomics.Genomics;
import com.google.api.services.genomics.Genomics.Datasets;
import com.google.api.services.genomics.model.Dataset;
import com.google.api.services.genomics.model.ListDatasetsResponse;
import com.google.api.services.genomics.model.Read;
import com.google.api.services.genomics.model.SearchReadsRequest;
import com.google.api.services.genomics.model.SearchReadsResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import savant.api.adapter.RangeAdapter;
import savant.api.util.Resolution;
import savant.data.types.BAMIntervalRecord;
import savant.util.Range;

/**
 *
 * @author mfiume
 */
public class GoogleReadSearch {

    private final Genomics genomics;

    public GoogleReadSearch(Genomics genomics) {
        this.genomics = genomics;
    }

    SearchReadsResponse searchReads(List<String> datasetIDs, List<String> readsetIds, String sequenceName, int sequenceStart, int sequenceEnd, String pageToken)
            throws IOException, IllegalArgumentException {
        
        SearchReadsRequest content = new SearchReadsRequest()
                .setDatasetIds(datasetIDs)
                .setReadsetIds(readsetIds);
        
        System.out.println("Searching reads for " + content.getDatasetIds().get(0) + " " + content.getReadsetIds().get(0));

        // Range parameters must all be specified or none.
        if (!sequenceName.isEmpty() || sequenceStart > 0 || sequenceEnd > 0) {
            assertOrThrow(!sequenceName.isEmpty(), "Must specify a sequence_name");
            assertOrThrow(sequenceStart > 0, "sequence_start must be greater than 0");
            // getting this far implies target_start is greater than 0
            assertOrThrow(sequenceEnd >= sequenceStart, "sequence_end must be greater than sequence_start");

            content.setSequenceName(sequenceName)
                    .setSequenceStart(BigInteger.valueOf(sequenceStart))
                    .setSequenceEnd(BigInteger.valueOf(sequenceEnd)).setPageToken(pageToken);
        }
        
        return genomics.reads().search(content).execute();
    }

    private static void assertOrThrow(boolean condition, String headline) throws IllegalArgumentException {
        if (!condition) {
            throw new IllegalArgumentException(headline);
        }
    }

    public static void main(String[] argv) throws IOException, GeneralSecurityException, InterruptedException {
        //GoogleBAMDataSource ds = new GoogleBAMDataSource("383928317087", "Sample", "CJ_ppJ-WCxDxrtDr5fGIhBA");
        System.out.println("SEARCHING READS");
        GoogleBAMDataSource ds = new GoogleBAMDataSource("461916304629","Sample","CPXp7eK4DRCchIy-5774w0k");
        List<BAMIntervalRecord> results = ds.getRecords("chr20", new Range(68198,69000), Resolution.HIGH, null);
        for (BAMIntervalRecord r : results) {
            System.out.println(r);
        }
    }

}
