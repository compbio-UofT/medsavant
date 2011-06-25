/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.model.record.Chromosome;
import org.ut.biolab.medsavant.model.record.Genome;
import org.ut.biolab.medsavant.util.view.PeekingPanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.genetics.filter.FilterPanel;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;

/**
 *
 * @author mfiume
 */
public class GeneticsSummerizePage extends SubSectionView {

    private JPanel panel;
    private ChartContainer cc;
    
    public GeneticsSummerizePage(SectionView parent) { super(parent); }

    public String getName() {
        return "Summary";
    }

    public JPanel getView() {
        if (panel == null) {
            try {
                setPanel();
            } catch (NonFatalDatabaseException ex) {
            }
        }
        return panel;
    }

    private void setPanel() throws NonFatalDatabaseException {

        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        //PeekingPanel detailView = new PeekingPanel("Filters", BorderLayout.EAST, new FilterPanel(), true,400);
        //panel.add(detailView, BorderLayout.WEST);

        GenomeContainer gp = new GenomeContainer();
        List<Chromosome> chrs = new ArrayList<Chromosome>();
        chrs.add(new Chromosome("chr1", "1", -1, 197195432));
        chrs.add(new Chromosome("chr2", "2", -1, 181748087));
        chrs.add(new Chromosome("chr3", "3", -1, 159599783));
        chrs.add(new Chromosome("chr4", "4", -1, 155630120));
        chrs.add(new Chromosome("chr5", "5", -1, 152537259));
        chrs.add(new Chromosome("chr6", "6", -1, 149517037));
        chrs.add(new Chromosome("chr7", "7", -1, 152524553));
        chrs.add(new Chromosome("chr8", "8", -1, 131738871));
        chrs.add(new Chromosome("chr9", "9", -1, 124076172));
        chrs.add(new Chromosome("chr10", "10", -1, 129993255));
        chrs.add(new Chromosome("chr11", "11", -1, 121843856));
        chrs.add(new Chromosome("chr12", "12", -1, 121257530));
        chrs.add(new Chromosome("chr13", "13", -1, 120284312));
        chrs.add(new Chromosome("chr14", "14", -1, 125194864));
        chrs.add(new Chromosome("chr15", "15", -1, 103494974));
        chrs.add(new Chromosome("chr16", "16", -1, 98319150));
        chrs.add(new Chromosome("chr17", "17", -1, 95272651));
        chrs.add(new Chromosome("chr18", "18", -1, 90772031));
        chrs.add(new Chromosome("chr19", "19", -1, 63806651));
        chrs.add(new Chromosome("chr20", "20", -1, 62435965));
        chrs.add(new Chromosome("chr21", "21", -1, 46944323));
        chrs.add(new Chromosome("chr22", "22", -1, 49528953));
        chrs.add(new Chromosome("chrX", "X", -1, 154913754));
        chrs.add(new Chromosome("chrY", "Y", -1, 57741652));
        Genome g = new Genome(chrs);
        gp.setGenome(g);
        PeekingPanel genomeView = new PeekingPanel("Genome", BorderLayout.SOUTH, gp, false,225);
        //TODO: re-add genome view to panel

        
        cc = new ChartContainer();
        panel.add(cc, BorderLayout.CENTER);
    }
    
    public Component[] getBanner() {
        Component[] cs = new Component[1];
        JButton addButton = new JButton("Add chart");
        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                cc.addChart();
            }
        });
        cs[0] = addButton;
        return cs;
    }
}
