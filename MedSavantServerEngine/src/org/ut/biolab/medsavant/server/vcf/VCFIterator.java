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
package org.ut.biolab.medsavant.server.vcf;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Andrew
 */
public class VCFIterator {

    private static final Log LOG = LogFactory.getLog(VCFIterator.class);
    private File[] files;
    private File baseDir;
    private int updateId;
    private boolean includeHomoRef;
    private BufferedReader reader;
    private int fileIndex;
    private File outfile;
    //private int variantIdOffset;

    public VCFIterator(File[] files, File baseDir, int updateId, boolean includeHomoRef) throws IOException {
        this.files = files;
        this.baseDir = baseDir;
        this.updateId = updateId;
        this.includeHomoRef = includeHomoRef;
        this.fileIndex = 0;
        createReader();
    }

    private void createReader() throws IOException {
        LOG.info(String.format("Parsing file %s", files[fileIndex].getName()));
        reader = VCFParser.openFile(files[fileIndex]);
    }

    public File next() throws IOException {

        outfile = new File(baseDir, "tmp_" + System.nanoTime() + ".tdf");
        int numWritten = 0;

        int num = VCFParser.parseVariantsFromReader(reader, outfile, updateId, fileIndex, includeHomoRef);
        numWritten += num;

        return numWritten > 0 ? outfile : null;
    }
}
