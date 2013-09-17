/*
 *    Copyright 2012 University of Toronto
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

package org.ut.biolab.medsavant.server.db.admin;

import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import net.sf.samtools.util.BlockCompressedInputStream;
import org.ut.biolab.medsavant.server.serverapi.GeneSetManager;
import org.ut.biolab.medsavant.shared.model.Gene;
import org.ut.biolab.medsavant.shared.model.GeneSet;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.util.IOUtils;
import org.ut.biolab.medsavant.shared.util.NetworkUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * Loads an SQL table with genes based on the contents of a tabix file.
 * Since we read the file in it's entirety, we skip all the tabix processing and just pull in the file
 * as fast as possible.
 *
 * @author tarkvara
 */
public class TabixTableLoader {
    private final DbTable table;

    public TabixTableLoader(DbTable t) {
        table = t;
    }

    public void loadGenes(String sessID, URI uri, String genome, String type, String... colNames) throws IOException, SQLException, SessionExpiredException {
        InputStream input = null;
        Connection conn = null;
        String line;
        int lineNum = 1;

        try {
            input = new BlockCompressedInputStream(NetworkUtils.getSeekableStreamForURI(uri));

            InsertQuery query = new InsertQuery(table);
            query.addColumn(table.findColumn("genome"), genome);
            query.addColumn(table.findColumn("type"), type);

            List<Column> dataCols = new ArrayList<Column>(colNames.length);
            for (String colName: colNames) {
                if (colName != null) {
                    dataCols.add(table.findColumn(colName));
                }
            }
            query.addPreparedColumnCollection(dataCols);
            boolean trace = true;

            while ((line = IOUtils.readLine(input)) != null) {
                if (line.charAt(0) != '#') {
                    String[] fields = line.split("\t");
                    String transcript = fields[1];
                    String chrom = fields[2];
                    int start = Integer.parseInt(fields[4]);
                    int end = Integer.parseInt(fields[5]);
                    int codingStart = Integer.parseInt(fields[6]);
                    int codingEnd = Integer.parseInt(fields[7]);
                    String exonStarts = fields[9];
                    String exonEnds = fields[10];
                    String name = fields[12];
                    Gene gene = new Gene(name, chrom, start, end, codingStart, codingEnd, transcript, exonStarts, exonEnds);
                    gene.setGenome(genome);
                    gene.setType(type);

                    GeneSetManager.getInstance().addGene(gene);
                }
                lineNum++;
            }
            GeneSet geneSet = new GeneSet(genome, type, lineNum);
            GeneSetManager.getInstance().addGeneSet(geneSet);
        } catch (IOException x) {
            throw x;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
            if (input != null) {
                input.close();
            }
        }
    }
}
