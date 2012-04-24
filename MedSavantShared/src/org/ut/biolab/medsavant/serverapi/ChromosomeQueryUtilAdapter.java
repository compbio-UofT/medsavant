package org.ut.biolab.medsavant.serverapi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import org.ut.biolab.medsavant.model.Chromosome;

/**
 *
 * @author mfiume
 */
public interface ChromosomeQueryUtilAdapter extends Remote {

    public List<Chromosome> getContigs(String sid,int refid) throws SQLException, RemoteException;;
}
