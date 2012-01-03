package org.ut.biolab.medsavant.db.util.query.api;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;

/**
 *
 * @author mfiume
 */
public interface SettingsQueryUtilAdapter extends Remote {

    public void addSetting(String sid, String key, String value) throws SQLException, RemoteException;
    public String getSetting(String sid, String key) throws SQLException, RemoteException;

}
