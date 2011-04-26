/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.controller;

import fiume.vcf.VariantRecord;
import fiume.vcf.VariantSet;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mfiume
 */
public class ResultController {

    private static List<VariantRecord> variants = new ArrayList<VariantRecord>();

    public static List<VariantRecord> getVariantRecords() {
        return variants;
    }

    public static void clearVariants() {
        variants = new ArrayList<VariantRecord>();
    }

    public static void addVariantSet(VariantSet s) {
        variants.addAll(s.getRecords());
    }

}
