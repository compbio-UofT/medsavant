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
package org.ut.biolab.medsavant.shared.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.medsavant.api.common.storage.MedSavantFile;

/**
 *
 * @author jim
 */
public class MedSavantFileUtils {

    final public static int BUF_SIZE = 1024 * 64;

    public static void copy(InputStream in, OutputStream out)
            throws IOException {
        try {
            byte[] b = new byte[BUF_SIZE];
            int len;
            while ((len = in.read(b)) >= 0) {
                out.write(b, 0, len);
            }
        } finally {
            in.close();
            out.close();
        }
    }

    public static void copy(File src, MedSavantFile dst) throws IOException{
        try {
            OutputStream os = dst.getOutputStreamForAppend();
            InputStream is = new FileInputStream(src); //throws fnfe
            MedSavantFileUtils.copy(is, os);
        } catch (FileNotFoundException fnfe) {
            throw new IOException("Error opening file ", fnfe);
        }
    }

}
