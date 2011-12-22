package org.ut.biolab.medsavant.db.util.query.api;

import java.rmi.Remote;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.model.BEDRecord;
import org.ut.biolab.medsavant.db.model.GenomicRegion;
import org.ut.biolab.medsavant.db.model.RegionSet;

/**
 *
 * @author mfiume
 */
public interface RegionQueryUtilAdapter extends Remote {

    public void addRegionList(String sid,String geneListName, int genomeId, Iterator<String[]> i) throws NonFatalDatabaseException, SQLException;
    public void removeRegionList(String sid,int regionSetId) throws SQLException;
    public List<RegionSet> getRegionSets(String sid) throws SQLException;
    public int getNumberRegions(String sid, int regionSetId) throws SQLException;
    public List<String> getRegionNamesInRegionSet(String sid, int regionSetId, int limit) throws SQLException;
    public List<GenomicRegion> getRegionsInRegionSet(String sid, int regionSetId) throws SQLException;
    public List<BEDRecord> getBedRegionsInRegionSet(String sid, int regionSetId, int limit) throws NonFatalDatabaseException, SQLException;
}
