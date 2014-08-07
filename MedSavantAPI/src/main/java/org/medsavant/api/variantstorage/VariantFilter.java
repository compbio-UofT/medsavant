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
public interface VariantFilter {

    Condition getCondition();

    MedSavantProject getProject();

    PublicationStatus getPublicationStatus();

    Reference getReference();
    
}
