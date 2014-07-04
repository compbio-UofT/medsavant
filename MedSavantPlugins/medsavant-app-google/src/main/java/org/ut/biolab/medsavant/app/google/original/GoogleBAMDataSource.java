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

import com.google.api.services.genomics.Genomics;
import com.google.api.services.genomics.model.Read;
import com.google.api.services.genomics.model.SearchReadsResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMRecord;
import savant.api.adapter.BAMDataSourceAdapter;
import savant.api.adapter.BookmarkAdapter;
import savant.api.adapter.DataSourceAdapter;
import savant.api.adapter.RangeAdapter;
import savant.api.adapter.RecordFilterAdapter;
import savant.api.data.DataFormat;
import savant.api.util.Resolution;
import savant.data.types.BAMIntervalRecord;
import savant.util.MiscUtils;

/**
 *
 * @author mfiume
 */
public class GoogleBAMDataSource implements DataSourceAdapter<BAMIntervalRecord>, BAMDataSourceAdapter {

    private final GoogleReadSearch search;
    private final ArrayList<String> readsetIds;
    private final String readSetName;

    public GoogleBAMDataSource(String readSetName, String readSetID) throws IOException, GeneralSecurityException {
        
        Genomics genomics = GoogleAuthenticate.buildService();
        search = new GoogleReadSearch(genomics);

        this.readSetName = readSetName;
        
        readsetIds = new ArrayList<String>();
        readsetIds.add(readSetID);
    }

    @Override
    public Set<String> getReferenceNames() {
        Set<String> chroms = new HashSet<String>();

        for (int i = 1; i < 22; i++) {
            chroms.add("chr" + i); // TODO: actually look this up
        }
        chroms.add("chrX");
        chroms.add("chrY");
        chroms.add("chrM");

        return chroms;
    }

    private enum HomogenizeState { UNKNOWN, DONT_HOMOGENIZE, DO_HOMOGENIZE };
    private HomogenizeState homogenizeSequence = HomogenizeState.UNKNOWN;
    
    @Override
    public List<BAMIntervalRecord> getRecords(String ref, RangeAdapter range, Resolution resolution, RecordFilterAdapter<BAMIntervalRecord> filter) throws IOException, InterruptedException {

        
        List<BAMIntervalRecord> results = new ArrayList<BAMIntervalRecord>();
        String pageToken = null;
        
        // don't try to get records for large window sizes
        if (range.getLength() > 5000) {
            return results;
        }

        int iteration = 0;
        
        while (true) {
            
            iteration++;
            
            // homogenize the sequence if it was determined to help
            if (homogenizeSequence == HomogenizeState.DO_HOMOGENIZE) {
                ref = MiscUtils.homogenizeSequence(ref);
            }
            
            SearchReadsResponse searchReadsResponse = search.searchReads(readsetIds, ref, range.getFrom(), range.getTo(), pageToken);
            pageToken = searchReadsResponse.getNextPageToken();
            List<Read> reads = searchReadsResponse.getReads();

            // test homogenization
            if (iteration == 1 && homogenizeSequence == HomogenizeState.UNKNOWN) {
                
                // homogenization may work, if the alternative didn't
                if (reads == null || reads.isEmpty()) {
                    ref = MiscUtils.homogenizeSequence(ref);
                    searchReadsResponse = search.searchReads(readsetIds, ref, range.getFrom(), range.getTo(), pageToken);
                    pageToken = searchReadsResponse.getNextPageToken();
                    reads = searchReadsResponse.getReads();
                    
                    if (reads != null && !reads.isEmpty()) {
                        homogenizeSequence = HomogenizeState.DO_HOMOGENIZE;
                    }
                    
                // don't homogenize, we already got reads
                } else {
                    homogenizeSequence = HomogenizeState.DONT_HOMOGENIZE;
                }
            }
            
            if (reads == null || reads.isEmpty()) {
                break;
            } else {
                for (Read r : reads) {
                    SAMRecord sam = readToSAM(r);
                    BAMIntervalRecord record = BAMIntervalRecord.valueOf(sam);
                    results.add(record);
                }
            }
            
            if (pageToken == null || pageToken.isEmpty()) {
                break;
            }
        }

        return results;
    }

    @Override
    public URI getURI() {
        try {
            return new URI("gs://" + readSetName);
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public String getName() {
        return "Google Genomics Readset (" + readSetName + ")";
    }

    @Override
    public void close() {

    }

    @Override
    public DataFormat getDataFormat() {
        return DataFormat.ALIGNMENT;
    }

    @Override
    public String[] getColumnNames() {
        return new String[]{"Read Name", "Sequence", "Length", "First of Pair", "Position", "Strand +", "Mapping Quality", "Base Qualities", "CIGAR", "Mate Position", "Strand +", "Inferred Insert Size"};
    }

    @Override
    public void loadDictionary() throws IOException {
    }

    @Override
    public List<BookmarkAdapter> lookup(String key) {
        List<BookmarkAdapter> empty = new ArrayList<BookmarkAdapter>();
        return empty;
    }

    @Override
    public SAMFileHeader getHeader() {
        return new SAMFileHeader(); // TODO: fill in
    }

    private SAMRecord readToSAM(Read r) {
        SAMRecord sam = new SAMRecord(getHeader());

        sam.setReadName(r.getName());
        sam.setFlags(r.getFlags());
        sam.setReferenceName(r.getReferenceSequenceName());
        sam.setAlignmentStart(r.getPosition()); // ?
        sam.setCigarString(r.getCigar());
        sam.setMateAlignmentStart(r.getMatePosition());
        sam.setMateReferenceName(r.getMateReferenceSequenceName());
        sam.setMappingQuality(r.getMappingQuality());
        sam.setReadString(r.getOriginalBases()); // ?
        sam.setBaseQualityString(r.getBaseQuality());

        return sam;
    }
}
