/*
 *    Copyright 2012 University of Toronto
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

package org.ut.biolab.medsavant.shared.serverapi;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;


/**
 *
 * @author Andrew
 */
public interface SetupAdapter extends Remote {

    public void createDatabase(String dbHost, int port, String dbname, String adminName, char[] rootPassword, String versionString) throws IOException, SQLException, RemoteException, SessionExpiredException;

    public void removeDatabase(String dbHost, int port, String dbname, String adminName, char[] rootPassword) throws SQLException, RemoteException, SessionExpiredException;

    public String getServerVersion() throws RemoteException, SessionExpiredException;
}
