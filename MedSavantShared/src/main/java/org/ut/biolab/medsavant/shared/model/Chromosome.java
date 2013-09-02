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

package org.ut.biolab.medsavant.shared.model;

import java.io.Serializable;


/**
 *
 * @author mfiume
 */
public class Chromosome implements Serializable {
    private final int centromerePos;
    private final String name;
    private final int length;
    private int referenceId;
    private int contigId;

    public Chromosome(int centromerePos, String name, int length, int referenceId, int contigId) {
        this.centromerePos = centromerePos;
        this.name = name;
        this.length = length;
        this.referenceId = referenceId;
        this.contigId = contigId;
    }

    public Chromosome(String name, int centromerePos, int length) {
        this.name = name;
        this.length = length;
        this.centromerePos = centromerePos;
        contigId = 0;
        referenceId = 0;
    }

    public int getCentromerePos() {
        return centromerePos;
    }

    public int getLength() {
        return length;
    }

    public String getName() {
        return name;
    }

    public int getReferenceId() {
        return referenceId;
    }

    public int getContigId() {
        return contigId;
    }

    public String getShortname() {
        return generateShortName(getName());
    }

    private String generateShortName(String name){
        return name.toLowerCase().replace("chr","").replace("contig", "").toUpperCase();
    }

    public void setReferenceId(int referenceId) {
        this.referenceId = referenceId;
    }

    public void setContigId(int contigId) {
        this.contigId = contigId;
    }

    public static Chromosome[] getHG17Chromosomes(){
        return new Chromosome[] {
            new Chromosome("chr1", 125000000, 245522847),
            new Chromosome("chr2", 93300000, 243018229),
            new Chromosome("chr3", 91000000, 199505740),
            new Chromosome("chr4", 50400000, 191411218),
            new Chromosome("chr5", 48400000, 180857866),
            new Chromosome("chr6", 61000000, 170975699),
            new Chromosome("chr7", 59900000, 158628139),
            new Chromosome("chr8", 45600000, 146274826),
            new Chromosome("chr9", 49000000, 138429268),
            new Chromosome("chr10", 40200000, 135413628),
            new Chromosome("chr11", 53700000, 134452384),
            new Chromosome("chr12", 35800000, 132449811),
            new Chromosome("chr13", 17900000, 114142980),
            new Chromosome("chr14", 17600000, 106368585),
            new Chromosome("chr15", 19000000, 100338915),
            new Chromosome("chr16", 36600000, 88827254),
            new Chromosome("chr17", 24000000, 78774742),
            new Chromosome("chr18", 17200000, 76117153),
            new Chromosome("chr19", 26500000, 63811651),
            new Chromosome("chr20", 27500000, 62435964),
            new Chromosome("chr21", 13200000, 46944323),
            new Chromosome("chr22", 14700000, 49554710),
            new Chromosome("chrX", 60600000, 154824264),
            new Chromosome("chrY", 12500000, 57701691)
        };
    }

    public static Chromosome[] getHG18Chromosomes(){
        return new Chromosome[] {
            new Chromosome("chr1", 125000000, 247249719),
            new Chromosome("chr2", 93300000, 242951149),
            new Chromosome("chr3", 91000000, 199501827),
            new Chromosome("chr4", 50400000, 191273063),
            new Chromosome("chr5", 48400000, 180857866),
            new Chromosome("chr6", 61000000, 170899992),
            new Chromosome("chr7", 59900000, 158821424),
            new Chromosome("chr8", 45600000, 146274826),
            new Chromosome("chr9", 49000000, 140273252),
            new Chromosome("chr10", 40200000, 135374737),
            new Chromosome("chr11", 53700000, 134452384),
            new Chromosome("chr12", 35800000, 132349534),
            new Chromosome("chr13", 17900000, 114142980),
            new Chromosome("chr14", 17600000, 106368585),
            new Chromosome("chr15", 19000000, 100338915),
            new Chromosome("chr16", 36600000, 88827254),
            new Chromosome("chr17", 24000000, 78774742),
            new Chromosome("chr18", 17200000, 76117153),
            new Chromosome("chr19", 26500000, 63811651),
            new Chromosome("chr20", 27500000, 62435964),
            new Chromosome("chr21", 13200000, 46944323),
            new Chromosome("chr22", 14700000, 49691432),
            new Chromosome("chrX", 60600000, 154913754),
            new Chromosome("chrY", 12500000, 57772954)
        };
    }

    public static Chromosome[] getHG19Chromosomes(){
        return new Chromosome[] {
            new Chromosome("chr1", 125000000, 249250621),
            new Chromosome("chr2", 93300000, 243199373),
            new Chromosome("chr3", 91000000, 198022430),
            new Chromosome("chr4", 50400000, 191154276),
            new Chromosome("chr5", 48400000, 180915260),
            new Chromosome("chr6", 61000000, 171115067),
            new Chromosome("chr7", 59900000, 159138663),
            new Chromosome("chr8", 45600000, 146364022),
            new Chromosome("chr9", 49000000, 141213431),
            new Chromosome("chr10", 40200000, 135534747),
            new Chromosome("chr11", 53700000, 135006516),
            new Chromosome("chr12", 35800000, 133851895),
            new Chromosome("chr13", 17900000, 115169878),
            new Chromosome("chr14", 17600000, 107349540),
            new Chromosome("chr15", 19000000, 102531392),
            new Chromosome("chr16", 36600000, 90354753),
            new Chromosome("chr17", 24000000, 81195210),
            new Chromosome("chr18", 17200000, 78077248),
            new Chromosome("chr19", 26500000, 59128983),
            new Chromosome("chr20", 27500000, 63025520),
            new Chromosome("chr21", 13200000, 48129895),
            new Chromosome("chr22", 14700000, 51304566),
            new Chromosome("chrX", 60600000, 155270560),
            new Chromosome("chrY", 12500000, 59373566)
        };
    }

}
