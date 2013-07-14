package org.ut.biolab.medsavant.shared.query;

import java.util.Map;

/**
 * Holds a result row
 */
public class ResultRow {

    private Map<String, Object> results;

    private Object getObject(String key) {
        return results.get(key);
    }

    private Object getObject(int index) {
        return results.values().toArray()[index];
    }

    private Object put(String key, Object value) {
        return results.put(key, value);
    }

}
