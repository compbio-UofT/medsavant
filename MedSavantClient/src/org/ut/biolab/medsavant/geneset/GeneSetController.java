/*
 *    Copyright 2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.geneset;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.reference.ReferenceController;
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

            GeneSet geneSet = MedSavantClient.GeneSetManager.getGeneSets(
                    session,
                    ReferenceController.getInstance().getCurrentReferenceName()).get(0);

            genes = MedSavantClient.GeneSetManager.getGenes(session, geneSet);
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
