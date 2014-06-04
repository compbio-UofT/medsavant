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

import java.io.File;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.SelectQuery;

import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.exception.LockException;
import org.ut.biolab.medsavant.shared.model.ProgressStatus;
import org.ut.biolab.medsavant.shared.model.Range;
import org.ut.biolab.medsavant.shared.model.ScatterChartMap;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.model.SimplePatient;
import org.ut.biolab.medsavant.shared.model.SimpleVariantFile;
import org.ut.biolab.medsavant.shared.model.VariantComment;
import org.ut.biolab.medsavant.shared.util.Modifier;
import static org.ut.biolab.medsavant.shared.util.ModificationType.*;
import org.ut.biolab.medsavant.shared.model.UserComment;
import org.ut.biolab.medsavant.shared.model.UserCommentGroup;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;


/**
 *
 * @author mfiume
 */
public interface VariantManagerAdapter extends Remote {

    /**
     * Check the status of a lengthy process, giving the user the option to cancel.
     */   
    ProgressStatus checkProgress(String sessID, boolean userCancelled) throws RemoteException, SessionExpiredException;

    //These methods modify the database, but nothing happens until publishVariants is called, at which point MedSavant exits anyway.
    public int uploadVariants(String sessID, int[] fileIDs, int projID, int refID, String[][] variantTags, boolean includeHomoRef, String email, boolean autoPublish, boolean preAnnotateWithAnnovar, boolean doPhasing) throws RemoteException, IOException, LockException, Exception;
    public int uploadVariants(String sessID, File dirContainingVCFs, int projID, int refID, String[][] tags, boolean includeHomoRef, String email, boolean autoPublish, boolean preAnnotateWithAnnovar, boolean doPhasing) throws RemoteException, IOException, LockException, Exception;
    
    //Synonym for uplaodVariants -- for compatibility with JSON client.
    public int uploadTransferredVariants(String sessID, int[] fileIDs, int projID, int refID, String[][] variantTags, boolean includeHomoRef, String email, boolean autoPublish, boolean preAnnotateWithAnnovar, boolean doPhasing) throws RemoteException, IOException, LockException, Exception;
    public void publishVariants(String sessID, int projID, int referenceID, int updateID) throws LockException, Exception;
    public void publishVariants(String sessID, int projID) throws LockException, Exception;
    public void cancelPublish(String sessID, int projID, int referenceID, int updateID) throws LockException, Exception;

    //for testing, unnecessary because medsavant exits anyway after publishing.
    @Modifier(type = VARIANT)
    public int updateTable(String sessID, int projID, int refID, int[] annotIDs, CustomField[] variantFields, boolean autoPublish, String email) throws LockException, Exception;

    //for testing, unnecessary because medsavant exits anyway after publishing.
    @Modifier(type = VARIANT)
    public int removeVariants(String sessID, int projID, int refID, List<SimpleVariantFile> files, boolean autoPublish, String email) throws LockException, Exception;

    //For testing.
    @Modifier(type = VARIANT)
    public int exportVariants(String sessID, int projID, int refID, Condition[][] conditions, boolean orderedByPosition, boolean zipped) throws SQLException, SessionExpiredException, RemoteException, IOException, InterruptedException;

