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

package org.ut.biolab.medsavant.server.serverapi;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.RegionSetMembershipColumns;
import org.ut.biolab.medsavant.server.db.util.DBUtils;
import org.ut.biolab.medsavant.shared.importing.FileFormat;
import org.ut.biolab.medsavant.shared.importing.ImportDelimitedFile;
import org.ut.biolab.medsavant.shared.model.GenomicRegion;
import org.ut.biolab.medsavant.shared.model.RegionSet;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.persistence.EntityManager;
import org.ut.biolab.medsavant.shared.persistence.EntityManagerFactory;
import org.ut.biolab.medsavant.shared.query.Query;
import org.ut.biolab.medsavant.shared.query.QueryManager;
import org.ut.biolab.medsavant.shared.query.QueryManagerFactory;
import org.ut.biolab.medsavant.shared.serverapi.RegionSetManagerAdapter;
import org.ut.biolab.medsavant.shared.solr.exception.InitializationException;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 *
 * @author Andrew
 */
public class RegionSetManager extends MedSavantServerUnicastRemoteObject implements RegionSetManagerAdapter, RegionSetMembershipColumns {
    private static final Log LOG = LogFactory.getLog(RegionSetManager.class);

    private static RegionSetManager instance;
    private static QueryManager queryManager;
    private static EntityManager entityManager;

    private RegionSetManager() throws RemoteException, SessionExpiredException {
        queryManager = QueryManagerFactory.getQueryManager();
        entityManager = EntityManagerFactory.getEntityManager();
    }

    public static synchronized RegionSetManager getInstance() throws RemoteException, SessionExpiredException {
        if (instance == null) {
            instance = new RegionSetManager();
        }
        return instance;
    }

    @Override
    public void addRegionSet(String sessID, String regionSetName, int genomeID, char delim, FileFormat fileFormat, int numHeaderLines, int fileID) throws IOException, SQLException, RemoteException, SessionExpiredException {

        int regionSetId = DBUtils.generateId("id", "RegionSet");
        RegionSet regionSet = new RegionSet(regionSetId,regionSetName, -1);

        try {
            entityManager.persist(regionSet);
        } catch (InitializationException e) {
            LOG.error("Error persiting region set");
        }

        File f = NetworkManager.getInstance().getFileByTransferID(sessID, fileID);
        Iterator<String[]> i = ImportDelimitedFile.getFileIterator(f.getAbsolutePath(), delim, numHeaderLines, fileFormat);

        List<GenomicRegion> genomicRegionList = new ArrayList<GenomicRegion>();
        while (i.hasNext() && !Thread.currentThread().isInterrupted()){
            String[] line = i.next();
            LOG.info(StringUtils.join(line, '\t'));
            GenomicRegion genomicRegion = new GenomicRegion(genomeID,
                                                            regionSetId,
                                                            line[0],
                                                            line[1],
                                                            Integer.parseInt(line[2]),
                                                            Integer.parseInt(line[3]));
            genomicRegionList.add(genomicRegion);
        }
        try {
            entityManager.persistAll(genomicRegionList);
        } catch (InitializationException e) {
            LOG.error("Error persisting genomic regions");
        }
    }

    @Override
    public void removeRegionSet(String sessID, int regionSetID) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Delete from RegionSet r where r.id= :id");
        query.setParameter("id", regionSetID);
        query.executeDelete();
    }

    @Override
    public List<RegionSet> getRegionSets(String sessID) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Select r from RegionSet");
        return query.execute();
    }

    @Override
    public List<GenomicRegion> getRegionsInSet(String sessID, RegionSet set) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Select g from GenomicRegion where g.id= :id");
        query.setParameter("id", set.getID());
        return query.execute();
    }

    @Override
    public List<GenomicRegion> getRegionsInSets(String sessID, Collection<RegionSet> sets) throws SQLException, SessionExpiredException {

        String rsCol = StringUtils.join(sets, ",");
        Query query = queryManager.createQuery(String.format("Select g from GenomicRegion where g.id IN (%s)", rsCol));
        return query.execute();
    }

    @Override
    public void addToRegionSet(String sessID, RegionSet set, int genomeID, String chrom, int start, int end, String desc) throws SQLException, RemoteException, SessionExpiredException{

        GenomicRegion genomicRegion = new GenomicRegion(genomeID, set.getID(), desc, chrom, start, end);
        try {
            entityManager.persist(genomicRegion);
        } catch (InitializationException e) {
            LOG.error("Error persisting genomic region");
        }

    }
}
