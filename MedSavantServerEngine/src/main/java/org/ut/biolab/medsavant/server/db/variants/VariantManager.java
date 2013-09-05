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
package org.ut.biolab.medsavant.server.db.variants;

import com.healthmarketscience.sqlbuilder.*;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.server.SessionController;
import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.VariantStarredTableSchema;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.VariantTagColumns;
import org.ut.biolab.medsavant.server.db.PooledConnection;
import org.ut.biolab.medsavant.server.db.util.CustomTables;
import org.ut.biolab.medsavant.server.db.util.DBUtils;
import org.ut.biolab.medsavant.server.log.EmailLogger;
import org.ut.biolab.medsavant.server.serverapi.*;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.*;
import org.ut.biolab.medsavant.shared.model.AnnotationLog.Status;
import org.ut.biolab.medsavant.shared.persistence.EntityManager;
import org.ut.biolab.medsavant.shared.persistence.EntityManagerFactory;
import org.ut.biolab.medsavant.shared.query.*;
import org.ut.biolab.medsavant.shared.query.Query;
import org.ut.biolab.medsavant.shared.serverapi.VariantManagerAdapter;
import org.ut.biolab.medsavant.shared.solr.exception.InitializationException;
import org.ut.biolab.medsavant.shared.util.*;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 *
 * @author Andrew
 */
public class VariantManager extends MedSavantServerUnicastRemoteObject implements VariantManagerAdapter, BasicVariantColumns {

    private static final Log LOG = LogFactory.getLog(VariantManager.class);
    // thresholds for querying and drawing
    private static final int COUNT_ESTIMATE_THRESHOLD = 1000;
    private static final int BIN_TOTAL_THRESHOLD = 1000000;
    private static final int PATIENT_HEATMAP_THRESHOLD = 1000;
    // Stages within the upload process.
    private static final double LOG_FRACTION = 0.05;
    private static final double DUMP_FRACTION = 0.1;
    private static final double SORTING_FRACTION = 0.1;
    private static final double CUSTOM_FIELD_FRACTION = 0.05;
    private static final double ANNOTATING_FRACTION = 0.15;
    private static final double SPLITTING_FRACTION = 0.05;
    private static final double MERGING_FRACTION = 0.05;
    private static final double CREATING_TABLES_FRACTION = 0.05;
    private static final double SUBSET_FRACTION = 0.05;
    private static final double LOAD_TABLE_FRACTION = 0.15;             // Happens twice
    private static VariantManager instance;
    //public static boolean REMOVE_TMP_FILES = false;
    static boolean REMOVE_WORKING_DIR = true;

    private static QueryManager queryManager;
    private static EntityManager entityManager;

    private VariantManager() throws RemoteException, SessionExpiredException {
        queryManager = QueryManagerFactory.getQueryManager();
        entityManager = EntityManagerFactory.getEntityManager();
    }

    public static synchronized VariantManager getInstance() throws RemoteException, SessionExpiredException {
        if (instance == null) {
            instance = new VariantManager();
        }
        return instance;
    }

    /**
     * Make all variant tables live for a project. All users accessing this
     * database will be logged out.
     */
    @Override
    public void publishVariants(String sessID, int projectID) throws Exception {
        //ToDo
        LOG.info("Beginning publish of all tables for project " + projectID);

        PooledConnection conn = ConnectionController.connectPooled(sessID);

        try {
            //get update ids and references
            LOG.info("Getting map of update ids");
            int[] refIDs = ProjectManager.getInstance().getReferenceIDsForProject(sessID, projectID);
            Map<Integer, Integer> ref2Update = new HashMap<Integer, Integer>();
            for (int refID : refIDs) {
                ref2Update.put(refID, ProjectManager.getInstance().getNewestUpdateID(sessID, projectID, refID, false));
            }

            //update annotation log table
            LOG.info("Setting log status to published");
            for (Integer refId : ref2Update.keySet()) {
                AnnotationLogManager.getInstance().setAnnotationLogStatus(sessID, ref2Update.get(refId), Status.PUBLISHED);
            }

            //publish
            LOG.info("Releasing database lock.");
            SettingsManager.getInstance().releaseDBLock(conn);
            LOG.info("Terminating active sessions");
            SessionController.getInstance().terminateSessionsForDatabase(SessionController.getInstance().getDatabaseForSession(sessID), "Administrator (" + SessionController.getInstance().getUserForSession(sessID) + ") published new variants");
            LOG.info("Publishing tables");
            for (Integer refId : ref2Update.keySet()) {
                ProjectManager.getInstance().publishVariantTable(conn, projectID, refId, ref2Update.get(refId));
            }

            LOG.info("Publish complete");
        } finally {
            conn.close();
        }
    }

    /**
     * Make a variant table live. All users accessing this database will be
     * logged out.
     */
    @Override
    public void publishVariants(String sessID, int projID, int refID, int updID) throws Exception {
        LOG.info("Publishing table. pid:" + projID + " refid:" + refID + " upid:" + updID);
        PooledConnection conn = ConnectionController.connectPooled(sessID);
        try {
            LOG.info("Setting log status to published");
            AnnotationLogManager.getInstance().setAnnotationLogStatus(sessID, updID, Status.PUBLISHED);
            LOG.info("Releasing database lock.");
            SettingsManager.getInstance().releaseDBLock(conn);
            LOG.info("Terminating active sessions");
            SessionController.getInstance().terminateSessionsForDatabase(SessionController.getInstance().getDatabaseForSession(sessID), "Administrator (" + SessionController.getInstance().getUserForSession(sessID) + ") published new variants");
            LOG.info("Publishing table");
            ProjectManager.getInstance().publishVariantTable(conn, projID, refID, updID);
            LOG.info("Publish complete");
        } finally {
            conn.close();
        }
    }


    /*
     * Remove an unpublished variant table.
     */
    @Override
    public void cancelPublish(String sid, int projectID, int referenceID, int updateID) throws Exception {
        LOG.info("Cancelling publish. pid:" + projectID + " refid:" + referenceID + " upid:" + updateID);
        ProjectManager.getInstance().removeTables(sid, projectID, referenceID, updateID, updateID);
        LOG.info("Cancel complete");
    }

