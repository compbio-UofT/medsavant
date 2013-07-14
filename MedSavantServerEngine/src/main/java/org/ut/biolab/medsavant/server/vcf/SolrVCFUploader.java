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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.shared.solr.exception.InitializationException;
import org.ut.biolab.medsavant.shared.solr.service.VariantData;
import org.ut.biolab.medsavant.shared.solr.service.VariantService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @version $Id$
 */
public class SolrVCFUploader
{

    private static final Log LOG = LogFactory.getLog(SolrVCFUploader.class);

    /** Prefix for medatada rows. */
    private static final String METADATA_PREFIX = "##";

    /** Prefix for header rows. */
    private static final String HEADER_PREFIX = "#";

    /** Interface to Solr. */
    private VariantService vcfSolrService;

    /**
     * The list with the parsed columns.
     */
    private List<VCFColumn> columns = new ArrayList<VCFColumn>();
    /*
        Initialize service
     */
    {
        vcfSolrService = new VariantService();
        try {
            vcfSolrService.initialize();
        } catch (InitializationException e) {
            LOG.error("Error initializing Solr variant service");
        }
    }

    /**
     * This function currently ignores the metadata information, which is optional.
     *
     * Also, the ##fileformat line existence is NOT enforced.
     * @param input input vcf file
     */
    public int processAndIndex(File input) {

        BufferedReader in = null;

        int variantsIndexed = 0;

        long before = System.currentTimeMillis();

        try {

            in = new BufferedReader(new FileReader(input));
            String line;

            while ((line = in.readLine()) != null) {
                variantsIndexed+= processLine(line);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        vcfSolrService.commitChanges();

        long after = System.currentTimeMillis();

        long totalTime = after - before;

        System.out.println("Total time to index " + totalTime);

        return variantsIndexed;
    }

    /**
     * Parse a line and save field values.
     * @param line input line
     * @return The number of variants found on the line.
     */
    private int processLine(String line) {

        int variantsIndexed = 0;

        if (!shouldIgnoreLine(line)) {

            if (isHeaderLine(line)) {
                columns = extractColumns(line);
            } else {
                variantsIndexed = extractValues(line);
            }
        }

        return variantsIndexed;
    }

    /**
     * Extract the columns and stored them into a list.
     *
     * @param line  The current row in the document.
     * @return      The list of columns that match the know column names.
     */
    private List<VCFColumn> extractColumns(String line) {

        /* Disregard the leading # */
        Scanner scanner = new Scanner(line.substring(1));

        /* Parse columns and store them to an array */
        while (scanner.hasNext()) {
            String columnToken = scanner.next();
            VCFColumn column = getColumn(columnToken);

            if (column != null && !columns.contains(column)) {
                columns.add(column);
            }
        }

        return columns;
    }

    /**
     * Match the current column token to the know column names and return the suitable enum. If a match is not
     * found return null.
     * @param token     The current column token.
     * @return          The corresponding column enum, or null if a match is not possible.
     */
    private VCFColumn getColumn(String token) {

        VCFColumn matchedColumn = null;

        for (VCFColumn column : VCFColumn.values()) {
            if (column.name().equalsIgnoreCase(token)) {
                matchedColumn = column;
            }
        }

        if (matchedColumn == null) {
             /* Must be a patient dna id */
            matchedColumn = VCFColumn.PATIENT;
            matchedColumn.addPatientID(token);
        }

        return matchedColumn;
    }

    /**
     * Extract the values for the current vcf row. The columns parsed beforehand are used to determine the order.
     * @param line  The current line in the document.
     * @return      The number of variants parsed and indexed from the line.
     */
    private int extractValues(String line) {

        Scanner scanner = new Scanner(line);

        int index = 0;

        VariantData variantData = new VariantData();

        /*
            Gather all columns
         */
        while (scanner.hasNext() && index < columns.size() - 1) {

            String token = scanner.next();;

            VCFColumn column = columns.get(index);
            variantData = column.process(token, variantData);
            index++;
        }

        VCFColumn column = columns.get(index);

        column.startNewLine();

        int variantsParsed = 0;
        while (scanner.hasNext()) {

            String token = scanner.next();
            variantData = column.process(token, variantData);

            if (variantData != null) {
                variantData.addUUID();
                column.incrementVariantsParsed();

                vcfSolrService.scheduleToIndex(variantData);

                variantData.clearDnaSampleData();

                variantsParsed++;
            }
        }

        vcfSolrService.indexCurrentBuffer();

        return variantsParsed;
    }

    /**
     * Determine if the line should be ignored.
     *
     * Currently all the meta-information is ignored, but not the header line and the actual data.
     * @param line      The current line
     * @return          true if line should be ignored, false otherwise.
     */
    private boolean shouldIgnoreLine(String line) {
        return line.startsWith(METADATA_PREFIX);
    }


    /**
     * Determine if the line is the header line.
     *
     * The header line is used to extract the list of columns for which the values will be stored.
     * @param line      The current line
     * @return          true if line is the header line, false otherwise.
     */
    private boolean isHeaderLine(String line) {
        return line.startsWith(HEADER_PREFIX);
    }

}
