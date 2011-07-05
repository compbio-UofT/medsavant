/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.model.record.Chromosome;
import org.ut.biolab.medsavant.model.record.Genome;
import org.ut.biolab.medsavant.util.view.PeekingPanel;

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
        chrs.add(new Chromosome("chr1", "1", 125000000, 197195432));
        chrs.add(new Chromosome("chr2", "2", 93300000, 181748087));
        chrs.add(new Chromosome("chr3", "3", 91000000, 159599783));
        chrs.add(new Chromosome("chr4", "4", 50400000, 155630120));
        chrs.add(new Chromosome("chr5", "5", 48400000, 152537259));
        chrs.add(new Chromosome("chr6", "6", 61000000, 149517037));
        chrs.add(new Chromosome("chr7", "7", 59900000, 152524553));
        chrs.add(new Chromosome("chr8", "8", 45600000, 131738871));
        chrs.add(new Chromosome("chr9", "9", 49000000, 124076172));
        chrs.add(new Chromosome("chr10", "10", 40200000, 129993255));
        chrs.add(new Chromosome("chr11", "11", 53700000, 121843856));
        chrs.add(new Chromosome("chr12", "12", 35800000, 121257530));
        chrs.add(new Chromosome("chr13", "13", 17900000, 120284312));
        chrs.add(new Chromosome("chr14", "14", 17600000, 125194864));
        chrs.add(new Chromosome("chr15", "15", 19000000, 103494974));
        chrs.add(new Chromosome("chr16", "16", 36600000, 98319150));
        chrs.add(new Chromosome("chr17", "17", 24000000, 95272651));
        chrs.add(new Chromosome("chr18", "18", 17200000, 90772031));
        chrs.add(new Chromosome("chr19", "19", 26500000, 63806651));
        chrs.add(new Chromosome("chr20", "20", 27500000, 62435965));
        chrs.add(new Chromosome("chr21", "21", 13200000, 46944323));
        chrs.add(new Chromosome("chr22", "22", 14700000, 49528953));
        chrs.add(new Chromosome("chrX", "X", 60600000, 154913754));
        chrs.add(new Chromosome("chrY", "Y", 12500000, 57741652));
        Genome g = new Genome(chrs);
        gp.setGenome(g);
        PeekingPanel genomeView = new PeekingPanel("Genome", BorderLayout.SOUTH, gp, false,225);
        panel.add(genomeView, BorderLayout.NORTH);
        
        panel.add(new TablePanel(), BorderLayout.CENTER);
    }

    public Component[] getBanner() {
        Component[] cs = new Component[2];
        cs[0] = new JButton("Save Result Set");
        cs[1] = new JButton("Show in Savant");
        return cs;
    }
}
