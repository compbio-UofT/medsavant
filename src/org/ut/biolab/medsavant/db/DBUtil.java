/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.Column;
import fiume.vcf.VCFParser;
import fiume.vcf.VariantRecord;
import fiume.vcf.VariantSet;
import java.awt.Dialog.ModalityType;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.db.table.GeneListMembershipTableSchema;
import org.ut.biolab.medsavant.db.table.GeneListTableSchema;
import org.ut.biolab.medsavant.db.table.TableSchema;
import org.ut.biolab.medsavant.db.table.TableSchema.ColumnType;
import org.ut.biolab.medsavant.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.exception.NonFatalDatabaseException;
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
        for (VariantRecord record : variants.getRecords()) {

            numrecords++;
            if (numrecords % 10000 == 0) {
                System.out.println("Prepared " + numrecords + " records");
            }

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

            String sql1 = "SELECT id FROM cohort WHERE name=\"" + cohort_name + "\"";
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

            String sql1 = "DELETE FROM subject "
                    + "WHERE hospital_id=?";
            PreparedStatement pstmt1 = conn.prepareStatement(sql1);

            //TODO: THIS SHOULD BE DONE USING FOREIGN KEYS IN TABLE
            String sql2 = "DELETE FROM cohort_membership "
                    + "WHERE hospital_id=?";
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

        System.out.println("Querying for: " + q1.toString());

        ResultSet r1 = s1.executeQuery(q1.toString());
        r1.next();

        int genelistid = r1.getInt(GeneListTableSchema.DBFIELDNAME_ID);

        System.out.println("Gene list id = " + genelistid);

        conn.setAutoCommit(false);

        InsertQuery q2;
        Statement s2;

        TableSchema glmembership = MedSavantDatabase.getInstance().getGeneListMembershipTableSchema();

        while (i.hasNext()) {

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
}
