package medsavant.secondary.localDB;

import CSVLoader.CSVLoader;
import java.io.File;
import java.io.FileInputStream;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.ut.biolab.medsavant.client.settings.DirectorySettings;


/**
 * Interfaces with the Incidentalome DB.
 * 
 * @author rammar
 */
public class DiscoveryDB {
	
	private static Connection conn;
	private static String DB_URL;
	private static String DB_USER;
	private static String DB_PASSWORD;
	private static Properties properties;
	
			
	/**
	 * Creates all tables to the DB and populates with all data from packaged
	 * text files.
	 * @param	url	The DB URL
	 * @param	user	Username for login
	 * @param	passw	Password for login
	 * @throws SQLException 
	 */
	public static void populateDB(String url, String user, String passw, Properties prop) throws SQLException {
		DB_URL= url;
		DB_USER= user;
		DB_PASSWORD= passw;
		properties= prop;
		
		conn= connectionToServer();	
		createSchema(conn);
		loadTables(conn);
	}

	
	/** Close connection to DB. */
	public static void closeConnectionToDB() throws SQLException {
		conn.close();
	}
	
	
	/**
	 * Create JDBC-based connection to HSQL server.
	 * By default, connection is auto-committing.
	 */
	private static Connection connectionToServer () throws SQLException {
		try {
			Class.forName("org.hsqldb.jdbcDriver");
		} catch (Exception e) {
			System.out.println("ERROR: failed to load HSQLDB JDBC driver.");
			e.printStackTrace();
		}
		return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
	}
	
	
	/**
	 * Creates the table schema.
	 * @param c The HSQL DB connection
	 */
	private static void createSchema(Connection c) {
		try {
			Statement s= c.createStatement();
			String sql;

			/* Execute SQL as a batch, rather than individually. This is more efficient
			 * when processing many 
			 */

			/* // Simple test table
			sql=	"CREATE TABLE dbtest ( " +
					"	intField1 INT, " +
					"	intField2 INT, " +
					")";
			s.addBatch(sql);
			*/

			
			/* Create all the Incidentalome tables. These will be populated by
			 * CSV Loader. */
			
			sql=	"CREATE TABLE disease_classification ( " +
					"  inheritance varchar(45) NOT NULL,  " +
					"  zygosity varchar(45) NOT NULL,  " +
					"  gender varchar(45) DEFAULT 'both' NOT NULL,  " +
					"  classification varchar(45) NOT NULL,  " +
					"  PRIMARY KEY (inheritance,zygosity,gender)  " +
					")";
			s.addBatch(sql);

			
			sql=	"CREATE TABLE CGD_synonym ( " +
					"	inheritance varchar(200) NOT NULL, " + 
					"	synonym varchar(10) DEFAULT NULL, " +
					"	comments varchar(500) DEFAULT NULL, " + 
					"	PRIMARY KEY (inheritance) " +
					")";
			s.addBatch(sql);

			sql=	"CREATE TABLE CGD ( " +
					"	Gene varchar(20) NOT NULL, " +
					"	Entrez_Gene_Id varchar(20) NOT NULL, " +
					"	Condition varchar(500) NOT NULL, " +
					"	Inheritance varchar(500) NOT NULL, " +
					"	Age_Group varchar(500) NOT NULL, " +
					"	Allelic_Conditions varchar(1000) NOT NULL, " +
					"	Manifestation_Categories varchar(1000) NOT NULL, " +
					"	Intervention_Categories varchar(1000) NOT NULL, " +
					"	Comments varchar(1000) NOT NULL, " +
					"	Intervention_Rationale varchar(10000) NOT NULL, " +
					"	Refs varchar(1000) NOT NULL, " +
					"	PRIMARY KEY (Gene) " +
					")";
			s.addBatch(sql);
		
			sql=	"CREATE TABLE ACMG ( " +
					"	gene varchar(20) NOT NULL, " +
					"	PRIMARY KEY (Gene) " +
					")";
			s.addBatch(sql);
			
/*
			// OLD TABLES
			sql=	"CREATE TABLE incidentalome_annotated ( " +
					"  Gene varchar(100) NOT NULL, " +
					"  Disease varchar(1000) NOT NULL, " +
					"  Inheritance_JS varchar(5) NOT NULL, " +
					"  MedicalAction varchar(100) NOT NULL, " +
					"  ModifiedInh_JS varchar(1) NOT NULL, " +
					"  Source varchar(100) NOT NULL, " +
					"  Inheritance_source varchar(5) NOT NULL, " +
					"  Gene_ReviewedJS varchar(100) NOT NULL, " +
					"  Comments_JS varchar(1000) NOT NULL " +
					")";
			s.addBatch(sql);

			sql=	"CREATE TABLE hgmd_pro_allmut ( " +
					"  disease varchar(125) DEFAULT NULL, " +
					"  gene varchar(10) DEFAULT NULL, " +
					"  chrom varchar(25) DEFAULT NULL, " +
					"  genename varchar(200) DEFAULT NULL, " +
					"  gdbid varchar(8) DEFAULT NULL, " +
					"  omimid varchar(8) DEFAULT NULL, " +
					"  amino varchar(8) DEFAULT NULL, " +
					"  deletion varchar(65) DEFAULT NULL, " +
					"  insertion varchar(65) DEFAULT NULL, " +
					"  codon integer DEFAULT NULL, " +
					"  codonAff integer DEFAULT NULL, " +
					"  descr varchar(125) DEFAULT NULL, " +
					"  hgvs varchar(60) DEFAULT NULL, " +
					"  hgvsAll varchar(120) DEFAULT NULL, " +
					"  dbsnp varchar(12) DEFAULT NULL, " +
					"  chromosome varchar(2) DEFAULT NULL, " +
					"  startCoord integer DEFAULT NULL, " +
					"  endCoord integer DEFAULT NULL, " +
					"  tag varchar(10) DEFAULT NULL, " +
					"  author varchar(25) DEFAULT NULL, " +
					"  fullname varchar(50) DEFAULT NULL, " +
					"  allname varchar(300) DEFAULT NULL, " + // increased to 300 from 200
					"  vol varchar(6) DEFAULT NULL, " +
					"  page varchar(50) DEFAULT NULL, " + // some entries were larger than 10 characters
					"  year_field char(4) DEFAULT NULL, " + // "year" was an invalid column name
					"  pmid varchar(8) DEFAULT NULL, " +
					"  reftag char(3) DEFAULT NULL, " +
					"  comments varchar(300) DEFAULT NULL, " + // increased to 300 from 125
					"  acc_num varchar(10) DEFAULT '' NOT NULL, " +
					"  new_date date DEFAULT NULL, " +
					"  base char(1) DEFAULT NULL " +
					")";
			s.addBatch(sql);

			sql=	"CREATE TABLE clinvar_20130808 ( " +
					"  chromosome varchar(2) NOT NULL, " +
					"  position int NOT NULL, " +
					"  id varchar(30) NOT NULL, " +
					"  ref varchar(1000) default NULL, " +
					"  alt varchar(1000) default NULL, " +
					"  qual varchar(45) default NULL, " +
					"  filter varchar(45) default NULL, " +
					"  INFO varchar(10000) default NULL, " +
					"  PRIMARY KEY  (chromosome,position,id) " +
					")";
			s.addBatch(sql);
*/
			
			
			s.executeBatch();
			s.close();
		} catch (SQLException e) {
			System.err.println("[IncidentalDB]: Error creating tables " + e.toString());
			e.printStackTrace();
		}
	}
	
	
	/** 
	 * Load tables into the DB.
	 * @param c The HSQL DB connection
	 */
	private static void loadTables(Connection c) throws SQLException {
		
/* 
		// load test table dbtest
		Statement s= c.createStatement();
		int x= 1;
		int y= 10;
		
		for (int i= 0; i != 10; i++) {
			s.execute("INSERT INTO dbtest VALUES ("+ x++ +","+ y++ + ")");
		}
		s.close();
*/
		
		/* Load the delimited tables from text files. */
		CSVLoader loader;
		try {

			String filepath;			
			
			loader= new CSVLoader(connectionToServer()); // pass a new connection since it auto-closes it.
			loader.setSeprator('\t');
			filepath= "/db_files/disease_classification.txt";
			loader.loadCSV(DiscoveryDB.class.getResourceAsStream(filepath), "disease_classification", false);
			
			loader= new CSVLoader(connectionToServer()); // pass a new connection since it auto-closes it.
			loader.setSeprator('\t');
			filepath= "/db_files/CGD_synonyms.txt";
			loader.loadCSV(DiscoveryDB.class.getResourceAsStream(filepath), "CGD_synonym", false);
			
			loader= new CSVLoader(connectionToServer()); // pass a new connection since it auto-closes it.
			loader.setSeprator('\t');
			loader.loadCSV(new FileInputStream(DirectorySettings.getMedSavantDirectory().getPath() +
				File.separator + "cache" + File.separator + properties.getProperty("CGD_DB_filename")),
				"CGD", false);
			
			loader= new CSVLoader(connectionToServer()); // pass a new connection since it auto-closes it.
			loader.setSeprator('\t');
			filepath= "/db_files/ACMG_incidental_genes_list.txt";
			loader.loadCSV(DiscoveryDB.class.getResourceAsStream(filepath), "ACMG", false);
			
/* 
			//OLD TABLES
			loader= new CSVLoader(connectionToServer()); // pass a new connection since it auto-closes it.
			loader.setSeprator('\t');
			filepath= "/db_files/incidentalome_annotated.txt";
			loader.loadCSV(IncidentalDB.class.getResourceAsStream(filepath), "incidentalome_annotated", false);

			loader= new CSVLoader(connectionToServer()); // pass a new connection since it auto-closes it.
			loader.setSeprator('\t');
			filepath= "/db_files/clinvar_20130808.txt";
			loader.loadCSV(IncidentalDB.class.getResourceAsStream(filepath), "clinvar_20130808", false);
			
			loader= new CSVLoader(connectionToServer()); // pass a new connection since it auto-closes it.
			loader.setSeprator('\t');
			filepath= "/db_files/hgmd_pro_allmut.txt";
			loader.loadCSV(IncidentalDB.class.getResourceAsStream(filepath), "hgmd_pro_allmut", false);
*/
			
		} catch (Exception e) {
			System.err.println("[IncidentalDB]: Error loading tables " + e.toString());
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Execute an SQL command.
	 * @param	sql	The SQL statement to execute.
	 * @return	ResultSet object corresponding to the SQL statement's output
	 * @precondition Static Connection conn not null.
	 */
	public static ResultSet executeQuery(String sql) throws SQLException {
		Statement s= conn.createStatement();
		s.execute(sql);
		
		ResultSet rs= s.getResultSet();
		
		s.close();
		
		return rs;
	}
	
	
	/**
	 * Print text output for a given query.
	 * @param	sql	String SQL statement to be executed. Includes all possible SQL statements.
	 * @precondition Static Connection conn not null.
	 */
	public static void printQueryResults(String sql) throws SQLException {
		Statement s= conn.createStatement();
		s.execute(sql);
		
		ResultSet rs= s.getResultSet();
		while (rs.next()) {
			System.out.println("[IncidentalDB]: " + getRowAsString(rs));
		}
		
		s.close();
	}
	
	
	/**
	 * Output the entire row because ResultSet get methods are busting my balls.
	 */
	public static String getRowAsString(ResultSet rs) throws SQLException {
		String result= "";
		
		ResultSetMetaData rsmd= rs.getMetaData();
		int columnsNumber= rsmd.getColumnCount();
		
		// Remember, columsn start at index 1
		for (int i= 1; i <= columnsNumber; ++i) {
			result += rs.getString(i) + "\t";
		}
		
		return result;
	}
	
	
	/**
	 * Output the entire row because ResultSet get methods are busting my balls.
	 * Gets the row pointed to by cursor's current position.
	 */
	public static List<Object> getRowAsList(ResultSet rs) throws SQLException {
		ResultSetMetaData rsmd= rs.getMetaData();
		int columnsNumber= rsmd.getColumnCount();
		
		LinkedList<Object> result= new LinkedList<Object>();
		
		// Remember, columns start at index 1
		for (int i= 1; i <= columnsNumber; ++i) {
			result.add(rs.getString(i));
		}
		
		return result;
	}
	
}
