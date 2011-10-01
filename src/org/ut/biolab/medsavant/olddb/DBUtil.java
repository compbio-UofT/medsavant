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

        s0.executeUpdate(q0.toString());

        SelectQuery q1 = new SelectQuery();

        q1.addFromTable(geneListTable.getTable());
        q1.addAllColumns();
        q1.addCondition(new BinaryCondition(BinaryCondition.Op.EQUAL_TO,
                geneListTable.getDBColumn(GeneListTableSchema.ALIAS_NAME),
                geneListName));

        Statement s1 = conn.createStatement();

        ResultSet r1 = s1.executeQuery(q1.toString());
        r1.next();

        int genelistid = r1.getInt(GeneListTableSchema.DBFIELDNAME_ID);

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
