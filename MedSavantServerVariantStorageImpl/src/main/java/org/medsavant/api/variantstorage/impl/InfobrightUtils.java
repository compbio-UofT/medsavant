/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.medsavant.api.variantstorage.impl;

import com.healthmarketscience.sqlbuilder.Query;
import com.healthmarketscience.sqlbuilder.UnionQuery;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import javax.sql.PooledConnection;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.medsavant.api.common.MedSavantUpdate;
import org.medsavant.api.database.MedSavantJDBCPooledConnection;
import static org.medsavant.api.variantstorage.impl.DBSettings.ENCLOSED_BY;
import static org.medsavant.api.variantstorage.impl.DBSettings.ESCAPE_CHAR;
import static org.medsavant.api.variantstorage.impl.DBSettings.FIELD_DELIMITER;
import static org.medsavant.api.variantstorage.impl.DBSettings.getVariantFileIBTableName;
import org.medsavant.api.variantstorage.impl.schemas.DBSchemas;
import static org.medsavant.api.variantstorage.impl.schemas.DBSchemas.schema;
import org.medsavant.api.variantstorage.impl.schemas.VariantFileTableSchema;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.format.AnnotationFormat;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.util.DirectorySettings;

/**
 *
 * @author jim
 */
public class InfobrightUtils {

    private static final Log LOG = LogFactory.getLog(InfobrightUtils.class);

    private static final String BRIGHTHOUSE_ENGINE = "BRIGHTHOUSE";
    private static final String MYISAM_ENGINE = "MYISAM";

    public static void dumpTable(MedSavantJDBCPooledConnection conn, String tableName, File dst) throws SQLException {
        String intoString
                = "INTO OUTFILE \"" + dst.getAbsolutePath().replaceAll("\\\\", "/") + "\" "
                + "FIELDS TERMINATED BY '" + StringEscapeUtils.escapeJava(FIELD_DELIMITER) + "' "
                + "ENCLOSED BY '" + ENCLOSED_BY + "' "
                + "ESCAPED BY '" + StringEscapeUtils.escapeJava(ESCAPE_CHAR) + "' "
                + " LINES TERMINATED BY '\\r\\n' ";

        String query = "SELECT * FROM " + tableName + " " + intoString;
        conn.executeQuery(query);

    }

    public static void loadTable(MedSavantJDBCPooledConnection conn, File src, String dst) throws SQLException {
        String query = "LOAD DATA LOCAL INFILE '" + src.getAbsolutePath().replaceAll("\\\\", "/") + "' "
                + "INTO TABLE " + dst + " "
                + "FIELDS TERMINATED BY '" + FIELD_DELIMITER + "' ENCLOSED BY '" + ENCLOSED_BY + "' "
                + "ESCAPED BY '" + StringEscapeUtils.escapeJava(ESCAPE_CHAR) + "' "
                + " LINES TERMINATED BY '\\r\\n'"
                + ";";
        LOG.info(query);
        conn.executeQuery(query);
    }

    public static void copyTable(MedSavantJDBCPooledConnection conn, String srcTableName, String dstTableName) throws IOException, SQLException {
        File tmp = null;
        try {
            int i = 0;
            do {
                File parent = DirectorySettings.getDatabaseWorkingDir();
                tmp = new File(parent, "tmpfile_" + i++);
            } while (tmp.exists());
            LOG.info("Copying table " + srcTableName + " to " + dstTableName + " by dumping and reloading...");
            LOG.info("Dumping " + srcTableName + " to file " + tmp.getAbsolutePath());
            dumpTable(conn, srcTableName, tmp);
            LOG.info("Loading " + tmp.getAbsolutePath() + " into " + dstTableName);
            loadTable(conn, tmp, dstTableName);
        } finally {
            if (tmp != null && tmp.exists()) {
                tmp.delete();
            }
        }
    }

    public static synchronized TableSchema makeTemporaryVariantFileIBTable(MedSavantJDBCPooledConnection conn) throws IOException, SQLException {
        int i = 0;
        String tableName;
        final String suffixPrefix = "_ib_tmp";
        String suffix;
        do {
            suffix = suffixPrefix + i;
            tableName = VariantFileTableSchema.TABLE_NAME_PREFIX + suffix;
            i++;
        } while (conn.tableExists(tableName));

        makeVariantFileTable(conn, true, tableName, BRIGHTHOUSE_ENGINE);
        return new VariantFileTableSchema(schema, suffix);

    }

    public static void makeVariantFileTable(MedSavantJDBCPooledConnection conn, boolean brighthouse) throws IOException, SQLException {
        String tableName;
        String engine;

        if (brighthouse) {
            tableName = getVariantFileIBTableName();
            engine = BRIGHTHOUSE_ENGINE;
        } else {
            tableName = DBSchemas.VariantFileTableSchema.getTableName();
            engine = MYISAM_ENGINE;
        }
        makeVariantFileTable(conn, brighthouse, tableName, engine);
    }

