package org.ut.biolab.medsavant.db.util.query.api;

import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.model.Range;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;

/**
 *
 * @author mfiume
 */
public interface QueryUtilAdapter extends Remote {

    public List<String> getDistinctValuesForColumn(String sid,TableSchema t, DbColumn col) throws SQLException, RemoteException;
    public List<String> getDistinctValuesForColumn(String sid,TableSchema t, DbColumn col, int limit) throws SQLException, RemoteException;
    public Range getExtremeValuesForColumn(String sid,TableSchema t, DbColumn col) throws SQLException, RemoteException;
    public List<String> getBAMFilesForDNAIds(List<String> dnaIds) throws SQLException, NonFatalDatabaseException, RemoteException;
    public String getGenomeBAMPathForVersion(String genomeVersion) throws SQLException, NonFatalDatabaseException, RemoteException;
    public Condition getRangeCondition(DbColumn col, Range r) throws RemoteException;
    public int getNumRecordsInTable(String sid,String name) throws SQLException, RemoteException;
}
