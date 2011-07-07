/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.table;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;

/**
 *
 * @author AndrewBrook
 */
public class PatientTableSchema extends TableSchema{
    
    public static final String TABLE_NAME = "dsmerge1";
    
    public static int INDEX_INDEXID;
    public static int INDEX_FAMNUM;
    public static int INDEX_FIRSTNAM;
    public static int INDEX_LASTNAME;
    public static int INDEX_GENDER;
    public static int INDEX_DOB;
    public static int INDEX_DOD;
    public static int INDEX_IDBIOMOM;
    public static int INDEX_IDBIODAD;
    public static int INDEX_BASNOTE;
    public static int INDEX_BASCHK;
    public static int INDEX_DATERECD;
    public static int INDEX_DNA1;
    public static int INDEX_SOURCE1;
    public static int INDEX_AVAILABLE;
    public static int INDEX_AFFY1;
    public static int INDEX_AFFY1_DATE;
    public static int INDEX_RACEORIG;
    public static int INDEX_ETHGROUP;
    public static int INDEX_IQLR;
    public static int INDEX_IQWDATE;
    public static int INDEX_IQWAGEMO;
    public static int INDEX_IQWTYPE;
    public static int INDEX_WISC4VCI;
    public static int INDEX_WISC4PRI;
    public static int INDEX_WISC4WMI;
    public static int INDEX_WISC4PSI;
    public static int INDEX_IQWVERB;
    public static int INDEX_IQWPERF;
    public static int INDEX_IQWFULL;
    
    public static final String ALIAS_INDEXID = "Index ID";
    public static final String ALIAS_FAMNUM = "Family Number";
    public static final String ALIAS_FIRSTNAM = "First Name";
    public static final String ALIAS_LASTNAME = "Last Name";
    public static final String ALIAS_GENDER = "Gender";
    public static final String ALIAS_DOB = "Date of Birth";
    public static final String ALIAS_DOD = "Date of Death";
    public static final String ALIAS_IDBIOMOM = "Bio Mom ID";
    public static final String ALIAS_IDBIODAD = "Bio Dad ID";
    public static final String ALIAS_BASNOTE = "Basic Information Card Notes";
    public static final String ALIAS_BASCHK = "Basic Information Card Checked";
    public static final String ALIAS_DATERECD = "Date Received";
    public static final String ALIAS_DNA1 = "DNA ID";
    public static final String ALIAS_SOURCE1 = "DNA Source";
    public static final String ALIAS_AVAILABLE = "DNA Available";
    public static final String ALIAS_AFFY1 = "Affymetrix Array";
    public static final String ALIAS_AFFY1_DATE = "Date Submitted";
    public static final String ALIAS_RACEORIG = "Racial Origin";
    public static final String ALIAS_ETHGROUP = "Ethnic Group";
    public static final String ALIAS_IQLR = "Leiter-R Full IQ";
    public static final String ALIAS_IQWDATE = "Date of Wechsler Administration";
    public static final String ALIAS_IQWAGEMO = "Age at Date of Wechsler (months)";
    public static final String ALIAS_IQWTYPE = "Type of Wechsler Administered";
    public static final String ALIAS_WISC4VCI = "WISC-IV Verbal Comprehension Index (VCI)";
    public static final String ALIAS_WISC4PRI = "WISC-IV Perceptual Reasoning Index (PRI)";
    public static final String ALIAS_WISC4WMI = "WISC-IV Working Memory Index (WMI)";
    public static final String ALIAS_WISC4PSI = "WISC-IV Processing Speed Index (PSI)";
    public static final String ALIAS_IQWVERB = "Wechsler Verbal IQ Score";
    public static final String ALIAS_IQWPERF = "Wechsler Performance IQ Score";
    public static final String ALIAS_IQWFULL = "Wechsler Full Scale IQ Score";
    
