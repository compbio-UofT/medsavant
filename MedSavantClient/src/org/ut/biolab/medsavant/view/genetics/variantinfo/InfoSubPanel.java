package org.ut.biolab.medsavant.view.genetics.variantinfo;

import javax.swing.JPanel;
import org.ut.biolab.medsavant.vcf.VariantRecord;

/**
 *
 * @author mfiume
 */
public abstract class InfoSubPanel {

    public abstract String getName();
    public abstract JPanel getInfoPanel();
    public boolean canDisplayFor(VariantRecord r) { return true; }

}
