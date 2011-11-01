/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.importfile;

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

            public void remove() {
                try {
                    reader.readNext();
                } catch (IOException ex) {
                }
            }
        };

        return i;
    }

    static Object[] getPreview(
            String path,
            char separator,
            int numHeaderLines,
            int numLines,
            FileFormat ff) throws FileNotFoundException {

        int[] fields;
        if (ff != null) {
            fields = ff.getRequiredFieldIndexes();
        } else {
            fields = null;
        }

        CSVReader reader = getFileReader(path, separator);

        List<String[]> headerLines = getLinesFromReader(reader, numHeaderLines);
        List<String[]> previewLines = null;
        try {
            previewLines = getLinesFromReader(reader, numLines, fields);
        } catch (Exception e){
            e.printStackTrace();
        }
        
        
        Object[] lines = new Object[2];
        lines[0] = headerLines;
        lines[1] = previewLines;

        return lines;
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
