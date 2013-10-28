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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.shared.importing;

import au.com.bytecode.opencsv.CSVReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author mfiume
 */
public class ImportDelimitedFile {

    public static CSVReader getFileReader(String path, char separator) throws FileNotFoundException {
        CSVReader reader = new CSVReader(new FileReader(path), separator);
        return reader;
    }

    public static Iterator<String[]> getFileIterator(String path, char separator, int headerLines, final FileFormat ff) throws FileNotFoundException, IOException {
        final CSVReader reader = getFileReader(path, separator);

        for (int i = 0; i < headerLines; i++) {
            reader.readNext();
        }

        final int[] fields = ff.getRequiredFieldIndexes();

        Iterator<String[]> i = new Iterator<String[]>() {

            private String[] nextLine;

            @Override
            public boolean hasNext() {
                if (nextLine == null) {
                    try {
                        if (ff == null) {
                            nextLine = reader.readNext();
                        } else {
                            nextLine = getOnlyRequiredFields(reader.readNext(), fields);
                        }
                        return true;
                    } catch (Exception ex) {
                        try {
                            reader.close();
                        } catch (IOException ex1) {
                        }
                        return false;
                    }
                }
                boolean hasNext = nextLine == null;

                if (!hasNext) {
                    try {
                        reader.close();
                    } catch (IOException ex) {
                    }
                }

                return hasNext;
            }

            @Override
            public String[] next() {
                if (nextLine != null) {
                    String[] result = nextLine;
                    nextLine = null;
                    return result;
                } else {
                    try {
                        if (ff == null) {
                            return reader.readNext();
                        } else {
                            return getOnlyRequiredFields(reader.readNext(), fields);
                        }
                    } catch (IOException ex) {
                        return null;
                    }
                }
            }

            @Override
            public void remove() {
                try {
                    reader.readNext();
                } catch (IOException ex) {
                }
            }
        };

        return i;
    }

    @SuppressWarnings("unchecked")
    public static List<String[]>[] getPreview(String path, char separator, int numHeaderLines, int numLines, FileFormat ff) throws FileNotFoundException {

        int[] fields;
        if (ff != null) {
            fields = ff.getRequiredFieldIndexes();
        } else {
            fields = null;
        }

        CSVReader reader = getFileReader(path, separator);

        List<String[]> headerLines = getLinesFromReader(reader, numHeaderLines);
        List<String[]> previewLines = null;

        previewLines = getLinesFromReader(reader, numLines, fields);

        return (List<String[]>[])new List[] { headerLines, previewLines };
    }

    private static List<String[]> getLinesFromReader(CSVReader reader, int numLines) {
        return getLinesFromReader(reader, numLines, null);
    }

    private static List<String[]> getLinesFromReader(CSVReader reader, int numLines, int[] fields) {

        List<String[]> lines = new ArrayList<String[]>();
        for (int i = 0; i < numLines; i++) {
            try {
                String[] line = reader.readNext();

                if(line == null) break;

                if (fields == null) {
                    lines.add(line);
                } else {
                    lines.add(getOnlyRequiredFields(line, fields));
                }
            } catch (IOException ex) {
                System.err.println("Warning: hit end of file while getting lines from file");
                break;
            }
        }

        return lines;
    }

    private static String[] getOnlyRequiredFields(String[] line, int[] fields) {
        String[] result = new String[fields.length];

        for (int i = 0; i < fields.length; i++) {
            result[i] = line[fields[i]];
        }
        return result;
    }
}
