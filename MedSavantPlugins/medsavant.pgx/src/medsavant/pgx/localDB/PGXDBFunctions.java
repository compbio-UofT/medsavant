package medsavant.pgx.localDB;

import NaturalSorting.NaturalOrderComparator;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import medsavant.pgx.PGXException;
import medsavant.pgx.PGXGene;
import medsavant.pgx.PGXGenotype;
import org.apache.commons.lang3.StringUtils;
import org.ut.biolab.medsavant.shared.appdevapi.DBAnnotationColumns;
import org.ut.biolab.medsavant.shared.appdevapi.Variant;

/**
 * Assorted DB functions.
 * 
 * @author rammar
 */
public class PGXDBFunctions {
	
	/* The maximum number of markers to drop when search for similar haplotypes. */
	public static final int SIMILAR_HAPLOTYPE_DEPTH= 3;
	public static final String UNKNOWN_HAPLOTYPE= "UNKNOWN";
	public static final String PIPE= "\\|";
	
	
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
	 * Assign the maternal and paternal phased genotypes for this gene.
	 * Once assigned, the input object stores these phased genotype hashes/maps
	 * of marker, nucleotide (ref/alt) pairs.
	 * @param pg the gene-variants pair object
	 */
	public static void assignParentalGenotypes(PGXGene pg) {
		/* Hash of marker, nucleotide (ref/alt) pairs. */
		Map<String, PGXGenotype> maternalGenotypes= new HashMap<String, PGXGenotype>();
		Map<String, PGXGenotype> paternalGenotypes= new HashMap<String, PGXGenotype>();
		
		/* Iterate through the Variants and grab the haplotypes. Look for the
		 * correct alternate number when processing the GT fields. So, if the 
		 * field is 0|2 and we're on alt_number = 1, skip that variant.
		 * PRECONDITION: We do not deal with two non-zero alleles in the same GT
		 * field. The reason I'm making this assumption is that dbSNP variants
		 * are annotation by chr, position, ref and alt. A line that is recorded
		 * as having 1/2 will refer to two different alt positions, and since 
		 * the PGXAnalysis queries by rsID, I don't anticipate GT fields like
		 * this. However, I'm adding a check in here anyway. */
		for (Variant v : pg.getVariants()) {
			// populate an array of ref and alt for this allele- not all values 
			// will be initialized, it's just for simplicity below.
			String[] refAndAlts= new String[v.getAlternateNumber() + 1];
			// 0 = ref, 1 = alt_number_1, 2 = alt_number_2, etc.
			refAndAlts[0]= v.getReference();
			refAndAlts[v.getAlternateNumber()]= v.getAlternate();
			
			/* Make sure ALL variants are phased. That is, if a "|" is missing
			 * and the GT field length exceed 1 (ie. is not a phased haploid
			 * locus), this variant is unphased.*/
			Matcher m= Pattern.compile(PIPE).matcher(v.getGT());
			if (m.find()) {
				String[] gt= v.getGT().split(PIPE); // split on "|"
				int maternalGT= Integer.parseInt(gt[0]);
				int paternalGT= Integer.parseInt(gt[1]);
			
				String currentRsID= (String) v.getColumn(DBAnnotationColumns.DBSNP_TEXT);
				maternalGenotypes.put(currentRsID, new PGXGenotype(refAndAlts[maternalGT], false));
				paternalGenotypes.put(currentRsID, new PGXGenotype(refAndAlts[paternalGT], false));
			} else if (v.getGT().length() > 1) {
				pg.setUnphased();
			}
		}
		
		/* Update the gene object with the phased genotypes. */
		pg.setMaternalGenotypes(maternalGenotypes);
		pg.setPaternalGenotypes(paternalGenotypes);
	}
	
	
	/**
	 * Return a string of the diplotype for this gene-variants pair using star (*) nomenclature.
	 * @param pg the gene-variants pair object
	 * @param assumeRef if true and a marker is missing, assumes reference nucleotide. If false, marker is left blank.
	 * @return a String of the diplotype in the form "*1/*17" NOT with the gene
	 *	like "CYP2C19*1/*17"; null if this gene has no * diplotypes or if 
	 *  the genotypes are unphased.
	 */
	public static String getDiplotype(PGXGene pg, boolean assumeRef) throws PGXException, SQLException {	
		String diplotype= null;
		
		/* Assign the phased parental genotypes to be used for haplotype translation. */
		assignParentalGenotypes(pg);
		
		/* Continue only if the genotypes are phased. */
		if (pg.isPhased()) {
			Map<String, PGXGenotype> maternalGenotypes= pg.getMaternalGenotypes();
			Map<String, PGXGenotype> paternalGenotypes= pg.getPaternalGenotypes();

			/* Get the haplotypes and check for novel ones (ie. no match found).
			 * If a haplotype is novel, output "UNKNOWN" and then append the most
			 * similar haplotype. */
			pg.setMaternalHaplotype(getHaplotype(pg.getGene(), maternalGenotypes, assumeRef));
			String maternalHaplotype= new String(pg.getMaternalHaplotype()); // create a copy, since we're going to modify the string
			if (maternalHaplotype.equals(UNKNOWN_HAPLOTYPE)) {
				List<String> maternalSimilar= getSimilarHaplotypes(
					pg.getGene(), maternalGenotypes, SIMILAR_HAPLOTYPE_DEPTH);
				if (maternalSimilar.size() > 0) {
					maternalHaplotype += " (similar to " + StringUtils.join(maternalSimilar, ',') + ")";
				}
			}

			pg.setPaternalHaplotype(getHaplotype(pg.getGene(), paternalGenotypes, assumeRef));
			String paternalHaplotype= new String(pg.getPaternalHaplotype()); // create a copy, since we're going to modify the string
			if (paternalHaplotype.equals(UNKNOWN_HAPLOTYPE)) {
				List<String> paternalSimilar= getSimilarHaplotypes(
					pg.getGene(), paternalGenotypes, SIMILAR_HAPLOTYPE_DEPTH);
				if (paternalSimilar.size() > 0) {
					 paternalHaplotype += " (similar to " + StringUtils.join(paternalSimilar, ',') + ")";
				}
			}

			/* Create a list of diplotypes and sort these naturally/lexicographically
			 * so that diplotypes appear as "*1/*17" instead of "*17/*1". */
			List<String> haplotypes= new ArrayList<String>();
			haplotypes.add(maternalHaplotype);
			haplotypes.add(paternalHaplotype);
			Collections.sort(haplotypes, new NaturalOrderComparator());			
			diplotype= haplotypes.get(0) + "/" + haplotypes.get(1);
		}
		
		return diplotype;
	}
	
	
	/**
	 * Convert marker-genotype pairs into a * nomenclature haplotype for this gene.
	 * @param gene the gene name/symbol
	 * @param markerGenotypePairs a hash of marker-genotype pairs
	 * @param assumeRef if true and a marker is missing, assumes reference nucleotide. If false, marker is left blank.
	 * @return a string representing the * nomenclature haplotype for this hash, empty string if no haplotype found
	 * @throws PGXException
	 * @throws SQLException 
	 */
	private static String getHaplotype(String gene, Map<String, PGXGenotype> markerGenotypePairs, boolean assumeRef)
		throws PGXException, SQLException {
		
		Map<String, String> markerRef= getMarkerRefMap(gene);
		
		String sql=	"SELECT H.haplotype_symbol " +
					"FROM haplotype_markers H " +
					"WHERE gene = '" + gene + "' ";
		
		/* Iterate over the markers and construct a query for the local DB.
		 * Marker order doesn't affect the query. */
		for (String marker : getMarkers(gene)) {
			
			/* ***VERY IMPORTANT***
			 * If the marker was found, use the reported variant call. If it is
			 * NOT found, ASSUME it is a reference call, and append a ref call
			 * to the marker list "profile". Assuming it's a ref call is goverened
			 * by the argument assumeRef. Ideally, this would only be true for 
			 * WGS data, but it's a parameter that can be toggled regardless of 
			 * the data source. Adding reference call information ensures
			 * specificity. For example, if a marker is missing and it is one 
			 * marker off from being called a specific haplotype, it is not a
			 * match. Filling in missing markers with reference calls makes 
			 * this difference. */
			if (markerGenotypePairs.containsKey(marker)) {
				sql +=	"	AND marker_info LIKE '%" + marker + "=" + markerGenotypePairs.get(marker).getGenotype() +"%' ";
			} else if (assumeRef && markerRef.containsKey(marker)) { // some markers don't have a ref call, ignore for now
				sql +=	"	AND marker_info LIKE '%" + marker + "=" + markerRef.get(marker) +"%' ";
				
				// Add this marker to the list of inferred markers for this haplotype
				markerGenotypePairs.put(marker, new PGXGenotype(markerRef.get(marker), true));
			}
				
		}		
		
		/* Get all * alleles that can be retrieved with this query (>= 1). */
		List<String> allPossibleAlleles= new ArrayList<String>();
		try {
			ResultSet rs= PGXDB.executeQuery(sql);
			
			while (rs.next()) {
				// only grab the first column because we're only SELECTing it
				// above in the SQL statement
				allPossibleAlleles.add((String) PGXDB.getRowAsList(rs).get(0));
			}
		} catch (SQLException se) {
			se.printStackTrace();
		}
		
		/* Sort the list of haplotypes naturally/lexicographically. */
		Collections.sort(allPossibleAlleles, new NaturalOrderComparator());
		String haplotype= StringUtils.join(allPossibleAlleles, ',');
		if (haplotype.equals(""))
			haplotype= UNKNOWN_HAPLOTYPE;
		
		return haplotype;
	}
	
	
	/**
	 * Return a map of reference nucleotides keyed by marker ID, for this gene.
	 * @param gene The gene symbol
	 * @return a Map of reference nucleotides keyed by marker ID
	 */
	private static Map<String, String> getMarkerRefMap(String gene) throws PGXException, SQLException {
		Map<String, String> output= new HashMap<String, String>();
		
		List<String> markerList= getMarkers(gene);
		String sql;
		for (String marker : markerList) {
			sql=	"SELECT M.ref " +
					"FROM marker_coordinates M " +
					"WHERE M.marker = '" + marker + "' ";	
			ResultSet rs= PGXDB.executeQuery(sql);
			
			/* Just get the first line. According to the Java API:
			 * "A ResultSet cursor is initially positioned before the first row;
			 * the first call to the method next makes the first row the current row".
			 * NOTE: not all markers have corresponding coordinates or ref/alt calls. */
			if (rs.next())
				output.put(marker, (String) PGXDB.getRowAsList(rs).get(0));
		}
				
		return output;
	}
	
	
	/**
	 * Get the * nomenclature haplotypes that are most similar to the marker-genotype pairs.
	 * @param gene the gene name/symbol
	 * @param markerGenotypePairs a hash of marker-genotype pairs
	 * @param remove the maximum number of markers to remove to find similar haplotypes
	 * @return a List of possible haplotypes that are similar.
	 * @throws PGXException
	 * @throws SQLException 
	 */
	private static List<String> getSimilarHaplotypes(String gene, Map<String, PGXGenotype> markerGenotypePairs, int remove)
		throws PGXException, SQLException {
		
		/* Initialize the output list. */
		Set<String> similarAlleles= new HashSet<String>();
		
		/* Get the reference calls for all markers for this gene. */
		Map<String, String> markerRef= getMarkerRefMap(gene);

		
		/* Create a List of Pair objects from all the markers. I need a list 
		 * because that's what my sublists method takes. */
		List<Pair> originalMarkers= new ArrayList<Pair>();
		for (String marker : markerGenotypePairs.keySet()) {
			originalMarkers.add(new Pair(marker, markerGenotypePairs.get(marker).getGenotype()));
		}
		
		/* Create all possible sublists of this original marker list, iterating 
		 * through specified depth. Then try to find a most similar haplotype.
		 * This means that if the specified max number of markers to remove was 
		 * 3, first try to remove 1 and find a match, then try 2, then 3. If a 
		 * similar haplotype is found by removing 1 marker, it's more similar
		 * than those that can only be found after removing 3. */
		boolean found= false;
		for (int depth= 0; depth <= remove && !found; ++depth) {
			Set<List<Pair>> similarMarkers= Sublists.Sublists.sublists(originalMarkers, remove);
		
			/* For each of the sublists, look for a matching haplotype. */
			for (List<Pair> lop : similarMarkers) { // "lop" = "list of pairs"
				/* Create the query beginning. */
				String sql=	"SELECT H.haplotype_symbol " +
							"FROM haplotype_markers H " +
							"WHERE gene = '" + gene + "' ";
				
				// Convert the List<Pair> back to a Map<String, String>
				Map<String, String> sublistMap= convertListOfPairsToMap(lop);
				//List<String> sublistOmitted= getOmitted(markerGenotypePairs, sublistMap);
				
				/* Iterate over the markers and construct a query for the local DB.
				 * Marker order doesn't affect the query. */
				for (String marker : getMarkers(gene)) {

					/* This is where things start to get a little tricky. Unlike 
					 * in getHaplotype(), things need not be as precise when
					 * identifying a "similar" haplotype, since it is not intended
					 * to be an exact match. In this case, we start off with the
					 * list of variants but do not add any reference calls for
					 * missing markers. If nothing is identified in this "loose"
					 * matching query, remove a marker and try again. */				
					if (sublistMap.containsKey(marker)) {
						sql +=	"	AND marker_info LIKE '%" + marker + "=" + sublistMap.get(marker) +"%' ";
					//} else if (!sublistOmitted.contains(marker) && markerRef.containsKey(marker)) { // some markers don't have a ref call, ignore for now
					//	sql +=	"	AND marker_info LIKE '%" + marker + "=" + markerRef.get(marker) +"%' ";
					}
				}
				
				/* Get all * alleles that can be retrieved with this query (>= 1). */
				try {
					ResultSet rs= PGXDB.executeQuery(sql);

					while (rs.next()) {
						// only grab the first column because we're only SELECTing it
						// above in the SQL statement
						similarAlleles.add((String) PGXDB.getRowAsList(rs).get(0));
					}
				} catch (SQLException se) {
					se.printStackTrace();
				}
			}
			
			/* If this depth found a marker, stop search. Otherwise, continue. */
			if (similarAlleles.size() > 1) {
				found= true;
				System.out.println("Similar haplotype found at depth = " + depth); // Testing
			}
		}
		
		/* Sort the list of haplotypes naturally/lexicographically. */
		List<String> output= new ArrayList<String>();
		output.addAll(similarAlleles);
		Collections.sort(output, new NaturalOrderComparator());
		
		return output;	
	}	
	
	
	/**
	 * Get the activity for each haplotype.
	 * @param gene The gene symbol
	 * @param haplotype The haplotype symbol
	 * @return the activity value, null if it doesn't exist
	 * 
	 */
	public static String getActivities(String gene, String haplotype) {
		String activity= null;
		
		String sql=	"SELECT activity_phenotype " +
					"FROM haplotype_activity " +
					"WHERE gene = '" + gene + "' " +
					"	AND haplotype = '" + haplotype + "' ";
		
		/* There should only be a single activity value for this haplotype. */
		try {
			ResultSet rs= PGXDB.executeQuery(sql);

			while (rs.next()) {
				// only grab the first column because we're only SELECTing it
				// above in the SQL statement
				activity= (String) PGXDB.getRowAsList(rs).get(0);
			}
		} catch (SQLException se) {
			se.printStackTrace();
		}
		
		return activity;
	}
	
	
	/**
	 * Get the metabolizer class for a diplotype.
	 * @param hap1Activity Activity phenotype for haplotype 1; haplotype order is irrelevant
	 * @param hap2Activity Activity phenotype for haplotype 2; haplotype order is irrelevant
	 * @return the metabolizer class, null if it doesn't exist
	 * 
	 */
	public static String getMetabolizerClass(String hap1Activity, String hap2Activity) {
		String metabolizer= "unknown";
		
		String sql=	"SELECT metabolizer_class " +
					"FROM phenotype_to_metabolizer " +
					"WHERE (haplotype_1_activity = '" + hap1Activity + "' " +
					"	AND haplotype_2_activity = '" + hap2Activity + "') " +
					"	OR (haplotype_1_activity = '" + hap2Activity + "' " +
					"	AND haplotype_2_activity = '" + hap1Activity + "') ";
		
		/* There should only be a single activity value for this haplotype. */
		try {
			ResultSet rs= PGXDB.executeQuery(sql);

			while (rs.next()) {
				// only grab the first column because we're only SELECTing it
				// above in the SQL statement
				metabolizer= (String) PGXDB.getRowAsList(rs).get(0);
			}
		} catch (SQLException se) {
			se.printStackTrace();
		}
		
		return metabolizer;
	}
	
	
	/**
	 * Inner class to represent gene and marker pairs as a single object.
	 */
	private static class Pair {
		
