/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.controller.ThreadController;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.db.model.Chromosome;
import org.ut.biolab.medsavant.db.util.query.ChromosomeQueryUtil;
import org.ut.biolab.medsavant.model.record.Genome;
import org.ut.biolab.medsavant.view.util.PeekingPanel;

/**
 *
 * @author mfiume
 */
public class GeneticsTablePage extends SubSectionView {

    private JPanel panel;
    private TablePanel tablePanel;
    private GenomeContainer gp;

    public GeneticsTablePage(SectionView parent) { 
        super(parent);       
    }

    public String getName() {
        return "  Table";
    }

    public JPanel getView(boolean update) {
        if (panel == null || update) {
            setPanel();
        }
        tablePanel.updateIfRequired();
        gp.updateIfRequired();
        return panel;
    }

    private void setPanel() {
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        gp = new GenomeContainer(getName());

        List<Chromosome> chrs = new ArrayList<Chromosome>();
        try {
            chrs = ChromosomeQueryUtil.getContigs(ReferenceController.getInstance().getCurrentReferenceId());
        } catch (SQLException ex) {
            Logger.getLogger(GeneticsTablePage.class.getName()).log(Level.SEVERE, null, ex);
        }
        Genome g = new Genome(chrs);
        gp.setGenome(g);
        PeekingPanel genomeView = new PeekingPanel("Genome", BorderLayout.SOUTH, gp, true,225);
        panel.add(genomeView, BorderLayout.NORTH);
        
        tablePanel = new TablePanel(getName());
        panel.add(tablePanel, BorderLayout.CENTER);
    }

    public Component[] getBanner() {
        return null;
    }
    
    
    @Override
    public void viewDidLoad() {
    }

    @Override
    public void viewDidUnload() {
        ThreadController.getInstance().cancelWorkers(getName());
        if(!tablePanel.isInit()){
            this.setUpdateRequired(true);
        }
    }

}
