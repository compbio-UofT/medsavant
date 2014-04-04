package medsavant.pgx.localDB;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import medsavant.pgx.PGXException;

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
	
}
