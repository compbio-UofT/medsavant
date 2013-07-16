package org.ut.biolab.medsavant.shared.query;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds a result row
 */
public class ResultRow {

    private Map<String, Object> results;

    public ResultRow() {
        this.results = new HashMap<String, Object>();
    }

    public ResultRow(Map<String, Object> results) {
        this.results = results;
    }

    public Object getObject(String key) {
        return results.get(key);
    }

    public Object getObject(int index) {
        return results.values().toArray()[index];
    }

    public Object put(String key, Object value) {
        return results.put(key, value);
    }

    @Override
    public String toString() {
        return "ResultRow{" +
                "results=" + results +
                '}';
    }
}
