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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.util.MiscUtils;

/**
 * Helper for VariantManagerUtils.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class VariantManagerUtilsHelper {

    private static final Log LOG = LogFactory.getLog(VariantManagerUtilsHelper.class);

    /**
     * Exports variant table on each shard to a local TSV file.
     * 
     * @param exportQuery
     *            parametrized query to run
     * @throws SessionExpiredException
     * @throws SQLException
     */
    public void exportVariantTablesToTSVFiles(String sid, String exportQuery) throws SQLException, SessionExpiredException {
        ConnectionController.executeQuery(sid, exportQuery);
    }

    /**
     * Loads TSV file to variant table.
     * 
     * @param sid
     *            session ID
     * @param file
     *            TSV file
     * @param tableName
     *            name of the table to load to
     * @throws SQLException
     * @throws IOException
     * @throws SessionExpiredException
     */
    public static void uploadTSVFileToVariantTable(String sid, File file, String tableName) throws SQLException, IOException, SessionExpiredException {

        BufferedReader br = new BufferedReader(new FileReader(file));

        // TODO: for some reason the connection is closed going into this
        // function
        Connection c = ConnectionController.connectPooled(sid);

        c.setAutoCommit(false);

        int chunkSize = 100000; // number of lines per chunk (100K lines =
        // ~50MB for a standard VCF file)

        int lineNumber = 0;

        BufferedWriter bw = null;
        String currentOutputPath = null;

        boolean stateOpen = false;

        String parentDirectory = file.getParentFile().getAbsolutePath();

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

                LOG.info("Closing and uploading partial file " + currentOutputPath);

                String query = "LOAD DATA LOCAL INFILE '" + currentOutputPath.replaceAll("\\\\", "/") + "' " + "INTO TABLE " + tableName + " " + "FIELDS TERMINATED BY '"
                        + VariantManagerUtils.FIELD_DELIMITER + "' ENCLOSED BY '" + VariantManagerUtils.ENCLOSED_BY + "' " + "ESCAPED BY '"
                        + StringEscapeUtils.escapeJava(VariantManagerUtils.ESCAPE_CHAR) + "' "
                        // + " LINES TERMINATED BY '\\r\\n'";
                        + ";";

                // LOG.info(query);
                Statement s = c.createStatement();
                s.setQueryTimeout(30 * 60); // 30 minutes
                s.execute(query);

                /*
                 * if (VariantManager.REMOVE_TMP_FILES) { boolean deleted = new
                 * File(currentOutputPath).delete(); LOG.info("Deleting " +
                 * currentOutputPath + " - " + (deleted ? "successful" :
                 * "failed")); }
                 */
                stateOpen = false;
            }
        }

        // write the remaining open file
        if (bw != null && stateOpen) {
            bw.close();
            String query = "LOAD DATA LOCAL INFILE '" + currentOutputPath.replaceAll("\\\\", "/") + "' " + "INTO TABLE " + tableName + " " + "FIELDS TERMINATED BY '"
                    + StringEscapeUtils.escapeJava(VariantManagerUtils.FIELD_DELIMITER) + "' ENCLOSED BY '" + VariantManagerUtils.ENCLOSED_BY + "' " + "ESCAPED BY '"
                    + StringEscapeUtils.escapeJava(VariantManagerUtils.ESCAPE_CHAR) + "'"
                    // + " LINES TERMINATED BY '\\r\\n'"
                    + ";";

            LOG.info("Closing and uploading last partial file " + currentOutputPath);

            // LOG.info(query);
            Statement s = c.createStatement();
            s.setQueryTimeout(60 * 60); // 1 hour
            s.execute(query);

            /*
             * if (VariantManager.REMOVE_TMP_FILES) { boolean deleted = new
             * File(currentOutputPath).delete(); LOG.info("Deleting " +
             * currentOutputPath + " - " + (deleted ? "successful" : "failed"));
             * }
             */
        }

        LOG.info("Imported " + lineNumber + " lines of variants in total");

        c.commit();
        c.setAutoCommit(true);

        c.close();

        /*
         * if (VariantManager.REMOVE_TMP_FILES) { boolean deleted =
         * file.delete(); LOG.info("Deleting " + file.getAbsolutePath() + " - "
         * + (deleted ? "successful" : "failed")); }
         */
    }
}
