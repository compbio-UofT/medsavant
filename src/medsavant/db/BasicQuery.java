/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package medsavant.db;

import com.healthmarketscience.sqlbuilder.FunctionCall;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.Table;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 *
 * @author mfiume
 */
public class BasicQuery {


    

    private static int getRowsInTable(Connection c, Table t) throws SQLException {

        FunctionCall count = FunctionCall.countAll();
        SelectQuery q = new SelectQuery();
        q.addFromTable(t);
        q.addCustomColumns(count);

        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery(q.toString());
        rs.next();

        int numrows = rs.getInt(1);
        s.close();
        
        return numrows;
    }
}
