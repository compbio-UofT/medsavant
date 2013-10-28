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
