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
import org.ut.biolab.medsavant.shared.model.Gene;

/**
 * Adapter class for mapping Solr documents to Gene objects.
 */
public class SearcheableGene {

    private String name;
    private String chrom;
    private int start;
    private int end;
    private int codingStart;
    private int codingEnd;
    private int exonStart;
    private int exonEnd;
    private String transcript;
    private Gene gene;

    public SearcheableGene() {  }

    public SearcheableGene(Gene gene) {
        this.gene = gene;
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

    @Field("coding_start")
    public void setCodingStart(int codingStart) {
        this.codingStart = codingStart;
    }

    @Field("coding_end")
    public void setCodingEnd(int codingEnd) {
        this.codingEnd = codingEnd;
    }

    @Field("transcript")
    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    @Field("exon_start")
    public void setExonStart(int exonStart) {
        this.exonStart = exonStart;
    }

    @Field("exon_end")
    public void setExonEnd(int exonEnd) {
        this.exonEnd = exonEnd;
    }

    public String getName() {
        return gene.getName();
    }

    public String getChrom() {
        return gene.getChrom();
    }

    public int getStart() {
        return gene.getStart();
    }

    public int getEnd() {
        return gene.getEnd();
    }

    public int getCodingStart() {
        return gene.getCodingStart();
    }

    public int getCodingEnd() {
        return gene.getCodingEnd();
    }

    public String getTranscript() {
        return gene.getTranscript();
    }

    public int getExonStart() {
        return gene.getExonStart();
    }

    public int getExonEnd() {
        return gene.getExonEnd();
    }

    public Gene getGene() {
        this.gene = new Gene(name, chrom,start,end,codingStart,codingEnd,transcript,exonStart, exonEnd);
        return gene;
    }
}
