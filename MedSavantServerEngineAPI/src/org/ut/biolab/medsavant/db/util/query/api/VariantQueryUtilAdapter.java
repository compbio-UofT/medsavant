package org.ut.biolab.medsavant.db.util.query.api;

import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import java.io.File;
import java.rmi.Remote;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.model.Range;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;

/**
 *
 * @author mfiume
 */
public interface VariantQueryUtilAdapter extends Remote {

    public TableSchema getCustomTableSchema(String sessionId, int projectId, int referenceId) throws SQLException;
    public List<Object[]> getVariants(String sessionId,int projectId, int referenceId, int start, int limit) throws SQLException;
    public List<Object[]> getVariants(String sessionId,int projectId, int referenceId, Condition[][] conditions, int start, int limit) throws SQLException;
    public double[] getExtremeValuesForColumn(String sid,String tablename, String columnname) throws SQLException;
    public List<String> getDistinctValuesForColumn(String sid, String tablename, String columnname) throws SQLException;
    public int getNumFilteredVariants(String sid, int projectId, int referenceId) throws SQLException;
    public int getNumFilteredVariants(String sid,int projectId, int referenceId, Condition[][] conditions) throws SQLException;
    public int getNumVariantsForDnaIds(String sid, int projectId, int referenceId, Condition[][] conditions, List<String> dnaIds) throws SQLException;
    public int getFilteredFrequencyValuesForColumnInRange(String sid, int projectId, int referenceId, Condition[][] conditions, String columnname, double min, double max) throws SQLException;
    public Map<String, Integer> getFilteredFrequencyValuesForColumn(String sid, int projectId, int referenceId, Condition[][] conditions, String columnAlias) throws SQLException;
    public Map<String, Integer> getFilteredFrequencyValuesForColumn(String sid, DbTable table, Condition[][] conditions, DbColumn column) throws SQLException;
    public int getNumVariantsInRange(String sid, int projectId, int referenceId, Condition[][] conditions, String chrom, long start, long end) throws SQLException, NonFatalDatabaseException;
    public Map<String, Map<Range, Integer>> getChromosomeHeatMap(String sid, int projectId, int referenceId, Condition[][] conditions, int binsize) throws SQLException;
    public int[] getNumVariantsForBins(String sid, int projectId, int referenceId, Condition[][] conditions, String chrom, int binsize, int numbins) throws SQLException, NonFatalDatabaseException;
    public void uploadFileToVariantTable(String sid, File file, String tableName) throws SQLException;
    public int getNumPatientsWithVariantsInRange(String sid, int projectId, int referenceId, Condition[][] conditions, String chrom, int start, int end) throws SQLException;
    public void addConditionsToQuery(SelectQuery query, Condition[][] conditions);
    public Map<String, List<String>> getSavantBookmarkPositionsForDNAIds(String sid, int projectId, int referenceId, Condition[][] conditions, List<String> dnaIds, int limit) throws SQLException;
    public Map<String, Integer> getNumVariantsInFamily(String sid, int projectId, int referenceId, String familyId, Condition[][] conditions) throws SQLException;
    public void cancelUpload(String sid,int uploadId, String tableName);
    public void addTagsToUpload(String sid, int uploadID, String[][] variantTags) throws SQLException;
    public List<String> getDistinctTagNames(String sid) throws SQLException;
    public List<String> getValuesForTagName(String sid, String tagName) throws SQLException;
    public List<Integer> getUploadIDsMatchingVariantTags(String sid, String[][] variantTags) throws SQLException;
}
