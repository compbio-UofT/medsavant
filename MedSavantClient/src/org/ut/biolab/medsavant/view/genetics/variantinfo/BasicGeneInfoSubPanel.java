package org.ut.biolab.medsavant.view.genetics.variantinfo;

import javax.swing.JPanel;
import org.ut.biolab.medsavant.model.Gene;
import org.ut.biolab.medsavant.model.event.GeneSelectionChangedListener;
import org.ut.biolab.medsavant.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.view.genetics.GeneIntersectionGenerator;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class BasicGeneInfoSubPanel extends InfoSubPanel implements GeneSelectionChangedListener {

    private static String KEY_NAME = "Name";
    private static String KEY_CHROM = "Chromosome";
    private static String KEY_START = "Start";
    private static String KEY_END = "End";


    private KeyValuePairPanel p;

    public BasicGeneInfoSubPanel() {
        GeneIntersectionGenerator.addGeneSelectionChangedListener(this);
    }

    @Override
    public String getName() {
        return "Gene Details";
    }


    @Override
    public JPanel getInfoPanel() {
        if (p == null) {
            p = new KeyValuePairPanel();
            p.addKey(KEY_NAME);
            p.addKey(KEY_CHROM);
             p.addKey(KEY_START);
            p.addKey(KEY_END);
        }
        return p;
    }

    @Override
    public void geneSelectionChanged(Gene g) {
        if (p == null) { return; }
        if (g == null) {
            // TODO show other card
            return;
        }
        p.setValue(KEY_NAME, g.getName());
        p.setValue(KEY_CHROM, g.getChrom());
        p.setValue(KEY_START, ViewUtil.numToString(g.getStart()));
        p.setValue(KEY_END, ViewUtil.numToString(g.getEnd()));
    }

}
