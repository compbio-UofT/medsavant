package org.ut.biolab.medsavant.server.db.util.solr;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.server.SessionController;
import org.ut.biolab.medsavant.server.db.ConnectionPool;
import org.ut.biolab.medsavant.server.db.util.PersistenceEngine;
import org.ut.biolab.medsavant.server.db.variants.ImportUpdateManager;
import org.ut.biolab.medsavant.server.serverapi.AnnotationLogManager;
import org.ut.biolab.medsavant.server.serverapi.ProjectManager;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.*;
import org.ut.biolab.medsavant.shared.persistence.EntityManager;
import org.ut.biolab.medsavant.shared.persistence.EntityManagerFactory;
import org.ut.biolab.medsavant.shared.query.Query;
import org.ut.biolab.medsavant.shared.query.QueryManager;
import org.ut.biolab.medsavant.shared.query.QueryManagerFactory;
import org.ut.biolab.medsavant.shared.util.Entity;
import org.ut.biolab.medsavant.shared.util.MiscUtils;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;

import java.io.*;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Implements PersistenceEngine operations for the Solr backend.
 */
public class SolrPersistenceEngine implements PersistenceEngine {

    private static final Log LOG = LogFactory.getLog(SolrPersistenceEngine.class);

    private static QueryManager queryManager = QueryManagerFactory.getQueryManager();

    @Override
    public boolean fieldExists(String sid, String tableName, String fieldName) {
        return false;
    }

    @Override
    public DbTable importTable(String sessionId, String tablename) throws SQLException, SessionExpiredException {
        return null;
    }

    @Override
    public TableSchema importTableSchema(String sessionId, String tablename) throws SQLException, SessionExpiredException {

        CustomColumnType entity;
        List<CustomField> basicFields;
        tablename = tablename.toLowerCase(Locale.ROOT);
        if (tablename.contains(Entity.VARIANT)) {
            entity = CustomColumnType.VARIANT;
            basicFields = Arrays.asList(BasicVariantColumns.REQUIRED_VARIANT_FIELDS);
        } else if (tablename.contains(Entity.PATIENT)) {
            entity = CustomColumnType.PATIENT;
            basicFields = Arrays.asList(BasicPatientColumns.REQUIRED_PATIENT_FIELDS);
        } else {
            return null;
        }

        Query query = queryManager.createQuery("Select c from CustomColumn c where c.entity_name = :entityName");
        query.setParameter("entityName", entity);
        List<CustomField> customFields = query.execute();
        customFields = ListUtils.union(customFields, basicFields);

        DbSpec spec = new DbSpec();
        DbSchema schema = spec.addDefaultSchema();

        DbTable table = schema.addTable(entity.name());
        TableSchema ts = new TableSchema(table);
        for (CustomField customField : customFields) {
            table.addColumn(customField.getColumnName(), customField.getColumnType().toString(), customField.getColumnLength());
            ts.addColumn(customField.getColumnName(), customField.getColumnType(), customField.getColumnLength());
        }
        return ts;
    }

    public List<CustomField> getFields(String entity, int referenceId, int projectId, int uploadId) {

        CustomColumnType type;
        List<CustomField> basicFields;
        entity = entity.toLowerCase(Locale.ROOT);
        if (entity.contains(Entity.VARIANT)) {
            type = CustomColumnType.VARIANT;
            basicFields = Arrays.asList(BasicVariantColumns.REQUIRED_VARIANT_FIELDS);
        } else if (entity.contains(Entity.PATIENT)) {
            type = CustomColumnType.PATIENT;
            basicFields = Arrays.asList(BasicPatientColumns.REQUIRED_PATIENT_FIELDS);
        } else {
            return null;
        }

        Query query = queryManager.createQuery("Select c from CustomColumn c where c.entity_name = :entityName AND" +
                "c.reference_id = :referenceId AND c.project_id = :projectId AND c.upload_id = :uploadId");
        query.setParameter("entityName", type);
        query.setParameter("referenceId", referenceId);
        query.setParameter("projectId", projectId);
        query.setParameter("uploadId", uploadId);
        List<CustomField> customFields = query.execute();
        return ListUtils.union(customFields, basicFields);
    }

