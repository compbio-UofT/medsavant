/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.controller.ThreadController;
import org.ut.biolab.medsavant.db.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.db.model.Chromosome;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.model.record.Genome;
import org.ut.biolab.medsavant.view.util.PeekingPanel;

/**
 *
 * @author mfiume
 */
public class GeneticsTablePage extends SubSectionView implements FiltersChangedListener {

    private JPanel panel;
    private TablePanel tablePanel;
    private GenomeContainer gp;
    private boolean isLoaded = false;

    private static GeneticsTablePage instance;

    public GeneticsTablePage(SectionView parent) {
        super(parent);
        FilterController.addFilterListener(this);
        instance = this;
    }

    public String getName() {
        return "Table";
    }

    public JPanel getView(boolean update) {
        
        if (panel == null || update) {
            ThreadController.getInstance().cancelWorkers(getName());
            //if(tablePanel != null) FilterController.removeFilterListener(tablePanel);
            //if(gp != null) FilterController.removeFilterListener(gp);
            setPanel();
        }
        //tablePanel.updateIfRequired();
        //gp.updateIfRequired();
        return panel;
    }

    private void setPanel() {
        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        gp = new GenomeContainer(getName());

        List<Chromosome> chrs = new ArrayList<Chromosome>();
        try {
            chrs = MedSavantClient.ChromosomeQueryUtilAdapter.getContigs(LoginController.sessionId, ReferenceController.getInstance().getCurrentReferenceId());
        } catch (SQLException ex) {
            Logger.getLogger(GeneticsTablePage.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
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
        isLoaded = true;
    }

    @Override
    public void viewDidUnload() {
        ThreadController.getInstance().cancelWorkers(getName());
        if(!tablePanel.isInit()){
            this.setUpdateRequired(true);
        }
        isLoaded = false;
    }

    public static GeneticsTablePage getInstance(){
        return instance;
    }

    public void updateContents(){
        if(tablePanel == null || gp == null) return;
        tablePanel.setUpdateRequired(true);
        gp.setUpdateRequired(true);
        if(isLoaded){           
            tablePanel.updateIfRequired();
            gp.updateIfRequired();
        }
    }

    @Override
    public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {
        ThreadController.getInstance().cancelWorkers(getName());
        updateContents();
    }

}
