/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.variants.update;

import au.com.bytecode.opencsv.CSVReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;
import org.ut.biolab.medsavant.server.log.ServerLogger;
import org.ut.biolab.medsavant.vcf.VCFHeader;
import org.ut.biolab.medsavant.vcf.VCFParser;

/**
 *
 * @author Andrew
 */
public class VCFIterator {
 
    private static final int LINES_PER_IMPORT = 1000000000; // 1 billion output lines
    private File[] files;
    private File baseDir;
    private int updateId;
    private boolean includeHomoRef;
    private CSVReader r;
    private int fileIndex;
    private VCFHeader header;
    private File outfile;
    
    public VCFIterator(File[] files, File baseDir, int updateId, boolean includeHomoRef) throws FileNotFoundException, IOException{
        this.files = files;
        this.baseDir = baseDir;
        this.updateId = updateId;
        this.includeHomoRef = includeHomoRef;
        this.fileIndex = 0;
        createReader();
    }
    
    private void createReader() throws FileNotFoundException, IOException {
        ServerLogger.log(VCFIterator.class, "Parsing file " + files[fileIndex].getName());
        Reader reader;
        if (files[fileIndex].getAbsolutePath().endsWith(".gz") || files[fileIndex].getAbsolutePath().endsWith(".zip")) {
            FileInputStream fin = new FileInputStream(files[fileIndex].getAbsolutePath());
            reader = new InputStreamReader(new GZIPInputStream(fin));
        } else {
            reader = new FileReader(files[fileIndex]);
        }
        r = new CSVReader(reader, VCFParser.defaultDelimiter);
        header = VCFParser.parseVCFHeader(r);
    }
    
    public File next() throws IOException{
        
        ServerLogger.log(VCFIterator.class, "Next " + LINES_PER_IMPORT);
        
        outfile = new File(baseDir, "tmp_" + System.nanoTime() + ".tdf");
        int numWritten = 0;
        
        while (numWritten < LINES_PER_IMPORT){
            int num = VCFParser.parseVariantsFromReader(r, header, LINES_PER_IMPORT - numWritten, outfile, updateId, fileIndex, includeHomoRef);
            numWritten += num;
            if(num == 0){
                fileIndex++;
                if(fileIndex >= files.length){
                    break;
                } else {
                    createReader();
                }
            }
        }
        
        return numWritten > 0 ? outfile : null;
    }
    
}
