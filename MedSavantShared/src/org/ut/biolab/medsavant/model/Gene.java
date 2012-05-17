/*
 *    Copyright 2012 University of Toronto
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
package org.ut.biolab.medsavant.model;

import java.io.Serializable;

/**
 * All the information contained in a single record from a gene-set.  In it's current incarnation, these correspond to transcripts
 * rather than genes.
 *
 * @author tarkvara
 */
public class Gene implements Serializable {
    private final String name;
    private final String chrom;
    private final int start;
    private final int end;
    private final int codingStart;
    private final int codingEnd;
    private final String transcript;

    public Gene(String name, String chrom, int start, int end, int codingStart, int codingEnd, String transcript) {
        this.name = name;
        this.chrom = chrom;
        this.start = start;
        this.end = end;
        this.codingStart = codingStart;
        this.codingEnd = codingEnd;
        this.transcript = transcript;
    }

    public String getName() {
        return name;
    }

    public String getChrom() {
        return chrom;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getCodingStart() {
        return codingStart;
    }

    public int getCodingEnd() {
        return codingEnd;
    }

    public String getTranscript() {
        return transcript;
    }

    @Override
    public String toString() {
        return name;
    }


}
