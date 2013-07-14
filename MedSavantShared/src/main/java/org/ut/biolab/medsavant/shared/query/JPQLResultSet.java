package org.ut.biolab.medsavant.shared.query;

import java.util.ArrayList;
import java.util.Map;

/**
 * Holds a set of result rows.
 */
public class JPQLResultSet {

    private Map<String, String> results;

    private String getString(String key) {
        return results.get(key);
    }

    private int getInt(String key) {
        String intValue = results.get(key);

        return Integer.parseInt(intValue);
    }

    private long getLong(String key) {
        String longValue = results.get(key);

        return Long.parseLong(longValue);
    }

    private Double getDouble(String key) {
        String doubleValue = results.get(key);

        return Double.parseDouble(doubleValue);
    }


}
