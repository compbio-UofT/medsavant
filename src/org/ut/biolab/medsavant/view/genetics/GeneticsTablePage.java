/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.model.record.Chromosome;
import org.ut.biolab.medsavant.model.record.Genome;
import org.ut.biolab.medsavant.util.view.PeekingPanel;
import org.ut.biolab.medsavant.view.dialog.SavantExportForm;

/**
 *
 * @author mfiume
 */
public class GeneticsTablePage extends SubSectionView {

    private JPanel panel;

    public GeneticsTablePage(SectionView parent) { super(parent); }

    public String getName() {
        return "Table";
    }

    public JPanel getView() {
        if (panel == null) {
            setPanel();
        }
        return panel;
    }

    private void setPanel() {
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        GenomeContainer gp = new GenomeContainer();
        List<Chromosome> chrs = new ArrayList<Chromosome>();
        chrs.add(new Chromosome("chr1", "1", 125000000, 249250621));
        chrs.add(new Chromosome("chr2", "2", 93300000, 243199373));
        chrs.add(new Chromosome("chr3", "3", 91000000, 198022430));
        chrs.add(new Chromosome("chr4", "4", 50400000, 191154276));
        chrs.add(new Chromosome("chr5", "5", 48400000, 180915260));
        chrs.add(new Chromosome("chr6", "6", 61000000, 171115067));
        chrs.add(new Chromosome("chr7", "7", 59900000, 159138663));
        chrs.add(new Chromosome("chr8", "8", 45600000, 146364022));
        chrs.add(new Chromosome("chr9", "9", 49000000, 141213431));
        chrs.add(new Chromosome("chr10", "10", 40200000, 135534747));
        chrs.add(new Chromosome("chr11", "11", 53700000, 135006516));
        chrs.add(new Chromosome("chr12", "12", 35800000, 133851895));
        chrs.add(new Chromosome("chr13", "13", 17900000, 115169878));
        chrs.add(new Chromosome("chr14", "14", 17600000, 107349540));
        chrs.add(new Chromosome("chr15", "15", 19000000, 102531392));
        chrs.add(new Chromosome("chr16", "16", 36600000, 90354753));
        chrs.add(new Chromosome("chr17", "17", 24000000, 81195210));
        chrs.add(new Chromosome("chr18", "18", 17200000, 78077248));
        chrs.add(new Chromosome("chr19", "19", 26500000, 59128983));
        chrs.add(new Chromosome("chr20", "20", 27500000, 63025520));
        chrs.add(new Chromosome("chr21", "21", 13200000, 48129895));
        chrs.add(new Chromosome("chr22", "22", 14700000, 51304566));
        chrs.add(new Chromosome("chrX", "X", 60600000, 155270560));
        chrs.add(new Chromosome("chrY", "Y", 12500000, 59373566));
        Genome g = new Genome(chrs);
        gp.setGenome(g);
        PeekingPanel genomeView = new PeekingPanel("Genome", BorderLayout.SOUTH, gp, true,225);
        panel.add(genomeView, BorderLayout.NORTH);
        
        panel.add(new TablePanel(), BorderLayout.CENTER);
    }

    public Component[] getBanner() {
        return null;
    }
    
    
    
    
}
