/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.controller;

import fiume.vcf.VCFParser;
import fiume.vcf.VariantRecord;
import fiume.vcf.VariantSet;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.PostProcessFilter;
import org.ut.biolab.medsavant.model.record.FileRecord;

/**
 *
 * @author mfiume
 */
public class ResultController {

    //private static List<VariantRecord> variants = new ArrayList<VariantRecord>();

    /*
    public static List<VariantRecord> getVariantRecords() {
        return variants;
    }
     * 
     */

    public static List<VariantRecord> getAllVariantRecords() {
        List<VariantRecord> results = new ArrayList<VariantRecord>();
        for (FileRecord f : LibraryVariantsController.getInstance().getFileRecords()) {
            try {
                VariantSet set = VCFParser.parseVariants(new File(f.getFileName()));
                results.addAll(set.getRecords());
            } catch (IOException ex) {
            }
        }
        return results;
    }

    public static List<VariantRecord> getFilteredVariantRecords() {
        List<PostProcessFilter> filters = FilterController.getPostProcessFilters();
        List<VariantRecord> results = getAllVariantRecords();
        for (PostProcessFilter f : filters) {
            results = f.filterResults(results);
        }
        return results;
    }

    public static List<VariantRecord> getVariantRecords(List<FileRecord> files) {
        List<VariantRecord> results = new ArrayList<VariantRecord>();
        for (FileRecord f : files) {
            try {
                VariantSet set = VCFParser.parseVariants(new File(f.getFileName()));
                results.addAll(set.getRecords());
            } catch (IOException ex) {
            }
        }
        return results;
    }

    /*
    public static void clearVariants() {
        variants = new ArrayList<VariantRecord>();
    }
     * 
     */

    /*
    public static void addVariantSet(VariantSet s) {
        variants.addAll(s.getRecords());
    }
     *
     */

}