    public static final String DBFIELDNAME_INDEXID = "indexid";
    public static final String DBFIELDNAME_FAMNUM = "famnum";
    public static final String DBFIELDNAME_FIRSTNAM = "firstnam";
    public static final String DBFIELDNAME_LASTNAME = "lastname";
    public static final String DBFIELDNAME_GENDER = "gender";
    public static final String DBFIELDNAME_DOB = "dob";
    public static final String DBFIELDNAME_DOD = "dod";
    public static final String DBFIELDNAME_IDBIOMOM = "idbiomom";
    public static final String DBFIELDNAME_IDBIODAD = "idbiodad";
    public static final String DBFIELDNAME_BASNOTE = "basnote";
    public static final String DBFIELDNAME_BASCHK = "baschk";
    public static final String DBFIELDNAME_DATERECD = "daterecd";
    public static final String DBFIELDNAME_DNA1 = "dna1";
    public static final String DBFIELDNAME_SOURCE1 = "source1";
    public static final String DBFIELDNAME_AVAILABLE = "available";
    public static final String DBFIELDNAME_AFFY1 = "affy1";
    public static final String DBFIELDNAME_AFFY1_DATE = "affy1_date";
    public static final String DBFIELDNAME_RACEORIG = "raceorig";
    public static final String DBFIELDNAME_ETHGROUP = "ethgroup";
    public static final String DBFIELDNAME_IQLR = "iqlr";
    public static final String DBFIELDNAME_IQWDATE = "iqwdate";
    public static final String DBFIELDNAME_IQWAGEMO = "iqwagemo";
    public static final String DBFIELDNAME_IQWTYPE = "iqwtype";
    public static final String DBFIELDNAME_WISC4VCI = "wisc4vci";
    public static final String DBFIELDNAME_WISC4PRI = "wisc4pri";
    public static final String DBFIELDNAME_WISC4WMI = "wisc4wmi";
    public static final String DBFIELDNAME_WISC4PSI = "wisc4psi";
    public static final String DBFIELDNAME_IQWVERB = "iqwverb";
    public static final String DBFIELDNAME_IQWPERF = "iqwperf";
    public static final String DBFIELDNAME_IQWFULL = "iqwfull";
    
    private void setIndexes() {
        INDEX_INDEXID = this.getFieldIndexInDB(DBFIELDNAME_INDEXID);
        INDEX_FAMNUM = this.getFieldIndexInDB(DBFIELDNAME_FAMNUM);
        INDEX_FIRSTNAM = this.getFieldIndexInDB(DBFIELDNAME_FIRSTNAM);
        INDEX_LASTNAME = this.getFieldIndexInDB(DBFIELDNAME_LASTNAME);
        INDEX_GENDER = this.getFieldIndexInDB(DBFIELDNAME_GENDER);
        INDEX_DOB = this.getFieldIndexInDB(DBFIELDNAME_DOB);
        INDEX_DOD = this.getFieldIndexInDB(DBFIELDNAME_DOD);
        INDEX_IDBIOMOM = this.getFieldIndexInDB(DBFIELDNAME_IDBIOMOM);
        INDEX_IDBIODAD = this.getFieldIndexInDB(DBFIELDNAME_IDBIODAD);
        INDEX_BASNOTE = this.getFieldIndexInDB(DBFIELDNAME_BASNOTE);
        INDEX_BASCHK = this.getFieldIndexInDB(DBFIELDNAME_BASCHK);
        INDEX_DATERECD = this.getFieldIndexInDB(DBFIELDNAME_DATERECD);
        INDEX_DNA1 = this.getFieldIndexInDB(DBFIELDNAME_DNA1);
        INDEX_SOURCE1 = this.getFieldIndexInDB(DBFIELDNAME_SOURCE1);
        INDEX_AVAILABLE = this.getFieldIndexInDB(DBFIELDNAME_AVAILABLE);
        INDEX_AFFY1 = this.getFieldIndexInDB(DBFIELDNAME_AFFY1);
        INDEX_AFFY1_DATE = this.getFieldIndexInDB(DBFIELDNAME_AFFY1_DATE);
        INDEX_RACEORIG = this.getFieldIndexInDB(DBFIELDNAME_RACEORIG);
        INDEX_ETHGROUP = this.getFieldIndexInDB(DBFIELDNAME_ETHGROUP);
        INDEX_IQLR = this.getFieldIndexInDB(DBFIELDNAME_IQLR);
        INDEX_IQWDATE = this.getFieldIndexInDB(DBFIELDNAME_IQWDATE);
        INDEX_IQWAGEMO = this.getFieldIndexInDB(DBFIELDNAME_IQWAGEMO);
        INDEX_IQWTYPE = this.getFieldIndexInDB(DBFIELDNAME_IQWTYPE);
        INDEX_WISC4VCI = this.getFieldIndexInDB(DBFIELDNAME_WISC4VCI);
        INDEX_WISC4PRI = this.getFieldIndexInDB(DBFIELDNAME_WISC4PRI);
        INDEX_WISC4WMI = this.getFieldIndexInDB(DBFIELDNAME_WISC4WMI);
        INDEX_WISC4PSI = this.getFieldIndexInDB(DBFIELDNAME_WISC4PSI);
        INDEX_IQWVERB = this.getFieldIndexInDB(DBFIELDNAME_IQWVERB);
        INDEX_IQWPERF = this.getFieldIndexInDB(DBFIELDNAME_IQWPERF);
        INDEX_IQWFULL = this.getFieldIndexInDB(DBFIELDNAME_IQWFULL);
    }

