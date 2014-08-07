/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.common;

import java.util.List;
import org.medsavant.api.filestorage.MedSavantFile;

public class MedSavantUpdate {    
    private final int updateId;
    private final Reference reference;
    private final MedSavantProject project;
    private final List<MedSavantFile> vcfFiles;  
    private final List<Integer> fileIds;
    
    public MedSavantUpdate(int updateId, List<MedSavantFile> files, List<Integer> fileIds, MedSavantProject project, Reference reference){
        this.vcfFiles = files;
        this.updateId = updateId;
        this.project = project;
        this.reference = reference;
        this.fileIds = fileIds;
    }
       
    public int getUpdateID(){
        return this.updateId;
    }

    public MedSavantProject getProject(){
        return this.project;
    }
    
    public Reference getReference(){
        return this.reference;
    }
    
    public List<MedSavantFile> getVCFFiles(){
        return vcfFiles;
    }   
    
    public List<Integer> getFileIDs(){
        return fileIds;
    }
    
    @Override
    public String toString(){
        return "Update "+updateId+" for project "+project+", reference "+reference+" with "+fileIds.size()+" files";
    }
}