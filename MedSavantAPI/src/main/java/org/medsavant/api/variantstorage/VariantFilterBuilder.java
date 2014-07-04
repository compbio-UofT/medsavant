/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.medsavant.api.variantstorage;

import com.healthmarketscience.sqlbuilder.Condition;
import java.util.Collection;

/**
 *
 * @author jim
 */
public interface VariantFilterBuilder {

    public void addCondition(Condition condition);

    public void setPublicationStatus(PublicationStatus status);

    public void setProjectId(int projectId);

    public void setReferenceGenomeId(int referenceId);

    public interface VariantFilter {

        public Collection<Condition> getConditions();

        public PublicationStatus getPublicationStatus();

        public int getProjectId();

        public int getReferenceGenomeId();
    }

    public VariantFilter build();

}