    public List<String> getFieldNames(String entity, int referenceId, int projectId, int uploadId) {

        CustomColumnType type;
        List<CustomField> basicFields;
        entity = entity.toLowerCase(Locale.ROOT);
        if (entity.contains(Entity.VARIANT)) {
            type = CustomColumnType.VARIANT;
            basicFields = Arrays.asList(BasicVariantColumns.REQUIRED_VARIANT_FIELDS);
        } else if (entity.contains(Entity.PATIENT)) {
            type = CustomColumnType.PATIENT;
            basicFields = Arrays.asList(BasicPatientColumns.REQUIRED_PATIENT_FIELDS);
        } else {
            return null;
        }

        Query query = queryManager.createQuery("Select c from CustomColumn c where c.entity_name = :entityName AND " +
                "c.reference_id = :referenceId AND c.project_id = :projectId AND c.upload_id = :uploadId");
        query.setParameter("entityName", type);
        query.setParameter("referenceId", referenceId);
        query.setParameter("projectId", projectId);
        query.setParameter("uploadId", uploadId);
        List<CustomField> customFields = query.execute();

        List<String> fieldNames = new ArrayList<String>();
        for (CustomField field : basicFields) {
            fieldNames.add(field.getColumnName());
        }
        for (CustomField field : customFields) {
            fieldNames.add(field.getColumnName());
        }
        return fieldNames;
    }

    @Override
    public void dropTable(String sessID, String tableName) throws SQLException, SessionExpiredException { }

    @Override
    public void addUser(String sessID, String user, char[] pass, UserLevel level) throws SQLException, SessionExpiredException { }

    @Override
    public void grantPrivileges(String sessID, String name, UserLevel level) throws SQLException, SessionExpiredException { }

    @Override
    public void removeUser(String sid, String name) throws SQLException, SessionExpiredException { }

    @Override
    public void registerCredentials(String sessionId, String user, String password, String dbName) { }

    @Override
    public void removeDatabase(String dbHost, int port, String dbName, String adminName, char[] rootPassword) { }

    @Override
    public String createDatabase(String dbHost, int port, String dbName, String adminName, char[] rootPassword, String versionString) throws RemoteException, SQLException {
        SessionController sessController = SessionController.getInstance();
        return sessController.registerNewSession(adminName, new String(rootPassword), "");
    }

    @Override
    public void createTables(String sessID) throws SQLException, RemoteException, SessionExpiredException {  }

    @Override
    public void testConnection(String sessIDs) {
        //nothing for now
    }

    @Override
    public void initializePooledConnection(ConnectionPool pool) {
        //nothing for now
    }

    @Override
    public String createVariantTable(String sessID, int projID, int refID, int updID, int[] annIDs, boolean staging, boolean sub) {
        return "Variant";
    }

    @Override
    public String getVariantTableName(String sid, int projectid, int refid, boolean published, boolean sub) {
        return "Variant";
    }

    @Override
    public Object[] getVariantTableInfo(String sid, int projectid, int refid, boolean published) {
        return new Object[] { "Variant", "Variant" , 1};
    }

    @Override
    public int doImport(String sessionID, int projectID, int referenceID, boolean publishUponCompletion, File[] vcfFiles, boolean includeHomozygousReferenceCalls, String[][] tags) throws Exception {
        return ImportUpdateManager.doImport(sessionID, projectID, referenceID, publishUponCompletion, vcfFiles, includeHomozygousReferenceCalls, tags, false);
    }

