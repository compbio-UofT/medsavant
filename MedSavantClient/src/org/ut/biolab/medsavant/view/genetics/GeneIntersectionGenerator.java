package org.ut.biolab.medsavant.view.genetics;

import com.jidesoft.pane.event.CollapsiblePaneEvent;
import com.jidesoft.pane.event.CollapsiblePaneListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.model.Gene;
import org.ut.biolab.medsavant.model.GeneSet;
import org.ut.biolab.medsavant.model.event.GeneSelectionChangedListener;
import org.ut.biolab.medsavant.model.event.VariantSelectionChangedListener;
import org.ut.biolab.medsavant.vcf.VariantRecord;

/**
 *
 * @author mfiume
 */
public class GeneIntersectionGenerator implements VariantSelectionChangedListener, CollapsiblePaneListener {

    private static GeneIntersectionGenerator instance;

    static GeneIntersectionGenerator getInstance() {
        if (instance == null) {
            instance = new GeneIntersectionGenerator();
        }
        return instance;
    }
    private List<Gene> genes;

    private GeneIntersectionGenerator() {
        TablePanel.addVariantSelectionChangedListener(this);
        b = new JComboBox();
        b.addActionListener (new ActionListener () {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Broadcasting gene change: " + ((Gene)b.getSelectedItem()));
                geneSelectionChanged((Gene)b.getSelectedItem());
            }
        });
    }
    private JComboBox b;

    public JComboBox getGeneDropDown() {
        return b;
    }

    @Override
    public void variantSelectionChanged(VariantRecord r) {
        try {

            if (genes == null) {
                String session = LoginController.sessionId;
                GeneSet geneSet = MedSavantClient.GeneSetAdapter.getGeneSets(session).get(0);
                genes = MedSavantClient.GeneSetAdapter.getGenes(session, geneSet);
            }

            b.removeAllItems();

            for (Gene g : genes) {
                if (g.getChrom().equals(r.getChrom())
                        && r.getPosition() > g.getStart()
                        && r.getPosition() < g.getEnd()) {

                    //match = g;
                    //break;
                    b.addItem(g);
                }
            }

            /*
            if (match == null) {
            System.out.println("No match");
            } else {
            System.out.println("Matched " + match.getName());
            }
             *
             */

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static List<GeneSelectionChangedListener> listeners = new ArrayList<GeneSelectionChangedListener>();
    private boolean isShown;

    public static void addGeneSelectionChangedListener(GeneSelectionChangedListener l) {
        listeners.add(l);
    }
    Gene selectedGene;

    public void geneSelectionChanged(Gene r) {
        if (isShown) {
            for (GeneSelectionChangedListener l : listeners) {
                l.geneSelectionChanged(r);
            }
        }
        selectedGene = r;
    }

    @Override
    public void paneExpanding(CollapsiblePaneEvent cpe) {
        geneSelectionChanged(selectedGene);
    }

    @Override
    public void paneExpanded(CollapsiblePaneEvent cpe) {
        isShown = true;
    }

    @Override
    public void paneCollapsing(CollapsiblePaneEvent cpe) {
    }

    @Override
    public void paneCollapsed(CollapsiblePaneEvent cpe) {
        isShown = false;
    }
}
