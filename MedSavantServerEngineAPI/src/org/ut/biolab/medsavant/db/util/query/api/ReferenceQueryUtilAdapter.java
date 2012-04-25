package org.ut.biolab.medsavant.db.util.query.api;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.ut.biolab.medsavant.db.model.Chromosome;
import org.ut.biolab.medsavant.db.model.Reference;

/**
 *
 * @author mfiume
 */
public interface ReferenceQueryUtilAdapter extends Remote {

    public List<Reference> getReferences(String sid) throws SQLException, RemoteException;

    public List<String> getReferenceNames(String sid) throws SQLException, RemoteException;

    public int getReferenceId(String sid, String refName) throws SQLException, RemoteException;

    public boolean containsReference(String sid, String name) throws SQLException, RemoteException;

    public int addReference(String sid, String name, List<Chromosome> contigs) throws SQLException, RemoteException;

    public int addReference(String sid, String name, List<Chromosome> contigs, String url) throws SQLException, RemoteException;

    public boolean removeReference(String sid, int refid) throws SQLException, RemoteException;

    public List<String> getReferencesForProject(String sid, int projectid) throws SQLException, RemoteException;

    public List<Integer> getReferenceIdsForProject(String sid, int projectid) throws SQLException, RemoteException;

    public Map<Integer, String> getReferencesWithoutTablesInProject(String sid, int projectid) throws SQLException, RemoteException;

    public String getReferenceUrl(String sid, int referenceid) throws SQLException, RemoteException;

    public List<Chromosome> getChromosomes(String sid, int referenceid) throws SQLException, RemoteException;
}