		String genotype;
		String marker;
		
		public Pair(String marker, String genotype) {
			this.marker= marker;
			this.genotype= genotype;
		}
	}
	
	
	/**
	 * Convert a List<Pair> to Map<String, String> with marker as key genotype as value.
	 * Intended to be a private method for convenience.
	 */
	private static Map<String, String> convertListOfPairsToMap(List<Pair> lop) {
		Map<String, String> map= new HashMap<String, String>();
		for (Pair p : lop) {
			map.put(p.marker, p.genotype);
		}
		return map;
	}

	
	/**
	 * Get a List of markers that are absent from the sublist but present in the original.
	 */
	private static List<String> getOmitted(Map<String, String> original, Map<String, String> sublist) {
		List<String> omitted= new ArrayList<String>();
		for (String key : original.keySet()) {
			if (!sublist.containsKey(key))
				omitted.add(key);
		}
		return omitted;
	}
	
	
	/**
	 * Inner class to represent PGx markers.
	 */
	public static class PGXMarker {
		public String markerID;
		public String chromosome;
		public String position;
		public String ref;
		public String alt;
		
		public PGXMarker(String markerID, String chromosome, String position, String ref, String alt) {
			this.markerID= markerID;
			this.chromosome= chromosome;
			this.position= position;
			this.ref= ref;
			this.alt= alt;
		}
	}
	
	
	/**
	 * Return a list of all PGx markers, for this gene.
	 * @param gene The gene symbol
	 * @return a List of pgxMarker objects
	 */
	public static List<PGXMarker> getMarkerInfo(String gene) throws PGXException, SQLException {
		List<PGXMarker> output= new LinkedList<PGXMarker>();
		
		List<String> markerList= getMarkers(gene);
		Collections.sort(markerList); // sort the list of markers
		
		String sql;
		for (String marker : markerList) {
			sql=	"SELECT M.chromosome, M.position, M.ref, M.alt " +
					"FROM marker_coordinates M " +
					"WHERE M.marker = '" + marker + "' ";	
			ResultSet rs= PGXDB.executeQuery(sql);
			
			if (rs.next()) {
				List row= PGXDB.getRowAsList(rs);
				output.add(new PGXMarker(marker, (String) row.get(0),
					(String) row.get(1), (String) row.get(2), (String) row.get(3)));
			}
		}
				
		return output;
	}
	
	
	/**
	 * Return a list of Pubmed IDs for the specified genes.
	 * @param gene The gene symbol
	 * @return A list of the pmIDs. Empty list if gene not found.
	 */
	public static List<String> getPubMedIDs(String gene) {
		List<String> pmIDs= new ArrayList<String>();
		
		String sql;
		
		sql=	"SELECT DISTINCT(pubmed_id) " +
				"FROM haplotype_activity " +
				"WHERE gene = '" + gene + "' ";
		
		/* There should only be a single pubmed ID for this haplotype. */
		try {
			ResultSet rs= PGXDB.executeQuery(sql);

			while (rs.next()) {
				// only grab the first column because we're only SELECTing it
				// above in the SQL statement
				String pmidString= (String) PGXDB.getRowAsList(rs).get(0);
				pmIDs= Arrays.asList(pmidString.split(";"));
			}
		} catch (SQLException se) {
			se.printStackTrace();
		}
		
		return pmIDs;
	}
}