    public PatientTableSchema(DbSchema s) {
        super(s.addTable(TABLE_NAME));
        addColumns();
        setIndexes();
    }
    
    private void addColumns() {
        addColumn(DBFIELDNAME_INDEXID,ALIAS_INDEXID,TableSchema.ColumnType.VARCHAR,250);
        addColumn(DBFIELDNAME_FAMNUM,ALIAS_FAMNUM,TableSchema.ColumnType.VARCHAR,250);
        addColumn(DBFIELDNAME_FIRSTNAM,ALIAS_FIRSTNAM,TableSchema.ColumnType.VARCHAR,250);
        addColumn(DBFIELDNAME_LASTNAME,ALIAS_LASTNAME,TableSchema.ColumnType.VARCHAR,250);
        addColumn(DBFIELDNAME_GENDER,ALIAS_GENDER,TableSchema.ColumnType.INTEGER,1);
        addColumn(DBFIELDNAME_DOB,ALIAS_DOB,TableSchema.ColumnType.DATE,1);
        addColumn(DBFIELDNAME_DOD,ALIAS_DOD,TableSchema.ColumnType.DATE,1);
        addColumn(DBFIELDNAME_IDBIOMOM,ALIAS_IDBIOMOM,TableSchema.ColumnType.VARCHAR,250);
        addColumn(DBFIELDNAME_IDBIODAD,ALIAS_IDBIODAD,TableSchema.ColumnType.VARCHAR,250);
        addColumn(DBFIELDNAME_BASNOTE,ALIAS_BASNOTE,TableSchema.ColumnType.VARCHAR,250);
        addColumn(DBFIELDNAME_BASCHK,ALIAS_BASCHK,TableSchema.ColumnType.INTEGER,1);
        addColumn(DBFIELDNAME_DATERECD,ALIAS_DATERECD,TableSchema.ColumnType.DATE,1);
        addColumn(DBFIELDNAME_DNA1,ALIAS_DNA1,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_SOURCE1,ALIAS_SOURCE1,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_AVAILABLE,ALIAS_AVAILABLE,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_AFFY1,ALIAS_AFFY1,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_AFFY1_DATE,ALIAS_AFFY1_DATE,TableSchema.ColumnType.DATE,1);
        addColumn(DBFIELDNAME_RACEORIG,ALIAS_RACEORIG,TableSchema.ColumnType.VARCHAR,250);
        addColumn(DBFIELDNAME_ETHGROUP,ALIAS_ETHGROUP,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_IQLR,ALIAS_IQLR,TableSchema.ColumnType.DECIMAL,10);
        addColumn(DBFIELDNAME_IQWDATE,ALIAS_IQWDATE,TableSchema.ColumnType.DATE,1);
        addColumn(DBFIELDNAME_IQWAGEMO,ALIAS_IQWAGEMO,TableSchema.ColumnType.FLOAT,1);
        addColumn(DBFIELDNAME_IQWTYPE,ALIAS_IQWTYPE,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_WISC4VCI,ALIAS_WISC4VCI,TableSchema.ColumnType.DECIMAL,10);
        addColumn(DBFIELDNAME_WISC4PRI,ALIAS_WISC4PRI,TableSchema.ColumnType.DECIMAL,10);
        addColumn(DBFIELDNAME_WISC4WMI,ALIAS_WISC4WMI,TableSchema.ColumnType.DECIMAL,10);
        addColumn(DBFIELDNAME_WISC4PSI,ALIAS_WISC4PSI,TableSchema.ColumnType.DECIMAL,10);
        addColumn(DBFIELDNAME_IQWVERB,ALIAS_IQWVERB,TableSchema.ColumnType.DECIMAL,10);
        addColumn(DBFIELDNAME_IQWPERF,ALIAS_IQWPERF,TableSchema.ColumnType.DECIMAL,10);
        addColumn(DBFIELDNAME_IQWFULL,ALIAS_IQWFULL,TableSchema.ColumnType.DECIMAL,10);
    }
    
}
