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

package org.ut.biolab.medsavant.server.db.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.shared.model.Range;
import org.ut.biolab.medsavant.shared.util.DirectorySettings;

/**
 *
 * @author Andrew
 */
public class DistinctValuesCache {

    private static final Log LOG = LogFactory.getLog(DistinctValuesCache.class);
    private static final String CACHE_NULL = "##NULL";
    public static final int CACHE_LIMIT = 10000;

    private static File getDirectory(String dbName, String tableName) {
        return new File(new File(DirectorySettings.getCacheDirectory(), dbName), tableName);
    }

    private static File getFile(File dir, String columnName) {
        return new File(dir, columnName);
    }

    public static boolean isCached(String dbName, String tableName, String columnName) {
        return getFile(getDirectory(dbName, tableName), columnName).exists();
    }

    public static void cacheResults(String dbName, String tableName, String columnName, List result) {
        File dir = getDirectory(dbName, tableName);
        if (!dir.exists() && !dir.mkdirs()) {
            LOG.error(String.format("Unable to create cache directory %s.", dir.getAbsolutePath()));
            return; //couldn't create directory
        }

        File f = getFile(dir, columnName);
        LOG.info("Marked " + f.getAbsolutePath() + " for deletion on exit.");
        f.deleteOnExit();
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(f, false));
            if (result == null) {
                out.write(CACHE_NULL);
            } else {
                for (Object o : result) {
                    out.write(o.toString());
                    out.newLine();
                }
            }
            out.close();
        } catch (Exception ex) {
            LOG.error("Error caching results for " + dbName + "." + tableName + "." + columnName, ex);
            f.delete();
        }
    }

    private static List<String> getResults(String dbName, String tableName, String columnName) throws IOException {
        List<String> result = new ArrayList<String>();

        File dir = getDirectory(dbName, tableName);
        File f = getFile(dir, columnName);
        LOG.info("Marked " + f.getAbsolutePath() + " for deletion on exit.");
        f.deleteOnExit();
        BufferedReader in = new BufferedReader(new FileReader(f));
        String line;
        while ((line = in.readLine()) != null)   {
            if (result.isEmpty() && line.startsWith(CACHE_NULL)) {
                in.close();
                return null;
            }
            result.add(line);
        }
        in.close();

        LOG.info(result.size() + " values retrieved from " + f.getAbsolutePath());
        return result;
    }

    public static List<String> getCachedStringList(String dbName, String tableName, String columnName) throws IOException {
        return getResults(dbName, tableName, columnName);
    }

    public static Range getCachedRange(String dbName, String tableName, String columnName) throws IOException{
        List<String> results = getResults(dbName, tableName, columnName);
        if (results == null || results.size() != 2) {
            return null;
        }
        return new Range(Double.parseDouble(results.get(0)), Double.parseDouble(results.get(1)));
    }
}