    private static void makeVariantFileTable(MedSavantJDBCPooledConnection conn, boolean brighthouse, String tableName, String engine) throws IOException, SQLException {
        TableSchema table = DBSchemas.VariantFileTableSchema;

        String extras = "";
        if (!brighthouse) {
            extras = ",UNIQUE(upload_id, file_id), UNIQUE(file_id)";
        }

        conn.executeUpdate("DROP TABLE IF EXISTS " + tableName);
        String query = "CREATE TABLE  `" + tableName + "` ("
                + "`upload_id` int(11) NOT NULL,"
                + "`file_id` int(11) NOT NULL " + (brighthouse ? "" : "AUTO_INCREMENT") + ","
                + "`project_id` int(11) NOT NULL,"
                + "`reference_id` int(11) NOT NULL,"
                + "`file_name` varchar(500) COLLATE latin1_bin NOT NULL"
                + extras
                + ") ENGINE=" + engine + " DEFAULT CHARSET=latin1 COLLATE=latin1_bin";

        LOG.info(query);
        conn.executeUpdate(query);

        if (brighthouse) {
            copyTable(conn, DBSchemas.VariantFileTableSchema.getTableName(), getVariantFileIBTableName());
        }
    }
    
     
    public static void copyQueryResultToNewTable(File tmpOutputFile, final MedSavantJDBCPooledConnection conn, final Query sq, final String dstTable) throws IOException, SQLException {      
        File f = tmpOutputFile;

        try {
            String intoString
                    = "INTO OUTFILE \"" + f.getAbsolutePath().replaceAll("\\\\", "/") + "\" "
                    + "FIELDS TERMINATED BY '" + StringEscapeUtils.escapeJava(FIELD_DELIMITER) + "' "
                    + "ENCLOSED BY '" + ENCLOSED_BY + "' "
                    + "ESCAPED BY '" + StringEscapeUtils.escapeJava(ESCAPE_CHAR) + "' "
                    + " LINES TERMINATED BY '\\r\\n' ";
            String queryString;
            if (sq instanceof UnionQuery) {
                queryString = sq.toString() + " " + intoString;
            } else {
                //In MySQL, the 'INTO OUTFILE' is placed after the last select in a union, but
                //everything in the union will actually be outputted to the file.
                queryString = sq.toString().replace("FROM", intoString + "FROM");
            }

            LOG.info(queryString);
            conn.executeQuery(queryString);           
            loadTable(conn, f, dstTable);
            LOG.info("Query result copied!");
        } finally {
            if (f != null && f.exists()) {
                f.delete();
            }
        }
    }

    public static synchronized void copyQueryResultToNewTable(File tmpOutputFile, final MedSavantJDBCPooledConnection conn, String query, final String dstTable) throws IOException, SQLException {
        File f = tmpOutputFile;
        try {
            String intoString
                    = "INTO OUTFILE \"" + f.getAbsolutePath().replaceAll("\\\\", "/") + "\" "
                    + "FIELDS TERMINATED BY '" + StringEscapeUtils.escapeJava(FIELD_DELIMITER) + "' "
                    + "ENCLOSED BY '" + ENCLOSED_BY + "' "
                    + "ESCAPED BY '" + StringEscapeUtils.escapeJava(ESCAPE_CHAR) + "' "
                    + " LINES TERMINATED BY '\\r\\n' ";
            String queryString = query + " " + intoString;
            LOG.info(queryString);
            conn.executeQuery(queryString);
            loadTable(conn, f, dstTable);
            LOG.info("Query result copied!");
        } finally {
            if (f != null && f.exists()) {
                f.delete();
            }
        }
    }
    public static void dropTable(MedSavantJDBCPooledConnection conn, String tableName) throws SQLException {
        conn.executeUpdate("DROP TABLE IF EXISTS " + tableName + ";");
    }
    
    /**
     * Creates a new variant table or variant table subset.
     * 
     * @param sessID
     * @param update
     * @param annIDs
     * @param customFields
     * @param sub
     * @return
     * @throws SQLException
     * @throws SessionExpiredException 
     */
    public String addVariantTableToDatabase(String sessID, MedSavantUpdate update, int[] annIDs, CustomField[] customFields, boolean sub) throws SQLException, SessionExpiredException {
        // Create basic fields.        
        String tableName = DBSettings.getVariantTableName(update.getProject().getProjectId(), update.getReference().getID(), update.getUpdateID());
        if (sub) {
            tableName += "_subset";
        }        
        
        TableSchema variantSchema = new TableSchema(MedSavantDatabase.schema, tableName, BasicVariantColumns.REQUIRED_VARIANT_FIELDS);
        for (CustomField f : customFields) {
            variantSchema.addColumn(f);
        }
       
        String s = "";
        for (DbColumn c : variantSchema.getColumns()) {
            s += c.getColumnNameSQL() + " ";
        }
        LOG.info("Creating variant table " + tableName + " with fields " + s);

        PooledConnection conn = ConnectionController.connectPooled(sessID);

        try {
            int j = 0;
            for (int ann : annIDs) {
                annIDs[j++] = ann;
                AnnotationFormat annFmt = AnnotationManager.getInstance().getAnnotationFormat(sessID, ann);
                for (CustomField f : annFmt.getCustomFields()) {
                    variantSchema.addColumn(f);
                }
            }

            String updateString;
            if (MedSavantServerEngine.getInstance().useInfiniDB()) {

                updateString = variantSchema.getCreateQuery() + " ENGINE=INFINIDB;";
            } else {
                updateString = variantSchema.getCreateQuery() + " ENGINE=BRIGHTHOUSE DEFAULT CHARSET=latin1 COLLATE=latin1_bin;";
            }            
            conn.executeUpdate(updateString);
        } finally {
            conn.close();
        }

        return tableName;
    }
}