    /**
     * Perform updates to custom vcf fields and other annotations. Will result
     * in the creation of a new, unpublished, up-to-date variant table. This
     * method is used only by ProjectWizard.modifyProject().
     */
    @Override
    public int updateTable(String sessID, int projID, int refID, int[] annotIDs, CustomField[] customFields, boolean autoPublish, String email) throws Exception {
        //ToDo
        EmailLogger.logByEmail("Update STARTED", "Update started. " + annotIDs.length + " annotation(s) will be performed. You will be notified again upon completion.", email);
        try {
            int updateID = ImportUpdateManager.doUpdate(sessID, projID, refID, annotIDs, customFields, autoPublish);

            EmailLogger.logByEmail("Update COMPLETED", "Update completed. " + annotIDs.length + " annotation(s) were performed.", email);

            return updateID;
        } catch (Exception e) {
            EmailLogger.logByEmail("Update FAILED", "Update failed with error: " + MiscUtils.getStackTrace(e), email);
            LOG.error(e);
            throw e;
        }

    }

    /**
     * Import variant files which have been transferred from a client.
     */
    @Override
    public int uploadVariants(String sessID, int[] transferIDs, int projID, int refID, String[][] tags, boolean includeHomoRef, String email, boolean autoPublish) throws Exception {
        //FIXME update this to contain abstract code
        LOG.info("Importing variants by transferring from client");

        NetworkManager netMgr = NetworkManager.getInstance();
        File[] vcfFiles = new File[transferIDs.length];
        String[] sourceNames = new String[transferIDs.length];

        int i = 0;
        for (int id : transferIDs) {
            vcfFiles[i] = netMgr.getFileByTransferID(sessID, id);
            sourceNames[i] = netMgr.getSourceNameByTransferID(sessID, id);
            i++;
        }

        return uploadVariants(sessID, vcfFiles, sourceNames, projID, refID, tags, includeHomoRef, email, autoPublish);
    }


