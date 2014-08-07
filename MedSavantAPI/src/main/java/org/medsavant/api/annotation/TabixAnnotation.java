/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.annotation;

import org.medsavant.api.filestorage.MedSavantFile;

/**
 * Describes a tabix annotation file.  
 * @author jim
 */
public interface TabixAnnotation extends MedSavantAnnotation{        
    //Tabix specific
    public String getFieldDelimiter();    
    public MedSavantFile getTabixFile(); 
    public MedSavantFile getTabixFileIndex();
    public int getNumNonDefaultFields();
    public int getNumDefaultFields(boolean hasEndPosition);
    public boolean hasRef();
    public boolean hasAlt();
    public boolean isEndInclusive();
    public boolean isPositionAnnotation();
    public boolean isIntervalAnnotation();     
}
