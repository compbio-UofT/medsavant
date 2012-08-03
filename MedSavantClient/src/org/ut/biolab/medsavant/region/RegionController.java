/*
 *    Copyright 2011-2012 University of Toronto
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

package org.ut.biolab.medsavant.region;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.*;

import com.healthmarketscience.rmiio.RemoteInputStream;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.importing.FileFormat;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.GenomicRegion;
import org.ut.biolab.medsavant.model.RegionSet;
import org.ut.biolab.medsavant.reference.ReferenceController;
import org.ut.biolab.medsavant.util.Controller;


/**
 * Controller class which maintains the list of region sets.
 *
 * @author tarkvara
 */
public class RegionController extends Controller<RegionEvent> {
    private static RegionController instance;
    
    private List<AdHocRegionSet> localRegionSets = new ArrayList<AdHocRegionSet>();

    public static RegionController getInstance() {
        if (instance == null) {
            instance = new RegionController();
        }
        return instance;
    }

    public void addRegionSet(String regionSetName, RemoteInputStream remoteStream, char delim, FileFormat fileFormat, int numHeaderLines) throws IOException, SQLException, RemoteException {
        MedSavantClient.RegionSetManager.addRegionSet(LoginController.sessionId, regionSetName, ReferenceController.getInstance().getCurrentReferenceID(), remoteStream, delim, fileFormat, numHeaderLines);
        fireEvent(new RegionEvent(RegionEvent.Type.ADDED));
    }

    public void removeSet(int setID) throws SQLException, RemoteException {
        MedSavantClient.RegionSetManager.removeRegionSet(LoginController.sessionId, setID);
        fireEvent(new RegionEvent(RegionEvent.Type.REMOVED));
    }

    public List<RegionSet> getRegionSets() throws SQLException, RemoteException {
        List<RegionSet> result = new ArrayList<RegionSet>();
        result.addAll(MedSavantClient.RegionSetManager.getRegionSets(LoginController.sessionId));
        result.addAll(localRegionSets);
        return result;
    }

    public List<GenomicRegion> getRegionsInSet(RegionSet set) throws SQLException, RemoteException {
        if (set instanceof AdHocRegionSet) {
            return ((AdHocRegionSet)set).regions;
        }
        return MedSavantClient.RegionSetManager.getRegionsInSet(LoginController.sessionId, set, Integer.MAX_VALUE);
    }
    
    public List<GenomicRegion> getRegionsInSets(Collection<RegionSet> sets) throws SQLException, RemoteException {
        List<GenomicRegion> result = new ArrayList<GenomicRegion>();
        for (AdHocRegionSet r: localRegionSets) {
            if (sets.contains(r)) {
                result.addAll(r.regions);
                sets.remove(r);
            }
        }
        if (!sets.isEmpty()) {
            result.addAll(MedSavantClient.RegionSetManager.getRegionsInSets(LoginController.sessionId, sets, Integer.MAX_VALUE));
        }
        return result;
    }
    
    public void addToRegionSet(RegionSet set, String chrom, int start, int end, String desc) throws SQLException, RemoteException{
        MedSavantClient.RegionSetManager.addToRegionSet(LoginController.sessionId, set, Integer.MAX_VALUE, ReferenceController.getInstance().getCurrentReferenceID(), chrom, start, end, desc);
        fireEvent(new RegionEvent(RegionEvent.Type.ADDED));
    }
    
    /**
     * Create a region-set which wraps the given regions.  Right, we only need one ad hoc region-set to exist at a time.
     *
     * @param r the regions to be wrapped
     */
    public RegionSet createAdHocRegionSet(List<GenomicRegion> r) {
        localRegionSets.clear();    // There can be only one.
        AdHocRegionSet result = new AdHocRegionSet(r);
        localRegionSets.add(result);
        fireEvent(new RegionEvent(RegionEvent.Type.ADDED));
        return result;
    }

    /**
     * Class which wraps up a collection of GenomicRegions and makes them look like a RegionSet.
     */
    private class AdHocRegionSet extends RegionSet {
        final List<GenomicRegion> regions;

        AdHocRegionSet(List<GenomicRegion> r) {
            super(-1, "Ad Hoc Region Set", r.size());
            regions = r;
        }
    }
}
