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
package org.ut.biolab.medsavant.client.region;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.shared.importing.FileFormat;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.model.GenomicRegion;
import org.ut.biolab.medsavant.shared.model.RegionSet;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.util.Controller;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;


/**
 * Controller class which maintains the list of region sets.
 *
 * @author tarkvara
 */
public class RegionController extends Controller<RegionEvent> {
    private static RegionController instance;
    public static final String AD_HOC_REGION_SET_NAME = "Selected Regions";
    private List<AdHocRegionSet> localRegionSets = new ArrayList<AdHocRegionSet>();

    public static RegionController getInstance() {
        if (instance == null) {
            instance = new RegionController();
        }
        return instance;
    }

    public void addRegionSet(String regionSetName, char delim, FileFormat fileFormat, int numHeaderLines, int fileID) throws IOException, SQLException, RemoteException {
        try {
            MedSavantClient.RegionSetManager.addRegionSet(LoginController.getSessionID(), regionSetName, ReferenceController.getInstance().getCurrentReferenceID(), delim, fileFormat, numHeaderLines, fileID);
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return;
        }
        fireEvent(new RegionEvent(RegionEvent.Type.ADDED));
    }

    public void removeSet(int setID) throws SQLException, RemoteException {
        try {
            MedSavantClient.RegionSetManager.removeRegionSet(LoginController.getSessionID(), setID);
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return;
        }
        fireEvent(new RegionEvent(RegionEvent.Type.REMOVED));
    }

    public List<RegionSet> getRegionSets() throws SQLException, RemoteException {
        List<RegionSet> result = new ArrayList<RegionSet>();
        try {
            result.addAll(MedSavantClient.RegionSetManager.getRegionSets(LoginController.getSessionID()));
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return null;
        }
        result.addAll(localRegionSets);
        return result;
    }

    public List<GenomicRegion> getRegionsInSet(RegionSet set) throws SQLException, RemoteException {
        if (set instanceof AdHocRegionSet) {
            return ((AdHocRegionSet)set).regions;
        }
        try {
            return MedSavantClient.RegionSetManager.getRegionsInSet(LoginController.getSessionID(), set);
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return null;
        }
    }

    public List<GenomicRegion> getRegionsInSets(Collection<RegionSet> sets) throws SQLException, RemoteException {
        // We make a copy of the sets because the original value may be a collection which we shouldn't modify.
        Set<RegionSet> setsSet = new HashSet<RegionSet>();
        setsSet.addAll(sets);

        List<GenomicRegion> result = new ArrayList<GenomicRegion>();
        for (AdHocRegionSet r: localRegionSets) {
            if (setsSet.contains(r)) {
                result.addAll(r.regions);
                setsSet.remove(r);
            }
        }
        if (!setsSet.isEmpty()) {
            try {
                result.addAll(MedSavantClient.RegionSetManager.getRegionsInSets(LoginController.getSessionID(), setsSet));
            } catch (SessionExpiredException ex) {
                MedSavantExceptionHandler.handleSessionExpiredException(ex);
                return null;
            }
        }
        return result;
    }

    public void addToRegionSet(RegionSet set, String chrom, int start, int end, String desc) throws SQLException, RemoteException{
        try {
            MedSavantClient.RegionSetManager.addToRegionSet(LoginController.getSessionID(), set, ReferenceController.getInstance().getCurrentReferenceID(), chrom, start, end, desc);
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return;
        }
        fireEvent(new RegionEvent(RegionEvent.Type.ADDED));
    }

    /**
     * Create a region-set which wraps the given regions.  Right, we only need one ad hoc region-set to exist at a time.
     *
     * @param r the regions to be wrapped
     */
    public RegionSet createAdHocRegionSet(String name, List<GenomicRegion> r) {
        localRegionSets.clear();    // There can be only one.
        AdHocRegionSet result = new AdHocRegionSet(name, r);
        localRegionSets.add(result);
        fireEvent(new RegionEvent(RegionEvent.Type.ADDED));
        return result;
    }
    
}
