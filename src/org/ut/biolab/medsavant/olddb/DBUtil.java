/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.olddb;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.CreateTableQuery;
import com.healthmarketscience.sqlbuilder.DeleteQuery;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import org.ut.biolab.medsavant.vcf.VCFParser;
import org.ut.biolab.medsavant.vcf.VariantRecord;
import org.ut.biolab.medsavant.vcf.VariantSet;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.broad.tabix.TabixReader;
import org.ut.biolab.medsavant.olddb.table.CohortTableSchema;
import org.ut.biolab.medsavant.olddb.table.CohortViewTableSchema;
import org.ut.biolab.medsavant.olddb.table.GeneListMembershipTableSchema;
import org.ut.biolab.medsavant.olddb.table.GeneListTableSchema;
import org.ut.biolab.medsavant.olddb.table.ModifiableColumn;
import org.ut.biolab.medsavant.olddb.table.PatientTableSchema;
import org.ut.biolab.medsavant.olddb.table.TableSchema;
import org.ut.biolab.medsavant.olddb.table.TableSchema.ColumnType;
import org.ut.biolab.medsavant.olddb.table.VariantAnnotationGatkTableSchema;
import org.ut.biolab.medsavant.olddb.table.VariantAnnotationPolyphenTableSchema;
import org.ut.biolab.medsavant.olddb.table.VariantAnnotationSiftTableSchema;
import org.ut.biolab.medsavant.olddb.table.VariantTableSchema;
import org.ut.biolab.medsavant.db.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.view.dialog.ComboForm;
import org.ut.biolab.medsavant.view.dialog.ConfirmDialog;

/**
 *
 * @author mfiume, AndrewBrook
 */
public class DBUtil {

    public static List<Vector> parseResultSet(Object[][] columnsTypesIndices, ResultSet r1) throws SQLException {

        int numColumns = columnsTypesIndices.length;

        List<Vector> results = new ArrayList<Vector>();

        while (r1.next()) {

            Vector v = new Vector();

            for (int i = 0; i < numColumns; i++) {

                Integer index = (Integer) columnsTypesIndices[i][0];
                ColumnType type = (ColumnType) columnsTypesIndices[i][2];

                switch (type) {
                    case VARCHAR:
                        v.add(r1.getString(index));
                        break;
                    case BOOLEAN:
                        v.add(r1.getBoolean(index));
                        break;
                    case INTEGER:
                        v.add(r1.getInt(index));
                        break;
                    case FLOAT:
                        v.add(r1.getFloat(index));
                        break;
                    case DECIMAL:
                        v.add(r1.getDouble(index));
                        break;
                    case DATE:
                        v.add(r1.getDate(index));
                        break;
                    default:
                        throw new FatalDatabaseException("Unrecognized column type: " + type);
                }
            }

            results.add(v);
        }

        return results;

    }
    
