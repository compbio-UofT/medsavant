package org.ut.biolab.medsavant.db.util.query.api;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import java.io.File;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.model.Range;
import org.ut.biolab.medsavant.db.model.SimpleVariantFile;
import org.ut.biolab.medsavant.db.model.StarredVariant;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;

/**
 *
 * @author mfiume
 */
public interface VariantQueryUtilAdapter extends Remote {

    public TableSchema getCustomTableSchema(String sessionId, int projectId, int referenceId) throws SQLException, RemoteException;
    public List<Object[]> getVariants(String sessionId,int projectId, int referenceId, int start, int limit) throws SQLException, RemoteException;
    public List<Object[]> getVariants(String sessionId,int projectId, int referenceId, Condition[][] conditions, int start, int limit) throws SQLException, RemoteException;
    public List<Object[]> getVariants(String sessionId,int projectId, int referenceId, Condition[][] conditions, int start, int limit, Column[] order) throws SQLException, RemoteException;
    public double[] getExtremeValuesForColumn(String sid,String tablename, String columnname) throws SQLException, RemoteException;
    public List<String> getDistinctValuesForColumn(String sid, String tablename, String columnname) throws SQLException, RemoteException;
    public int getNumFilteredVariants(String sid, int projectId, int referenceId) throws SQLException, RemoteException;
    public int getNumFilteredVariants(String sid,int projectId, int referenceId, Condition[][] conditions) throws SQLException, RemoteException;
    public int getNumVariantsForDnaIds(String sid, int projectId, int referenceId, Condition[][] conditions, List<String> dnaIds) throws SQLException, RemoteException;
    public Map<Range,Long> getFilteredFrequencyValuesForNumericColumn(String sid, int projectId, int referenceId, Condition[][] conditions, String columnname, double min, double binSize) throws SQLException, RemoteException;
    public Map<String, Integer> getFilteredFrequencyValuesForCategoricalColumn(String sid, int projectId, int referenceId, Condition[][] conditions, String columnAlias) throws SQLException, RemoteException;
    public Map<String, Integer> getFilteredFrequencyValuesForCategoricalColumn(String sid, DbTable table, Condition[][] conditions, DbColumn column) throws SQLException, RemoteException;
    public int getNumVariantsInRange(String sid, int projectId, int referenceId, Condition[][] conditions, String chrom, long start, long end) throws SQLException, NonFatalDatabaseException, RemoteException;
    public Map<String, Map<Range, Integer>> getChromosomeHeatMap(String sid, int projectId, int referenceId, Condition[][] conditions, int binsize) throws SQLException, RemoteException;
    public int[] getNumVariantsForBins(String sid, int projectId, int referenceId, Condition[][] conditions, String chrom, int binsize, int numbins) throws SQLException, NonFatalDatabaseException, RemoteException;
    public void uploadFileToVariantTable(String sid, File file, String tableName) throws SQLException, RemoteException;
    public int getNumPatientsWithVariantsInRange(String sid, int projectId, int referenceId, Condition[][] conditions, String chrom, int start, int end) throws SQLException, RemoteException;
    public void addConditionsToQuery(SelectQuery query, Condition[][] conditions) throws RemoteException;
    public Map<String, List<String>> getSavantBookmarkPositionsForDNAIds(String sid, int projectId, int referenceId, Condition[][] conditions, List<String> dnaIds, int limit) throws SQLException, RemoteException;
    public Map<String, Integer> getNumVariantsInFamily(String sid, int projectId, int referenceId, String familyId, Condition[][] conditions) throws SQLException, RemoteException;
    public void cancelUpload(String sid,int uploadId, String tableName) throws RemoteException;
    public void addTagsToUpload(String sid, int uploadID, String[][] variantTags) throws SQLException, RemoteException;
    public List<String> getDistinctTagNames(String sid) throws SQLException, RemoteException;
    public List<String> getValuesForTagName(String sid, String tagName) throws SQLException, RemoteException;
    public List<Integer> getUploadIDsMatchingVariantTags(String sid, String[][] variantTags) throws SQLException, RemoteException;
    public List<SimpleVariantFile> getUploadedFiles(String sid, int projectId, int referenceId) throws SQLException, RemoteException;
    public List<String[]> getTagsForUpload(String sid, int uploadId) throws SQLException, RemoteException;
    public Set<StarredVariant> getStarredVariants(String sid, int projectId, int referenceId) throws SQLException, RemoteException;
    public int addStarredVariants(String sid, int projectId, int referenceId, List<StarredVariant> variant) throws SQLException, RemoteException;
    public void unstarVariant(String sid, int projectId, int referenceId, int uploadId, int fileId, int variantId, String user) throws SQLException, RemoteException;    

}
