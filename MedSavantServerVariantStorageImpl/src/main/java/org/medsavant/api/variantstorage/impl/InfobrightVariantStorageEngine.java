/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.medsavant.api.variantstorage.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.medsavant.api.annotation.TabixAnnotation;
import org.medsavant.api.common.GenomicVariant;
import org.medsavant.api.variantstorage.GenomicVariantRecord;
import org.medsavant.api.variantstorage.PublicationStatus;
import org.medsavant.api.variantstorage.VariantField;
import org.medsavant.api.variantstorage.VariantFilterBuilder.VariantFilter;
import org.medsavant.api.variantstorage.MedSavantVariantStorageEngine;
import org.medsavant.api.vcfstorage.VCFFileOld;

/**
 *
 * @author jim
 */
public class InfobrightVariantStorageEngine implements MedSavantVariantStorageEngine {

    public int countVariants(VariantFilter filter) {        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int countVariantsInFile(VariantFilter filter, Collection<VCFFileOld> files) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public VCFFileOld exportVariants(VariantFilter filter, boolean orderedByPosition, boolean compressOutput) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public List<GenomicVariant> getVariants(VariantFilter filter, long offset, int limit, VariantField[] orderBy) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Map<String[], Integer> getHistogram(VariantFilter filter, VariantField[] field, int[] numBins) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int countPatientsWithVariants(VariantFilter filter) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setVariantStatus(int projectId, int referenceId, int updateID, PublicationStatus status) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int addVariants(int projectId, int referenceId, Collection<GenomicVariantRecord> variants, VCFFileOld variantFile) {
        //query for which custominfo fields to use, and then see GenomicVariant.parseObject and VariantManagerUtils.addCustomVCFFields
        //to extract the custominfo fields you want to store.
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void addAnnotations(Collection<TabixAnnotation> annotations) throws UnsupportedOperationException {
        //Need to add custom fields??
        CustomField[] customFields = ProjectManager.getInstance().getCustomVariantFields(sessionID, projectID, referenceID, ProjectManager.getInstance().getNewestUpdateID(sessionID, projectID, referenceID, false));
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void removeAnnotation(Collection<TabixAnnotation> annotations) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean isAddingAnnotationSupported() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
