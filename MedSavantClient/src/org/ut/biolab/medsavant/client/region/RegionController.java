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

package org.ut.biolab.medsavant.client.region;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.*;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.shared.importing.FileFormat;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.shared.model.GenomicRegion;
import org.ut.biolab.medsavant.shared.model.RegionSet;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.util.Controller;


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

    public void addRegionSet(String regionSetName, char delim, FileFormat fileFormat, int numHeaderLines, int fileID) throws IOException, SQLException, RemoteException {
        MedSavantClient.RegionSetManager.addRegionSet(LoginController.getInstance().getSessionID(), regionSetName, ReferenceController.getInstance().getCurrentReferenceID(), delim, fileFormat, numHeaderLines, fileID);
        fireEvent(new RegionEvent(RegionEvent.Type.ADDED));
    }

    public void removeSet(int setID) throws SQLException, RemoteException {
        MedSavantClient.RegionSetManager.removeRegionSet(LoginController.getInstance().getSessionID(), setID);
        fireEvent(new RegionEvent(RegionEvent.Type.REMOVED));
    }

    public List<RegionSet> getRegionSets() throws SQLException, RemoteException {
        List<RegionSet> result = new ArrayList<RegionSet>();
        result.addAll(MedSavantClient.RegionSetManager.getRegionSets(LoginController.getInstance().getSessionID()));
        result.addAll(localRegionSets);
        return result;
    }

    public List<GenomicRegion> getRegionsInSet(RegionSet set) throws SQLException, RemoteException {
        if (set instanceof AdHocRegionSet) {
            return ((AdHocRegionSet)set).regions;
        }
        return MedSavantClient.RegionSetManager.getRegionsInSet(LoginController.getInstance().getSessionID(), set);
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
            result.addAll(MedSavantClient.RegionSetManager.getRegionsInSets(LoginController.getInstance().getSessionID(), setsSet));
        }
        return result;
    }

    public void addToRegionSet(RegionSet set, String chrom, int start, int end, String desc) throws SQLException, RemoteException{
        MedSavantClient.RegionSetManager.addToRegionSet(LoginController.getInstance().getSessionID(), set, ReferenceController.getInstance().getCurrentReferenceID(), chrom, start, end, desc);
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
