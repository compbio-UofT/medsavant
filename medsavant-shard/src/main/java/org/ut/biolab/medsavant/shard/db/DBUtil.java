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
package org.ut.biolab.medsavant.shard.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Helpers for direct database access.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class DBUtil {

    private static final String SHOW_DB_QUERY = "SHOW DATABASES";
    public static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

    public static List<String> getDatabases(String url, String user, String password) {
        List<String> res = new ArrayList<String>();
        PreparedStatement s = null;
        ResultSet r = null;

        try {
            Class.forName(JDBC_DRIVER);
            Connection conn = DriverManager.getConnection(url, user, password);

            s = conn.prepareStatement(SHOW_DB_QUERY);
            r = s.executeQuery();

            while (r.next()) {
                res.add(r.getString(1));
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Driver not loaded: " + JDBC_DRIVER);
        } catch (SQLException e) {
            System.err.println("Error connecting to: " + url);
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close the statement.");
                }
            }
            if (r != null) {
                try {
                    r.close();
                } catch (SQLException e) {
                    System.err.println("Resultset could not be closed.");
                }
            }
        }
        return res;
    }
}
