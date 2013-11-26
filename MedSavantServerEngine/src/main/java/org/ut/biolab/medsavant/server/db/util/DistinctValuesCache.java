/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
import org.ut.biolab.medsavant.server.IOJob;
import org.ut.biolab.medsavant.server.MedSavantIOController;

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

    public static void cacheResults(String dbName, String tableName, String columnName, final List result) {
        File dir = getDirectory(dbName, tableName);
        if (!dir.exists() && !dir.mkdirs()) {
            LOG.error(String.format("Unable to create cache directory %s.", dir.getAbsolutePath()));
            return; //couldn't create directory
        }

        File f = getFile(dir, columnName);
        LOG.info("Marked " + f.getAbsolutePath() + " for deletion on exit.");
        f.deleteOnExit();
        BufferedWriter out = null;
        try {
             out = new BufferedWriter(new FileWriter(f, false));
            if (result == null) {
                out.write(CACHE_NULL);
           } else {
                final BufferedWriter fout = out;
                MedSavantIOController.requestIO(new IOJob("Cache Results"){
                    
                    @Override
                    protected void doIO() throws IOException {
                        Object o = result.iterator().next();
                        fout.write(o.toString());
                        fout.newLine();
                    }

                    @Override
                    protected boolean continueIO() throws IOException {
                        return result.iterator().hasNext();
                        
                    }
                    
                });                                
            }
            out.close();
        } catch (Exception ex) {
            LOG.error("Error caching results for " + dbName + "." + tableName + "." + columnName, ex);
            f.delete();
        } finally {
            if(out != null){
                try{
                    out.close();
                }catch(IOException ex){
                    LOG.error("Couldn't close file "+f, ex);
                    f.delete();
                }
            }
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