    @Override
    public void annotateAndUploadTSVFiles(String sessionID, int updateID, int projectID, int referenceID, int[] annotationIDs, CustomField[] customFields, File[] tsvFiles, File workingDir) throws Exception {
        LOG.info("Annotating TSV files, working directory is " + workingDir.getAbsolutePath());

        ProjectManager.getInstance().setCustomVariantFields(sessionID, projectID, referenceID, updateID, customFields);

        boolean needsToUnlock = ImportUpdateManager.acquireLock(sessionID);

        try {
            //get annotation information
            Annotation[] annotations = ImportUpdateManager.getAnnotationsFromIDs(annotationIDs, sessionID);

            File[] splitTSVFiles = ImportUpdateManager.splitFilesByDNAAndFileID(tsvFiles, ImportUpdateManager.createSubdir(workingDir, "split"));
            File[] annotatedTSVFiles = ImportUpdateManager.annotateTSVFiles(sessionID, splitTSVFiles, annotations, customFields, ImportUpdateManager.createSubdir(workingDir, "annotate"));

            uploadTSVFiles(sessionID, annotatedTSVFiles, getFieldNames(Entity.VARIANT, referenceID, projectID, updateID));
            ImportUpdateManager.registerTable(sessionID, projectID, referenceID, updateID, Entity.VARIANT, Entity.VARIANT, annotationIDs);
            ImportUpdateManager.setAnnotationStatus(sessionID, updateID, AnnotationLog.Status.PENDING);

        } catch (Exception e) {
            AnnotationLogManager.getInstance().setAnnotationLogStatus(sessionID, updateID, AnnotationLog.Status.ERROR);
            throw e;
        } finally {
            ImportUpdateManager.releaseLock(sessionID, needsToUnlock);
        }
    }

    private void uploadTSVFiles(String sessionId, File[] files, List<String> fieldNames) throws IOException, SQLException, SessionExpiredException {
        for (File file : files) {
            uploadTSVFile(sessionId, file, fieldNames);
        }
    }
    private void uploadTSVFile(String sessionId, File file, List<String> fieldNames) throws IOException, SQLException, SessionExpiredException {

        //file = VariantManagerUtils.cleanVariantFile(file, Entity.VARIANT,sessionId);

        BufferedReader br = new BufferedReader(new FileReader(file));

        int chunkSize = 100000; // number of lines per chunk (100K lines = ~50MB for a standard VCF file)
        int lineNumber = 0;

        BufferedWriter bw = null;
        String currentOutputPath = null;

        boolean stateOpen = false;

        String parentDirectory = file.getParentFile().getAbsolutePath();
        EntityManager entityManager = EntityManagerFactory.getEntityManager();
        String line;

        while ((line = br.readLine()) != null) {
            lineNumber++;
            // start a new output file
            if (lineNumber % chunkSize == 1) {
                currentOutputPath = parentDirectory + "/" + MiscUtils.extractFileName(file.getAbsolutePath()) + "_" + (lineNumber / chunkSize);
                LOG.info("Opening new partial file " + currentOutputPath);
                bw = new BufferedWriter(new FileWriter(currentOutputPath));
                stateOpen = true;
            }

            // write line to chunk file
            bw.write(line + "\n");

            // close and upload this output file
            if (lineNumber % chunkSize == 0) {
                bw.close();
                try {
                    entityManager.persist(currentOutputPath, VariantRecord.class, fieldNames.toArray(new String[0]));
                } catch (Exception e) {
                    LOG.error("Error persisting data from file " + currentOutputPath);
                } finally {
                    boolean deleted = new File(currentOutputPath).delete();
                    LOG.info("Deleting " + currentOutputPath + " - " + (deleted ? "successful" : "failed"));
                }
                stateOpen = false;
            }
        }

        // write the remaining open file
        if (bw != null && stateOpen) {
            LOG.info("Closing and uploading last partial file " + currentOutputPath);
            bw.close();
            try {
                entityManager.persist(currentOutputPath, VariantRecord.class, fieldNames.toArray(new String[0]));
            } catch (Exception e) {
                LOG.error("Error persisting data from file " + currentOutputPath);
            } finally {
                boolean deleted = new File(currentOutputPath).delete();
                LOG.info("Deleting " + currentOutputPath + " - " + (deleted ? "successful" : "failed"));
            }
        }
        LOG.info("Imported " + lineNumber + " lines of variants in total");
    }
}