    /**
     * Use when variant files are already on the server. Performs variant import
     * of an entire directory.
     */
    @Override
    public int uploadVariants(String sessID, File dirContainingVCFs, int projID, int refID, String[][] tags, boolean includeHomoRef, String email, boolean autoPublish) throws RemoteException, SessionExpiredException, IOException, Exception {

        LOG.info("Importing variants already stored on server in dir " + dirContainingVCFs.getAbsolutePath());

        if (!dirContainingVCFs.exists()) {
            LOG.info("Directory from which to load variants does not exist, bailing out.");
            return -1;
        }

        File[] vcfFiles = dirContainingVCFs.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                String name = file.getName();
                return name.endsWith(".vcf") || name.endsWith(".vcf.gz");
            }
        });

        if (vcfFiles.length == 0) {
            LOG.info("Directory exists but contains no .vcf or .vcf.gz files.");
            return -1;
        }

        return uploadVariants(sessID, vcfFiles, null, projID, refID, tags, includeHomoRef, email, autoPublish);
    }

    /**
     * Start the upload process for new vcf files. Will result in the creation
     * of a new, unpublished, up-to-date variant table.
     *
     * @param sessID uniquely identifies the client
     * @param vcfFiles local VCF files on the server's file-system
     * @param sourceNames if non-null, client-side names of uploaded files
     */
    public int uploadVariants(String sessionID, File[] vcfFiles, String[] sourceNames, int projectID, int referenceID, String[][] tags, boolean includeHomoRef, String email, boolean autoPublish) throws Exception {

        EmailLogger.logByEmail("Upload STARTED", "Upload started. " + vcfFiles.length + " file(s) will be imported. You will be notified again upon completion.", email);
        try {
            int updateID = ImportUpdateManager.doImport(sessionID, projectID, referenceID, autoPublish, vcfFiles, includeHomoRef, tags);
            EmailLogger.logByEmail("Upload COMPLETED", "Upload completed. " + vcfFiles.length + " file(s) were imported.", email);

            return updateID;
        } catch (Exception e) {
            EmailLogger.logByEmail("Upload FAILED", "Upload failed with error: " + MiscUtils.getStackTrace(e), email);
            LOG.error(e);
            throw e;
        }
    }

    //ToDo
    @Override
    public int removeVariants(String sessID, int projID, int refID, List<SimpleVariantFile> files, boolean autoPublish, String email) throws Exception {
        LOG.info("Beginning removal of variants");
        String statement = "Delete from Variant v where %s";
        String conditions = "";
        for (int i = 0; i < files.size(); i++) {

            conditions +=
                    "( v." + UPLOAD_ID.getColumnName() + "=" + files.get(i).getUploadId()
                            + " AND v." + FILE_ID.getColumnName() + "=" + files.get(i).getFileId() + ")";
            if (i != files.size() - 1) {
                conditions += " AND ";
            }

            removeEntryFromFileTable(sessID, files.get(i).getUploadId(), files.get(i).getFileId());
        }

        Query query = queryManager.createQuery(String.format(statement,conditions));
        query.executeDelete();
        return 0;
    }

    @Override
    public int exportVariants(String sessID, int projID, int refID, Condition[][] conditions, boolean orderedByPosition, boolean zipOutputFile) throws SQLException, RemoteException, SessionExpiredException, IOException, InterruptedException, InitializationException {

        //generate directory
        File baseDir = DirectorySettings.generateDateStampDirectory(DirectorySettings.getTmpDirectory());
        Process p = Runtime.getRuntime().exec("chmod -R o+w " + baseDir.getCanonicalPath());
        p.waitFor();

        String filename = ProjectManager.getInstance().getProjectName(sessID, projID).replace(" ", "") + "-varexport-" + System.currentTimeMillis() + ".tdf";
        File file = new File(baseDir, filename);

        LOG.info("Exporting variants to " + file.getAbsolutePath());

        long start = System.currentTimeMillis();

        Query query = queryManager.createQuery("Select v from Variant v");
        queryManager.toTSV(query,file);

        LOG.info(query);
        LOG.info("Done exporting variants to " + file.getAbsolutePath());
        LOG.info("Export took " + ((System.currentTimeMillis() - start) / 1000) + " seconds");

        if (zipOutputFile) {
            LOG.info("Zipping export...");
            File zipFile = new File(file.getAbsoluteFile() + ".zip");
            IOUtils.zipFile(file, zipFile);
            boolean deleted = file.delete();
            LOG.info("Deleting " + file.getAbsolutePath() + " - " + (deleted ? "successful" : "failed"));
            file = zipFile;
            LOG.info("Done zipping");
        }

        // add file to map and send the id back
        int fileID = NetworkManager.getInstance().openReaderOnServer(sessID, file);
        return fileID;
    }

    @Override
    public TableSchema getCustomTableSchema(String sessionId, int projectId, int referenceId) throws SQLException, RemoteException, SessionExpiredException {
        return CustomTables.getInstance().getCustomTableSchema(sessionId, ProjectManager.getInstance().getVariantTableName(sessionId, projectId, referenceId, true));
    }

    @Override
    public List<Object[]> getVariants(String sessionId, int projectId, int referenceId, int start, int limit) throws SQLException, RemoteException, SessionExpiredException {
        return getVariants(sessionId, projectId, referenceId, new Condition[1][], start, limit);
    }

    @Override
    public List<Object[]> getVariants(String sessionId, int projectId, int referenceId, Condition[][] conditions, int start, int limit) throws SQLException, RemoteException, SessionExpiredException {
        return getVariants(sessionId, projectId, referenceId, conditions, start, limit, null);
    }

    @Override
    public List<Object[]> getVariants(String sessionId, int projectId, int referenceId, Condition[][] conditions, int start, int limit, String[] orderByCols) throws SQLException, RemoteException, SessionExpiredException {

        String statement = "Select v from Variant v";
        StringBuilder q = addConditionsToQuery(statement, conditions);

        org.ut.biolab.medsavant.shared.query.Query query = queryManager.createQuery(q.toString());

        query.setStart(start);
        query.setLimit(limit);

        List<VariantRecord> resultRowList = query.execute();
        return convertResultRowListToObjectArrayList(resultRowList);
    }

    @Override
    public int getVariantCount(String sid, int projectId, int referenceId) throws SQLException, RemoteException, SessionExpiredException {
        return getFilteredVariantCount(sid, projectId, referenceId, new Condition[0][], true);
    }

    @Override
    public int getFilteredVariantCount(String sid, int projectId, int referenceId, Condition[][] conditions) throws SQLException, RemoteException, SessionExpiredException {
        return getFilteredVariantCount(sid, projectId, referenceId, conditions, false);
    }

    private int getFilteredVariantCount(String sid, int projectId, int referenceId, Condition[][] conditions, boolean forceExact) throws SQLException, RemoteException, SessionExpiredException {

        // Approximation not good enough; use actual data.
        return getNumFilteredVariantsHelper(conditions);
    }

    public int getNumFilteredVariantsHelper(Condition[][] conditions) throws SQLException, RemoteException, SessionExpiredException {

        String statement = "Select v from Variant v";
        StringBuilder q = addConditionsToQuery(statement, conditions);

        Query query = queryManager.createQuery(q.toString());

        int variantsCount = (int) query.count();

        LOG.info("Number of variants remaining: " + variantsCount);

        return variantsCount;
    }

    /*
     * Convenience method
     */
    @Override
    public int getVariantCountForDNAIDs(String sessID, int projID, int refID, Condition[][] conditions, Collection<String> dnaIDs) throws SQLException, RemoteException, SessionExpiredException {

        if (dnaIDs.isEmpty()) {
            return 0;
        }

        String name = ProjectManager.getInstance().getVariantTableName(sessID, projID, refID, true);
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessID, name);
        Condition dnaCondition = new InCondition(table.getDBColumn(DNA_ID.getColumnName()), dnaIDs);

        Condition[] c1 = new Condition[conditions.length];
        for (int i = 0; i < conditions.length; i++) {
            c1[i] = ComboCondition.and(conditions[i]);
        }

        Condition[] finalCondition = new Condition[]{ComboCondition.and(dnaCondition, ComboCondition.or(c1))};

        return getFilteredVariantCount(sessID, projID, refID, new Condition[][]{finalCondition});
    }

    @Override
    public boolean willApproximateCountsForConditions(String sid, int projectId, int referenceId, Condition[][] conditions) throws SQLException, RemoteException, SessionExpiredException {
        int total = getFilteredVariantCount(sid, projectId, referenceId, conditions);
        return total >= BIN_TOTAL_THRESHOLD;
    }

    @Override
    public Map<Range, Long> getFilteredFrequencyValuesForNumericColumn(String sid, int projectId, int referenceId, Condition[][] conditions, CustomField column, boolean logBins) throws InterruptedException, SQLException, RemoteException, SessionExpiredException {
        //ToDo
        //pick table from approximate or exact

        //ToDo move logic for picking fields to Solr
        TableSchema table;
        int total = getFilteredVariantCount(sid, projectId, referenceId, conditions);
        Object[] variantTableInfo = ProjectManager.getInstance().getVariantTableInfo(sid, projectId, referenceId, true);
        String tablename = (String) variantTableInfo[0];
        String tablenameSub = (String) variantTableInfo[1];
        float multiplier = (Float) variantTableInfo[2];
        if (total >= BIN_TOTAL_THRESHOLD) {
            table = CustomTables.getInstance().getCustomTableSchema(sid, tablenameSub);
        } else {
            table = CustomTables.getInstance().getCustomTableSchema(sid, tablename);
            multiplier = 1;
        }

        Range range = DBUtils.getInstance().getExtremeValuesForColumn(sid, table.getTableName(), column.getColumnName());
        double binSize = MiscUtils.generateBins(column, range, logBins);
        String statement = String.format("Select count(v), v.%s from Variant v", column.getColumnName());
        StringBuilder q = addConditionsToQuery(statement, conditions);

        String groupByClause = String.format("group by v.%s", column.getColumnName());
        q.append(groupByClause);
        Query query = queryManager.createQuery(q.toString());
        List<ResultRow> resultRows = query.executeForRows();
        Map<Range, Long> results = new TreeMap<Range, Long>();

        for (ResultRow resultRow : resultRows) {
            long count = (Integer) resultRow.getObject(0); //add multiplier ?
            long binNo = (Integer) resultRow.getObject(1);
            Range r;
            if (logBins) {
                binNo = (long) Math.floor(Math.log10(binNo));
                r = new Range(Math.pow(10, binNo), Math.pow(10, binNo + 1));
            } else {
                binNo = (long) Math.floor(binNo / binSize);
                r = new Range(binNo * binSize, (binNo + 1) * binSize);
            }
            results.put(r, count);
        }
        return results;
    }

    @Override
    public Map<String, Integer> getFilteredFrequencyValuesForCategoricalColumn(String sessID, int projID, int refID, Condition[][] conditions, String colName) throws SQLException, RemoteException, SessionExpiredException {

        StringBuilder statement = new StringBuilder("Select v." + colName + " from Variant v");

        StringBuilder cond = new StringBuilder();
        for (int i = 0; i < conditions.length; i++) {
            cond.append(StringUtils.join(conditions[i]," AND "));
        }

        if (!("(1 = 1)".equals(cond.toString()) || "".equals(cond.toString()))) {
            statement.append(" where ");
            statement.append(cond);
        }

        statement.append(" group by " + "v." + colName);

        Query query = queryManager.createQuery(statement.toString());

        List<ResultRow> resultRowList = query.executeForRows();

        Map<String, Integer> map = new HashMap<String, Integer>();

        for (ResultRow resultRow : resultRowList) {
            //Todo multiplier?
            map.put((String) resultRow.getObject(1), (Integer) resultRow.getObject(0));
        }

        return map;
    }

    @Override
    public ScatterChartMap getFilteredFrequencyValuesForScatter(String sid, int projectId, int referenceId, Condition[][] conditions, String columnnameX, String columnnameY, boolean columnXCategorical, boolean columnYCategorical, boolean sortKaryotypically) throws InterruptedException, SQLException, RemoteException, SessionExpiredException {
        String statement = String.format("Select count(v), v.%s, v.%s from Variant v ", columnnameX, columnnameY);
        StringBuilder q = addConditionsToQuery(statement, conditions);

        String groupByClause = String.format("group by v.%s, v.%s",columnnameX,columnnameY);
        q.append(groupByClause);
        Query query = queryManager.createQuery(q.toString());

        List<ResultRow> resultRows = query.executeForRows();

        //ToDo move this logic to Solr
        TableSchema table;
        int total = getFilteredVariantCount(sid, projectId, referenceId, conditions);
        Object[] variantTableInfo = ProjectManager.getInstance().getVariantTableInfo(sid, projectId, referenceId, true);
        String tablename = (String) variantTableInfo[0];
        String tablenameSub = (String) variantTableInfo[1];
        float multiplier = (Float) variantTableInfo[2];
        if (total >= BIN_TOTAL_THRESHOLD) {
            table = CustomTables.getInstance().getCustomTableSchema(sid, tablenameSub);
        } else {
            table = CustomTables.getInstance().getCustomTableSchema(sid, tablename);
            multiplier = 1;
        }

        DbColumn columnX = table.getDBColumn(columnnameX);
        DbColumn columnY = table.getDBColumn(columnnameY);
        double binSizeX = 0;
        if (!columnXCategorical) {
            Range rangeX = DBUtils.getInstance().getExtremeValuesForColumn(sid, "Variant", columnnameX);
            binSizeX = MiscUtils.generateBins(new CustomField(columnnameX, columnX.getTypeNameSQL() + "(" + columnX.getTypeLength() + ")", false, "", ""), rangeX, false);
        }

        double binSizeY = 0;
        if (!columnYCategorical) {
            Range rangeY = DBUtils.getInstance().getExtremeValuesForColumn(sid, "Variant", columnnameY);
            binSizeY = MiscUtils.generateBins(new CustomField(columnnameY, columnY.getTypeNameSQL() + "(" + columnY.getTypeLength() + ")", false, "", ""), rangeY, false);
        }

        multiplier = 1;

        List<ScatterChartEntry> entries = new ArrayList<ScatterChartEntry>();
        List<String> xRanges = new ArrayList<String>();
        List<String> yRanges = new ArrayList<String>();

        for (ResultRow resultRow : resultRows) {
            String x = String.valueOf(resultRow.getObject(2));
            String y = String.valueOf(resultRow.getObject(1));

            if (!columnXCategorical) {
                x = MiscUtils.doubleToString(Integer.parseInt(x) * binSizeX, 2) + " - " + MiscUtils.doubleToString(Integer.parseInt(x) * binSizeX + binSizeX, 2);
            }
            if (!columnYCategorical) {
                y = MiscUtils.doubleToString(Integer.parseInt(y) * binSizeY, 2) + " - " + MiscUtils.doubleToString(Integer.parseInt(y) * binSizeY + binSizeY, 2);
            }

            ScatterChartEntry entry = new ScatterChartEntry(x, y, (int) ((Integer) resultRow.getObject(0) * multiplier));
            entries.add(entry);
            if (!xRanges.contains(entry.getXRange())) {
                xRanges.add(entry.getXRange());
            }
            if (!yRanges.contains(entry.getYRange())) {
                yRanges.add(entry.getYRange());
            }
        }

        if (sortKaryotypically) {
            Collections.sort(xRanges, new ChromosomeComparator());
        } else if (columnXCategorical) {
            Collections.sort(xRanges);
        }
        if (columnYCategorical) {
            Collections.sort(yRanges);
        }

        return new ScatterChartMap(xRanges, yRanges, entries);
    }

    /*
     * Convenience method
     */
    @Override
    public int getVariantCountInRange(String sid, int projectId, int referenceId, Condition[][] conditions, String chrom, long start, long end) throws SQLException, RemoteException, SessionExpiredException {

        String name = ProjectManager.getInstance().getVariantTableName(sid, projectId, referenceId, true);
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sid, name);

        Condition[] rangeConditions = new Condition[]{
            BinaryCondition.equalTo(table.getDBColumn(CHROM), chrom),
            BinaryCondition.greaterThan(table.getDBColumn(POSITION), start, true),
            BinaryCondition.lessThan(table.getDBColumn(POSITION), end, false)
        };

        Condition[] c1 = new Condition[conditions.length];
        for (int i = 0; i < conditions.length; i++) {
            c1[i] = ComboCondition.and(conditions[i]);
        }

        Condition[] finalCondition = new Condition[]{ComboCondition.and(ComboCondition.and(rangeConditions), ComboCondition.or(c1))};

        return getFilteredVariantCount(sid, projectId, referenceId, new Condition[][]{finalCondition});
    }

    @Override
    public Map<String, Map<Range, Integer>> getChromosomeHeatMap(String sid, int projectId, int referenceId, Condition[][] conditions, int binsize) throws SQLException, RemoteException, SessionExpiredException {

        //pick table from approximate or exact
        TableSchema table;
        int total = getFilteredVariantCount(sid, projectId, referenceId, conditions);
        Object[] variantTableInfo = ProjectManager.getInstance().getVariantTableInfo(sid, projectId, referenceId, true);
        String tablename = (String) variantTableInfo[0];
        String tablenameSub = (String) variantTableInfo[1];
        float multiplier = (Float) variantTableInfo[2];
        if (total >= BIN_TOTAL_THRESHOLD) {
            table = CustomTables.getInstance().getCustomTableSchema(sid, tablenameSub);
        } else {
            table = CustomTables.getInstance().getCustomTableSchema(sid, tablename);
            multiplier = 1;
        }

        //TableSchema table = CustomTables.getInstance().getCustomTableSchema(sid,ProjectQueryUtil.getInstance().getVariantTablename(sid,projectId, referenceId, true));

        SelectQuery queryBase = new SelectQuery();
        queryBase.addFromTable(table.getTable());

        queryBase.addColumns(table.getDBColumn(CHROM));

        String roundFunction = "ROUND(" + POSITION.getColumnName() + "/" + binsize + ",0)";

        queryBase.addCustomColumns(FunctionCall.countAll());
        queryBase.addGroupings(table.getDBColumn(CHROM));

        addConditionsToQuery(queryBase, conditions);

        String query = queryBase.toString().replace("COUNT(*)", "COUNT(*)," + roundFunction) + "," + roundFunction;

        //long start = System.nanoTime();
        ResultSet rs = ConnectionController.executeQuery(sid, query);
        //System.out.println(query);
        //System.out.println("  time:" + (System.nanoTime() - start) / 1000000000);

        Map<String, Map<Range, Integer>> results = new HashMap<String, Map<Range, Integer>>();
        while (rs.next()) {

            String chrom = String.valueOf(rs.getString(1));

            Map<Range, Integer> chromMap;
            if (!results.containsKey(chrom)) {
                chromMap = new HashMap<Range, Integer>();
            } else {
                chromMap = results.get(chrom);
            }

            int binNo = rs.getInt(3);
            Range binRange = new Range(binNo * binsize, (binNo + 1) * binsize);

            int count = (int) (rs.getInt(2) * multiplier);

            chromMap.put(binRange, count);
            results.put(chrom, chromMap);
        }

        return results;
    }

    @Override
    public int getPatientCountWithVariantsInRange(String sid, int projectId, int referenceId, Condition[][] conditions, String chrom, int start, int end) throws SQLException, RemoteException, SessionExpiredException {
        //ToDo
        //TODO: approximate counts??
        //might not be a good idea... don't want to miss a dna id

        TableSchema table = getCustomTableSchema(sid, projectId, referenceId);
        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addCustomColumns("COUNT(DISTINCT " + DNA_ID.getColumnName() + ")");
        addConditionsToQuery(q, conditions);

        Condition[] cond = new Condition[3];
        cond[0] = new BinaryCondition(BinaryCondition.Op.EQUAL_TO, table.getDBColumn(CHROM), chrom);
        cond[1] = new BinaryCondition(BinaryCondition.Op.GREATER_THAN_OR_EQUAL_TO, table.getDBColumn(POSITION), start);
        cond[2] = new BinaryCondition(BinaryCondition.Op.LESS_THAN, table.getDBColumn(POSITION), end);
        q.addCondition(ComboCondition.and(cond));

        String query = q.toString();
        query = query.replaceFirst("'", "").replaceFirst("'", "");

        ResultSet rs = ConnectionController.executeQuery(sid, query);
        rs.next();

        int numrows = rs.getInt(1);

        return numrows;
    }

    @Override
    public void addConditionsToQuery(SelectQuery query, Condition[][] conditions) {
        Condition[] c = new Condition[conditions.length];
        for (int i = 0; i < conditions.length; i++) {
            c[i] = ComboCondition.and(conditions[i]);
        }
        query.addCondition(ComboCondition.or(c));
    }

    @Override
    public Map<String, List<String>> getSavantBookmarkPositionsForDNAIDs(String sessID, int projID, int refID, Condition[][] conditions, List<String> dnaIds, int limit) throws SQLException, RemoteException, SessionExpiredException {

        Map<String, List<String>> results = new HashMap<String, List<String>>();

        TableSchema table = getCustomTableSchema(sessID, projID, refID);
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(DNA_ID), table.getDBColumn(CHROM), table.getDBColumn(POSITION));
        addConditionsToQuery(query, conditions);
        Condition[] dnaIdConditions = new Condition[dnaIds.size()];
        for (int i = 0; i < dnaIds.size(); i++) {
            dnaIdConditions[i] = BinaryConditionMS.equalTo(table.getDBColumn(DNA_ID), dnaIds.get(i));
            results.put(dnaIds.get(i), new ArrayList<String>());
        }
        query.addCondition(ComboCondition.or(dnaIdConditions));

        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString() + ((limit == -1) ? "" : (" LIMIT " + limit)));

        while (rs.next()) {
            results.get(rs.getString(1)).add(rs.getString(2) + ":" + (rs.getLong(3) - 100) + "-" + (rs.getLong(3) + 100));
        }

        return results;
    }

    @Override
    public Map<String, Integer> getNumVariantsInFamily(String sessID, int projID, int refID, String famID, Condition[][] conditions) throws SQLException, RemoteException, SessionExpiredException {

        //TODO: approximate counts

        String name = ProjectManager.getInstance().getVariantTableName(sessID, projID, refID, true);

        if (name == null) {
            return null;
        }

        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessID, name);

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addColumns(table.getDBColumn(DNA_ID));
        q.addCustomColumns(FunctionCall.countAll());
        q.addGroupings(table.getDBColumn(DNA_ID));
        addConditionsToQuery(q, conditions);

        Map<String, String> patientToDNAIDMap = PatientManager.getInstance().getDNAIDsForFamily(sessID, projID, famID);
        Map<String, List<String>> betterPatientToDNAIDMap = new HashMap<String, List<String>>();

        List<String> dnaIDs = new ArrayList<String>();
        for (String patientID : patientToDNAIDMap.keySet()) {
            String dnaIDString = patientToDNAIDMap.get(patientID);
            List<String> idList = new ArrayList<String>();
            for (String dnaID : dnaIDString.split(",")) {
                if (dnaID != null && !dnaID.isEmpty()) {
                    dnaIDs.add(dnaID);
                    idList.add(dnaID);
                }
            }
            betterPatientToDNAIDMap.put(patientID, idList);
        }

        Map<String, Integer> dnaIDsToCountMap = new HashMap<String, Integer>();

        if (!dnaIDs.isEmpty()) {

            Condition[] dnaIDConditions = new Condition[dnaIDs.size()];

            int i = 0;
            for (String dnaID : dnaIDs) {
                dnaIDConditions[i] = BinaryCondition.equalTo(table.getDBColumn(DNA_ID), dnaID);
                i++;
            }

            q.addCondition(ComboCondition.or(dnaIDConditions));

            ResultSet rs = ConnectionController.executeQuery(sessID, q.toString());

            while (rs.next()) {
                dnaIDsToCountMap.put(rs.getString(1), rs.getInt(2));
            }
        }

        Map<String, Integer> patientIDTOCount = new HashMap<String, Integer>();
        for (String patientID : betterPatientToDNAIDMap.keySet()) {
            int count = 0;
            for (String dnaID : betterPatientToDNAIDMap.get(patientID)) {
                if (dnaIDsToCountMap.containsKey(dnaID)) {
                    count += dnaIDsToCountMap.get(dnaID);
                }
            }
            patientIDTOCount.put(patientID, count);
        }

        return patientIDTOCount;
    }

    @Override
    public void cancelUpload(String sid, int uploadId, String tableName) {
        try {

            //remove log entry
            AnnotationLogManager.getInstance().removeAnnotationLogEntry(sid, uploadId);

            //drop staging table
            DBUtils.dropTable(sid, tableName);

        } catch (Exception ex) {
            LOG.warn("Error cancelling upload " + uploadId + " for " + tableName, ex);
        }
    }

    @Override
    public void addTagsToUpload(String sid, int uploadID, String[][] variantTags) throws SQLException, SessionExpiredException {

        try {
            List<VariantTag> tags = new ArrayList<VariantTag>();
            //add tags
            for (int i = 0; i < variantTags.length && !Thread.currentThread().isInterrupted(); i++) {
                tags.add(new VariantTag(variantTags[i][0], variantTags[i][1], uploadID));
            }
            entityManager.persistAll(tags);
        } catch (InitializationException e) {
            LOG.error("Error adding tags for upload id : " + uploadID, e);
        }
    }

    public void removeTags(String sessID, int uploadID) throws SQLException, SessionExpiredException {
        ConnectionController.executeUpdate(sessID, MedSavantDatabase.VariantTagTableSchema.delete(VariantTagColumns.UPLOAD_ID, uploadID).toString());
    }

    @Override
    public List<String> getDistinctTagNames(String sessID) throws SQLException, SessionExpiredException, QueryException {

        Query query = queryManager.createQuery("Select t.key from VariantTag t group by t.name");
        List<ResultRow> rows = query.executeForRows();

        List<String> tagNames = new ArrayList<String>();
        for (ResultRow row : rows) {
            tagNames.add(row.getString("key"));
        }
        return tagNames;
    }

    @Override
    public List<String> getValuesForTagName(String sessID, String tagName) throws SQLException, SessionExpiredException, QueryException {

        Query query = queryManager.createQuery("Select t.value from VariantTag t where t.key = :key");
        query.setParameter("key", tagName);
        List<ResultRow> rows = query.executeForRows();

        List<String> tagValues = new ArrayList<String>();
        for (ResultRow row : rows) {
            tagValues.add(row.getString("value"));
        }
        return tagValues;

    }

    @Override
    public List<Integer> getUploadIDsMatchingVariantTags(String sessID, String[][] variantTags) throws SQLException, SessionExpiredException, QueryException {

        //construct query conditions
        StringBuilder statement = new StringBuilder("Select t.upload_id from Variant tag");
        for (int i = 0; i < variantTags.length; i++) {
            if (i == 0) {
                statement.append(" where ");
            } else {
                statement.append(" or ");
            }
            statement.append("( t.key = " + variantTags[i][0] + " and t.value = " + variantTags[i][1] + ")");
        }

        //execute and parse results
        Query query = queryManager.createQuery(statement.toString());
        LOG.info(query.toString());
        List<ResultRow> rows = query.executeForRows();
        List<Integer> uploadIds = new ArrayList<Integer>();
        for (ResultRow row : rows) {
            uploadIds.add(row.getInt("upload_id"));
        }
        return uploadIds;
    }

    @Override
    public SimpleVariantFile[] getUploadedFiles(String sessID, int projID, int refID) throws SQLException, RemoteException, SessionExpiredException {

        String statement = "Select f from Variant_File f";
        Query query = queryManager.createQuery(statement);

        List<SimpleVariantFile> variantFileList = query.execute();

        return variantFileList.toArray(new SimpleVariantFile[]{});
    }

    @Override
    public List<String[]> getTagsForUpload(String sessID, int uplID) throws SQLException, RemoteException, SessionExpiredException {

        Query query = queryManager.createQuery("Select t from VariantTag t where t.upload_id = :uploadId");
        query.setParameter("uploadId", uplID);

        List<String[]> result = new ArrayList<String[]>();
        List<VariantTag> variantTags = query.execute();
        for (VariantTag tag : variantTags) {
            result.add(new String[] {tag.getKey(), tag.getValue()});
        }
        return result;
    }

    /*
     * @Override public Set<StarredVariant> getStarredVariants(String sid, int
     * projectId, int referenceId) throws SQLException, RemoteException, SessionExpiredException {
     *
     * TableSchema table = MedSavantDatabase.VariantStarredTableSchema;
     *
     * SelectQuery q = new SelectQuery(); q.addFromTable(table.getTable());
     * q.addColumns(
     * table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_UPLOAD_ID),
     * table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_FILE_ID),
     * table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_VARIANT_ID),
     * table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_USER),
     * table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_DESCRIPTION),
     * table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_TIMESTAMP));
     * q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_PROJECT_ID),
     * projectId));
     * q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_REFERENCE_ID),
     * referenceId));
     *
     * ResultSet rs = ConnectionController.executeQuery(sid, q.toString());
     *
     * Set<StarredVariant> result = new HashSet<StarredVariant>();
     * while(rs.next()) { result.add(new StarredVariant(
     * rs.getInt(VariantStarredTableSchema.COLUMNNAME_OF_UPLOAD_ID),
     * rs.getInt(VariantStarredTableSchema.COLUMNNAME_OF_FILE_ID),
     * rs.getInt(VariantStarredTableSchema.COLUMNNAME_OF_VARIANT_ID),
     * rs.getString(VariantStarredTableSchema.COLUMNNAME_OF_USER),
     * rs.getString(VariantStarredTableSchema.COLUMNNAME_OF_DESCRIPTION),
     * rs.getTimestamp(VariantStarredTableSchema.COLUMNNAME_OF_TIMESTAMP))); }
     * return result; }
     */
    @Override
    public List<VariantComment> getVariantComments(String sid, int projectId, int referenceId, int uploadId, int fileID, int variantID) throws SQLException, RemoteException, SessionExpiredException {

        String statement = "Select c from Comment c where " +
                "c.project_id = :projectId AND " +
                "c.reference_id = :referenceId AND " +
                "c.upload_id = :uploadId AND " +
                "c.file_id = :fileId AND " +
                "c.variant_id = :variantId";
        Query query = queryManager.createQuery(statement);
        query.setParameter("projectId", projectId);
        query.setParameter("referenceId", referenceId);
        query.setParameter("uploadId", uploadId);
        query.setParameter("fileId", fileID);
        query.setParameter("variantId", variantID);

        return query.execute();
    }

    @Override
    public void addVariantComments(String sid, List<VariantComment> variants) throws SQLException, RemoteException, SessionExpiredException {

        try {
            entityManager.persistAll(variants);
        } catch (InitializationException e) {
            LOG.error("Failed to persist variant comments.");
        }
    }

    @Override
    public void removeVariantComments(String sessID, List<VariantComment> comments) throws SQLException, SessionExpiredException {


        String statement = "Select c from Comment c where " +
                "c.project_id = :projectId AND " +
                "c.reference_id = :referenceId AND " +
                "c.upload_id = :uploadId AND " +
                "c.file_id = :fileId AND " +
                "c.variant_id = :variantId";
        Query query = queryManager.createQuery(statement);

        for (VariantComment comment : comments) {
            query.setParameter("projectId", comment.getProjectId());
            query.setParameter("referenceId", comment.getReferenceId());
            query.setParameter("uploadId", comment.getUploadId());
            query.setParameter("fileId", comment.getFileId());
            query.setParameter("variantId", comment.getVariantId());

            query.executeDelete();
        }
    }

    private int getTotalNumStarred(String sid, int projectId, int referenceId) throws SQLException, SessionExpiredException {
        //ToDo - remove, no longer used
        TableSchema table = MedSavantDatabase.VariantStarredTableSchema;

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addCustomColumns(FunctionCall.countAll());
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceId));

        ResultSet rs = ConnectionController.executeQuery(sid, q.toString());

        rs.next();
        return rs.getInt(1);
    }

    public static void addEntryToFileTable(String sid, int uploadId, int fileId, String fileName) throws SQLException, SessionExpiredException {

        try {
            String user = SessionController.getInstance().getUserForSession(sid);
            SimpleVariantFile simpleVariantFile = new SimpleVariantFile(uploadId,
                    fileId,
                    fileName,
                    Calendar.getInstance().getTime().toString(),
                    user);

            entityManager.persist(simpleVariantFile);
        } catch (RemoteException e) {
            LOG.error("Error retrieving current user", e);
        } catch (InitializationException e) {
            LOG.error("Error adding variant file", e);
        }
    }

    public void removeEntryFromFileTable(String sessID, int uploadID, int fileID) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Delete from VariantFile v where v.upload_id = :uploadId AND v.file_id = :fileId");
        query.setParameter("uploadId", uploadID);
        query.setParameter("fileId", fileID);

        query.executeDelete();
    }

    @Override
    public Map<SimplePatient, Integer> getPatientHeatMap(String sessID, int projID, int refID, Condition[][] conditions, Collection<SimplePatient> patients) throws SQLException, RemoteException, SessionExpiredException {

        //get dna ids
        List<String> dnaIds = new ArrayList<String>();
        for (SimplePatient sp : patients) {
            for (String id : sp.getDnaIds()) {
                if (!dnaIds.contains(id)) {
                    dnaIds.add(id);
                }
            }
        }

        Map<String, Integer> dnaIdMap = getDNAIDHeatMap(sessID, projID, refID, conditions, dnaIds);

        //map back to simple patients;
        Map<SimplePatient, Integer> result = new HashMap<SimplePatient, Integer>();
        for (SimplePatient p : patients) {
            Integer count = 0;
            for (String dnaId : p.getDnaIds()) {
                Integer i = dnaIdMap.get(dnaId);
                if (i != null) {
                    count += i;
                }
            }
            result.put(p, count);
        }

        return result;
    }

    @Override
    public Map<String, Integer> getDNAIDHeatMap(String sessID, int projID, int refID, Condition[][] conditions, Collection<String> dnaIDs) throws SQLException, RemoteException, SessionExpiredException {

        Map<String, Integer> dnaIDMap = new HashMap<String, Integer>();

        if (!dnaIDs.isEmpty()) {
            Condition[] c1 = new Condition[conditions.length];
            for (int i = 0; i < conditions.length; i++) {
                c1[i] = ComboCondition.and(conditions[i]);
            }
            Condition c2 = ComboCondition.or(c1);

            getDNAIDHeatMapHelper(sessID, null, 1, dnaIDs, c2, true, dnaIDMap);
        }

        return dnaIDMap;
    }

    private void getDNAIDHeatMapHelper(String sessID, TableSchema table, float multiplier, Collection<String> dnaIDs, Condition c, boolean useThreshold, Map<String, Integer> map) throws SQLException, SessionExpiredException {
        /* SELECT COUNT(*),t0.dna_id FROM z_variant_proj1_ref3_update9_subset t0 WHERE ((t0.dna_id IN ('NA00001','NA00002','NA00003') ) AND (1 = 1)) GROUP BY t0.dna_id  */

        String statement = "Select count(v), v.dna_id from Variant v where v.dna_id in (%s) and %s";

        statement = String.format(statement, StringUtils.join(dnaIDs, ","), c);

        Query query = queryManager.createQuery(statement);
        List<ResultRow> resultRows = query.executeForRows();
        for (ResultRow resultRow : resultRows) {
            int value = (Integer) resultRow.getObject("count") * new Double(multiplier).intValue();
            if (!useThreshold || value >= PATIENT_HEATMAP_THRESHOLD) {
                String dnaId = (String) resultRow.getObject("dna_id");
                map.put(dnaId, value);
            }
        }
    }

    private Condition createNucleotideCondition(DbColumn column) {
        return ComboCondition.or(
                BinaryCondition.equalTo(column, "A"),
                BinaryCondition.equalTo(column, "C"),
                BinaryCondition.equalTo(column, "G"),
                BinaryCondition.equalTo(column, "T"));
    }

    private Annotation[] getAnnotationsFromIDs(int[] annotIDs, String sessID) throws RemoteException, SessionExpiredException, SQLException {
        int numAnnotations = annotIDs.length;
        Annotation[] annotations = new Annotation[numAnnotations];
        for (int i = 0; i < numAnnotations; i++) {
            annotations[i] = AnnotationManager.getInstance().getAnnotation(sessID, annotIDs[i]);
            LOG.info("\t" + (i + 1) + ". " + annotations[i].getProgram() + " " + annotations[i].getReferenceName() + " " + annotations[i].getVersion());
        }
        return annotations;
    }

    /*
     * Convert VariantRecords into a list of Object[] to be displayed on the UI.
     */
    private List<Object[]> convertResultRowListToObjectArrayList(List<VariantRecord> variantRecords) {
        List<Object[]> resultList = new ArrayList(variantRecords.size());

        for (VariantRecord variantRecord : variantRecords) {
            resultList.add(convertResultRowToObjectArray(variantRecord));
        }

        return resultList;
    }

    private StringBuilder addConditionsToQuery(String stmt, Condition[][] conditions) {
        StringBuilder statement = new StringBuilder(stmt);

        StringBuilder cond = new StringBuilder();
        for (int i = 0; i < conditions.length; i++) {
            cond.append(StringUtils.join(conditions[i]," AND "));
        }

        if (!("(1 = 1)".equals(cond.toString()) || "null".equals(cond.toString()) || "".equals(cond.toString()))) {
            statement.append(" where ");
            statement.append(cond);
        }

        return statement;
    }

    public static boolean getREMOVE_WORKING_DIR() {
        return REMOVE_WORKING_DIR;
    }

    /*
         * Hacky and ugly, needs to be refactored.
         */
    private Object[] convertResultRowToObjectArray(VariantRecord variantRecord) {

        Object[] result = new Object[31];
        result[0] = variantRecord.getUploadID();
        result[1] = variantRecord.getFileID();
        result[2] = variantRecord.getVariantID();
        result[3] = variantRecord.getDnaID();
        result[4] = variantRecord.getChrom();
        result[5] = variantRecord.getPosition();
        result[6] = variantRecord.getDbSNPID();
        result[7] = variantRecord.getRef();
        result[8] = variantRecord.getAlt();
        result[9] = variantRecord.getQual();
        result[10] = variantRecord.getFilter();
        result[11] = variantRecord.getVariantType(variantRecord.getRef(), variantRecord.getAlt()).toString();
        result[12] = variantRecord.getZygosity().toString();
        result[13] = variantRecord.getGenotype();
        result[14] = variantRecord.getCustomInfo();
        result[15] = variantRecord.getAncestralAllele();
        result[16] = variantRecord.getAlleleCount();
        result[17] = variantRecord.getAlleleFrequency();
        result[18] = variantRecord.getNumberOfAlleles();
        result[19] = variantRecord.getBaseQuality();
        result[20] = variantRecord.getCigar();
        result[21] = variantRecord.getDbSNPMembership();
        result[22] = variantRecord.getDepthOfCoverage();
        result[23] = variantRecord.getEndPosition();
        result[24] = variantRecord.getHapmap2Membership();
        result[25] = variantRecord.getMappingQuality();
        result[26] = variantRecord.getNumberOfZeroMQ();
        result[27] = variantRecord.getNumberOfSamplesWithData();
        result[28] = variantRecord.getStrandBias();
        result[29] = variantRecord.getIsSomatic();
        result[30] = variantRecord.getIsValidated();

        return result;
    }
}
