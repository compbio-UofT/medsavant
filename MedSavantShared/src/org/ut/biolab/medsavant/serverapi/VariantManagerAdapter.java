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
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
import org.ut.biolab.medsavant.model.VariantComment;


/**
 *
 * @author mfiume
 */
public interface VariantManagerAdapter extends Remote {

    public int uploadVariants(String sessID, RemoteInputStream[] fileStreams, String[] fileNames, int projID, int refID, String[][] variantTags, boolean includeHomoRef) throws RemoteException, IOException, Exception;
    public int uploadVariants(String sessID, File dirContainingVCFs, int projID, int refID, String[][] tags, boolean includeHomoRef) throws RemoteException, IOException, Exception;
    public void publishVariants(String sessID, int projID, int referenceID, int updateID) throws Exception;
    public void publishVariants(String sessID, int projID) throws Exception;
    public void cancelPublish(String sessID, int projID, int referenceID, int updateID) throws Exception;
    public int updateTable(String sessID, int projID, int refID, int[] annotIDs, CustomField[] variantFields) throws Exception;
    public int removeVariants(String sessID, int projID, int refID, List<SimpleVariantFile> files) throws Exception;
    public RemoteInputStream exportVariants(String sessID, int projID, int refID, Condition[][] conditions) throws SQLException, RemoteException, IOException, InterruptedException;

    public TableSchema getCustomTableSchema(String sessID, int projID, int refID) throws SQLException, RemoteException;
    public List<Object[]> getVariants(String sessID,int projID, int refID, int start, int limit) throws SQLException, RemoteException;
    public List<Object[]> getVariants(String sessID,int projID, int refID, Condition[][] conditions, int start, int limit) throws SQLException, RemoteException;
    public List<Object[]> getVariants(String sessID,int projID, int refID, Condition[][] conditions, int start, int limit, Column[] order) throws SQLException, RemoteException;
    public List<Object[]> getVariants(String sessID,int projID, int refID, Condition[][] conditions, int start, int limit, Column[] order, Column[] columns) throws SQLException, RemoteException;
    public int getVariantCount(String sessID, int projID, int refID) throws SQLException, RemoteException;
    public int getFilteredVariantCount(String sessID,int projID, int refID, Condition[][] conditions) throws SQLException, RemoteException;
    public int getVariantCountForDNAIDs(String sessID, int projID, int refID, Condition[][] conditions, Collection<String> dnaIDs) throws SQLException, RemoteException;
    public Map<Range, Long> getFilteredFrequencyValuesForNumericColumn(String sessID, int projID, int refID, Condition[][] conditions, CustomField column, boolean logBins) throws SQLException, RemoteException;
    public Map<String, Integer> getFilteredFrequencyValuesForCategoricalColumn(String sessID, int projID, int refID, Condition[][] conditions, String columnAlias) throws SQLException, RemoteException;
    public int getVariantCountInRange(String sessID, int projID, int refID, Condition[][] conditions, String chrom, long start, long end) throws SQLException, RemoteException;
    public Map<String, Map<Range, Integer>> getChromosomeHeatMap(String sessID, int projID, int refID, Condition[][] conditions, int binsize) throws SQLException, RemoteException;
    //public void uploadFileToVariantTable(String sessID, File file, String tableName) throws SQLException, IOException, RemoteException;
    public int getPatientCountWithVariantsInRange(String sessID, int projID, int refID, Condition[][] conditions, String chrom, int start, int end) throws SQLException, RemoteException;
    public void addConditionsToQuery(SelectQuery query, Condition[][] conditions) throws RemoteException;
    public Map<String, List<String>> getSavantBookmarkPositionsForDNAIDs(String sessID, int projID, int refID, Condition[][] conditions, List<String> dnaIDs, int limit) throws SQLException, RemoteException;
    public Map<String, Integer> getNumVariantsInFamily(String sessID, int projID, int refID, String famID, Condition[][] conditions) throws SQLException, RemoteException;
    public void cancelUpload(String sessID,int uploadID, String tableName) throws RemoteException;
    public void addTagsToUpload(String sessID, int uploadID, String[][] variantTags) throws SQLException, RemoteException;
    public List<String> getDistinctTagNames(String sessID) throws SQLException, RemoteException;
    public List<String> getValuesForTagName(String sessID, String tagName) throws SQLException, RemoteException;
    public List<Integer> getUploadIDsMatchingVariantTags(String sessID, String[][] variantTags) throws SQLException, RemoteException;
    public SimpleVariantFile[] getUploadedFiles(String sessID, int projID, int refID) throws SQLException, RemoteException;
    public List<String[]> getTagsForUpload(String sessID, int uploadID) throws SQLException, RemoteException;
    //public Set<StarredVariant> getStarredVariants(String sessID, int projID, int refID) throws SQLException, RemoteException;
    public List<VariantComment> getVariantComments(String sessID, int projID, int refID, int uploadId, int fileID, int variantID) throws SQLException, RemoteException;
    public void addVariantComments(String sessID, List<VariantComment> comments) throws SQLException, RemoteException;
    public void removeVariantComments(String sessID, List<VariantComment> comments) throws SQLException, RemoteException;
    public Map<SimplePatient, Integer> getPatientHeatMap(String sessID, int projID, int refID, Condition[][] conditions, Collection<SimplePatient> patients) throws SQLException, RemoteException;
    public Map<String, Integer> getDNAIDHeatMap(String sessID, int projID, int refID, Condition[][] conditions, Collection<String> dnaIDs) throws SQLException, RemoteException;
    public ScatterChartMap getFilteredFrequencyValuesForScatter(String sessID, int projID, int refID, Condition[][] conditions, String columnnameX, String columnnameY, boolean columnXCategorical, boolean columnYCategorical, boolean sortKaryotypically) throws SQLException, RemoteException;

    public void registerProgressCallback(String sessID, ProgressCallbackAdapter callback) throws RemoteException;
}