    public TableSchema getCustomTableSchema(String sessID, int projID, int refID) throws SQLException, RemoteException, SessionExpiredException;
    public List<Object[]> getVariants(String sessID,int projID, int refID, int start, int limit) throws SQLException, RemoteException, SessionExpiredException;
    public List<Object[]> getVariants(String sessID,int projID, int refID, Condition[][] conditions, int start, int limit) throws SQLException, RemoteException, SessionExpiredException;
    public List<Object[]> getVariants(String sessID,int projID, int refID, Condition[][] conditions, int start, int limit, String[] orderByCols) throws SQLException, RemoteException, SessionExpiredException;	
    public int getVariantCount(String sessID, int projID, int refID) throws SQLException, RemoteException, SessionExpiredException;
    public int getFilteredVariantCount(String sessID,int projID, int refID, Condition[][] conditions) throws SQLException, RemoteException, SessionExpiredException;
    public int getVariantCountForDNAIDs(String sessID, int projID, int refID, Condition[][] conditions, Collection<String> dnaIDs) throws SQLException, RemoteException, SessionExpiredException;
    public Map<Range, Long> getFilteredFrequencyValuesForNumericColumn(String sessID, int projID, int refID, Condition[][] conditions, CustomField column, boolean logBins) throws InterruptedException, SQLException, RemoteException, SessionExpiredException;
    public Map<String, Integer> getFilteredFrequencyValuesForCategoricalColumn(String sessID, int projID, int refID, Condition[][] conditions, String columnAlias) throws SQLException, RemoteException, SessionExpiredException;
    public ScatterChartMap getFilteredFrequencyValuesForScatter(String sessID, int projID, int refID, Condition[][] conditions, String columnnameX, String columnnameY, boolean columnXCategorical, boolean columnYCategorical, boolean sortKaryotypically) throws InterruptedException, SQLException, RemoteException, SessionExpiredException;
    public int getVariantCountInRange(String sessID, int projID, int refID, Condition[][] conditions, String chrom, long start, long end) throws SQLException, RemoteException, SessionExpiredException;
    public Map<String, Map<Range, Integer>> getChromosomeHeatMap(String sessID, int projID, int refID, Condition[][] conditions, int binsize) throws SQLException, RemoteException, SessionExpiredException;
    //public void uploadFileToVariantTable(String sessID, File file, String tableName) throws SQLException, IOException, RemoteException, SessionExpiredException;
    public int getPatientCountWithVariantsInRange(String sessID, int projID, int refID, Condition[][] conditions, String chrom, int start, int end) throws SQLException, RemoteException, SessionExpiredException;
    public void addConditionsToQuery(SelectQuery query, Condition[][] conditions) throws RemoteException, SessionExpiredException;
    public Map<String, List<String>> getSavantBookmarkPositionsForDNAIDs(String sessID, int projID, int refID, Condition[][] conditions, List<String> dnaIDs, int limit) throws SQLException, RemoteException, SessionExpiredException;
    public Map<String, Integer> getNumVariantsInFamily(String sessID, int projID, int refID, String famID, Condition[][] conditions) throws SQLException, RemoteException, SessionExpiredException;
    public void cancelUpload(String sessID,int uploadID, String tableName) throws RemoteException, SessionExpiredException;

    public void addTagsToUpload(String sessID, int uploadID, String[][] variantTags) throws SQLException, RemoteException, SessionExpiredException;
    public List<String> getDistinctTagNames(String sessID) throws SQLException, RemoteException, SessionExpiredException;
    public List<String> getValuesForTagName(String sessID, String tagName) throws SQLException, RemoteException, SessionExpiredException;
    public List<Integer> getUploadIDsMatchingVariantTags(String sessID, String[][] variantTags) throws SQLException, RemoteException, SessionExpiredException;
    public SimpleVariantFile[] getUploadedFiles(String sessID, int projID, int refID) throws SQLException, RemoteException, SessionExpiredException;
    public List<String[]> getTagsForUpload(String sessID, int uploadID) throws SQLException, RemoteException, SessionExpiredException;
    //public Set<StarredVariant> getStarredVariants(String sessID, int projID, int refID) throws SQLException, RemoteException, SessionExpiredException;
    public List<VariantComment> getVariantComments(String sessID, int projID, int refID, int uploadId, int fileID, int variantID) throws SQLException, RemoteException, SessionExpiredException;

    @Modifier(type = VARIANT_COMMENT)
    public void addVariantComments(String sessID, List<VariantComment> comments) throws SQLException, RemoteException, SessionExpiredException;

    @Modifier(type = VARIANT_COMMENT)
    public void removeVariantComments(String sessID, List<VariantComment> comments) throws SQLException, RemoteException, SessionExpiredException;
    public Map<SimplePatient, Integer> getPatientHeatMap(String sessID, int projID, int refID, Condition[][] conditions, Collection<SimplePatient> patients) throws SQLException, RemoteException, SessionExpiredException;
    public Map<String, Integer> getDNAIDHeatMap(String sessID, int projID, int refID, Condition[][] conditions, Collection<String> dnaIDs) throws SQLException, RemoteException, SessionExpiredException;
    public boolean willApproximateCountsForConditions(String sessID, int projID, int refID, Condition[][] conditions) throws SQLException, RemoteException, SessionExpiredException;
    
    @Modifier(type = LOCUS_COMMENT)
    public int replyToUserCommentGroup(String sessID, int locusCommentGroupId, UserComment locusComment) throws SessionExpiredException, SQLException, RemoteException, SecurityException;
    public UserCommentGroup getUserCommentGroup(String sessID, int projectId, int refId, String chrom, long start_position, long end_position, String ref, String alt) throws RemoteException, SessionExpiredException, SQLException, SecurityException;    
    public UserCommentGroup getUserCommentGroup(String sessID, int projectId, int refId, VariantRecord vr) throws RemoteException, SessionExpiredException, SQLException, SecurityException;
    public UserCommentGroup createUserCommentGroup(String sessID, int projectId, int refId, String chrom, long start_position, long end_position, String ref, String alt) throws RemoteException, SQLException, SessionExpiredException, IllegalArgumentException;
    public UserCommentGroup createUserCommentGroup(String sessID, int projectId, int refId, VariantRecord vr) throws RemoteException, SQLException, SessionExpiredException, IllegalArgumentException;
}

