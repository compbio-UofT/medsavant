/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 * 
 * All rights reserved. No warranty, explicit or implicit, provided.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
 * FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
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

    SearchReadsResponse searchReads(List<String> readsetIds, String sequenceName, int sequenceStart, int sequenceEnd, String pageToken)
            throws IOException, IllegalArgumentException {
        
        SearchReadsRequest content = new SearchReadsRequest()
                .setReadsetIds(readsetIds);
        
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
        GoogleBAMDataSource ds = new GoogleBAMDataSource("Sample", "CJDmkYn8ChCcnc7i4KaWqmQ");
        System.out.println("SEARCHING READS");
        //GoogleBAMDataSource ds = new GoogleBAMDataSource("Sample","CPXp7eK4DRCchIy-5774w0k");
        List<BAMIntervalRecord> results = ds.getRecords("chr20", new Range(68198,69000), Resolution.HIGH, null);
        for (BAMIntervalRecord r : results) {
            System.out.println(r);
        }
    }

}
