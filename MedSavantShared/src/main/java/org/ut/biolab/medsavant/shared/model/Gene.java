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
package org.ut.biolab.medsavant.shared.model;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * All the information contained in a single record from a gene-set.  In it's current incarnation, these correspond to transcripts
 * rather than genes.
 *
 * @author tarkvara
 */
public class Gene implements Serializable, Comparable<Gene> {
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

    /**
     * In many cases, we're just using the gene for filtering, so we don't care about the coding or the transcript name.
     *
     * @param name gene name
     * @param chrom chromosome name, of the form "chr1"
     * @param start start position within <code>chrom</code>
     * @param end end position within <code>chrom</code>
     */
    public Gene(String name, String chrom, int start, int end) {
        this(name, chrom, start, end, -1, -1, null);
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

    public String getDescription() throws MalformedURLException, IOException{
        String baseURLp1 = "http://www.genenames.org/cgi-bin/quick_search.pl?.cgifields=type&type=equal&num=50&search=";
        String baseURLp2 = "&submit=Submit";
        URL url = new URL (baseURLp1 + name +baseURLp2);
        Document doc = Jsoup.parse(url, 20*1000);
        Elements tableClass = doc.select("table.quick_search");
        Element e = tableClass.select("tr:has(td)").first();
        String description = e.select("td:has(a) + td").text();
        return description;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Gene t) {
        int result = name.compareTo(t.name);
        if (result == 0) {
            result = chrom.compareTo(t.chrom);
            if (result == 0) {
                result = start - t.start;
                if (result == 0) {
                    result = end - t.end;
                    if (result == 0 && transcript != null) {
                        result = transcript.compareTo(t.transcript);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object that) {
        if (that instanceof Gene) {
            return compareTo((Gene)that) == 0;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 11 * hash + (this.chrom != null ? this.chrom.hashCode() : 0);
        hash = 11 * hash + this.start;
        hash = 11 * hash + this.end;
        hash = 11 * hash + (this.transcript != null ? this.transcript.hashCode() : 0);
        return hash;
    }


}
