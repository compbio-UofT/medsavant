/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.medsavant.api.common;

/**
 *
 * @author jim
 */
public class VariantUtils {

    /*
     * Return string without sequence title (chr, contig)
     */
    public static String homogenizeSequence(String s) {
        String result = s;
        if (result.contains("chr")) {
            result = result.replaceAll("chr", "");
        }
        if (result.contains("Chr")) {
            result = result.replaceAll("Chr", "");
        }
        if (result.contains("contig")) {
            result = result.replaceAll("contig", "");
        }
        if (result.contains("Contig")) {
            result = result.replaceAll("Contig", "");
        }
        return result;
    }
}
