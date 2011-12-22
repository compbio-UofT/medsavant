package org.ut.biolab.medsavant.db.util.query.api;

import java.rmi.Remote;
import java.sql.SQLException;
import java.util.List;
import org.ut.biolab.medsavant.db.model.Chromosome;

/**
 *
 * @author mfiume
 */
public interface ChromosomeQueryUtilAdapter extends Remote {

    public List<Chromosome> getContigs(String sid,int refid) throws SQLException;
}
