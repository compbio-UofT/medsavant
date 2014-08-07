/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.annotation;

import org.medsavant.api.variantstorage.MedSavantField;

/**
 *
 * @author jim
 */
public interface MedSavantAnnotation {
    public String getProgram();
    public String getVersion();    
    public String getReferenceName();    
    public MedSavantField[] getFields();
    @Override
    public int hashCode();
    @Override
    public boolean equals(Object o);
}
