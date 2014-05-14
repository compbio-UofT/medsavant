/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.geneset;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.model.Gene;
import org.ut.biolab.medsavant.shared.model.GeneSet;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.reference.ReferenceEvent;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.GeneSetManagerAdapter;


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
            try {
                currentSet = manager.getGeneSet(LoginController.getSessionID(), ReferenceController.getInstance().getCurrentReferenceName());
            } catch (SessionExpiredException ex) {
                MedSavantExceptionHandler.handleSessionExpiredException(ex);
                return null;
            }
        }
        return currentSet;
    }

    public Collection<Gene> getCurrentGenes() throws SQLException, RemoteException {
        if (genes == null) {
            genes = new HashMap<String, Gene>();
            Gene[] rawGenes;
            try {
                rawGenes = manager.getGenes(LoginController.getSessionID(), getCurrentGeneSet());
            } catch (SessionExpiredException ex) {
                MedSavantExceptionHandler.handleSessionExpiredException(ex);
                return null;
            }
            for (Gene g: rawGenes) {
                genes.put(g.getName().toUpperCase(), g);
            }
        }
        return genes.values();
    }

    public Gene getGene(String name) throws SQLException, RemoteException {
        getCurrentGenes();
        return genes.get(name.toUpperCase());
    }
}
