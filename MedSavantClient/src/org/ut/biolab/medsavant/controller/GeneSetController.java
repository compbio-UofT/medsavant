package org.ut.biolab.medsavant.controller;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.listener.ReferenceListener;
import org.ut.biolab.medsavant.model.Gene;
import org.ut.biolab.medsavant.model.GeneSet;

/**
 *
 * @author mfiume
 */
public class GeneSetController implements ReferenceListener {

    List<Gene> genesInCurrentSet;
    private List<Gene> genes;

    private GeneSetController instance;

    public GeneSetController getInstance() {
        if (instance == null) {
            instance = new GeneSetController();
        }
        return instance;
    }

    public List<Gene> getCurrentGeneSet() throws SQLException, SQLException, RemoteException {
        if (genes == null) {

            String session = LoginController.sessionId;

            GeneSet geneSet = MedSavantClient.GeneSetAdapter.getGeneSets(
                    session,
                    ReferenceController.getInstance().getCurrentReferenceName()).get(0);

            genes = MedSavantClient.GeneSetAdapter.getGenes(session, geneSet);
        }
        return genes;
    }

    @Override
    public void referenceAdded(String name) {
    }

    @Override
    public void referenceRemoved(String name) {
    }

    @Override
    public void referenceChanged(String name) {
        genes = null;
    }

}
