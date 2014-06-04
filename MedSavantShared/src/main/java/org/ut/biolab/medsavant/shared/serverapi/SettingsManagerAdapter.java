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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import org.ut.biolab.medsavant.shared.model.exception.LockException;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.model.exception.UnauthorizedException;


/**
 * Manager which takes care of server-side settings.
 *
 * @author mfiume
 */
public interface SettingsManagerAdapter extends Remote {

    public void addSetting(String sid, String key, String value) throws SQLException, RemoteException, SessionExpiredException;
    public String getSetting(String sid, String key) throws SQLException, RemoteException, SessionExpiredException;
    public void updateSetting(String sid, String key, String value) throws SQLException, RemoteException, SessionExpiredException;
    public void removeSetting(String sid, String key) throws SQLException, SessionExpiredException, RemoteException;

    //public boolean getDBLock(String sid) throws SQLException, RemoteException, SessionExpiredException;
    //public void releaseDBLock(String sid) throws SQLException, RemoteException, SessionExpiredException;
    public String getServerVersion() throws RemoteException, SessionExpiredException;

    public boolean isProjectLockedForChanges(String database, int projectID) throws RemoteException, SessionExpiredException;
    public void forceReleaseLockForProject(String sessionID, int projectID) throws RemoteException, SessionExpiredException, SQLException, LockException, UnauthorizedException;
    
}
