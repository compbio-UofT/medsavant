/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package medsavant.db;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import medsavant.db.table.TableSchema;
import medsavant.db.table.TableSchema.ColumnType;
import medsavant.exception.FatalDatabaseException;

/**
 *
 * @author mfiume
 */
public class DBUtil {

    public static List<Vector> parseResultSet(List<DbColumn> columns, ResultSet r1) throws FatalDatabaseException, SQLException {

        int numColumns = columns.size();

        List<Vector> results = new ArrayList<Vector>();
        List<ColumnType> columnTypeEnums = TableSchema.mapColumnsToEnums(columns);

        while (r1.next()) {

            Vector v = new Vector();

            for (int i = 1; i <= numColumns; i++) {
                int j = i-1;
                switch (columnTypeEnums.get(j)) {
                    case VARCHAR:
                        v.add(r1.getString(i));
                        break;
                    case BOOLEAN:
                        v.add(r1.getBoolean(i));
                        break;
                    case INTEGER:
                        v.add(r1.getInt(i));
                        break;
                    case FLOAT:
                        v.add(r1.getFloat(i));
                        break;
                    default:
                        throw new FatalDatabaseException("Unrecognized column type: " + columnTypeEnums.get(j));
                }
            }
            
            results.add(v);
        }

        return results;

    }

}
