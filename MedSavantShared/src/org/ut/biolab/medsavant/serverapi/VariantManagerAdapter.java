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

package org.ut.biolab.medsavant.serverapi;

import java.io.File;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.Column;

import org.ut.biolab.medsavant.clientapi.ProgressCallbackAdapter;
import org.ut.biolab.medsavant.db.TableSchema;
import org.ut.biolab.medsavant.format.CustomField;
import org.ut.biolab.medsavant.model.Range;
import org.ut.biolab.medsavant.model.ScatterChartMap;
import org.ut.biolab.medsavant.model.SimplePatient;
import org.ut.biolab.medsavant.model.SimpleVariantFile;
import org.ut.biolab.medsavant.model.StarredVariant;


/**
 *
 * @author mfiume
 */
public interface VariantManagerAdapter extends Remote {

    public int uploadVariants(String sid, RemoteInputStream[] fileStreams, String[] fileNames, int projectId, int referenceId, String[][] variantTags, boolean includeHomoRef) throws RemoteException, IOException, Exception;
    public void publishVariants(String sid, int projectID, int referenceID, int updateID) throws Exception;
    public void publishVariants(String sid, int projectID) throws Exception;
    public void cancelPublish(String sid, int projectID, int referenceID, int updateID) throws Exception;
    public int updateTable(String sid, int projectId, int referenceId, int[] annotationIds, List<CustomField> variantFields) throws Exception;
    public int removeVariants(String sid, int projectId, int referenceId, List<SimpleVariantFile> files) throws Exception;
    public RemoteInputStream exportVariants(String sessionId, int projectId, int referenceId, Condition[][] conditions) throws SQLException, RemoteException, IOException, InterruptedException;
  
    public TableSchema getCustomTableSchema(String sessionId, int projectId, int referenceId) throws SQLException, RemoteException;
    public List<Object[]> getVariants(String sessionId,int projectId, int referenceId, int start, int limit) throws SQLException, RemoteException;
    public List<Object[]> getVariants(String sessionId,int projectId, int referenceId, Condition[][] conditions, int start, int limit) throws SQLException, RemoteException;
    public List<Object[]> getVariants(String sessionId,int projectId, int referenceId, Condition[][] conditions, int start, int limit, Column[] order) throws SQLException, RemoteException;
    public List<Object[]> getVariants(String sessionId,int projectId, int referenceId, Condition[][] conditions, int start, int limit, Column[] order, Column[] columns) throws SQLException, RemoteException;
    public double[] getExtremeValuesForColumn(String sid,String tablename, String columnname) throws SQLException, RemoteException;
    public List<String> getDistinctValuesForColumn(String sid, String tablename, String columnname, boolean cache) throws SQLException, RemoteException;
    public int getNumFilteredVariants(String sid, int projectId, int referenceId) throws SQLException, RemoteException;
    public int getNumFilteredVariants(String sid,int projectId, int referenceId, Condition[][] conditions) throws SQLException, RemoteException;
    public int getNumVariantsForDnaIds(String sid, int projectId, int referenceId, Condition[][] conditions, List<String> dnaIds) throws SQLException, RemoteException;
    public Map<Range, Long> getFilteredFrequencyValuesForNumericColumn(String sid, int projectId, int referenceId, Condition[][] conditions, CustomField column, boolean logBins) throws SQLException, RemoteException;
    //public Map<String, Integer> getFilteredFrequencyValuesForCategoricalColumn(String sid, int projectId, int referenceId, Condition[][] conditions, String columnAlias) throws SQLException, RemoteException;
    //public Map<String, Integer> getFilteredFrequencyValuesForCategoricalColumn(String sid, DbTable table, Condition[][] conditions, DbColumn column) throws SQLException, RemoteException;
    public Map<String, Integer> getFilteredFrequencyValuesForCategoricalColumn(String sid, int projectId, int referenceId, Condition[][] conditions, String columnAlias) throws SQLException, RemoteException;
    public int getNumVariantsInRange(String sid, int projectId, int referenceId, Condition[][] conditions, String chrom, long start, long end) throws SQLException, RemoteException;
    public Map<String, Map<Range, Integer>> getChromosomeHeatMap(String sid, int projectId, int referenceId, Condition[][] conditions, int binsize) throws SQLException, RemoteException;
    //public int[] getNumVariantsForBins(String sid, int projectId, int referenceId, Condition[][] conditions, String chrom, int binsize, int numbins) throws SQLException, NonFatalDatabaseException, RemoteException;
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
    public Map<SimplePatient, Integer> getPatientHeatMap(String sid, int projectId, int referenceId, Condition[][] conditions, List<SimplePatient> patients) throws SQLException, RemoteException;
    public Map<String, Integer> getDnaIdHeatMap(String sid, int projectId, int referenceId, Condition[][] conditions, List<String> dnaIds) throws SQLException, RemoteException;
    public ScatterChartMap getFilteredFrequencyValuesForScatter(String sid, int projectId, int referenceId, Condition[][] conditions, String columnnameX, String columnnameY, boolean columnXCategorical, boolean columnYCategorical, boolean sortKaryotypically) throws SQLException, RemoteException;

    public void registerProgressCallback(String sessID, ProgressCallbackAdapter callback) throws RemoteException;
}

