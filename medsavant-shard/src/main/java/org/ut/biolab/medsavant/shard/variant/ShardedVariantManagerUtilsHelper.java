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
package org.ut.biolab.medsavant.shard.variant;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.hibernate.Session;
import org.ut.biolab.medsavant.shard.file.FileUtils;
import org.ut.biolab.medsavant.shard.file.TSVUtils;
import org.ut.biolab.medsavant.shard.nonshard.ShardedConnectionController;
import org.ut.biolab.medsavant.shard.strategy.PositionShardSelector;
import org.ut.biolab.medsavant.shared.util.MiscUtils;

/**
 * Sharded helper class for VariantManagerUtils.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class ShardedVariantManagerUtilsHelper {

    /**
     * Exports variant table on each shard to a local TSV file.
     * 
     * @param exportQuery
     *            parametrized query to run
     */
    public void exportVariantTablesToTSVFiles(String exportQuery) {
        Session session = ShardedSessionManager.openSession();

        ShardedConnectionController.executeQueryWithoutResultOnAllShards(exportQuery, true, true);

        ShardedSessionManager.closeSession(session);
    }

    /**
     * Exports variant table on each shard to a single shared TSV file.
     * 
     * @param file
     *            file
     * @param exportQuery
     *            query to run
     */
    public void exportVariantTablesToSingleTSVFile(File file, String exportQuery) {
        Session session = ShardedSessionManager.openSession();

        int shardNo = ShardedSessionManager.getShardNo();
        ShardedConnectionController.executeQueryWithoutResultOnAllShards(exportQuery, true, true);

        // merge files
        for (int i = 0; i < shardNo; i++) {
            FileUtils.mergeFiles(file.getAbsolutePath().replaceAll("\\\\", "/"), String.format(FileUtils.getParametrizedFilePath(file), i));
            // FileUtils.deleteFile(fileBase + FileUtils.getFileForShard(i));
        }

        ShardedSessionManager.closeSession(session);
    }

    /**
     * Loads TSV file to a table.
     * 
     * @param file
     *            file to load
     * @param tableName
     *            table to fill in
     * @param fieldDeliminer
     *            field deliminer
     * @param enclosedBy
     *            enclosed by string
     * @param escapeSequence
     *            escape string
     * @throws IOException
     */
    public static void uploadTSVFileToVariantTable(File file, String tableName, String fieldDeliminer, String enclosedBy, String escapeSequence) throws IOException {
        Session session = ShardedSessionManager.openSession();
        int shardNo = ShardedSessionManager.getShardNo();
        PositionShardSelector sel = new PositionShardSelector(ShardedSessionManager.MAX_VARIANT_POSITION, shardNo);

        int chunkSize = 100000; // number of lines per chunk (100K lines =
        // ~50MB for a standard VCF file)

        // parse the file and split it to separate files per-shard
        BufferedReader br = new BufferedReader(new FileReader(file));
        String parentDirectory = file.getParentFile().getAbsolutePath();
        List<BufferedWriter> bws = new ArrayList<BufferedWriter>();
        List<Integer> lineNumbers = new ArrayList<Integer>();
        List<String> currentOutputPaths = new ArrayList<String>();
        List<Boolean> stateOpens = new ArrayList<Boolean>();
        for (int i = 0; i < shardNo; i++) {
            bws.add(null);
            lineNumbers.add(0);
            currentOutputPaths.add(null);
            stateOpens.add(false);
        }

        String line;
        int currentShard;
        System.out.println(file.getPath());
        while ((line = br.readLine()) != null) {
            // determine shard
//            System.out.println(TSVUtils.getPos(line) + ": " + line);
            currentShard = sel.getShard(TSVUtils.getPos(line)).getId();

            lineNumbers.set(currentShard, lineNumbers.get(currentShard) + 1);

            // start a new output file
            if (lineNumbers.get(currentShard) % chunkSize == 1) {
                currentOutputPaths.set(currentShard,
                        parentDirectory + "/" + MiscUtils.extractFileName(file.getAbsolutePath()) + "_" + currentShard + "_" + (lineNumbers.get(currentShard) / chunkSize));
                bws.set(currentShard, new BufferedWriter(new FileWriter(currentOutputPaths.get(currentShard))));
                stateOpens.set(currentShard, true);
            }

            // write line to chunk file
            bws.get(currentShard).write(line + "\n");

            // close and upload this output file
            if (lineNumbers.get(currentShard) % chunkSize == 0) {
                try {
                    bws.get(currentShard).close();
                } catch (IOException e) {
                    System.err.println("Failed to close writer to: " + currentOutputPaths.get(currentShard));
                }

                String query = "LOAD DATA LOCAL INFILE '" + currentOutputPaths.get(currentShard).replaceAll("\\\\", "/") + "' " + "INTO TABLE " + tableName + " "
                        + "FIELDS TERMINATED BY '" + StringEscapeUtils.escapeJava(fieldDeliminer) + "' ENCLOSED BY '" + enclosedBy + "' " + "ESCAPED BY '" + StringEscapeUtils.escapeJava(escapeSequence) + "' "
                        // + " LINES TERMINATED BY '\\r\\n'";
                        + ";";

                ShardedConnectionController.executeQueryWithoutResultOnShard(currentShard, query);

                stateOpens.set(currentShard, false);
            }
        }

        // write the remaining open files
        for (int i = 0; i < shardNo; i++) {
            if (bws.get(i) != null && stateOpens.get(i)) {
                try {
                    bws.get(i).close();
                } catch (IOException e) {
                    System.err.println("Failed to close writer to: " + currentOutputPaths.get(i));
                }

                String query = "LOAD DATA LOCAL INFILE '" + currentOutputPaths.get(i).replaceAll("\\\\", "/") + "' " + "INTO TABLE " + tableName + " " + "FIELDS TERMINATED BY '"
                        + StringEscapeUtils.escapeJava(fieldDeliminer) + "' ENCLOSED BY '" + enclosedBy + "' " + "ESCAPED BY '" + StringEscapeUtils.escapeJava(escapeSequence)
                        + "'"
                        // + " LINES TERMINATED BY '\\r\\n'"
                        + ";";

                ShardedConnectionController.executeQueryWithoutResultOnShard(i, query);
            }
        }

        ShardedSessionManager.closeSession(session);
    }
}
