/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
