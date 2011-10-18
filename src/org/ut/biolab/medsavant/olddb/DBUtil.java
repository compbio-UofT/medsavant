/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.olddb;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.olddb.table.CohortTableSchema;
import org.ut.biolab.medsavant.olddb.table.GeneListMembershipTableSchema;
import org.ut.biolab.medsavant.olddb.table.GeneListTableSchema;
import org.ut.biolab.medsavant.olddb.table.ModifiableColumn;
import org.ut.biolab.medsavant.db.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.model.structure.MedSavantDatabase;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.model.structure.TableSchema.ColumnType;
import org.ut.biolab.medsavant.view.dialog.ComboForm;
import org.ut.biolab.medsavant.view.dialog.ConfirmDialog;

/**
 *
 * @author mfiume, AndrewBrook
 */
public class DBUtil {

    public static List<Vector> parseResultSet(Object[][] columnsTypesIndices, ResultSet r1) throws SQLException {

        int numColumns = columnsTypesIndices.length;

        List<Vector> results = new ArrayList<Vector>();

        while (r1.next()) {

            Vector v = new Vector();

            for (int i = 0; i < numColumns; i++) {

                Integer index = (Integer) columnsTypesIndices[i][0];
                ColumnType type = (ColumnType) columnsTypesIndices[i][2];

                switch (type) {
                    case VARCHAR:
                        v.add(r1.getString(index));
                        break;
                    case BOOLEAN:
                        v.add(r1.getBoolean(index));
                        break;
                    case INTEGER:
                        v.add(r1.getInt(index));
                        break;
                    case FLOAT:
                        v.add(r1.getFloat(index));
                        break;
                    case DECIMAL:
                        v.add(r1.getDouble(index));
                        break;
                    case DATE:
                        v.add(r1.getDate(index));
                        break;
                    default:
                        throw new FatalDatabaseException("Unrecognized column type: " + type);
                }
            }

            results.add(v);
        }

        return results;

    }
    
    public static void executeUpdate(String sql) throws SQLException, NonFatalDatabaseException {
        Connection conn = ConnectionController.connect();
        Statement s = conn.createStatement();
        s.executeUpdate(sql);
    }
}
