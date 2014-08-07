/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.variantstorage;

import com.healthmarketscience.sqlbuilder.Condition;
import org.medsavant.api.common.MedSavantProject;
import org.medsavant.api.common.Reference;

/**
 *
 * @author jim
 */
public class VariantFilterImpl implements VariantFilter{

    private Condition condition;
    private MedSavantProject project;
    private PublicationStatus pubStatus;
    private Reference reference;
    
    public VariantFilterImpl(Condition cond, MedSavantProject project, Reference reference){
        this(cond, project, reference, PublicationStatus.PUBLISHED);
    }
    
    public VariantFilterImpl(Condition cond, MedSavantProject project, Reference reference, PublicationStatus pubStatus){
        this.condition = cond;
        this.project = project;
        this.reference = reference;
        this.pubStatus = pubStatus;                
    }
    
    public Condition getCondition() {
        return condition;
    }

    public MedSavantProject getProject() {
        return project;
    }

    public PublicationStatus getPublicationStatus() {
        return pubStatus;
    }

    public Reference getReference() {
        return reference;
    }
    
}
