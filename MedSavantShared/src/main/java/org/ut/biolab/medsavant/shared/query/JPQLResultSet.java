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
