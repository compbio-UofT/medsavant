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
package org.ut.biolab.medsavant.shared.model.solr;

import org.apache.solr.client.solrj.beans.Field;
import org.ut.biolab.medsavant.shared.model.GenomicRegion;

/**
 * Adapter class for mapping Solr documents to GenomicRegion objects.
 */
public class SearcheableGenomicRegion {

    private GenomicRegion region;

    private int regionSetId;
    private String name;
    private String chrom;
    private int start;
    private int end;

    public SearcheableGenomicRegion() { }

    public SearcheableGenomicRegion(GenomicRegion region) {
        this.region = region;
    }

    public GenomicRegion getRegion() {
        this.region = new GenomicRegion(regionSetId, name, chrom,start, end);
        return region;
    }

    @Field("name")
    public void setName(String name) {
        this.name = name;
    }

    @Field("chrom")
    public void setChrom(String chrom) {
        this.chrom = chrom;
    }

    @Field("start")
    public void setStart(int start) {
        this.start = start;
    }

    @Field("end")
    public void setEnd(int end) {
        this.end = end;
    }

    @Field("region_set_id")
    public void setRegionSetId(int regionSetId) {
        this.regionSetId = regionSetId;
    }

    public String getName() {
        return region.getName();
    }

    public String getChrom() {
        return region.getChrom();
    }

    public int getStart() {
        return region.getStart();
    }

    public int getEnd() {
        return region.getEnd();
    }
}
