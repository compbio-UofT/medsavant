package medsavant.pgx.localDB;

import NaturalSorting.NaturalOrderComparator;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import medsavant.pgx.PGXException;
import medsavant.pgx.PGXGeneAndVariants;
import org.apache.commons.lang3.StringUtils;
import org.ut.biolab.medsavant.shared.appdevapi.DBAnnotationColumns;
import org.ut.biolab.medsavant.shared.appdevapi.Variant;

/**
 * Assorted DB functions.
 * 
 * @author rammar
 */
public class PGXDBFunctions {
	
	
	/** 
	 * Get all genes in the database.
	 * @return a List of all genes in this DB.
	 */
	public static List<String> getGenes() throws SQLException {
		List<String> genes= new LinkedList<String>();
		
		// MUST use single quotes for HyperSQL (hsql) SQL syntax
		String sql=	"SELECT G.gene " +
					"FROM gene_marker_list G ";
		
		ResultSet rs= PGXDB.executeQuery(sql);

		/* Grab the first element from each row in the results. */
		int rowCount= 0;
		while (rs.next()) {
			List rowTemp= PGXDB.getRowAsList(rs);
			genes.add((String) rowTemp.get(0));

			rowCount++;
		}
		
		return genes;
	}
	
	
	/** 
	 * Get this gene's semicolon-delimited list of markers.
	 * @param geneSymbol the gene symbol string (not case sensitive)
	 * @return a List of markers
	 */
	public static List<String> getMarkers(String geneSymbol) throws PGXException, SQLException {
		List<String> markers;
		
		// MUST use single quotes for HyperSQL (hsql) SQL syntax
		String sql=	"SELECT G.marker_list " +
					"FROM gene_marker_list G " +
					"WHERE G.gene = '" + geneSymbol + "' "; 
		
		ResultSet rs= PGXDB.executeQuery(sql);

		int rowCount= 0;
		String markerString= null;
		while (rs.next()) {
			/* There should only be a single result from this query. */
			if (rowCount > 1)
				throw new PGXException(">1 row found for query: " + sql);

			/* First and only element is the marker liststring, as specified
			 * in sql above. */
			List rowTemp= PGXDB.getRowAsList(rs);
			markerString= (String) rowTemp.get(0);

			rowCount++;
		}
		
		/* Split the marker list on semicolons. Empty list if no markerString */
		if (markerString != null) {
			markers= Arrays.asList(markerString.split(";"));
		} else  {
			markers= new LinkedList<String>(); // new empty list
		}
		
		return markers;
	}
	
	
	/**
	 * Return a string of the diplotype for this gene-variants pair using star (*) nomenclature.
	 * @param pgav the PGXGeneAndVariants gene-variants pair.
	 * @return a String of the diplotype in the form "*1/*17" NOT with the gene
	 *	like "CYP2C19*1/*17"; null if this gene has no * diplotypes.
	 */
	public static String getDiplotype(PGXGeneAndVariants pgav) {	
		String diplotype= null;
		
		/* Hash of marker, nucleotide (ref/alt) pairs. */
		Map<String, String> maternalMarkers= new HashMap<String, String>();
		Map<String, String> paternalMarkers= new HashMap<String, String>();
		
		/* Iterate through the Variants and grab the haplotypes. Look for the
		 * correct alternate number when processing the GT fields. So, if the 
		 * field is 0|2 and we're on alt_number = 1, skip that variant.
		 * PRECONDITION: We do not deal with two non-zero alleles in the same GT
		 * field. The reason I'm making this assumption is that dbSNP variants
		 * are annotation by chr, position, ref and alt. A line that is recorded
		 * as having 1/2 will refer to two different alt positions, and since 
		 * the PGXAnalysis queries by rsID, I don't anticipate GT fields like
		 * this. However, I'm adding a check in here anyway. */
		for (Variant v : pgav.getVariants()) {
			// populate an array of ref and alt for this allele- not all values 
			// will be initialized, it's just for simplicity below.
			String[] refAndAlts= new String[v.getAlternateNumber() + 1];
			// 0 = ref, 1 = alt_number_1, 2 = alt_number_2, etc.
			refAndAlts[0]= v.getReference();
			refAndAlts[v.getAlternateNumber()]= v.getAlternate();
			
			String[] gt= v.getGT().split("\\|"); // split on "|"
			int maternalGT= Integer.parseInt(gt[0]);
			int paternalGT= Integer.parseInt(gt[1]);
			
			String currentRsID= (String) v.getColumn(DBAnnotationColumns.DBSNP_TEXT);
			maternalMarkers.put(currentRsID, refAndAlts[maternalGT]);
			paternalMarkers.put(currentRsID, refAndAlts[paternalGT]);
			
			// TESTING
			//System.out.println("[TESTING]: " + StringUtils.join(new Object[]{v.getGT(), currentRsID,
			//	maternalMarkers.toString(), paternalMarkers.toString()}, " "));
		}
		
		/* Create a list of diplotypes and sort these naturally/lexicographically
		 * so that diplotypes appear as "*1/*17" instead of "*17/*1". */
		List<String> haplotypes= new ArrayList<String>();
		haplotypes.add(getHaplotype(pgav.getGene(), maternalMarkers));
		haplotypes.add(getHaplotype(pgav.getGene(), paternalMarkers));
		Collections.sort(haplotypes, new NaturalOrderComparator());			
		diplotype= haplotypes.get(0) + "/" + haplotypes.get(1); // there are only 2 haplotypes - added above
		
		
		// TO DO ////////////////////
		
		// Also need to deal with *1 - how is that query formed?
		
		// check if gene is in the haplotype table and then output
		// null if not in the table.
		
		return diplotype;
	}
	
	
	/**
	 * Convert marker-genotype pairs into a * nomenclature haplotype for this gene.
	 * @param gene the gene name/symbol
	 * @param markerGenotypePairs a hash of marker-genotype pairs
	 * @return a string representing the * nomenclature haplotype for this hash
	 */
	private static String getHaplotype(String gene, Map<String, String> markerGenotypePairs) {
		String sql=	"SELECT H.haplotype_symbol " +
					"FROM haplotype_markers H " +
					"WHERE gene = '" + gene + "' ";
		
		/* Iterate over the markers and construct a query for the local DB.
		 * Marker order doesn't affect the query. */
		for (String marker : markerGenotypePairs.keySet()) {
			sql +=	"	AND marker_info LIKE '%" + marker + "=" + markerGenotypePairs.get(marker) +"%' ";
		}
		
		/* Get all * alleles that can be retrieved with this query (>= 1). */
		List<String> allPossibleAlleles= new ArrayList<String>();
		try {
			ResultSet rs= PGXDB.executeQuery(sql);
			
			while (rs.next()) {
				// only grab the first element because we're only querying it
				// above in the SELECT statement
				allPossibleAlleles.add((String) PGXDB.getRowAsList(rs).get(0));
			}
		} catch (SQLException se) {
			se.printStackTrace();
		}
		
		/* Sort the list of haplotypes naturally/lexicographically. */
		Collections.sort(allPossibleAlleles, new NaturalOrderComparator());
		String haplotype= StringUtils.join(allPossibleAlleles);
		
		return haplotype;
	}
}
