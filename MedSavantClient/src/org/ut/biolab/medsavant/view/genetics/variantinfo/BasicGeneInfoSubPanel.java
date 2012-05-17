package org.ut.biolab.medsavant.view.genetics.variantinfo;


import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.model.Gene;
import org.ut.biolab.medsavant.model.event.GeneSelectionChangedListener;
import org.ut.biolab.medsavant.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.view.component.LinkButton;
import org.ut.biolab.medsavant.view.genetics.GeneIntersectionGenerator;

/**
 *
 * @author mfiume
 */
public class BasicGeneInfoSubPanel extends InfoSubPanel implements GeneSelectionChangedListener {

    private JLabel chromLabel;
    private JLabel positionLabel;
    private JLabel dnaLabel;
    private JLabel refLabel;
    private JLabel altLabel;
    private JLabel qualityLabel;
    private JLabel dbsnpLabel;
    private LinkButton ncbiButton;
    private KeyValuePairPanel p;

    public BasicGeneInfoSubPanel() {
        GeneIntersectionGenerator.addGeneSelectionChangedListener(this);
    }

    @Override
    public String getName() {
        return "Gene Details";
    }

    private static String KEY_NAME = "Name";

    @Override
    public JPanel getInfoPanel() {
        p = new KeyValuePairPanel();
        p.addKey(KEY_NAME);
        return p;
    }

    @Override
    public void geneSelectionChanged(Gene g) {
        p.setValue(KEY_NAME, g.getName());
    }

}
