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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
