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
public enum VariantType {
    //BND => complex rearrangement
    SNP, Insertion, Deletion, Various, Unknown, InDel, Complex, HomoRef;

    public static VariantType getVariantType(int type) {
        switch (type) {
            case 0:
                return SNP;
            case 1:
                return Insertion;
            case 2:
                return Deletion;
            case 3:
                return Various;
            case 4:
                return Unknown;
            case 5:
                return InDel;
            case 6:
                return Complex;
            case 7:
                return HomoRef;
            default:
                return Unknown;
        }
    }
    
}
