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
public enum Zygosity {
    HomoRef, HomoAlt, Hetero, HeteroTriallelic, Missing;

    public static Zygosity getZygosity(int zygosity) {
        switch (zygosity) {
            case 0:
                return HomoRef;
            case 1:
                return HomoAlt;
            case 2:
                return Hetero;
            case 3:
                return HeteroTriallelic;
            case 4:
                return Missing;
            default:
                return HomoRef;
        }
    }
    
}
