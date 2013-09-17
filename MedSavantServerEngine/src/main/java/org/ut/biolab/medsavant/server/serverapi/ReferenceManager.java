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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.GeneSetColumns;
import org.ut.biolab.medsavant.server.db.util.DBUtils;
import org.ut.biolab.medsavant.shared.model.Chromosome;
import org.ut.biolab.medsavant.shared.model.Reference;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.persistence.EntityManager;
import org.ut.biolab.medsavant.shared.persistence.EntityManagerFactory;
import org.ut.biolab.medsavant.shared.query.Query;
import org.ut.biolab.medsavant.shared.query.QueryManager;
import org.ut.biolab.medsavant.shared.query.QueryManagerFactory;
import org.ut.biolab.medsavant.shared.query.ResultRow;
import org.ut.biolab.medsavant.shared.serverapi.ReferenceManagerAdapter;
import org.ut.biolab.medsavant.shared.solr.exception.InitializationException;
import org.ut.biolab.medsavant.shared.util.Entity;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author mfiume
 */
public class ReferenceManager extends MedSavantServerUnicastRemoteObject implements ReferenceManagerAdapter, GeneSetColumns {

    private static final Log LOG = LogFactory.getLog(ReferenceManager.class);

    private static ReferenceManager instance;
    private static QueryManager queryManager;
    private static EntityManager entityManager;

    public static synchronized ReferenceManager getInstance() throws RemoteException, SessionExpiredException {
        if (instance == null) {
            instance = new ReferenceManager();
        }
        return instance;
    }

    public ReferenceManager() throws RemoteException, SessionExpiredException {super();
        queryManager = QueryManagerFactory.getQueryManager();
        entityManager = EntityManagerFactory.getEntityManager();
    }

    @Override
    public Reference[] getReferences(String sessID) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Select r from Reference r");
        List<Reference> referenceList = query.execute();
        return referenceList.toArray(new Reference[referenceList.size()]);
    }

    @Override
    public String[] getReferenceNames(String sessID) throws SQLException, SessionExpiredException {
        Query query = queryManager.createQuery("Select r.name from Reference r");
        List<ResultRow> resultRowList = query.executeForRows();

        String[] names = new String[resultRowList.size()];
        for (int i = 0; i < resultRowList.size(); i++) {
            names[i] = (String) resultRowList.get(i).getObject("name");
        }

        return names;
    }

    @Override
    public int getReferenceID(String sessID, String refName) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Select r from Reference r where r.name= :name");
        query.setParameter("name", refName);


        Reference reference = query.getFirst();
        return (reference == null) ? -1 : reference.getID();
    }

    @Override
    public boolean containsReference(String sessID, String refName) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Select r from Reference r where r.name= :name");
        query.setParameter("name", refName);
        List<Reference> referenceList = query.execute();

        return (referenceList.size() > 0 ) ? true : false;
    }

    @Override
    public int addReference(String sessID, String refName, Chromosome[] chroms, String url) throws SQLException, SessionExpiredException {

        int referenceId = -1;

        try {
            referenceId = DBUtils.generateId("id", Entity.REFERENCE);
            Reference reference = new Reference(referenceId, refName, url);

            entityManager.persist(reference, true);

            int contigId = 0;
            for (Chromosome chromosome : chroms) {
                chromosome.setContigId(contigId++);
                chromosome.setReferenceId(referenceId);
            }

            entityManager.persistAll(Arrays.asList(chroms));
        } catch (InitializationException e) {
            LOG.error("Error persisting reference");
        }

        return referenceId;
    }

    @Override
    public boolean removeReference(String sessID, int refID) throws SQLException, SessionExpiredException {

        Query deleteReferenceQuery = queryManager.createQuery("Delete from Reference where r.id= :id");
        deleteReferenceQuery.setParameter("id", refID);
        deleteReferenceQuery.executeDelete();

        Query deleteChromosomesQuery = queryManager.createQuery("Delete from Chromosome where c.reference_id= :referenceId");
        deleteChromosomesQuery.setParameter("referenceId", refID);
        deleteChromosomesQuery.executeDelete();

        //should return false if no reference exists?
        return true;
    }

/*    @Override
    public Map<Integer, String> getReferencesWithoutTablesInProject(String sid,int projectid) throws SQLException, SessionExpiredException {

        ResultSet rs = ConnectionController.executeQuery(sid,
                "SELECT *"
                + " FROM " + ReferenceTableSchema.TABLE_NAME
                + " WHERE " + ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID + " NOT IN"
                + " (SELECT " + VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID + " FROM " + VariantTablemapTableSchema.TABLE_NAME
                + " WHERE " + VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID + "=" + projectid + ")");

        HashMap<Integer,String> result = new HashMap<Integer,String>();

        while (rs.next()) {
            result.put(rs.getInt(1), rs.getString(2));
        }

        return result;
    }*/

    @Override
    public String getReferenceUrl(String sid,int referenceid) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Select r from Reference r where r.id= :id");
        query.setParameter("id", referenceid);
        Reference reference = query.getFirst();

        return reference.getUrl();
    }

    @Override
    public Chromosome[] getChromosomes(String sid, int referenceid) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Select c from Chromosome c where c.reference_id= :referenceId");
        query.setParameter("referenceId", referenceid);
        List<Chromosome> chromosomeList = query.execute();
        return chromosomeList.toArray(new Chromosome[chromosomeList.size()]);
    }

    String getReferenceName(String sid, int refID) throws SQLException, SessionExpiredException {
        Query query = queryManager.createQuery("Select r from Reference r where r.id= :id");
        query.setParameter("id", refID);
        Reference reference = query.getFirst();

        return reference.getName();
    }
}
