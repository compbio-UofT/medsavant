/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.variantinfo;

import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.model.Gene;
import org.ut.biolab.medsavant.model.event.GeneSelectionChangedListener;
import org.ut.biolab.medsavant.view.genetics.GeneIntersectionGenerator;
/**
 *
 * @author khushi
 */
public class GeneManiaInfoSubPanel extends InfoSubPanel implements GeneSelectionChangedListener {
    private final String name;
    private JLabel l;

    public GeneManiaInfoSubPanel(){
        this.name = "Gene Mania";
        GeneIntersectionGenerator.addGeneSelectionChangedListener(this);
    }

    @Override
    public String getName(){
        return this.name;
    }

     @Override
     public JPanel getInfoPanel() {
         JPanel p = new JPanel();
         l = new JLabel("Selected gene: ");
         p.add(l);
         return p;
     }

    @Override
    public void geneSelectionChanged(Gene g) {
        if (g == null) {
            l.setText("None");
        } else {
            System.out.println("Received gene " + g.getName());
            l.setText(g.getName());
        }
    }
}