    public static void addVcfToDb(String filename, int genome_id, int pipeline_id) throws SQLException {
        
        
        long totalStart = System.nanoTime();
        
        //establish connection
        Connection conn;
        try {
            conn = ConnectionController.connect();
        } catch (Exception ex) {
            Logger.getLogger(DBUtil.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        
        TableSchema table = MedSavantDatabase.getInstance().getVariantTableSchema();              
        int base_variant_id = QueryUtil.getMaxValueForColumn(conn, table, table.getDBColumn(VariantTableSchema.ALIAS_VARIANTID)) + 1;        
        
        conn.setAutoCommit(false);    
        VariantSet vs = new VariantSet();
        try {
            System.out.println("Parsing variants...");
            vs = VCFParser.parseVariants(new File(filename), base_variant_id, genome_id, pipeline_id);
            System.out.println("Done parsing variants...");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        conn.commit();
        conn.setAutoCommit(true);   
             
        
        List<VariantRecord> records = vs.getRecords();
        if(records.isEmpty()) return; 
        System.out.println("Num records: " + records.size());
        
        //join with sift
        long start = System.nanoTime();
        addAnnotation(AnnotationName.SIFT, "annotations" + System.getProperty("file.separator") + "sift_final_tabix", records);
        long end = System.nanoTime();
        System.out.println("Sift Total time: " + (end-start));
        System.out.println("Sift Time per record: " + ((end-start)/records.size()));
        
        //join with polyphen
        start = System.nanoTime();
        addAnnotation(AnnotationName.POLYPHEN, "annotations" + System.getProperty("file.separator") + "polyphen_final_tabix", records);
        end = System.nanoTime();
        System.out.println("Polyphen Total time: " + (end-start));
        System.out.println("Polyphen Time per record: " + ((end-start)/records.size()));
        
        //join with gatk
        start = System.nanoTime();
        addAnnotation(AnnotationName.GATK, "annotations" + System.getProperty("file.separator") + "gatk_final_tabix", records);
        end = System.nanoTime();
        System.out.println("Gatk Total time: " + (end-start));
        System.out.println("Gatk Time per record: " + ((end-start)/records.size()));
        

        //Write to outfile
        start = System.nanoTime();
        String fileName = "annotations" + System.getProperty("file.separator") + "join1";
        try {        
            BufferedWriter out = new BufferedWriter(new FileWriter(new File(fileName), false));
            for(VariantRecord r : records){
                out.write(r.toTabsForUpload() + "\n");
            }
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(DBUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        end = System.nanoTime();
        System.out.println("Write time: " + (end-start));
        System.out.println("Time per record: " + ((end-start)/records.size()));
        
        
        //upload outfile to db
        System.out.println("starting load");
        start = System.nanoTime();
        String loadString = 
                "LOAD DATA LOCAL INFILE '" + fileName.replaceAll("\\\\", "/") + "' " +
                "INTO TABLE " + "variant_combined_ib" + " " + 
                "FIELDS TERMINATED BY '\\t' " +
                "LINES TERMINATED BY '\\n'";
        Statement s = conn.createStatement();
        s.execute(loadString);
        end = System.nanoTime();
        System.out.println("Import time: " + (end-start));
        System.out.println("Time per record: " + ((end-start)/records.size()));
        
        
        long totalEnd = System.nanoTime();
        System.out.println("TOTAL TIME FOR " + records.size() + " RECORDS: " + (totalEnd-totalStart));
       
        
    }
    
    private enum AnnotationName {GATK, SIFT, POLYPHEN};
    
    private static void addAnnotation(AnnotationName annotationName, String annotationFilename, List<VariantRecord> records){
        try {
            
            TabixReader reader = new TabixReader(annotationFilename);
            
            org.broad.tabix.TabixReader.Iterator it = null;
            int pos = 0;
            List<VariantRecord> currentRecords = getCurrentRecords(records, pos);
            String chrom = currentRecords.get(0).getChrom();
            String annotation = null;
            String annChr = null;
            boolean endOfIt = true;
            
            int i = 1;
            while(true){
                
                if(i%10000000 == 0){
                    System.out.println(i);
                }
                i++;
                
                //if we've reached last variantRecord in chrom, get new iterator
                if(!currentRecords.get(0).getChrom().equals(chrom) || endOfIt){
                    chrom = currentRecords.get(0).getChrom();
                    int tid = reader.chr2tid(chrom);
                    it = reader.query(tid, 0, Integer.MAX_VALUE);
                    endOfIt = false;
                }
                
                //get the current annotation
                annotation = it.next();  
                if(annotation == null){
                    endOfIt = true;
                    while(VariantRecord.compareChrom(chrom, annChr) <= 0){
                        pos += currentRecords.size();
                        if(pos >= records.size()) break;
                        currentRecords = getCurrentRecords(records, pos);
                        chrom = currentRecords.get(0).getChrom();
                    }
                    if(pos >= records.size()) break;
                    continue;
                }
                
                //parse information from annotation
                int i1 = annotation.indexOf("\t");
                int i2 = annotation.indexOf("\t", i1+1);
                int i3 = annotation.indexOf("\t", i2+1);              
                annChr = annotation.substring(i1+1, i2);
                long position = Long.parseLong(annotation.substring(i2+1, i3));
                
                //perform comparison
                int compare = currentRecords.get(0).compareTo(annChr, position);
                
                //iterate through VariantRecords until position >= annotation
                while(compare < 0){
                    pos += currentRecords.size();
                    if(pos >= records.size()) break;
                    currentRecords = getCurrentRecords(records, pos);
                    compare = currentRecords.get(0).compareTo(annChr, position);  
                }
                if(pos >= records.size()) break;
                
                //match found, update record
                if(compare == 0){
                    
                    switch(annotationName){
                        case GATK:
                            annotateGatk(currentRecords, annotation);
                            break;
                        case SIFT:
                            annotateSift(currentRecords, annotation);
                            break;
                        case POLYPHEN:
                            annotatePolyphen(currentRecords, annotation);
                            break;
                        default:
                            break;
                    }
                    
                }             
            }
            
        } catch (IOException ex) {
            Logger.getLogger(DBUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void annotateSift(List<VariantRecord> currentRecords, String annotation){
        //get annotation fields
        VariantRecord a = getSiftAnnotationRecord(annotation);

        for(VariantRecord rec : currentRecords){
            rec.setNameSift(a.getNameSift());
            rec.setName2Sift(a.getName2Sift());
            rec.setDamageProbability(a.getDamageProbability());
        }
    }
    
    private static VariantRecord getSiftAnnotationRecord(String line){
        String[] values = line.split("\t");
        VariantRecord a = new VariantRecord();
        
        VariantAnnotationSiftTableSchema s = MedSavantDatabase.getInstance().getVariantSiftTableSchema();
        
        Double doubleValue;
        
        a.setNameSift(values[s.INDEX_NAME_SIFT-1]);
        a.setName2Sift(values[s.INDEX_NAME2_SIFT-1]);
        if((doubleValue = stringToDouble(values[s.INDEX_DAMAGEPROBABILITY-1])) != null) a.setDamageProbability(doubleValue);
        
        return a;
    }
    
    private static void annotatePolyphen(List<VariantRecord> currentRecords, String annotation){
        //get annotation fields
        VariantRecord a = getPolyphenAnnotationRecord(annotation);

        for(VariantRecord rec : currentRecords){
            rec.setCdnacoord(a.getCdnacoord());
            rec.setOpos(a.getOpos());
            rec.setOaa1(a.getOaa1());
            rec.setOaa2(a.getOaa2());
            rec.setSnpid(a.getSnpid());
            rec.setAcc(a.getAcc());
            rec.setPos(a.getPos());
            rec.setPrediction(a.getPrediction());
            rec.setPph2class(a.getPph2class());
            rec.setPph2prob(a.getPph2prob());
            rec.setPph2fpr(a.getPph2fpr());
            rec.setPph2tpr(a.getPph2tpr());
            rec.setPph2fdr(a.getPph2fdr());
            rec.setTransv(a.getTransv());
            rec.setCodpos(a.getCodpos());
            rec.setCpg(a.getCpg());
            rec.setMindjnc(a.getMindjnc());
            rec.setIdpmax(a.getIdpmax());
            rec.setIdpsnp(a.getIdpsnp());
            rec.setIdqmin(a.getIdqmin());
        }
    }
    
    private static VariantRecord getPolyphenAnnotationRecord(String line){
        String[] values = line.split("\t");
        VariantRecord a = new VariantRecord();
        
        VariantAnnotationPolyphenTableSchema s = MedSavantDatabase.getInstance().getVariantPolyphenTableSchema();
        
        Integer intValue;
        Float floatValue;
        
        a.setCdnacoord(values[s.INDEX_CDNACOORD-1]);
        if((intValue = stringToInt(values[s.INDEX_OPOS-1])) != null) a.setOpos(intValue);
        a.setOaa1(values[s.INDEX_OAA1-1]);
        a.setOaa2(values[s.INDEX_OAA2-1]);
        a.setSnpid(values[s.INDEX_SNPID-1]);
        a.setAcc(values[s.INDEX_ACC-1]);
        if((intValue = stringToInt(values[s.INDEX_POS-1])) != null) a.setPos(intValue);
        a.setPrediction(values[s.INDEX_PREDICTION-1]);
        a.setPph2class(values[s.INDEX_PPH2CLASS-1]);
        if((floatValue = stringToFloat(values[s.INDEX_PPH2PROB-1])) != null) a.setPph2prob(floatValue);
        if((floatValue = stringToFloat(values[s.INDEX_PPH2FPR-1])) != null) a.setPph2fpr(floatValue);
        if((floatValue = stringToFloat(values[s.INDEX_PPH2TPR-1])) != null) a.setPph2tpr(floatValue);
        if((floatValue = stringToFloat(values[s.INDEX_PPH2FDR-1])) != null) a.setPph2fdr(floatValue);
        if((intValue = stringToInt(values[s.INDEX_TRANSV-1])) != null) a.setTransv(intValue);
        if((intValue = stringToInt(values[s.INDEX_CODPOS-1])) != null) a.setCodpos(intValue);
        if((intValue = stringToInt(values[s.INDEX_CPG-1])) != null) a.setCpg(intValue);
        if((floatValue = stringToFloat(values[s.INDEX_MINDJNC-1])) != null) a.setMindjnc(floatValue);
        if((floatValue = stringToFloat(values[s.INDEX_IDPMAX-1])) != null) a.setIdpmax(floatValue);
        if((floatValue = stringToFloat(values[s.INDEX_IDPSNP-1])) != null) a.setIdpsnp(floatValue);
        if((floatValue = stringToFloat(values[s.INDEX_IDQMIN-1])) != null) a.setIdqmin(floatValue);
 
        return a;
    }
    
    private static void annotateGatk(List<VariantRecord> currentRecords, String annotation){
        //get annotation fields
        VariantRecord a = getGatkAnnotationRecord(annotation);

        for(VariantRecord rec : currentRecords){
            rec.setName_gatk(a.getName_gatk());
            rec.setName2_gatk(a.getName2_gatk());
            rec.setTranscriptStrand(a.getTranscriptStrand());
            rec.setPositionType(a.getPositionType());
            rec.setFrame(a.getFrame());
            rec.setMrnaCoord(a.getMrnaCoord());
            rec.setCodonCoord(a.getCodonCoord());
            rec.setSpliceDist(a.getSpliceDist());
            rec.setReferenceCodon(a.getReferenceCodon());
            rec.setReferenceAA(a.getReferenceAA());
            rec.setVariantCodon(a.getVariantCodon());
            rec.setVariantAA(a.getVariantAA());
            rec.setChangesAA(a.getChangesAA());
            rec.setFunctionalClass(a.getFunctionalClass());
            rec.setCodingCoordStr(a.getCodingCoordStr());
            rec.setProteinCoordStr(a.getProteinCoordStr());
            rec.setInCodingRegion(a.getInCodingRegion());
            rec.setSpliceInfo(a.getSpliceInfo());
            rec.setUorfChange(a.getUorfChange());
            rec.setSpliceInfoCopy(a.getSpliceInfoCopy());
        }
    }
    
    private static VariantRecord getGatkAnnotationRecord(String line){
        String[] values = line.split("\t");
        VariantRecord a = new VariantRecord();
        
        VariantAnnotationGatkTableSchema s = MedSavantDatabase.getInstance().getVariantGatkTableSchema();
        
        Integer intValue;
        
        a.setName_gatk(values[s.INDEX_NAME_GATK-1]);
        a.setName2_gatk(values[s.INDEX_NAME2_GATK-1]);
        a.setTranscriptStrand(values[s.INDEX_TRANSCRIPTSTRAND-1]);
        a.setPositionType(values[s.INDEX_POSITIONTYPE-1]);      
        if((intValue = stringToInt(values[s.INDEX_FRAME-1])) != null) a.setFrame(intValue);
        if((intValue = stringToInt(values[s.INDEX_MRNACOORD-1])) != null) a.setMrnaCoord(intValue);
        if((intValue = stringToInt(values[s.INDEX_CODONCOORD-1])) != null) a.setCodonCoord(intValue);
        if((intValue = stringToInt(values[s.INDEX_SPLICEDIST-1])) != null) a.setSpliceDist(intValue);
        a.setReferenceCodon(values[s.INDEX_REFERENCECODON-1]);
        a.setReferenceAA(values[s.INDEX_REFERENCEAA-1]);
        a.setVariantCodon(values[s.INDEX_VARIANTCODON-1]);
        a.setVariantAA(values[s.INDEX_VARIANTAA-1]);
        a.setChangesAA(values[s.INDEX_CHANGESAA-1]);
        a.setFunctionalClass(values[s.INDEX_FUNCTIONALCLASS-1]);
        a.setCodingCoordStr(values[s.INDEX_CODINGCOORDSTR-1]);
        a.setProteinCoordStr(values[s.INDEX_PROTEINCOORDSTR-1]);
        a.setInCodingRegion(values[s.INDEX_INCODINGREGION-1]);
        a.setSpliceInfo(values[s.INDEX_SPLICEINFO-1]);
        a.setUorfChange(values[s.INDEX_UORFCHANGE-1]);
        a.setSpliceInfoCopy(values[s.INDEX_SPLICEINFOCOPY-1]);
        
        return a;
    }
    
    private static Integer stringToInt(String s){
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private static Float stringToFloat(String s){
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private static Double stringToDouble(String s){
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    
    private static List<VariantRecord> getCurrentRecords(List<VariantRecord> records, int startPosition){
        List<VariantRecord> result = new ArrayList<VariantRecord>();
        result.add(records.get(startPosition));
        String chrom = result.get(0).getChrom();
        long position = result.get(0).getPosition();
               
        while(true){
            startPosition++;
            if(startPosition >= records.size()) break;
            VariantRecord next = records.get(startPosition);
            if(next.getChrom().equals(chrom) && next.getPosition() == position){
                result.add(next);
            } else {
                break;
            }
        }
        
        return result;
    }
    
    /*public static PreparedStatement getVcfStatement(Connection conn) throws SQLException{
        
        TableSchema table = MedSavantDatabase.getInstance().getVariantTableSchema();        
        InsertQuery is = new InsertQuery(MedSavantDatabase.getInstance().getVariantTableSchema().getTable());

        List<DbColumn> columns = table.getColumns();
        for(DbColumn col : columns){
            is.addPreparedColumns(col);   
        }

        String insertString = is.toString().replaceAll(VariantTableSchema.TABLE_NAME, "variant_staging"); //TODO: safe?
        PreparedStatement ps = conn.prepareStatement(insertString);
        return ps;
    }*/
    
   /* public static void clearStagingTable(Connection conn) throws SQLException {
        
        TableSchema table = MedSavantDatabase.getInstance().getVariantTableSchema();        
        DeleteQuery d = new DeleteQuery(table.getTable());
        d.addCondition(BinaryCondition.equalTo(1, 1));
        
        Statement s = conn.createStatement();
        s.executeUpdate(d.toString().replaceAll(VariantTableSchema.TABLE_NAME, "variant_staging")); 
        
        //create new variant_staging_ib table
        Statement s1 = conn.createStatement();
        s1.execute("drop table variant_staging_ib");
        
        Statement s2 = conn.createStatement();
        s2.execute(
            "CREATE TABLE  `medsavantdb`.`variant_staging_ib` (" +
              "`variant_id` int(11) NOT NULL," +
              "`genome_id` int(11) NOT NULL," +
              "`pipeline_id` varchar(10) COLLATE latin1_bin NOT NULL," +
              "`dna_id` varchar(10) COLLATE latin1_bin NOT NULL," +
              "`chrom` varchar(5) COLLATE latin1_bin NOT NULL DEFAULT ''," +
              "`position` int(11) NOT NULL," +
              "`dbsnp_id` varchar(45) COLLATE latin1_bin DEFAULT NULL," +
              "`ref` varchar(30) COLLATE latin1_bin DEFAULT NULL," +
              "`alt` varchar(30) COLLATE latin1_bin DEFAULT NULL," +
              "`qual` float DEFAULT NULL," +
              "`filter` varchar(500) COLLATE latin1_bin DEFAULT NULL," +
              "`aa` varchar(500) COLLATE latin1_bin DEFAULT NULL," +
              "`ac` varchar(500) COLLATE latin1_bin DEFAULT NULL," +
              "`af` varchar(500) COLLATE latin1_bin DEFAULT NULL," +
              "`an` int(11) DEFAULT NULL," +
              "`bq` float DEFAULT NULL," +
              "`cigar` varchar(500) COLLATE latin1_bin DEFAULT NULL," +
              "`db` tinyint(1) DEFAULT NULL," +
              "`dp` int(11) DEFAULT NULL," +
              "`end` varchar(500) COLLATE latin1_bin DEFAULT NULL," +
              "`h2` tinyint(1) DEFAULT NULL," +
              "`mq` varchar(500) COLLATE latin1_bin DEFAULT NULL," +
              "`mq0` varchar(500) COLLATE latin1_bin DEFAULT NULL," +
              "`ns` int(11) DEFAULT NULL," +
              "`sb` varchar(500) COLLATE latin1_bin DEFAULT NULL," +
              "`somatic` tinyint(1) DEFAULT NULL," +
              "`validated` tinyint(1) DEFAULT NULL," +
              "`custom_info` varchar(500) COLLATE latin1_bin DEFAULT NULL" +
            ") ENGINE=BRIGHTHOUSE DEFAULT CHARSET=latin1 COLLATE=latin1_bin;");
    }*/
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    /*
     * Given path to vcf file, add to database.
     * Return true iff success.
     */
   /* public static void addVcfToDb(String filename, int genome_id, int pipeline_id) throws SQLException {

        //establish connection
        Connection conn;
        try {
            conn = ConnectionController.connect();
        } catch (Exception ex) {
            Logger.getLogger(DBUtil.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        
        //create current variant table
       // String tableName = createVariantTable(conn);
        
        TableSchema table = MedSavantDatabase.getInstance().getVariantTableSchema(); 
        int variant_id = QueryUtil.getMaxValueForColumn(conn, table, table.getDBColumn(VariantTableSchema.ALIAS_VARIANTID)) + 1; 
        
        //get variants from file
        //VariantSet variants = new VariantSet();
        //String fileName = "C:/Users/Andrew/Documents/medsavant/variant_tdf"; //TODO: how do we do this when db is not local???
        //File tempFile = new File(fileName);
        try {
            System.out.println("Parsing variants...");
            VCFParser.parseVariants(new File(filename));
            System.out.println("Done parsing variants...");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        //add to db
        //addVariantsToDb(conn, fileName);
        //addVariantsToDb(variants, genome_id, pipeline_id, conn);
        //FilterController.fireFiltersChangedEvent();
    }*/
    
   /* private static void addVariantsToDb(Connection conn, String fileName){
        
        String query = 
                "LOAD DATA INFILE '" + fileName + "' " + 
                "INTO TABLE " + VariantTableSchema.TABLE_NAME + " " + 
                "FIELDS TERMINATED BY '\\t'";
        
        try {
            Statement s = conn.createStatement();
            s.execute(query);
        } catch (SQLException ex) {
            Logger.getLogger(DBUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }*/
    
   /* private static String createVariantTable(Connection conn){
        
        String baseName = VariantTableSchema.TABLE_NAME;
        String tableName = baseName + "_";

        String query = 
                "CREATE TABLE " + tableName + " AS " +
                "(SELECT * FROM " + baseName + " WHERE 1=2)";
        
        try {
            Statement s = conn.createStatement();
            //System.out.println(query);
            s.execute(query);
        } catch (SQLException ex) {
            Logger.getLogger(DBUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        TableSchema vcf = MedSavantDatabase.getInstance().getVCFUploadTableSchema();
        InsertQuery iq = new InsertQuery(vcf.getTable());
        iq.addColumn(vcf.getDBColumn(VCFUploadTableSchema.ALIAS_TABLENAME), tableName);
        
        try {
            Statement s = conn.createStatement();
            //System.out.println(iq.toString());
            s.execute(iq.toString());
        } catch (SQLException ex) {
            Logger.getLogger(DBUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        return tableName;
    }*/

    /*private static void addVariantsToDb(VariantSet variants, int genome_id, int pipeline_id, Connection conn) throws SQLException {

        
        
        TableSchema table = MedSavantDatabase.getInstance().getVariantTableSchema();        
        InsertQuery is = new InsertQuery(MedSavantDatabase.getInstance().getVariantTableSchema().getTable());

        List<DbColumn> columns = table.getColumns();
        for(DbColumn col : columns){
            is.addPreparedColumns(col);   
        }

        String insertString = is.toString().replace(VariantTableSchema.TABLE_NAME, "variant_staging");
        PreparedStatement ps = conn.prepareStatement(insertString);
        
        conn.setAutoCommit(false);
        
        int numRecords = 0;
        
        System.out.println("Preparing " + variants.getRecords().size() + " records ...");
        
        
        int variant_id = QueryUtil.getMaxValueForColumn(conn, table, table.getDBColumn(VariantTableSchema.ALIAS_VARIANTID)) + 1; 
        
        //add records
        for (VariantRecord record : variants.getRecords()) {

            numRecords++;
            if (numRecords % 10000 == 0) {
                System.out.println("Prepared " + numRecords + " records");
            }
            
            for(int i = 0; i < columns.size(); i++){
                DbColumn col = columns.get(i);
                switch(VariantTableSchema.FIELD_NAMES.valueOf(col.getColumnNameSQL().toUpperCase())){
                    case VARIANT_ID:
                        ps.setInt(i+1, variant_id);
                        break;
                    case GENOME_ID:
                        ps.setInt(i+1, genome_id);
                        break;
                    case PIPELINE_ID:
                        ps.setInt(i+1, pipeline_id);
                        break;
                    case DNA_ID:
                        ps.setString(i+1, record.getDnaID());
                        break;
                    case CHROM:
                        ps.setString(i+1, record.getChrom());
                        break;
                    case POSITION:
                        ps.setLong(i+1, record.getPos());
                        break;
                    case DBSNP_ID:
                        ps.setString(i+1, record.getDbSNPID());
                        break;
                    case REF:
                        ps.setString(i+1, record.getRef());
                        break;
                    case ALT:
                        ps.setString(i+1, record.getAlt());
                        break;
                    case QUAL:
                        ps.setFloat(i+1, record.getQual());
                        break;
                    case FILTER:
                        ps.setString(i+1, record.getFilter());
                        break;
                    case AA:
                        ps.setString(i+1, record.getAA());
                        break;
                    case AC:
                        ps.setString(i+1, record.getAC());
                        break;
                    case AF:
                        ps.setString(i+1, record.getAF());
                        break;
                    case AN:
                        ps.setInt(i+1, record.getAN());
                        break;
                    case BQ:
                        ps.setFloat(i+1, record.getBQ());
                        break;
                    case CIGAR:
                        ps.setString(i+1, record.getCigar());
                        break;
                    case DB:
                        ps.setBoolean(i+1, record.getDB());
                        break;
                    case DP:
                        ps.setInt(i+1, record.getDP());
                        break;
                    case END:
                        ps.setLong(i+1, record.getEnd());
                        break;
                    case H2:
                        ps.setBoolean(i+1, record.getH2());
                        break;
                    case MQ:
                        ps.setFloat(i+1, record.getMQ());
                        break;
                    case MQ0:
                        ps.setInt(i+1, record.getMQ0());
                        break;
                    case NS:
                        ps.setInt(i+1, record.getNS());
                        break;
                    case SB:
                        ps.setFloat(i+1, record.getSB());
                        break;
                    case SOMATIC:
                        ps.setBoolean(i+1, record.getSomatic());
                        break;
                    case VALIDATED:
                        ps.setBoolean(i+1, record.getValidated());
                        break;
                    case CUSTOM_INFO:
                        ps.setString(i+1, record.getCustomInfo());
                        break;
                    case VARIANT_ANNOTATION_SIFT_ID:
                        ps.setInt(i+1, 0);
                        break;
                    default:
                        break;
                }              
            }
            variant_id++;
            ps.executeUpdate();
            
        }
        
        conn.commit();
        conn.setAutoCommit(true);
        
    }*/

    public static void addIndividualsToCohort(String[] patient_ids) {

        HashMap<String, Integer> cohortMap = new HashMap<String, Integer>();

        Connection conn;
        try {
            conn = ConnectionController.connect();
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM cohort");
            while (rs.next()) {
                cohortMap.put(rs.getString(2), rs.getInt(1));
            }

        } catch (Exception ex) {
            Logger.getLogger(DBUtil.class.getName()).log(Level.SEVERE, null, ex);
            return; //TODO
        }

        Object[] options = cohortMap.keySet().toArray();
        ComboForm form = new ComboForm(options, "Select Cohort", "Select which cohort to add to:");
        String selected = (String) form.getSelectedValue();
        if (selected == null) {
            return;
        }
        int cohort_id = cohortMap.get(selected);

        try {
            String sql = "INSERT INTO cohort_membership ("
                    + "cohort_id,"
                    + "hospital_id) "
                    + "VALUES (?,?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            conn.setAutoCommit(false);

            for (String patient_id : patient_ids) {
                pstmt.setInt(1, cohort_id);
                pstmt.setString(2, patient_id);

                pstmt.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);

        } catch (SQLException ex) {
            Logger.getLogger(DBUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void removeIndividualsFromCohort(String cohort_name, String[] patient_ids) {
        try {
            Connection conn = ConnectionController.connect();

            String sql1 = "SELECT cohort_id FROM cohort WHERE name=\"" + cohort_name + "\"";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql1);
            int cohort_id = -1;
            if (rs.next()) {
                cohort_id = rs.getInt(1);
            } else {
                return;
            }

            String sql2 = "DELETE FROM cohort_membership "
                    + "WHERE cohort_id=? AND hospital_id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql2);
            conn.setAutoCommit(false);

            for (String patient_id : patient_ids) {
                pstmt.setInt(1, cohort_id);
                pstmt.setString(2, patient_id);

                pstmt.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);

        } catch (Exception ex) {
            Logger.getLogger(DBUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void deleteIndividuals(String[] patient_ids) {

        String message = "Do you really want to delete these individuals?";
        if (patient_ids.length == 1) {
            message = "Do you really want to delete " + patient_ids[0] + "?";
        }

        ConfirmDialog cd = new ConfirmDialog("Confirm delete", message);
        boolean confirmed = cd.isConfirmed();
        cd.dispose();
        if (!confirmed) {
            return;
        }


        try {
            Connection conn = ConnectionController.connect();
            
            String sql1 = "DELETE FROM " + PatientTableSchema.TABLE_NAME
                    + " WHERE " + PatientTableSchema.DBFIELDNAME_PATIENTID + "=?";
            PreparedStatement pstmt1 = conn.prepareStatement(sql1);

            String sql2 = "DELETE FROM " + CohortViewTableSchema.TABLE_NAME
                    + " WHERE " + CohortViewTableSchema.DBFIELDNAME_HOSPITALID + "=?";
            PreparedStatement pstmt2 = conn.prepareStatement(sql2);

            conn.setAutoCommit(false);

            for (String patient_id : patient_ids) {
                pstmt1.setString(1, patient_id);
                pstmt1.executeUpdate();

                pstmt2.setString(1, patient_id);
                pstmt2.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);

        } catch (Exception ex) {
            Logger.getLogger(DBUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void addPatient(List<ModifiableColumn> cols, List<String> values){
        //TODO: make sure row doesn't already exist.
        TableSchema t = MedSavantDatabase.getInstance().getPatientTableSchema();
        InsertQuery is = new InsertQuery(t.getTable());
        
        for(int i = 0; i < cols.size(); i++){
            ModifiableColumn c = cols.get(i);
            String s = values.get(i);          
            if(s == null || s.equals("")){
                continue;
            }
            switch(c.getType()){
                case BOOLEAN:
                    is.addColumn(t.getDBColumn(c.getShortName()), Boolean.getBoolean(s));
                    break;
                case DATE:
                    is.addColumn(t.getDBColumn(c.getShortName()), Date.valueOf(s));
                    break;
                case DECIMAL:
                    is.addColumn(t.getDBColumn(c.getShortName()), Double.parseDouble(s));
                    break;
                case FLOAT:
                    is.addColumn(t.getDBColumn(c.getShortName()), Float.parseFloat(s));
                    break;
                case INTEGER:
                    is.addColumn(t.getDBColumn(c.getShortName()), Integer.parseInt(s));
                    break;
                case VARCHAR:
                    is.addColumn(t.getDBColumn(c.getShortName()), s);
                    break;                
            }
        }
        
        try {
            Statement s = ConnectionController.connect().createStatement();
            s.executeUpdate(is.toString());
        } catch (NonFatalDatabaseException ex) {
            Logger.getLogger(DBUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(DBUtil.class.getName()).log(Level.SEVERE, null, ex);
        }   
    }
    
    public static void addCohort(String cohort_name) {
        //TODO: make sure name doesn't already exist.
        TableSchema t = MedSavantDatabase.getInstance().getCohortTableSchema();
        InsertQuery is = new InsertQuery(t.getTable());
        is.addColumn(t.getDBColumn(CohortTableSchema.ALIAS_COHORTNAME), cohort_name);        
        try {
            Statement s = ConnectionController.connect().createStatement();
            s.executeUpdate(is.toString());
        } catch (NonFatalDatabaseException ex) {
            Logger.getLogger(DBUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(DBUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void deleteCohorts(String[] cohort_names) {

        String message = "Do you really want to delete these cohorts?";
        if (cohort_names.length == 1) {
            message = "Do you really want to delete " + cohort_names[0] + "?";
        }

        ConfirmDialog cd = new ConfirmDialog("Confirm delete", message);
        boolean confirmed = cd.isConfirmed();
        cd.dispose();
        if (!confirmed) {
            return;
        }


        try {
            Connection conn = ConnectionController.connect();

            String sql1 = "DELETE FROM cohort "
                    + "WHERE name=?";
            PreparedStatement pstmt1 = conn.prepareStatement(sql1);

            conn.setAutoCommit(false);

            for (String cohort_name : cohort_names) {
                pstmt1.setString(1, cohort_name);
                pstmt1.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);

        } catch (Exception ex) {
            Logger.getLogger(DBUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void addGeneListToDatabase(String geneListName, Iterator<String[]> i) throws NonFatalDatabaseException, SQLException {

        Connection conn = ConnectionController.connect();

        // create gene list
        TableSchema geneListTable = MedSavantDatabase.getInstance().getGeneListTableSchema();
        InsertQuery q0 = new InsertQuery(geneListTable.getTable());
        
        q0.addColumn(geneListTable.getDBColumn(GeneListTableSchema.ALIAS_NAME), geneListName);

        Statement s0 = conn.createStatement();

        System.out.println("Inserting: " + q0.toString());

        s0.executeUpdate(q0.toString());

        System.out.println("Done executing statement");

        SelectQuery q1 = new SelectQuery();

        q1.addFromTable(geneListTable.getTable());
        q1.addAllColumns();
        q1.addCondition(new BinaryCondition(BinaryCondition.Op.EQUAL_TO,
                geneListTable.getDBColumn(GeneListTableSchema.ALIAS_NAME),
                geneListName));

        Statement s1 = conn.createStatement();

        //System.out.println("Querying for: " + q1.toString());

        ResultSet r1 = s1.executeQuery(q1.toString());
        r1.next();

        int genelistid = r1.getInt(GeneListTableSchema.DBFIELDNAME_ID);

        //System.out.println("Gene list id = " + genelistid);

        conn.setAutoCommit(false);

        InsertQuery q2;
        Statement s2;

        TableSchema glmembership = MedSavantDatabase.getInstance().getGeneListMembershipTableSchema();

        while (i.hasNext()) {

                    System.out.println("Sending region member");

            String[] line = i.next();

            q2 = new InsertQuery(glmembership.getTable());
            q2.addColumn(glmembership.getDBColumn(GeneListMembershipTableSchema.ALIAS_REGIONSETID), genelistid);
            //TODO: dont hard code! Get from the user!!
            q2.addColumn(glmembership.getDBColumn(GeneListMembershipTableSchema.ALIAS_GENOMEID), 1);
            q2.addColumn(glmembership.getDBColumn(GeneListMembershipTableSchema.ALIAS_CHROM), line[0]);
            q2.addColumn(glmembership.getDBColumn(GeneListMembershipTableSchema.ALIAS_START), line[1]);
            q2.addColumn(glmembership.getDBColumn(GeneListMembershipTableSchema.ALIAS_END), line[2]);
            q2.addColumn(glmembership.getDBColumn(GeneListMembershipTableSchema.ALIAS_DESCRIPTION), line[3]);

            s2 = conn.createStatement();

            //System.out.println("Inserting: " + q2.toString());
            s2.executeUpdate(q2.toString());
        }

        conn.commit();
        conn.setAutoCommit(true);

        /**
         * TODO: all this!
         */
        //SelectQuery q = new SelectQuery();
            /*
        q.addFromTable(t.getTable());
        q.addCustomColumns(FunctionCall.min().addColumnParams(col));
        q.addCustomColumns(FunctionCall.max().addColumnParams(col));
        
        Statement s = conn.createStatement();
        ResultSet rs = s.executeQuery(q.toString());
        
        
        
        
        
        
        
        
        String sql = "INSERT INTO cohort_membership ("
        + "cohort_id,"
        + "hospital_id) "
        + "VALUES (?,?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        conn.setAutoCommit(false);
        
        for(String patient_id : patient_ids){       
        pstmt.setInt(1, cohort_id);
        pstmt.setString(2, patient_id);
        
        pstmt.executeUpdate();
        }
        
        conn.commit();
        conn.setAutoCommit(true);
        
        } catch (SQLException ex) {
        Logger.getLogger(DBUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // put genes into gene list
         * 
         */
    }
    
    public static void executeUpdate(String sql) throws SQLException, NonFatalDatabaseException {
        Connection conn = ConnectionController.connect();
        Statement s = conn.createStatement();
        s.executeUpdate(sql);
    }
}
