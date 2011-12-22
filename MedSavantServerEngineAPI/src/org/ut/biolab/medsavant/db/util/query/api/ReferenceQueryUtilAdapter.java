package org.ut.biolab.medsavant.db.util.query.api;

import java.rmi.Remote;
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

    public List<Reference> getReferences(String sid) throws SQLException;

    public List<String> getReferenceNames(String sid) throws SQLException;

    public int getReferenceId(String sid, String refName) throws SQLException;

    public boolean containsReference(String sid, String name) throws SQLException;

    public int addReference(String sid, String name, List<Chromosome> contigs) throws SQLException;

    public int addReference(String sid, String name, List<Chromosome> contigs, String url) throws SQLException;

    public boolean removeReference(String sid, int refid) throws SQLException;

    public List<String> getReferencesForProject(String sid, int projectid) throws SQLException;

    public List<Integer> getReferenceIdsForProject(String sid, int projectid) throws SQLException;

    public Map<Integer, String> getReferencesWithoutTablesInProject(String sid, int projectid) throws SQLException;

    public String getReferenceUrl(String sid, int referenceid) throws SQLException;

    public List<Chromosome> getChromosomes(String sid, int referenceid) throws SQLException;
}
