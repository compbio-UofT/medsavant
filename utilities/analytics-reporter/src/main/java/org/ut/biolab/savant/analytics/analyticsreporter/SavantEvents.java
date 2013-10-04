package org.ut.biolab.savant.analytics.analyticsreporter;

import java.util.List;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;

/**
 *
 * @author mfiume
 */
class SavantEvents {

    public static String KEY_MESSAGE = "msg";
    public static String KEY_SESSIONID = "session-id";
    public static String KEY_SERVERTIME = "server-time";
    public static String VALUE_NEWSESSION = "NewSession";
    public static String VALUE_ONSESSIONSTART = "SessionStart";
    public static String VALUE_ONSESSIONEND = "SessionEnd";
    private static List<SavantEvent> events;

    static List<SavantEvent> getEvents() throws ClassNotFoundException, SQLException {
        return getEventsFromDatabase(null, null);
    }

    static List<SavantEvent> getEventsFromDatabase(String key, String value) throws ClassNotFoundException, SQLException {
        if (events == null) {
            events = downloadEvents();
            System.out.println(events.size() + " events downloaded");
        }

        return getEventsFromSet(key, value, events);
    }

    static SavantEvent getFirstEventFromSet(String key, String value, List<SavantEvent> fromList) {
        List<SavantEvent> results = getEventsFromSet(key, value, fromList);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    static List<SavantEvent> getEventsFromSet(String key, String value, List<SavantEvent> fromList) {
        if (key == null) {
            return fromList;
        }
        List<SavantEvent> subset = new ArrayList<SavantEvent>();
        for (SavantEvent e : fromList) {
            if (e.hasValueForKey(key, value)) {
                subset.add(e);
            }
        }
        System.out.println(subset.size() + " events have key " + key + "=>" + value);
        return subset;
    }

    static List<SavantEvent> filterEventsByKey(String key, List<SavantEvent> events) {
        List<SavantEvent> results = new ArrayList<SavantEvent>();

        for (SavantEvent e : events) {
            if (e.hasKey(key)) {
                results.add(e);
            }
        }

        return results;
    }

    private static List<SavantEvent> downloadEvents() throws ClassNotFoundException, SQLException {

        Connection conn = connect();

        Statement stmt = null;
        ResultSet rs = null;

        List<SavantEvent> events = new ArrayList<SavantEvent>();

        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM log");

            // or alternatively, if you don't know ahead of time that
            // the query will be a SELECT...

            while (rs.next()) {
                events.add(new SavantEvent(rs.getInt("id"), rs.getString("msg")));
            }

            // Now do something with the ResultSet ....
        } catch (SQLException ex) {
            throw ex;
        } finally {
            // it is a good idea to release
            // resources in a finally{} block
            // in reverse-order of their creation
            // if they are no-longer needed

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) {
                } // ignore

                rs = null;
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) {
                } // ignore

                stmt = null;
            }
        }

        return events;
    }

    private static Connection connect() throws ClassNotFoundException, SQLException {
        // This will load the MySQL driver, each DB has its own driver
        Class.forName("com.mysql.jdbc.Driver");
        // Setup the connection with the DB
        Connection connect = DriverManager
                .getConnection("jdbc:mysql://50.63.244.198/savantusage?"
                + "user=savantusageguest&password=Savant12!");

        return connect;
    }
}
