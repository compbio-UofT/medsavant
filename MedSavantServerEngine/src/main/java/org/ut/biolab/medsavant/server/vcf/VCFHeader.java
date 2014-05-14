/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 * 
 * All rights reserved. No warranty, explicit or implicit, provided.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
 * FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.ut.biolab.medsavant.server.vcf;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mfiume
 */
public class VCFHeader {

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
