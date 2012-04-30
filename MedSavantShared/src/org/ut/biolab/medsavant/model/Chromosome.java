/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mfiume
 */
public class Chromosome implements Serializable {
    private final long centromerepos;
    private final String name;
    private final String shortname;
    private final long length;

    public Chromosome(String name, String shortname, long centromerepos, long length) {
        this.name = name;
        this.length = length;
        this.centromerepos = centromerepos;
        
        if(shortname == null && name != null){
            this.shortname = generateShortname(name);
        } else {
            this.shortname = shortname;
        }
    }

    public long getCentromerepos() {
        return centromerepos;
    }

    public long getLength() {
        return length;
    }

    public String getName() {
        return name;
    }

    public String getShortname() {
        return shortname;
    }
    
    private String generateShortname(String name){
        return  name.toLowerCase().replace("chr", "").replace("contig", "");      
    }
    
    public static List<Chromosome> getHG17Chromosomes(){
        List<Chromosome> chrs = new ArrayList<Chromosome>();
        chrs.add(new Chromosome("chr1", "1", 125000000, 245522847));
        chrs.add(new Chromosome("chr2", "2", 93300000, 243018229));
        chrs.add(new Chromosome("chr3", "3", 91000000, 199505740));
        chrs.add(new Chromosome("chr4", "4", 50400000, 191411218));
        chrs.add(new Chromosome("chr5", "5", 48400000, 180857866));
        chrs.add(new Chromosome("chr6", "6", 61000000, 170975699));
        chrs.add(new Chromosome("chr7", "7", 59900000, 158628139));
        chrs.add(new Chromosome("chr8", "8", 45600000, 146274826));
        chrs.add(new Chromosome("chr9", "9", 49000000, 138429268));
        chrs.add(new Chromosome("chr10", "10", 40200000, 135413628));
        chrs.add(new Chromosome("chr11", "11", 53700000, 134452384));
        chrs.add(new Chromosome("chr12", "12", 35800000, 132449811));
        chrs.add(new Chromosome("chr13", "13", 17900000, 114142980));
        chrs.add(new Chromosome("chr14", "14", 17600000, 106368585));
        chrs.add(new Chromosome("chr15", "15", 19000000, 100338915));
        chrs.add(new Chromosome("chr16", "16", 36600000, 88827254));
        chrs.add(new Chromosome("chr17", "17", 24000000, 78774742));
        chrs.add(new Chromosome("chr18", "18", 17200000, 76117153));
        chrs.add(new Chromosome("chr19", "19", 26500000, 63811651));
        chrs.add(new Chromosome("chr20", "20", 27500000, 62435964));
        chrs.add(new Chromosome("chr21", "21", 13200000, 46944323));
        chrs.add(new Chromosome("chr22", "22", 14700000, 49554710));
        chrs.add(new Chromosome("chrX", "X", 60600000, 154824264));
        chrs.add(new Chromosome("chrY", "Y", 12500000, 57701691));
        return chrs;
    }
    
    public static List<Chromosome> getHG18Chromosomes(){
        List<Chromosome> chrs = new ArrayList<Chromosome>();
        chrs.add(new Chromosome("chr1", "1", 125000000, 247249719));
        chrs.add(new Chromosome("chr2", "2", 93300000, 242951149));
        chrs.add(new Chromosome("chr3", "3", 91000000, 199501827));
        chrs.add(new Chromosome("chr4", "4", 50400000, 191273063));
        chrs.add(new Chromosome("chr5", "5", 48400000, 180857866));
        chrs.add(new Chromosome("chr6", "6", 61000000, 170899992));
        chrs.add(new Chromosome("chr7", "7", 59900000, 158821424));
        chrs.add(new Chromosome("chr8", "8", 45600000, 146274826));
        chrs.add(new Chromosome("chr9", "9", 49000000, 140273252));
        chrs.add(new Chromosome("chr10", "10", 40200000, 135374737));
        chrs.add(new Chromosome("chr11", "11", 53700000, 134452384));
        chrs.add(new Chromosome("chr12", "12", 35800000, 132349534));
        chrs.add(new Chromosome("chr13", "13", 17900000, 114142980));
        chrs.add(new Chromosome("chr14", "14", 17600000, 106368585));
        chrs.add(new Chromosome("chr15", "15", 19000000, 100338915));
        chrs.add(new Chromosome("chr16", "16", 36600000, 88827254));
        chrs.add(new Chromosome("chr17", "17", 24000000, 78774742));
        chrs.add(new Chromosome("chr18", "18", 17200000, 76117153));
        chrs.add(new Chromosome("chr19", "19", 26500000, 63811651));
        chrs.add(new Chromosome("chr20", "20", 27500000, 62435964));
        chrs.add(new Chromosome("chr21", "21", 13200000, 46944323));
        chrs.add(new Chromosome("chr22", "22", 14700000, 49691432));
        chrs.add(new Chromosome("chrX", "X", 60600000, 154913754));
        chrs.add(new Chromosome("chrY", "Y", 12500000, 57772954));
        return chrs;
    }
    
    public static List<Chromosome> getHG19Chromosomes(){
        List<Chromosome> chrs = new ArrayList<Chromosome>();
        chrs.add(new Chromosome("chr1", "1", 125000000, 249250621));
        chrs.add(new Chromosome("chr2", "2", 93300000, 243199373));
        chrs.add(new Chromosome("chr3", "3", 91000000, 198022430));
        chrs.add(new Chromosome("chr4", "4", 50400000, 191154276));
        chrs.add(new Chromosome("chr5", "5", 48400000, 180915260));
        chrs.add(new Chromosome("chr6", "6", 61000000, 171115067));
        chrs.add(new Chromosome("chr7", "7", 59900000, 159138663));
        chrs.add(new Chromosome("chr8", "8", 45600000, 146364022));
        chrs.add(new Chromosome("chr9", "9", 49000000, 141213431));
        chrs.add(new Chromosome("chr10", "10", 40200000, 135534747));
        chrs.add(new Chromosome("chr11", "11", 53700000, 135006516));
        chrs.add(new Chromosome("chr12", "12", 35800000, 133851895));
        chrs.add(new Chromosome("chr13", "13", 17900000, 115169878));
        chrs.add(new Chromosome("chr14", "14", 17600000, 107349540));
        chrs.add(new Chromosome("chr15", "15", 19000000, 102531392));
        chrs.add(new Chromosome("chr16", "16", 36600000, 90354753));
        chrs.add(new Chromosome("chr17", "17", 24000000, 81195210));
        chrs.add(new Chromosome("chr18", "18", 17200000, 78077248));
        chrs.add(new Chromosome("chr19", "19", 26500000, 59128983));
        chrs.add(new Chromosome("chr20", "20", 27500000, 63025520));
        chrs.add(new Chromosome("chr21", "21", 13200000, 48129895));
        chrs.add(new Chromosome("chr22", "22", 14700000, 51304566));
        chrs.add(new Chromosome("chrX", "X", 60600000, 155270560));
        chrs.add(new Chromosome("chrY", "Y", 12500000, 59373566));
        return chrs;
    }

}
