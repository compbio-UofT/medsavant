package org.ut.biolab.medsavant.db.util.query.api;

import com.healthmarketscience.rmiio.RemoteInputStream;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.importfile.FileFormat;
import org.ut.biolab.medsavant.db.model.BEDRecord;
import org.ut.biolab.medsavant.db.model.GenomicRegion;
import org.ut.biolab.medsavant.db.model.RegionSet;

/**
 *
 * @author mfiume
 */
public interface RegionQueryUtilAdapter extends Remote {

    public void addRegionList(String sid,String geneListName, int genomeId, RemoteInputStream fileStream, char delim, FileFormat fileFormat, int numHeaderLines) throws NonFatalDatabaseException, SQLException, RemoteException;
    public void removeRegionList(String sid,int regionSetId) throws SQLException, RemoteException;
    public List<RegionSet> getRegionSets(String sid) throws SQLException, RemoteException;
    public int getNumberRegions(String sid, int regionSetId) throws SQLException, RemoteException;
    public List<String> getRegionNamesInRegionSet(String sid, int regionSetId, int limit) throws SQLException, RemoteException;
    public List<GenomicRegion> getRegionsInRegionSet(String sid, int regionSetId) throws SQLException, RemoteException;
    public List<BEDRecord> getBedRegionsInRegionSet(String sid, int regionSetId, int limit) throws NonFatalDatabaseException, SQLException, RemoteException;
    public boolean listNameExists(String sid, String name) throws SQLException, RemoteException;
}
