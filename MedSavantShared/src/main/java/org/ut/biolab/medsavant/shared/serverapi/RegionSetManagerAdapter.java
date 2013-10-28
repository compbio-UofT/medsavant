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
package org.ut.biolab.medsavant.shared.serverapi;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.ut.biolab.medsavant.shared.importing.FileFormat;
import org.ut.biolab.medsavant.shared.model.GenomicRegion;
import org.ut.biolab.medsavant.shared.model.RegionSet;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;


/**
 *
 * @author mfiume
 */
public interface RegionSetManagerAdapter extends Remote {

    public void addRegionSet(String sessID, String regionSetName, int genomeID, char delim, FileFormat fileFormat, int numHeaderLines, int fileID) throws IOException, SQLException, RemoteException, SessionExpiredException;
    public void removeRegionSet(String sessID, int regionSetID) throws SQLException, RemoteException, SessionExpiredException;

    public List<RegionSet> getRegionSets(String sessID) throws SQLException, RemoteException, SessionExpiredException;
    public List<GenomicRegion> getRegionsInSet(String sessID, RegionSet set) throws SQLException, RemoteException, SessionExpiredException;
    public List<GenomicRegion> getRegionsInSets(String sessID, Collection<RegionSet> sets) throws SQLException, RemoteException, SessionExpiredException;

    public void addToRegionSet(String sessID, RegionSet set, int genomeID, String chrom, int start, int end, String desc) throws SQLException, RemoteException, SessionExpiredException;
}
