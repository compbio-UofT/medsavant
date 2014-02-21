/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package medsavant.secondary.localDB;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Assorted DB functions.
 * @author rammar
 */
public class DiscoveryDBFunctions {
	
	private static Map<String, String> zygosityMap;
	
	/**
	 * For variant DB lookup - Assign zygosity values based on VariantRecord in org.ut.biolab.medsavant.shared.vcf.
	 */
	private static void initZygosityMap() {
		if (zygosityMap == null) {
			zygosityMap= new HashMap<String, String>();
			zygosityMap.put("HomoAlt", "hom");
			zygosityMap.put("Hetero", "het");
		}
	}
	
	
	/** Get the variant classification.
	 * Variant can be classified as: disease, complex, potential compound het
	 * or carrier.
	 * @param geneSymbol The gene symbol string
	 * @param zygosity zygosity string (either "Hetero" or "HomoAlt", in accordance with MedSavant syntax)
	 * @param panel Currently limited to "ACMG" or ""
	 * @param gender gender string (either "m" or "f")
	 * @return a list containing the disease classification and inheritance
	 */
	public static List<String> getClassification(String geneSymbol, String zygosity, String panel, String gender) {
		initZygosityMap();
		
		String classification= null;
		String inheritance= null;
		String queryAddition= "";
		
		if (panel.equals("ACMG")) {
			queryAddition= "AND C.gene in (SELECT gene FROM acmg) ";
		}
		
		// MUST use single quotes for HyperSQL (hsql) SQL syntax
		String sql=	"SELECT D.classification, S.synonym, C.* " +
					"FROM CGD C, disease_classification D, CGD_synonym S " +
					"WHERE C.gene LIKE '" + geneSymbol + "' " + 
					queryAddition +
					"	AND C.inheritance = S.inheritance " +
					"	AND S.synonym = D.inheritance " +
					"	AND D.zygosity LIKE '" + zygosityMap.get(zygosity) + "' " +
					"	AND (D.gender LIKE '" + gender + "' OR D.gender LIKE 'both') ";
		
		ResultSet rs;
		try {
			rs= DiscoveryDB.executeQuery(sql);
		
			int rowCount= 0;
			while (rs.next()) {
				/* There should only be a single result from this query. */
				if (rowCount > 1)
					System.err.println(">1 row found for query: " + sql);
				
				/* First and only element is the inheritance, as specified in sql above. */
				List temp= DiscoveryDB.getRowAsList(rs);
				classification= (String) temp.get(0);
				inheritance= (String) temp.get(1);
				
				rowCount++;
			}
		} catch (SQLException e) {
			System.out.println("This was just executed: " + sql);
			e.printStackTrace();
		}
		
		
		List<String> output= new ArrayList<String>();
		output.add(classification);
		output.add(inheritance);
		
		return output;
	}
	
	
	/** 
	 * Get the gene's associated condition from CGD.
	 * @param geneSymbol the gene symbol string
	 * @return a String describing the condition associated with this gene, if present. Null otherwise.
	 */
	public static String getDisease(String geneSymbol) {
		String disease= null;
		
		// MUST use single quotes for HyperSQL (hsql) SQL syntax
		String sql=	"SELECT C.condition " +
					"FROM CGD C " +
					"WHERE C.gene = '" + geneSymbol + "' "; 
		
		ResultSet rs;
		try {
			rs= DiscoveryDB.executeQuery(sql);
		
			int rowCount= 0;
			while (rs.next()) {
				/* There should only be a single result from this query. */
				if (rowCount > 1)
					System.err.println(">1 row found for query: " + sql);
				
				/* First and only element is the condition/disease, as specified in sql above. */
				List temp= DiscoveryDB.getRowAsList(rs);
				disease= (String) temp.get(0);
				
				rowCount++;
			}
		} catch (SQLException e) {
			System.err.println("This was just executed: " + sql);
			e.printStackTrace();
		}
		
		return disease;
	}
}
