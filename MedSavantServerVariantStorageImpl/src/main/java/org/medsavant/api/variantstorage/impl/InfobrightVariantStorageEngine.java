/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.medsavant.api.variantstorage.impl;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.CustomSql;
import com.healthmarketscience.sqlbuilder.FunctionCall;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.OrderObject.Dir;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.medsavant.api.annotation.TabixAnnotation;
import org.medsavant.api.common.GenomicVariant;
import org.medsavant.api.common.InvalidConfigurationException;
import org.medsavant.api.common.MedSavantDatabaseException;
import org.medsavant.api.common.MedSavantProject;
import org.medsavant.api.common.MedSavantSecurityException;
import org.medsavant.api.common.MedSavantServerContext;
import org.medsavant.api.common.MedSavantSession;
import org.medsavant.api.common.MedSavantUpdate;
import org.medsavant.api.common.Reference;
import org.medsavant.api.common.impl.GenomicVariantFactory;
import org.medsavant.api.database.CustomTableUtils;
import org.medsavant.api.database.MedSavantJDBCPooledConnection;
import org.medsavant.api.filestorage.MedSavantFile;
import org.medsavant.api.filestorage.MedSavantFileDirectoryException;
import org.medsavant.api.filestorage.MedSavantFileType;
import org.medsavant.api.variantstorage.GenomicVariantRecord;
import org.medsavant.api.variantstorage.MedSavantField;
import org.medsavant.api.variantstorage.PublicationStatus;
import org.medsavant.api.variantstorage.VariantField;
import org.medsavant.api.variantstorage.MedSavantVariantStorageEngine;
import org.medsavant.api.variantstorage.VariantFilter;
import org.medsavant.api.variantstorage.VariantFilterImpl;
import org.medsavant.api.variantstorage.impl.schemas.DBSchemas;
import org.medsavant.api.variantstorage.impl.schemas.VariantTablemapTableSchema;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.model.AnnotationLog;
import org.ut.biolab.medsavant.shared.util.BinaryConditionMS;
import org.ut.biolab.medsavant.shared.util.IOUtils;

/**
 *
 * @author jim
 */
public class InfobrightVariantStorageEngine implements MedSavantVariantStorageEngine {

    private Log LOG = LogFactory.getLog(InfobrightVariantStorageEngine.class);

    private static final int COUNT_ESTIMATE_THRESHOLD = 1000;
    private static final String SERVER_VERSION_KEY = "server version";
    private CustomTableUtils ctu;
    private MedSavantServerContext serverContext;

    private ResultSet executeQuery(String query) {
        //TODO
    }

    private void executeUpdate(String query) {

    }

    private TableSchema getLatestViewSchema(String databaseName, int projectId, int refId) throws SQLException {
        String viewName = DBSettings.getVariantViewName(projectId, refId);
        return ctu.getCustomTableSchema(getConnection(), databaseName, viewName);
    }

    //This method should be removed.  TODO: replace calls to this method with something like
    //ctu.getCustomTableSchema(getConnection(), session.getDatabaseName(), viewName, false);
    private TableSchema getCustomTableSchema(String tableName) {
        //TODO
    }

    private MedSavantJDBCPooledConnection getConnection() {

    }

    private String[] getTSVArray(GenomicVariantRecord gvr, TableSchema variantTableSchema) {

    }

