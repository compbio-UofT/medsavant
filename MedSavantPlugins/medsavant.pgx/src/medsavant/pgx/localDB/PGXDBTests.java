/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package medsavant.pgx.localDB;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
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
