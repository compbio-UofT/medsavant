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
package org.ut.biolab.medsavant.client.view.variants;

import java.util.ArrayList;
import java.util.List;
import org.broad.igv.feature.genome.Genome.ChromosomeComparator;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import savant.api.data.PointRecord;
import savant.api.data.VariantRecord;
import savant.api.data.VariantType;

/**
 *
 * @author Andrew
 */
public class MergedMedSavantVariantRecord implements VariantRecord {

    private int count;
    private int start_position;
    private int end_position;
    private String chrom;
    private String name = "";
    private VariantType[][] variantTypes;
    private String ref;
    private VariantType defaultVariantType = VariantType.NONE;
    private List<String> altAlleles = new ArrayList<String>();
    private int[][] participantAlts;

    public MergedMedSavantVariantRecord(Object[] base, int participantCount) {
        this.chrom = (String) base[BasicVariantColumns.INDEX_OF_CHROM];
        this.start_position = (Integer) base[BasicVariantColumns.INDEX_OF_START_POSITION];
        this.end_position = (Integer) base[BasicVariantColumns.INDEX_OF_END_POSITION];
        this.ref = (String) base[BasicVariantColumns.INDEX_OF_REF];
        this.count = participantCount;
        this.variantTypes = new VariantType[count][];
        this.participantAlts = new int[count][];
    }

    public void addRecord(Object[] arr, int index) {

        //name
        if (name.length() == 0) {
            this.name = (String) arr[BasicVariantColumns.INDEX_OF_DBSNP_ID];
        }

        //alt alleles
        String[] alts = ((String) arr[BasicVariantColumns.INDEX_OF_ALT]).split(",");
        for (int i = 0; i < alts.length; i++) {
            if (!altAlleles.contains(alts[i])) {
                altAlleles.add(alts[i]);
            }
        }
        String[] gt = ((String) arr[BasicVariantColumns.INDEX_OF_GT]).split("/|\\\\|\\|");
        int[] participantAlt = new int[gt.length];
        VariantType[] participantVariant = new VariantType[gt.length];
        boolean foundNonZero = false;
        for (int i = 0; i < gt.length; i++) {
            Integer x;

            try {
                x = Integer.parseInt(gt[i]);
                if (x == 0) {
                    participantAlt[i] = 0;
                    participantVariant[i] = VariantType.NONE;
                } else {
                    participantAlt[i] = altAlleles.indexOf(alts[x - 1]) + 1;
                    participantVariant[i] = convertVariantType(org.ut.biolab.medsavant.shared.vcf.VariantRecord.VariantType.valueOf((String) arr[BasicVariantColumns.INDEX_OF_VARIANT_TYPE]), alts[x - 1]);
                    defaultVariantType = participantVariant[i];
                    foundNonZero = true;
                }
            // If a genotype is unknown, the record will have the format ./. and
            // a number format exception will be thrown when creating x. In this case
            // we don't know what type of variant this is.
            } catch (Exception e) {
                //e.printStackTrace();
                participantAlt[i] = 0;
                participantVariant[i] = VariantType.OTHER;
            }

        }
        if (foundNonZero) {
            participantAlts[index] = participantAlt;
            variantTypes[index] = participantVariant;
        }

    }

    @Override
    public VariantType getVariantType() {
        return defaultVariantType;
    }

    @Override
    public String getRefBases() {
        return ref;
    }

    @Override
    public String[] getAltAlleles() {
        String[] alts = new String[altAlleles.size()];
        for (int i = 0; i < altAlleles.size(); i++) {
            alts[i] = altAlleles.get(i);
        }
        return alts;
    }

    @Override
    public int getParticipantCount() {
        return count;
    }

    @Override
    public VariantType[] getVariantsForParticipant(int index) {
        if (variantTypes[index] == null) {
            return new VariantType[]{VariantType.NONE};
        }
        return variantTypes[index];
    }

    @Override
    public int[] getAllelesForParticipant(int index) {
        if (participantAlts[index] == null) {
            return new int[]{0};
        }
        return participantAlts[index];
    }

    @Override
    public int getPosition() {
        return start_position;
    }

    public int getStartPosition(){
        return start_position;
    }
    
    public int getEndPosition(){
        return end_position;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getReference() {
        return chrom;
    }

    @Override
    public int compareTo(Object o) {
        if (!(o instanceof PointRecord)) {
            return -1;
        }
        PointRecord other = (PointRecord) o;
        int chromCompare = (new ChromosomeComparator()).compare(chrom, other.getReference());
        if (chromCompare != 0) {
            return chromCompare;
        }
                
                
        return ((Integer) start_position).compareTo(other.getPosition());
    }

    public static VariantType convertVariantType(org.ut.biolab.medsavant.shared.vcf.VariantRecord.VariantType type, String alt) {
        switch (type) {
            case Insertion:
                return VariantType.INSERTION;
            case Deletion:
                return VariantType.DELETION;
            case SNP:
                if (alt != null && alt.length() > 0) {
                    String a = alt.substring(0, 1).toLowerCase();
                    if (a.equals("a")) {
                        return VariantType.SNP_A;
                    } else if (a.equals("c")) {
                        return VariantType.SNP_C;
                    } else if (a.equals("g")) {
                        return VariantType.SNP_G;
                    } else if (a.equals("t")) {
                        return VariantType.SNP_T;
                    }
                }
            default:
                return VariantType.OTHER;
        }
    }

    @Override
    public boolean isPhased() {
        return false;
    }
}
