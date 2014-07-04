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
package org.ut.biolab.medsavant.server.db.admin;

import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import net.sf.samtools.util.BlockCompressedInputStream;
import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

import org.ut.biolab.medsavant.shared.util.IOUtils;
import org.ut.biolab.medsavant.shared.util.NetworkUtils;


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

            conn = ConnectionController.connectPooled(sessID);
            conn.setAutoCommit(false);

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
            PreparedStatement prep = conn.prepareStatement(query.toString());

            boolean trace = true;
            while ((line = IOUtils.readLine(input)) != null) {
                if (line.charAt(0) != '#') {
                    String[] fields = line.split("\t");
                    prep.clearParameters();
                    int j = 0;
                    for (int i = 0; i < colNames.length; i++) {
                        if (colNames[i] != null) {
                            prep.setObject(++j, fields[i]);
                            if (trace) {
                            //    System.out.println(j + ": " + colNames[i] + " = " + fields[i]);
                            }
                        }
                    }
                    prep.executeUpdate();
                    trace = false;
                }
                lineNum++;
            }
        } catch (IOException x) {
            throw x;
        } catch (SQLException x) {
            System.out.println("Error at Tabix line " + lineNum);
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
