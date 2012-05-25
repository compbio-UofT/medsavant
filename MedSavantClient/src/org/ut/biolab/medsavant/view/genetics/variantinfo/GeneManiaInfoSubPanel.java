/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.variantinfo;


import java.awt.BorderLayout;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.ut.biolab.medsavant.model.Gene;
import org.ut.biolab.medsavant.model.event.GeneSelectionChangedListener;
import org.ut.biolab.medsavant.view.genetics.GeneIntersectionGenerator;
import org.xml.sax.SAXException;
/**
 *
 * @author khushi
 */
public class GeneManiaInfoSubPanel extends InfoSubPanel implements GeneSelectionChangedListener {
    private final String name;
    private JLabel l;
    private JPanel p;

    public GeneManiaInfoSubPanel(){
        this.name = "Related Genes";
        GeneIntersectionGenerator.addGeneSelectionChangedListener(this);
    }

    @Override
    public String getName(){
        return this.name;
    }

     @Override
     public JPanel getInfoPanel() {
         JPanel currGenePanel = new JPanel();
         JPanel settingsPanel = new JPanel();
         p = new JPanel();
         p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
         JButton settingsButton = new JButton ("Settings");
         settingsButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_settingsActionPerformed();
            }
         });
         l = new JLabel("Selected gene: ");
         currGenePanel.add(l);
         settingsPanel.add(settingsButton);
         p.add(settingsPanel);
         p.add(currGenePanel);
         return p;
     }
     
     private void button_settingsActionPerformed (){
         
     }
     
    @Override
    public void geneSelectionChanged(Gene g) {
        if (g == null) {
            l.setText("None");
        } else {
            System.out.println("Received gene " + g.getName());
            l.setText(g.getName());
            updateRelatedGenesPanel(g);
            
        }
    }

    private void updateRelatedGenesPanel(Gene g) {
        try {
           GenemaniaInfoRetriever genemania= new GenemaniaInfoRetriever(g.getName());
           GeneSetFetcher geneSetFetcher = new GeneSetFetcher();
           if (genemania.validGene()){
                    //Iterator<String> itr = genemania.getRelatedGeneNamesByScore().iterator();
                    Iterator<org.ut.biolab.medsavant.model.Gene> itr= geneSetFetcher.getGenesByNumVariants(genemania.getRelatedGeneNamesByScore()).iterator();
                    JPanel panel;
                    JLabel relatedGeneName;
                    itr.next();//skip the first one (it's the name of selected gene already displayed)
                    while (itr.hasNext()){
                        panel = new JPanel(); 
                        relatedGeneName = new JLabel("");
                        relatedGeneName.setText(itr.next().getName());
                        panel.add(relatedGeneName);
                        p.add(panel);
                    }
            }
        } catch (ApplicationException ex) {
            Logger.getLogger(GeneManiaInfoSubPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DataStoreException ex) {
            Logger.getLogger(GeneManiaInfoSubPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (SAXException ex) {
                Logger.getLogger(GeneManiaInfoSubPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
