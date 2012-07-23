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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.api.Listener;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.Gene;
import org.ut.biolab.medsavant.model.GeneSet;
import org.ut.biolab.medsavant.reference.ReferenceController;
import org.ut.biolab.medsavant.reference.ReferenceEvent;
import org.ut.biolab.medsavant.serverapi.GeneSetManagerAdapter;


/**
 *
 * @author mfiume
 */
public class GeneSetController {

    private GeneSet currentSet;
    private Map<String, Gene> genes;

    private static GeneSetController instance;
    private GeneSetManagerAdapter manager;

    private GeneSetController() {
        manager = MedSavantClient.GeneSetManager;

        ReferenceController.getInstance().addListener(new Listener<ReferenceEvent>() {
            @Override
            public void handleEvent(ReferenceEvent event) {
                if (event.getType() == ReferenceEvent.Type.CHANGED) {
                    currentSet = null;
                    genes = null;
                }
            }
        });
    }

    public static GeneSetController getInstance() {
        if (instance == null) {
            instance = new GeneSetController();
        }
        return instance;
    }

    public GeneSet getCurrentGeneSet() throws SQLException, RemoteException {
        if (currentSet == null) {
            currentSet = manager.getGeneSet(LoginController.sessionId, ReferenceController.getInstance().getCurrentReferenceName());
        }
        return currentSet;
    }

    public Collection<Gene> getCurrentGenes() throws SQLException, RemoteException {
        if (genes == null) {
            genes = new HashMap<String, Gene>();
            Gene[] rawGenes = manager.getGenes(LoginController.sessionId, getCurrentGeneSet());
            for (Gene g: rawGenes) {
                genes.put(g.getName(), g);
            }
        }
        return genes.values();
    }
    
    public Gene getGene(String name) throws SQLException, RemoteException {
        getCurrentGenes();
        return genes.get(name);
    }
}
