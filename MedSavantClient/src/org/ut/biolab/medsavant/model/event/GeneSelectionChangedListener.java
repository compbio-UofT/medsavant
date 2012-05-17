package org.ut.biolab.medsavant.model.event;

import org.ut.biolab.medsavant.model.Gene;

/**
 *
 * @author mfiume
 */
public interface GeneSelectionChangedListener {
    public void geneSelectionChanged(Gene g);
}