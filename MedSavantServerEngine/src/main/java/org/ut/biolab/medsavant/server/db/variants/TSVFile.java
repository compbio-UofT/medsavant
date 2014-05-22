/*
 * Copyright (C) 2014 University of Toronto, Computational Biology Lab.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package org.ut.biolab.medsavant.server.db.variants;

import java.io.File;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.shared.util.IOUtils;

/**
 * Tracks the file and number of lines for an annotated TSV File.  
 *
 */
public class TSVFile {
    private final Log LOG = LogFactory.getLog(TSVFile.class);
    private File file;
    private final int numLines;    
    
    public TSVFile(File f, int numLines) {
        this.file = f;
        this.numLines = numLines;
    }

    /**
     * @return the number of lines in this annotated tsv file, each corresponding
     * to a row in the variant table of the database.
     *      
     */
    public int getNumLines() {
        return numLines;
    }

    public File getFile() {
        return file;
    }

    /**
     * Moves this annotated file to a new location.  This method modifies the object.
     * 
     * @param dst The new destination for this annotated file.
     * @return A reference to this object.
     */
    public TSVFile moveTo(File dst) {
        File f = this.getFile();
        LOG.info("Renaming " + f.getAbsolutePath() + " to " + dst.getAbsolutePath());
        if (!IOUtils.moveFile(f, dst)){
            LOG.error("Couldn't rename " + f.getAbsolutePath() + " to " + dst.getAbsolutePath());
        }
        this.file = dst;
        return this;
    }
    
}
