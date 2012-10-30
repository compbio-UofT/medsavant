package org.ut.biolab.medsavant.model.event;

import org.ut.biolab.medsavant.vcf.VariantRecord;
import org.ut.biolab.medsavant.view.genetics.inspector.VariantInspectorDialog.SimpleVariant;

/**
 *
 * @author mfiume
 */
public interface VariantSelectionChangedListener {

    public void variantSelectionChanged(VariantRecord r);

}