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
package org.ut.biolab.medsavant.shared.db.shard.result;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 *
 * @author mfiume
 */
public class ShardedResultSet extends AbstractResultSet {

    private Queue<ResultSet> resultSets;
    private final List<ResultSet> resultSetsList;

    public ShardedResultSet(List<ResultSet> resultSetsList) {
        super();
        this.resultSetsList = resultSetsList;
        this.resultSets = new PriorityQueue<ResultSet>(resultSetsList);
    }

    private ResultSet rs() {
        return resultSets.peek();
    }

    @Override
    public boolean next() throws SQLException {
        boolean b = rs().next();

        // hit the end of this result set
        if (!b) {

            // remove the head of the queue
            resultSets.remove().close();

            // no more result sets
            if (resultSets.isEmpty()) {
                return false;

            // consider the next result set
            } else {
                return next();
            }

        // still some left in this result set
        } else {
            return b; // true
        }
    }

    @Override
    public void close() throws SQLException {
        for (ResultSet s : resultSets) {
            s.close();
        }
    }

    @Override
    public boolean wasNull() throws SQLException {
        return rs().wasNull();
    }

    @Override
    public String getString(int i) throws SQLException {
        return rs().getString(i);
    }

    @Override
    public boolean getBoolean(int i) throws SQLException {
        return rs().getBoolean(i);
    }

    @Override
    public byte getByte(int i) throws SQLException {
        return rs().getByte(i);
    }

    @Override
    public short getShort(int i) throws SQLException {
        return rs().getShort(i);
    }

    @Override
    public int getInt(int i) throws SQLException {
         return rs().getInt(i);
    }

    @Override
    public long getLong(int i) throws SQLException {
        return rs().getLong(i);
    }

    @Override
    public float getFloat(int i) throws SQLException {
        return rs().getFloat(i);
    }

    @Override
    public double getDouble(int i) throws SQLException {
        return rs().getDouble(i);
    }

    @Override
    public BigDecimal getBigDecimal(int i, int i1) throws SQLException {
        return rs().getBigDecimal(i,i1);
    }

    @Override
    public byte[] getBytes(int i) throws SQLException {
        return rs().getBytes(i);
    }

    @Override
    public Date getDate(int i) throws SQLException {
        return rs().getDate(i);
    }

    @Override
    public Time getTime(int i) throws SQLException {
        return rs().getTime(i);
    }

    @Override
    public Timestamp getTimestamp(int i) throws SQLException {
        return rs().getTimestamp(i);
    }

    @Override
    public InputStream getAsciiStream(int i) throws SQLException {
        return rs().getAsciiStream(i);
    }

    @Override
    public InputStream getUnicodeStream(int i) throws SQLException {
        return rs().getUnicodeStream(i);
    }

    @Override
    public InputStream getBinaryStream(int i) throws SQLException {
        return rs().getBinaryStream(i);
    }

    @Override
    public String getString(String string) throws SQLException {
        return rs().getString(string);
    }

    @Override
    public boolean getBoolean(String string) throws SQLException {
        return rs().getBoolean(string);
    }

    @Override
    public byte getByte(String string) throws SQLException {
        return rs().getByte(string);
    }

    @Override
    public short getShort(String string) throws SQLException {
        return rs().getShort(string);
    }

    @Override
    public int getInt(String string) throws SQLException {
        return rs().getInt(string);
    }

    @Override
    public long getLong(String string) throws SQLException {
        return rs().getLong(string);
    }

    @Override
    public float getFloat(String string) throws SQLException {
        return rs().getFloat(string);
    }

    @Override
    public double getDouble(String string) throws SQLException {
        return rs().getDouble(string);
    }

    @Override
    public BigDecimal getBigDecimal(String string, int i) throws SQLException {
        return rs().getBigDecimal(string,i);
    }

    @Override
    public byte[] getBytes(String string) throws SQLException {
        return rs().getBytes(string);
    }

    @Override
    public Date getDate(String string) throws SQLException {
        return rs().getDate(string);
    }

    @Override
    public Time getTime(String string) throws SQLException {
        return rs().getTime(string);
    }

    @Override
    public Timestamp getTimestamp(String string) throws SQLException {
        return rs().getTimestamp(string);
    }

    @Override
    public InputStream getAsciiStream(String string) throws SQLException {
        return rs().getAsciiStream(string);
    }

    @Override
    public InputStream getUnicodeStream(String string) throws SQLException {
        return rs().getUnicodeStream(string);
    }

    @Override
    public InputStream getBinaryStream(String string) throws SQLException {
        return rs().getBinaryStream(string);
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return rs().getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        rs().clearWarnings();
    }

    @Override
    public String getCursorName() throws SQLException {
        return rs().getCursorName();
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return rs().getMetaData(); // TODO: possibly needs merging
    }

    @Override
    public Object getObject(int i) throws SQLException {
        return rs().getObject(i);
    }

    @Override
    public Object getObject(String string) throws SQLException {
        return rs().getObject(string);
    }

    @Override
    public int findColumn(String string) throws SQLException {
        return rs().findColumn(string);
    }

    @Override
    public Reader getCharacterStream(int i) throws SQLException {
        return rs().getCharacterStream(i); // TODO: possibly needs merging
    }

    @Override
    public Reader getCharacterStream(String string) throws SQLException {
        return rs().getCharacterStream(string); // TODO: possibly needs merging
    }

    @Override
    public BigDecimal getBigDecimal(int i) throws SQLException {
        return rs().getBigDecimal(i);
    }

    @Override
    public BigDecimal getBigDecimal(String string) throws SQLException {
        return rs().getBigDecimal(string);
    }
}
