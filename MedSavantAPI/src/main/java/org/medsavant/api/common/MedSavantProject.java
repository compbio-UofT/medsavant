/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.medsavant.api.common;

import java.util.List;
import org.medsavant.api.annotation.MedSavantAnnotation;
import org.medsavant.api.variantstorage.MedSavantVariantStorageEngine;

/**
 *
 * @author jim
 */
public interface MedSavantProject {
    public int getProjectId();
    public String getProjectName();
    public List<MedSavantAnnotation> getInstalledAnnotations(MedSavantVariantStorageEngine mvse);
    public String getDatabaseName();

}
