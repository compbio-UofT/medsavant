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

    public RegionSet[] getRegionSets() throws SQLException, RemoteException {
        return MedSavantClient.RegionSetManager.getRegionSets(LoginController.sessionId);
    }

    public GenomicRegion[] getRegionsInSet(RegionSet set) throws SQLException, RemoteException {
        return MedSavantClient.RegionSetManager.getRegionsInSet(LoginController.sessionId, set, Integer.MAX_VALUE);
    }
    
    public void addToRegionSet(RegionSet set, String chrom, int start, int end, String desc) throws SQLException, RemoteException{
        MedSavantClient.RegionSetManager.addToRegionSet(LoginController.sessionId, set, Integer.MAX_VALUE, ReferenceController.getInstance().getCurrentReferenceID(), chrom, start, end, desc);
        fireEvent(new RegionEvent(RegionEvent.Type.ADDED));
    }
}
