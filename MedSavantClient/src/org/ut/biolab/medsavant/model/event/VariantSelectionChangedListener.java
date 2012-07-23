package org.ut.biolab.medsavant.model.event;

import org.ut.biolab.medsavant.vcf.VariantRecord;

/**
 *
 * @author mfiume
 */
public interface VariantSelectionChangedListener {

    public void variantSelectionChanged(VariantRecord r);

}