    private Object[] getVariantTableViewInfo(int projectid, int refid) throws SQLException {
        TableSchema table = DBSchemas.VarianttablemapTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(
                table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_SUBSET_TABLENAME),
                table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_SUBSET_MULTIPLIER));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), refid));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED), true));

        query.addOrdering(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), Dir.DESCENDING);

        ResultSet rs = null;
        try {
            rs = executeQuery(query.toString());

            if (rs.next()) {
                //If the subset table doesn't exist, then the subset view cannot exist either.
                rs.getString(1);
                String subsetViewName = rs.wasNull() ? null : DBSettings.getVariantSubsetViewName(projectid, refid);

                return new Object[]{
                    DBSettings.getVariantViewName(projectid, refid),
                    subsetViewName,
                    rs.getFloat(2)};
            } else {
                return null;
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
    }

    private int getNumFilteredVariantsHelper(String tableViewName, Condition condition) throws SQLException {
        TableSchema table = getCustomTableSchema(tableViewName);

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addCustomColumns(FunctionCall.countAll());
        q.addCondition(condition);
        LOG.info(q);

        ResultSet rs = null;
        try {
            rs = executeQuery(q.toString());
            rs.next();
            return rs.getInt(1);
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
    }

    //private TableSchema getUnpublishedVariantTableSchema(MedSavantSession session, MedSavantUpdate update) throws SQLException {               
       /* //Load the schema from variant_tablemap
     VariantTableSchemaBuilder vtsb = 
     new VariantTableSchemaBuilder(session.getDatabaseName(), update).lo
        
     vtsb.load(getConnection(), update.getUpdateID());
        
     //Gets the tablename corresponding to given identifiers.
     String tableName = DBSettings.getVariantTableName(update.getProject().getProjectId(), update.getReference().getID(), update.getUpdateID());
     TableSchema tableSchema = ctu.getCustomTableSchema(getConnection(), session.getDatabaseName(), tableName, false);
     return tableSchema;
     */
    //}
    //uploadId, projectId, referenceId, name -> msFile.getName();
    //deprecate in favor of registerUpdate?
    private synchronized List<Integer> getFileIDs(int uploadId, int projectID, int referenceID, List<MedSavantFile> msFiles) throws SQLException {
        TableSchema table = DBSchemas.VariantFileTableSchema;

        List<Integer> fileIds = new ArrayList<Integer>(msFiles.size());
        for (MedSavantFile msFile : msFiles) {
            InsertQuery q = new InsertQuery(table.getTable());
            q.addColumn(table.getDBColumn(DBSchemas.VariantFileTableSchema.COLUMNNAME_OF_UPLOAD_ID), uploadId);
            //q.addColumn(table.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_FILE_ID), fileId);
            q.addColumn(table.getDBColumn(DBSchemas.VariantFileTableSchema.COLUMNNAME_OF_PROJECT_ID), projectID);
            q.addColumn(table.getDBColumn(DBSchemas.VariantFileTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceID);
            q.addColumn(table.getDBColumn(DBSchemas.VariantFileTableSchema.COLUMNNAME_OF_FILE_NAME), msFile.getName());
            executeUpdate(q.toString());

            String query = "SELECT last_insert_id() AS last_id from " + table.getTableName();
            ResultSet rs = null;
            try {
                rs = executeQuery(query);
                if (!rs.first()) {
                    throw new SQLException("Couldn't fetch file_id for file " + msFile.getName() + " on project " + projectID + ", ref " + referenceID);
                }

                int file_id = rs.getInt(1);
                if (file_id < 1) {//assert
                    throw new SQLException("Invalid file_id for file " + msFile.getName() + " on project " + projectID + ", ref " + referenceID);
                }
                fileIds.add(file_id);
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }

        }
        return fileIds;
    }

    /**
     *
     * @param nv The number of variants in the main variant view.
     * @return a query condition to randomly sample approximately
     * nv*getSubsetFraction(nv) variants from the main variant view (@see
     * DBSettings.getVariantViewName).
     */
    public Condition getSubsetRestrictionCondition(int nv) {
        LOG.info("getting subset restriction condition for " + nv + " variants");
        return BinaryCondition.lessThan(new FunctionCall(new CustomSql("RAND")), VariantTableSchemaBuilder.getSubsetFraction(nv), true);
    }

    @Override
    public MedSavantUpdate registerUpdate(MedSavantSession session, List<MedSavantFile> files, MedSavantProject project, Reference reference) throws MedSavantDatabaseException {
        try {
            final int updateID = AnnotationLogManager.getInstance().addAnnotationLogEntry(session, project.getProjectId(), reference.getID(), AnnotationLog.Action.ADD_VARIANTS);
            List<Integer> fileIds = getFileIDs(updateID, project.getProjectId(), reference.getID(), files);
            //MedSavantUpdate(int updateId, List<MedSavantFile> files, List<Integer> fileIds, MedSavantProject project, Reference reference){
            MedSavantUpdate msu = new MedSavantUpdate(updateID, files, fileIds, project, reference);
            return msu;
        } catch (SQLException sqe) {
            throw new MedSavantDatabaseException("Error writing to database log -- cannot register update");
        }
    }

    //This method is called ONLY to end import jobs.  Import job: registerUpdate; addVariants; endUpdate;
    //Schema change: addAnnotation(), deleteAnnotation.  There is a seperate 'registerAnnotation' method also.  addAnnotation and maybe deleteAnnotation are long jobs.
    @Override
    public void endUpdate(MedSavantSession session, MedSavantUpdate update) throws MedSavantDatabaseException {
        try {

            VariantTableSchemaBuilder vt = new VariantTableSchemaBuilder(session.getDatabaseName(), update);

            //Calculate the total number of variants that would remain if this update were published. (Sum of 
            //all currently published variants, plus those that were imported in this update)           
            VariantFilter publishedFilter = new VariantFilterImpl(null, update.getProject(), update.getReference(), PublicationStatus.PUBLISHED);
            VariantFilter unpublishedFilter = new VariantFilterImpl(BinaryCondition.equalTo(VariantTableSchemaBuilder.UPLOAD_ID, update.getUpdateID()),
                    update.getProject(),
                    update.getReference(),
                    PublicationStatus.PENDING);

            int approxTotalNumVariants = countVariants(unpublishedFilter, true) + countVariants(publishedFilter, true);

            //Create a new variant table subset and register it in the variant_tablemap. 
            vt.refreshSubset(getConnection(), serverContext, approxTotalNumVariants);

            //addTagsToUpload?
            //createPatientsForUpdate?
            /*
             registerTable(sessionID, projectID, referenceID, updateID, currentTableName, tableNameSubset, allAnnotationIDs);
             VariantManagerUtils.addTagsToUpload(sessionID, updateID, tags);
             // create patients for all DNA ids in this update
             createPatientsForUpdate(sessionID, currentTableName, projectID, updateID);
             */
        } catch (MedSavantDatabaseException mde) {
            LOG.error("MedSavantDatabase exception while creating subset table: ", mde);
            cancelUpdate(session, update);
            throw new MedSavantDatabaseException("Couldn't finish update -- update has been cancelled.");
        }
    }

    @Override
    public String getComponentID() {
        return InfobrightVariantStorageEngine.class.getCanonicalName();
    }

    @Override
    public String getComponentName() {
        return "Infobright Variant Storage";
    }

    @Override
    public void configure(Dictionary dict) throws InvalidConfigurationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void configure(String key, Object val) throws InvalidConfigurationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setServerContext(MedSavantServerContext context) throws InvalidConfigurationException {
        this.serverContext = context;
    }

    @Override
    public int countVariants(VariantFilter filter, boolean estimateAllowed) throws MedSavantDatabaseException {
        try {
            //String name = ProjectQueryUtil.getInstance().getVariantTablename(sid,projectId, referenceId, true);
            Object[] variantTableInfo = getVariantTableViewInfo(filter.getProject().getProjectId(), filter.getReference().getID());
            String tableViewName = (String) variantTableInfo[0];
            String tablenameSub = (String) variantTableInfo[1];
            float subMultiplier = (Float) variantTableInfo[2];

            if (tableViewName == null) {
                return -1;
            }

            //try to get a reasonable approximation
            if (tablenameSub != null && estimateAllowed && filter.getCondition() != null) {
                int estimate = (int) (getNumFilteredVariantsHelper(tablenameSub, filter.getCondition()) * subMultiplier);
                if (estimate >= COUNT_ESTIMATE_THRESHOLD) {
                    return estimate; // TODO: this should be rounded instead of a precise one
                }
            }

            // Approximation not good enough; use actual data.
            return getNumFilteredVariantsHelper(tableViewName, filter.getCondition());
        } catch (SQLException sqe) {
            throw new MedSavantDatabaseException("Database Error: error counting variants");
        }
    }

    //what to do with file ids.  Keep this method, ro change to countVariantsInUpdate(...updateId...)?
    //This method may not be necessary???
    /*
    @Override
    public int countVariantsInFile(MedSavantSession session, VariantFilter filter, Collection<Integer> fileIds, boolean estimateAllowed) throws MedSavantDatabaseException {
        try {           
            MedSavantJDBCPooledConnection conn = getConnection();
            
            TableSchema vtable = (new VariantTableSchemaBuilder(session.getDatabaseName())).getLatestViewSchema(conn, filter.getProject().getProjectId(), filter.getReference().getID());
                    
            int i = 0;
            Condition[] conditions = new Condition[files.size()];
            for (MedSavantFile f : files) {
                VariantTableSchemaBuilder.FILE_ID,
                conditions[i++] = BinaryConditionMS.equalTo(vtable.getDBColumn(BasicVariantColumns.FILE_ID), f.getFileId());
            }

            SelectQuery query = new SelectQuery();
            query.addFromTable(vtable.getTable());
            query.addCustomColumns(FunctionCall.countAll());
            addConditionsToQuery(query, new Condition[][]{{ComboCondition.or(conditions)}});
            ResultSet rs = null;
            try {
                rs = getConnection().executeQuery(query.toString());

                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    LOG.error("Couldn't count published variants, query: " + query.toString());
                    throw new MedSavantDatabaseException("Couldn't count published variants");
                }
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException sqe) {
                        LOG.error("Couldn't close result set -- continuing anyway", sqe);
                    }
                }
            }
        } catch (SQLException sqe) {
            LOG.error("Couldn't count variants with fileIds", sqe);
            throw new MedSavantDatabaseException("Couldn't count published variants");
        }
    }
*/
    @Override
    public String getVersion() throws MedSavantDatabaseException {
        TableSchema settingsTable = DBSchemas.SettingsTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(settingsTable.getTable());
        query.addAllColumns();
        query.addCondition(BinaryCondition.equalTo(settingsTable.getDBColumn(DBSchemas.SettingsTableSchema.COLUMNNAME_OF_KEY), SERVER_VERSION_KEY));
        ResultSet rs = null;

        try {
            rs = executeQuery(query.toString());
            return rs.getString(2);
        } catch (SQLException sqe) {
            LOG.error("Could not fetch version corresponding to this database", sqe);
            throw new MedSavantDatabaseException("Couldn't fetch version corresponding to this database");
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException se) {
                    LOG.error("Couldn't close result set", se);
                }
            }
        }
    }

    @Override
    public MedSavantFile exportVariants(MedSavantSession session, VariantFilter filter, boolean orderedByPosition, boolean compressOutput) throws MedSavantDatabaseException, MedSavantSecurityException, MedSavantFileDirectoryException {
        try {
            MedSavantJDBCPooledConnection conn = getConnection();
            TableSchema tableSchema = (new VariantTableSchemaBuilder(session.getDatabaseName())).getLatestViewSchema(conn, filter.getProject().getProjectId(), filter.getReference().getID());                        
            SelectQuery query = new SelectQuery();
            query.addFromTable(tableSchema.getTable());
            query.addAllColumns();
            if (filter.getCondition() != null) {
                query.addCondition(filter.getCondition());
            }
            File tmpFile = serverContext.getTemporaryFile(session);
            if (orderedByPosition) {
                query.addOrderings(tableSchema.getDBColumn(VariantTableSchemaBuilder.START_POSITION), tableSchema.getDBColumn(VariantTableSchemaBuilder.END_POSITION));
            }
            String intoString
                    = "INTO OUTFILE \"" + tmpFile.getAbsolutePath().replaceAll("\\\\", "/") + "\" "
                    + "FIELDS TERMINATED BY '" + StringEscapeUtils.escapeJava(DBSettings.FIELD_DELIMITER) + "' "
                    + "ENCLOSED BY '" + DBSettings.ENCLOSED_BY + "' "
                    + "ESCAPED BY '" + StringEscapeUtils.escapeJava(DBSettings.ESCAPE_CHAR) + "' " //+ " LINES TERMINATED BY '\\r\\n' ";
                    ;
            String queryString = query.toString().replace("FROM", intoString + "FROM");
            LOG.info(queryString);
            executeQuery(queryString);

            if (compressOutput) {
                LOG.info("Zipping export...");
                File zipFile = new File(tmpFile.getAbsoluteFile() + ".zip");
                IOUtils.zipFile(tmpFile, zipFile);
                tmpFile = zipFile;
            }

            MedSavantFile dstFile = serverContext.getMedSavantFileDirectory().createFile(session, MedSavantFileType.OTHER, tmpFile.getName());
            dstFile.replaceWithFile(session, serverContext.getMedSavantFileDirectory(), tmpFile);
            return dstFile;
        } catch (IOException ie) {
            LOG.error("IO error while exporting file from database: ", ie);       
            throw new MedSavantDatabaseException("Error exporting file from database -- please contact your system administrator");
        } catch (SQLException sqe) {
            LOG.error("Database error while exporting file from database: ", sqe);
            throw new MedSavantDatabaseException("Database error while exporting file from database");
        }
    }

    @Override
    public List<GenomicVariantRecord> getVariantRecords(MedSavantSession session, VariantFilter filter, long offset, int limit, MedSavantField[] orderBy) throws SQLException{        
         MedSavantJDBCPooledConnection conn = getConnection();
         TableSchema tableSchema = (new VariantTableSchemaBuilder(session.getDatabaseName())).getLatestViewSchema(conn, filter.getProject().getProjectId(), filter.getReference().getID());                        
         
         SelectQuery sq = new SelectQuery();
         sq.addFromTable(tableSchema.getTable());
         sq.addAllColumns();
         Condition condition = filter.getCondition();
         if(condition != null){
            sq.addCondition(condition);
         }
         
        String queryString = sq.toString();
        if (limit != -1) {
            if (offset != -1) {
                queryString += " LIMIT " + offset + ", " + limit;
            } else {
                queryString += " LIMIT " + limit;
            }
        }
        
        if(orderBy != null){
            String[] colNames = new String[orderBy.length];
            int i = 0;
            for(MedSavantField f : orderBy){
                colNames[i] = f.getAlias();
            }
            sq.addCustomOrderings((Object[])colNames);
        }
        List<GenomicVariantRecord> variantRecords;
        if(limit > 0){
            variantRecords = new ArrayList<GenomicVariantRecord>(limit);
        }else{
            variantRecords = new ArrayList<GenomicVariantRecord>();
        }
        LOG.info(queryString);
        
        //Get the annotations that have been applied
        HERE HERE HERE
        ResultSet rs = null;
        try{
            rs = conn.executeQuery(sq.toString());
            while(rs.next()){
                String dnaId = rs.getString(VariantTableSchemaBuilder.VARIANT_ID.getAlias());
                int fileId = rs.getInt(VariantTableSchemaBuilder.FILE_ID.getAlias());
                int variantId = rs.getInt(VariantTableSchemaBuilder.VARIANT_ID.getAlias());
                GenomicVariant gv = GenomicVariantFactory.create(
                    rs.getString(VariantTableSchemaBuilder.CHROM.getAlias()),
                    rs.getInt(VariantTableSchemaBuilder.START_POSITION.getAlias()),
                    rs.getInt(VariantTableSchemaBuilder.END_POSITION.getAlias()),
                    rs.getString(VariantTableSchemaBuilder.DBSNP_ID.getAlias()),                    
                    rs.getString(VariantTableSchemaBuilder.REF.getAlias()),
                    rs.getString(VariantTableSchemaBuilder.ALT.getAlias()),
                    rs.getInt(VariantTableSchemaBuilder.ALT_NUMBER.getAlias()),
                    rs.getFloat(VariantTableSchemaBuilder.QUAL.getAlias()),
                    rs.getString(VariantTableSchemaBuilder.FILTER.getAlias()),                    
                    rs.getString(VariantTableSchemaBuilder.CUSTOM_INFO.getAlias()));
                GenomicVariantRecord gvr = new GenomicVariantRecord(gv, dnaId, fileId, variantId);
                
                gvr.addAnnotationValues(...);
                variantRecords.add(gvr);
            }
            return variantRecords;
        }finally{
            if(rs != null){
                try{
                    rs.close();
                }catch(SQLException sqe){
                    LOG.error("Couldn't close result set -- continuing anyway", sqe);
                }
            }
        }                    
    }

    @Override
    public Map<String[], Integer> getHistogram(VariantFilter filter, VariantField[] field, int[] numBins) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int countPatientsWithVariants(VariantFilter filter) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setVariantStatus(MedSavantSession session, int referenceId, int updateID, PublicationStatus status) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addVariants(MedSavantSession session, Collection<GenomicVariantRecord> variantRecords, MedSavantUpdate update) throws MedSavantDatabaseException {
        try {
            //Get the schema for the table where the variants will be stored.            
            TableSchema tableSchema = getUnpublishedVariantTableSchema(session, update);

            //should be the same as DBSettings.getVariantTableName(update.getProject().getProjectId(), update.getReference().getID(), update.getUpdateID());
            String tableName = tableSchema.getTableName();

            File tmpFile = null;

            try {
                tmpFile = serverContext.getTemporaryFile(session);
                BufferedWriter bos = new BufferedWriter(new FileWriter(tmpFile));
                for (GenomicVariantRecord gvr : variantRecords) {
                    //Convert genomic variant into a tab delimited string in the same order as expected by the table schema.
                    String[] tsvArray = getTSVArray(gvr, tableSchema);

                    //append variant to TSV file.                    
                    bos.write(StringUtils.join(DBSettings.FIELD_DELIMITER, tsvArray) + "\n");
                }
                bos.close();

                //Append TSV file to variant table.
                String query = "LOAD DATA LOCAL INFILE '" + tmpFile.getAbsolutePath().replaceAll("\\\\", "/") + "' "
                        + "INTO TABLE " + tableName + " "
                        + "FIELDS TERMINATED BY '" + DBSettings.FIELD_DELIMITER + "' ENCLOSED BY '" + DBSettings.ENCLOSED_BY + "' "
                        + "ESCAPED BY '" + StringEscapeUtils.escapeJava(DBSettings.ESCAPE_CHAR) + "' "
                        + " LINES TERMINATED BY '\\r\\n'"
                        + ";";
                executeQuery(query);

                //Create patients for any new DNA Ids?  Other stuff in former doImport method?
            } catch (IOException ie) { //thrown by getTemporaryFile, FileOutputStream

            } finally {
                //clean up temporary file.
                if (tmpFile != null) {
                    tmpFile.delete();
                }
            }
        } catch (SQLException se) {

        }
    }

    @Override
    public void addVariants(Collection<GenomicVariantRecord> variantRecords, int updateId) {
        for (GenomicVariantRecord gvr : variantRecords) {

            //what you don't have is a FILE id.  Do you need one?
            gvr.getDNAId();
            gvr.getUploadId();
            gvr.getGenomicVariant(); //the genomic variant
            gvr.getAnnotationValues();
            gvr.getAppliedAnnotations();
            gvr.getVariantId();
            gvr.getVCFFile();

        }
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int addVariants(MedSavantSession session, int referenceId, Collection<GenomicVariantRecord> variantRecords, MedSavantFile variantFile) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addAnnotations(Collection<TabixAnnotation> annotations) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeAnnotation(Collection<TabixAnnotation> annotations) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void cancelUpdate(MedSavantSession session, MedSavantUpdate update) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /*
     public int countVariants(VariantFilter filter) {        
     throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }

     public int countVariantsInFile(VariantFilter filter, Collection<VCFFileOld> files) {
     throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }

     public VCFFileOld exportVariants(VariantFilter filter, boolean orderedByPosition, boolean compressOutput) {
     throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }

     public List<GenomicVariant> getVariants(VariantFilter filter, long offset, int limit, VariantField[] orderBy) {
     throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }

     public Map<String[], Integer> getHistogram(VariantFilter filter, VariantField[] field, int[] numBins) {
     throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }

     public int countPatientsWithVariants(VariantFilter filter) {
     throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }

     public void setVariantStatus(int projectId, int referenceId, int updateID, PublicationStatus status) {
     throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }

     @Override
     public int addVariants(int projectId, int referenceId, Collection<GenomicVariantRecord> variants, VCFFileOld variantFile) {
     //query for which custominfo fields to use, and then see GenomicVariant.parseObject and VariantManagerUtils.addCustomVCFFields
     //to extract the custominfo fields you want to store.
     throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }

     public void addAnnotations(Collection<TabixAnnotation> annotations) throws UnsupportedOperationException {
     //Need to add custom fields??
     CustomField[] customFields = ProjectManager.getInstance().getCustomVariantFields(sessionID, projectID, referenceID, ProjectManager.getInstance().getNewestUpdateID(sessionID, projectID, referenceID, false));
     throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }

     public void removeAnnotation(Collection<TabixAnnotation> annotations) throws UnsupportedOperationException {
     throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }

     public boolean isAddingAnnotationSupported() {
     throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
     */
}
