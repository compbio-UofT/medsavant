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

package org.ut.biolab.medsavant.shared.serverapi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import org.ut.biolab.medsavant.shared.model.Chromosome;
import org.ut.biolab.medsavant.shared.model.Reference;



/**
 * Remote class which manages the reference and its associated sequence, chromosome, and gene information.
 *
 * @author mfiume
 */
public interface ReferenceManagerAdapter extends Remote {

    public Reference[] getReferences(String sessID) throws SQLException, RemoteException;

    public String[] getReferenceNames(String sessID) throws SQLException, RemoteException;

    public int getReferenceID(String sessID, String refName) throws SQLException, RemoteException;

    public boolean containsReference(String sessID, String name) throws SQLException, RemoteException;

    public int addReference(String sessID, String name, Chromosome[] chroms, String url) throws SQLException, RemoteException;

    public boolean removeReference(String sessID, int refid) throws SQLException, RemoteException;

    public String getReferenceUrl(String sessID, int refID) throws SQLException, RemoteException;

    public Chromosome[] getChromosomes(String sessID, int refID) throws SQLException, RemoteException;
}
