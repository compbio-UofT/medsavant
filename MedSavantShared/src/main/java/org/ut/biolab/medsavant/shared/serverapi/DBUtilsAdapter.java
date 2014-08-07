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
import java.util.List;

import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;

import org.ut.biolab.medsavant.shared.model.ProgressStatus;
import org.ut.biolab.medsavant.shared.model.Range;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;


/**
 *
 * @author Andrew
 */
public interface DBUtilsAdapter extends Remote {

    /**
     * Check the status of a lengthy process, giving the user the option to cancel.
     */
    ProgressStatus checkProgress(String sessID, boolean userCancelled) throws RemoteException, SessionExpiredException;    

    public List<String> getDistinctValuesForColumn(String sessID, String tableName, String columnName, boolean useCache) throws InterruptedException, SQLException, RemoteException, SessionExpiredException;
    public List<String> getDistinctValuesForColumn(String sessID, String tableName, String columnName, boolean explodeCommaSeparated, boolean useCache) throws InterruptedException, SQLException, RemoteException, SessionExpiredException;
    public Range getExtremeValuesForColumn(String sid, String tablename, String columnname) throws InterruptedException, SQLException, RemoteException, SessionExpiredException;
    public Condition getRangeCondition(DbColumn col, Range r) throws RemoteException;
    public int getNumRecordsInTable(String sessID, String name) throws SQLException, RemoteException, SessionExpiredException;
}
