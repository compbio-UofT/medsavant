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
package org.ut.biolab.medsavant.shard.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.ut.biolab.medsavant.shard.variant.ShardedSessionManager;

/**
 * Utils for file manipulation.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class FileUtils {

    /**
     * Merges two files into one.
     * 
     * @param baseFilename
     *            starting file to keep
     * @param appendingFilename
     *            appending file
     */
    public static void mergeFiles(String baseFilename, String appendingFilename) {
        BufferedWriter writer = null;
        BufferedReader reader = null;
        File baseFile = new File(baseFilename);
        File appendingFile = new File(appendingFilename);
        try {
            if (!baseFile.exists()) {
                baseFile.createNewFile();
            }

            if (appendingFile.exists()) {
                writer = new BufferedWriter(new FileWriter(baseFile, true));
                reader = new BufferedReader(new FileReader(appendingFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(line);
                    writer.write("\r\n");
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                // closing failed, ignore
            }
            try {
                if (writer != null)
                    writer.close();
            } catch (IOException e) {
                // closing failed, ignore
            }
        }
    }

    /**
     * Deletes the given file.
     * 
     * @param fileName
     *            file to delete
     */
    public static void deleteFile(String fileName) {
        File file = new File(fileName);
        if (!(file.exists() && file.delete())) {
            System.err.println("File " + fileName + " could not be deleted.");
        }
    }

    /**
     * Generates a shard-specific file to store data in.
     * 
     * @param shardId
     *            ID of the shard to use
     * @return file name
     */
    public static String getFileForShard(Integer shardId) {
        return ShardedSessionManager.getTable() + "-" + shardId;
    }

    /**
     * Renames a file.
     * 
     * @param oldName
     *            original name
     * @param newName
     *            target name
     * @param overwrite
     *            true if the target file should be overwritten, false if
     *            exception should be thrown in case of collision
     * @return true if the file was successfully renamed, false otherwise
     * @throws IOException
     *             in case of target file name collision
     */
    public static boolean renameFile(String oldName, String newName, boolean overwrite) {
        File file = new File(oldName);
        File file2 = new File(newName);

        if (file2.exists()) {
            if (overwrite) {
                deleteFile(newName);
            } else {
                return false;
            }
        }

        return file.renameTo(file2);
    }

    /**
     * Generates a shard-specific file name.
     * 
     * @param file
     *            file to use
     * @return parametrized file name
     */
    public static String getParametrizedFilePath(File file) {
        return file.getAbsolutePath().replaceAll("\\\\", "/") + "%d";
    }
}
