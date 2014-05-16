/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package medsavant.pgx.localDB;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test concepts in the pharmacogenomics database in this class.
 * 
 * @author rammar
 */
public class PGXDBTests {
	
	/** 
	 * Run the tests.
	 * To run this from NetBeans, right click on the class and select "Run File".
	 */
	public static void main(String[] args) {
		// Load the DB
		try {
			PGXDB.initialize();
		} catch (SQLException se) {
			stderr("Error loading the pharmacogenomics database.");
			se.printStackTrace();
		}
		
		// Tests
		getAllAlleles();
		getAllGenes();
		getMarkerPositionsForGene("CYP2C19");
		getMetabolizer();
		getPubmedIDs();
		
		// Specific tests
		//printSQLResults("SELECT H.haplotype_symbol FROM haplotype_markers H WHERE gene = 'CYP2C19' 	AND marker_info LIKE '%rs4244285=G%' 	AND marker_info LIKE '%rs4986893=G%' 	AND marker_info LIKE '%rs28399504=A%' 	AND marker_info LIKE '%rs56337013=C%' 	AND marker_info LIKE '%rs72552267=G%' 	AND marker_info LIKE '%rs72558186=T%' 	AND marker_info LIKE '%rs41291556=T%' 	AND marker_info LIKE '%rs12248560=C%' 	AND marker_info LIKE '%rs11188072=C%'", "");
		//printSQLResults("SELECT H.haplotype_symbol FROM haplotype_markers H WHERE gene = 'CYP2C19' 	AND marker_info LIKE '%rs4244285=G%' 	AND marker_info LIKE '%rs4986893=A%' 	AND marker_info LIKE '%rs28399504=A%' 	AND marker_info LIKE '%rs56337013=C%' 	AND marker_info LIKE '%rs72552267=G%' 	AND marker_info LIKE '%rs72558186=T%' 	AND marker_info LIKE '%rs41291556=T%' 	AND marker_info LIKE '%rs12248560=C%' 	AND marker_info LIKE '%rs11188072=C%'", "");
		//printSQLResults("SELECT activity_phenotype FROM haplotype_activity WHERE gene = 'CYP2D6' AND haplotype = '*17'", "");
	}
	
	
	/**
	 * Query the DB and get all alleles that have the following markers.
	 * This is a test to determine the number of star alleles that can be 
	 * retrieved without complete data on all markers specified by PharmGKB for
	 * these haplotypes.
	 */
	private static void getAllAlleles() {
		String sql;
		
		String test1= "Testing retrieving CYP2C19*2 alleles with incomplete rsID/marker information";
		stdout(test1);
		
		/* Get all CYP2C19*2 alleles. */
		// MUST use single quotes for HyperSQL (hsql) SQL syntax
		sql=	"SELECT H.gene, H.haplotype_symbol " +
				"FROM haplotype_markers H " +
				"WHERE gene = 'CYP2C19' " +
				"	AND marker_info LIKE '%rs4244285=A%' ";
		
		/* Output all the haplotype star alleles to stdout. */
		printSQLResults(sql, test1);
		
		String test2= "Testing retrieving anything with the following alleles rs4917623=C;rs3758581=G;rs17885098=T";
		stdout(test2);
		
		/* Get all CYP2C19*2 alleles. */
		// MUST use single quotes for HyperSQL (hsql) SQL syntax
		sql=	"SELECT H.gene, H.haplotype_symbol " +
				"FROM haplotype_markers H " +
				"WHERE gene = 'CYP2C19' " +
				"	AND marker_info LIKE '%rs4917623=c%' " +
				"	AND marker_info LIKE '%rs3758581=g%' " +
				"	AND marker_info LIKE '%rs17885098=T%' ";
		
		/* Output all the haplotype star alleles to stdout. */
		printSQLResults(sql, test2);
		
		
		String test3= "Testing retrieving the *1;*1A alleles which should have no markers (all ref calls)";
		stdout(test3);
		
		/* Query the *1 (no variants) allele. */
		// MUST use single quotes for HyperSQL (hsql) SQL syntax
		sql=	"SELECT H.gene, H.haplotype_symbol " +
				"FROM haplotype_markers H " +
				"WHERE gene = 'CYP2C19' " +
				"	AND marker_info LIKE '' ";
		
		/* Output all the haplotype star alleles to stdout. */
		printSQLResults(sql, test3);
		
		
		String test4= "Testing retrieving all alleles with NULL marker_info fields. The *1;*1A alleles " +
			"which have no markers (all ref calls), store an empty string ''.";
		stdout(test4);
		
		/* Test to see what comes up with a NULL marker_info field. Should be empty. */
		// MUST use single quotes for HyperSQL (hsql) SQL syntax
		sql=	"SELECT H.gene, H.haplotype_symbol " +
				"FROM haplotype_markers H " +
				"WHERE gene = 'CYP2C19' " +
				"	AND marker_info IS NULL ";
		
		/* Output all the haplotype star alleles to stdout. */
		printSQLResults(sql, test4);
		
		String test5= "Testing getting * nomenclature for this haplotype from GS000035328-ASM.";
		stdout(test5);
		
		/* Test to see what comes up with a NULL marker_info field. Should be empty. */
		// MUST use single quotes for HyperSQL (hsql) SQL syntax
		sql=	"SELECT H.gene, H.haplotype_symbol " +
				"FROM haplotype_markers H " +
				"WHERE gene = 'CYP2C19' " +
				"	AND marker_info LIKE '%rs7902257=g%' ";
		
		/* Output all the haplotype star alleles to stdout. */
		printSQLResults(sql, test5);
		
	}
	
	
	/**
	 * Query the DB and get all genes that have pharmacogenomic variants.
	 * This is a test to obtain all gene symbols from the local PGx DB.
	 */
	private static void getAllGenes() {
		String sql;
		
		String test1= "Testing retrieving all PGx genes";
		stdout(test1);
		
		// MUST use single quotes for HyperSQL (hsql) SQL syntax
		sql=	"SELECT G.gene " +
				"FROM gene_marker_list G ";
		
		printSQLResults(sql, test1);
	}
	
	
	/**
	 * Query the DB and get all marker coordinates for the given gene symbol.
	 * @param gene The gene symbol
	 */
	private static void getMarkerPositionsForGene(String gene) {
		String sql;
		
		String test1= "Testing retrieving all markers for gene " + gene;
		stdout(test1);
		
		// MUST use single quotes for HyperSQL (hsql) SQL syntax
		sql=	"SELECT marker_list " +
				"FROM gene_marker_list " +
				"WHERE gene = '" + gene + "' ";
		
		List<String> markerList= new ArrayList<String>();
		try {
			ResultSet rs= PGXDB.executeQuery(sql);
			
			/* Just get the first line. According to the Java API:
			 * "A ResultSet cursor is initially positioned before the first row;
			 * the first call to the method next makes the first row the current row" */
			if (rs.next())
				markerList= Arrays.asList(((String) PGXDB.getRowAsList(rs).get(0)).split(";"));
					
		} catch (SQLException se) {
			stderr(test1);
			se.printStackTrace();
		}
		
		/* Get the marker coordinates for each marker. */
		for (String marker : markerList) {
			sql=	"SELECT M.chromosome, M.position, M.ref, M.alt " +
					"FROM marker_coordinates M " +
					"WHERE M.marker = '" + marker + "' ";
			printSQLResults(sql, test1 + " for marker " + marker);
		}
	}
	
	
	/**
	 * Query the DB to test for metabolism class interpretations.
	 */
	private static void getMetabolizer() {
		String sql;
		
		String test1= "Testing the activity scores of CYP2D6 extensive metabolizer at score 0.8";
		stdout(test1);
		
		double totalActivity= 0.8;
		
		/* Get the activity score that is the highest score just below our total activity. */
		
		// MUST use single quotes for HyperSQL (hsql) SQL syntax
		sql=	"SELECT total_activity_score_minimum, metabolizer_class " +
				"FROM activity_to_metabolizer " +
				"WHERE total_activity_score_minimum = (SELECT MAX(total_activity_score_minimum) " +
				"										FROM activity_to_metabolizer " +
				"										WHERE total_activity_score_minimum <= " + totalActivity + ")";
		
		printSQLResults(sql, test1);
		
		String test2= "Testing the activity scores of CYP2D6 extensive metabolizer at score 2.0." +
			" Should be classified as extensive. >2.0 is Ultrarapid.";
		stdout(test2);
		
		totalActivity= 2.0;
		
		/* Get the activity score that is the highest score just below our total activity. */
		sql=	"SELECT total_activity_score_minimum, metabolizer_class " +
				"FROM activity_to_metabolizer " +
				"WHERE total_activity_score_minimum = (SELECT MAX(total_activity_score_minimum) " +
				"										FROM activity_to_metabolizer " +
				"										WHERE total_activity_score_minimum <= " + totalActivity + ")";
		
		printSQLResults(sql, test2);

		String test3= "Getting the total activity score for CYP2D6 *1/*1, should be 2.0.";
		stdout(test3);
		
		String hap1= "*1";
		String hap2= "*1";
		
		/* Only works for diplotypes. Not if there are >2 haplotypes. */
		
		// MUST use single quotes for HyperSQL (hsql) SQL syntax
		sql=	"SELECT SUM(activity_score) "+
				"FROM (SELECT * " +
				"		FROM haplotype_activity " +
				"		WHERE gene = 'cyp2d6' " +
				"			AND haplotype = '" + hap1 + "' " +
				"		UNION ALL " +
				"		SELECT * " +
				"		FROM haplotype_activity " +
				"		WHERE gene = 'cyp2d6' " +
				"			AND haplotype = '" + hap2 + "') ";
		
		printSQLResults(sql, test3);
		
		// TEST 4
		hap1= "*3";
		hap2= "*6";
		String test4= "Testing the metabolizer class of CYP2D6 " + hap1 + "/" + hap2;
		stdout(test4);
		
		/* I don't like this query - it might be better to make multiple queries
		 * and process the remainder in Java code. */
		
		// MUST use single quotes for HyperSQL (hsql) SQL syntax
		sql=	"SELECT total_activity_score_minimum, metabolizer_class " +
				"FROM activity_to_metabolizer " +
				"WHERE total_activity_score_minimum = " +
				"	(SELECT MAX(total_activity_score_minimum) " +
				"	FROM activity_to_metabolizer " +
				"	WHERE total_activity_score_minimum <= " +
				"		(SELECT SUM(activity_score) "+
				"		FROM (SELECT * " +
				"			FROM haplotype_activity " +
				"			WHERE gene = 'cyp2d6' " +
				"				AND haplotype = '" + hap1 + "' " +
				"			UNION ALL " +
				"			SELECT * " +
				"			FROM haplotype_activity " +
				"			WHERE gene = 'cyp2d6' " +
				"				AND haplotype = '" + hap2 + "')) " +
				"	)";
		
		printSQLResults(sql, test4);
	}
	
	
	/**
	 * Get the PubmedIDs for a gene symbol.
	 */
	private static void getPubmedIDs() {
		String sql;
		
		String gene= "cyp2d6";
		String test1= "Testing retrieving the pubmed IDs for " + gene + ".";
		stdout(test1);		
		
		// MUST use single quotes for HyperSQL (hsql) SQL syntax
		sql=	"SELECT DISTINCT(pubmed_id) " +
				"FROM haplotype_activity " +
				"WHERE gene = '" + gene + "' ";
		
		printSQLResults(sql, test1);
		
		String test2= "Testing retrieving the pubmed IDs for all genes.";
		stdout(test2);
		
		// MUST use single quotes for HyperSQL (hsql) SQL syntax
		sql=	"SELECT DISTINCT(pubmed_id) " +
				"FROM haplotype_activity ";
		
		printSQLResults(sql, test2);
	}
	
	
	/**
	 * Print all rows from this query to stdout.
	 * @param sql the SQL query
	 * @param errorMessage the error message to be printed if something fails.
	 */
	private static void printSQLResults(String sql, String errorMessage) {
		try {
			ResultSet rs= PGXDB.executeQuery(sql);
			
			/* Output all lines as they are stored in the DB. */
			while (rs.next()) {
				stdout(PGXDB.getRowAsList(rs).toString());
			}		
		} catch (SQLException se) {
			stderr(errorMessage);
			se.printStackTrace();
		}
	}
	
	
	/**
	 * Output to stderr.
	 * @param message the output message
	 */
	private static void stderr(String message) {
		System.err.println("[" + PGXDBTests.class.getSimpleName() + "]: ERROR - " + message);
	}
	
	
	/**
	 * Output to stdout.
	 * @param message the output message
	 */
	private static void stdout(String message) {
		System.out.println("[" + PGXDBTests.class.getSimpleName() + "]:" + message);
	}
}
