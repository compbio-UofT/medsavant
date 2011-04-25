/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vcfparse;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mfiume
 */
class VCFHeader {

    private static final int NUM_MANDATORY_FIELDS = 8;
    
    private List<String> genotypeLabels;

    public VCFHeader() {
        genotypeLabels = new ArrayList<String>();
    }

    public static int getNumMandatoryFields() {
        return NUM_MANDATORY_FIELDS;
    }

    void addGenotypeLabel(String label) {
        genotypeLabels.add(label);
    }

    public String getGenotypeLabelForIndex(int index) {

        if (!containsGenotypeInformation()) {
            return null;
        }

        int adjustedIndex = index-getNumMandatoryFields()+1; // +1 for "format" column
        if (adjustedIndex < 0 || adjustedIndex >= genotypeLabels.size()) { return null; }
        else { return genotypeLabels.get(adjustedIndex); }
    }

    public List<String> getGenotypeLabels() {
        return genotypeLabels;
    }

    @Override
    public String toString() {

        String s = "";
        for (String l : genotypeLabels) {
            s += "label:" + l +"|";
        }

        return "VCFHeader{" + "genotypeLabels=" + s + '}';
    }

    public boolean containsGenotypeInformation() {
        return !this.genotypeLabels.isEmpty();
    }

}
