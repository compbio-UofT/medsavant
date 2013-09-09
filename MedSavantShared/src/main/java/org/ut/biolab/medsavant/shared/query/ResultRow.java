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
package org.ut.biolab.medsavant.shared.query;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    public int getInt(String key) throws QueryException {
        return getObjectOfClass(key, Integer.class);
    }

    public long getLong(String key) throws QueryException {
        return getObjectOfClass(key, Long.class);
    }

    public boolean getBoolean(String key) throws QueryException {
        return getObjectOfClass(key, Boolean.class);
    }

    public String getString(String key) throws QueryException {
        return getObjectOfClass(key, String.class);
    }

    public float getFloat(String key) throws QueryException {
        return getObjectOfClass(key, Float.class);
    }

    public double getDouble(String key) throws QueryException {
        return getObjectOfClass(key, Double.class);
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

    public Collection<Object> valueSet() {
        return results.values();
    }

    public Set<String> keySet() {
        return results.keySet();
    }

    @Override
    public String toString() {
        return "ResultRow{" +
                "results=" + results +
                '}';
    }

    private <T> T getObjectOfClass(String key, Class<T> clazz) throws QueryException {
        try {
            Object value = results.get(key);
            return value != null ? clazz.cast(value) : clazz.newInstance();
        } catch (InstantiationException e) {
            throw new QueryException("Could not retrieve value of type " + clazz.getName());
        } catch (IllegalAccessException e) {
            throw new QueryException("Could not retrieve value of type " + clazz.getName());
        }
    }
}
