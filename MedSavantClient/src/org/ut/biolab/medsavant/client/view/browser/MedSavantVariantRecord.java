/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.browser;

import org.broad.igv.feature.genome.Genome.ChromosomeComparator;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import savant.api.data.PointRecord;
import savant.api.data.VariantType;

/**
 *
 * @author Andrew
 */
public class MedSavantVariantRecord implements savant.api.data.VariantRecord {

    private String chrom;
    private int position;
    private String ref;
    private String alt;
    private org.ut.biolab.medsavant.shared.vcf.VariantRecord.VariantType type;
    private int index;
    private String genotype;
    private String name;

    public MedSavantVariantRecord(Object[] arr, int index) {
        TableSchema sc = ProjectController.getInstance().getCurrentVariantTableSchema();

        this.chrom = (String)arr[BasicVariantColumns.INDEX_OF_CHROM];
        this.position = (Integer)arr[BasicVariantColumns.INDEX_OF_POSITION];
        this.ref = (String)arr[BasicVariantColumns.INDEX_OF_REF];
        this.alt = (String)arr[BasicVariantColumns.INDEX_OF_ALT];
        this.type = org.ut.biolab.medsavant.shared.vcf.VariantRecord.VariantType.valueOf((String)arr[BasicVariantColumns.INDEX_OF_VARIANT_TYPE]);
        this.genotype = (String)arr[BasicVariantColumns.INDEX_OF_GT];
        this.name = (String)arr[BasicVariantColumns.INDEX_OF_DBSNP_ID];
        this.index = index;
    }

    @Override
    public VariantType getVariantType() {
        switch(type){
            case Insertion:
                return VariantType.INSERTION;
            case Deletion:
                return VariantType.DELETION;
            case SNP:
                if(alt != null && alt.length() > 0){
                    String a = alt.substring(0, 1).toLowerCase();
                    if(a.equals("a")){
                        return VariantType.SNP_A;
                    } else if (a.equals("c")){
                        return VariantType.SNP_C;
                    } else if (a.equals("g")){
                        return VariantType.SNP_G;
                    } else if (a.equals("t")){
                        return VariantType.SNP_T;
                    }
                }
            default:
                return VariantType.OTHER;
        }
    }

    @Override
    public String getRefBases() {
        return ref;
    }

    @Override
    public String[] getAltAlleles() {
        return alt.split(",");
    }

    @Override
    public int getParticipantCount() {
        return 1;
    }

    @Override
    public VariantType[] getVariantsForParticipant(int index) {
        if(index == this.index){
            return new VariantType[]{getVariantType()};
        } else {
            return new VariantType[]{VariantType.NONE};
        }
    }

    @Override
    public int[] getAllelesForParticipant(int index) {
        if(index == this.index){
            String[] gt = genotype.split("/|\\\\|\\|");
            if(gt.length != 2){
                return new int[]{0};
            }
            int a = Integer.parseInt(gt[0]);
            int b = Integer.parseInt(gt[1]);
            if(a == b){
                return new int[]{a};
            } else {
                return new int[]{a,b};
            }
        } else {
            return new int[]{0};
        }
    }

    @Override
    public int getPosition() {
        return position;
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
        if(!(o instanceof PointRecord)) return -1;
        PointRecord other = (PointRecord)o;
        int chromCompare = (new ChromosomeComparator()).compare(chrom, other.getReference());
        if(chromCompare != 0) return chromCompare;
        return ((Integer)position).compareTo(other.getPosition());
    }

    public String toString(){
        return getName();
    }

    @Override
    public boolean isPhased() {
        return false; // TODO: report this value from database (if stored)
    }

}
