/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.db;

import fiume.vcf.VCFParser;
import fiume.vcf.VariantRecord;
import fiume.vcf.VariantSet;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.db.table.TableSchema.ColumnType;
import org.ut.biolab.medsavant.exception.FatalDatabaseException;

/**
 *
 * @author mfiume
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
                    default:
                        throw new FatalDatabaseException("Unrecognized column type: " + type);
                }
            }
            
            results.add(v);
        }

        return results;

    }

    /*
     * Given path to vcf file, add to database.
     * Return true iff success.
     */
    public static void addVcfToDb(String filename, String genome_id, String pipeline_id) throws SQLException {

        //get variants from file
        VariantSet variants = new VariantSet();
        try {
            System.out.println("Parsing variants...");
            variants = VCFParser.parseVariants(new File(filename));
            System.out.println("Done parsing variants...");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        //add to db
        addVariantsToDb(variants, genome_id, pipeline_id);
        FilterController.fireFiltersChangedEvent();
    }

    private static void addVariantsToDb(VariantSet variants, String genome_id, String pipeline_id) throws SQLException {

        Connection conn;
        try {
            conn = ConnectionController.connect();
        } catch (Exception ex) {
            Logger.getLogger(DBUtil.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        // Prepare a statement to insert a record
        String sql = "INSERT INTO variant ("
                + "dna_id,"
                + "chrom,"
                + "position,"
                + "id,"
                + "ref,"
                + "alt,"
                + "qual,"
                + "filter,"
                //+ "info) "
                + "aa,"
                + "ac,"
                + "af,"
                + "an,"
                + "bq,"
                + "cigar,"
                + "db,"
                + "dp,"
                + "end,"
                + "h2,"
                + "mq,"
                + "mq0,"
                + "ns,"
                + "sb,"
                + "somatic,"
                + "validated,"
                + "custom_info,"
                + "genome_id,"
                + "pipeline_id) "
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);

        conn.setAutoCommit(false);
        
        int numrecords = 0;
        
        System.out.println("Preparing " + variants.getRecords().size() + " records ...");
        
        //add records
        for(VariantRecord record : variants.getRecords()){
            
            numrecords++;
            if (numrecords % 1000 == 0) {
                System.out.println("Prepared " + numrecords + " records");
            }
            
            if (numrecords == 10000) { break; }

            pstmt.setString(1, record.getDnaID());
            pstmt.setString(2, record.getChrom());
            pstmt.setLong(3, record.getPos());
            pstmt.setString(4, record.getId());
            pstmt.setString(5, record.getRef());
            pstmt.setString(6, record.getAlt());
            pstmt.setFloat(7, record.getQual());
            pstmt.setString(8, record.getFilter());
            //pstmt.setString(9, record.getInfo());
            pstmt.setString(9, record.getAA());
            pstmt.setString(10, record.getAC());
            pstmt.setString(11, record.getAF());
            pstmt.setInt(12, record.getAN());
            pstmt.setFloat(13, record.getBQ());
            pstmt.setString(14, record.getCigar());
            pstmt.setBoolean(15, record.getDB());
            pstmt.setInt(16, record.getDP());
            pstmt.setLong(17, record.getEnd());
            pstmt.setBoolean(18, record.getH2());
            pstmt.setFloat(19, record.getMQ());
            pstmt.setInt(20, record.getMQ0());
            pstmt.setInt(21, record.getNS());
            pstmt.setFloat(22, record.getSB());
            pstmt.setBoolean(23, record.getSomatic());
            pstmt.setBoolean(24, record.getValidated());
            pstmt.setString(25, record.getCustomInfo());
            pstmt.setString(26, genome_id);
            pstmt.setString(27, pipeline_id);

            pstmt.executeUpdate();
        }
        
        conn.commit();
        conn.setAutoCommit(true);

    }

}
