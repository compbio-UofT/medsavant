package medsavant.pgx;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import medsavant.pgx.localDB.PGXDB;
import medsavant.pgx.localDB.PGXDBFunctions;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.shared.appdevapi.Variant;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.format.AnnotationFormat;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.format.CustomField;

/**
 * Performs a pharmacogenomic analysis for this individual.
 * 
 * @author rammar
 */
public class PGXAnalysis {
	
	private static final String DBSNP_COLUMN= "snp137, RSID";
	
	private static Connection pgxdbConn;
	private static TableSchema ts= ProjectController.getInstance().getCurrentVariantTableSchema();
	private static ComboCondition standardPGXCondition= buildCondition();
	private static Map<String, String> columns= getDbToHumanReadableMap();
	
	private String dnaID;
	private List<Variant> pgxVariants= new LinkedList<Variant>();
	
	/**
	 * Initiate a pharacogenomic analysis.
	 * @param dnaID the DNA ID for this individual
	 */
	public PGXAnalysis(String dnaID) throws SQLException {
		this.dnaID= dnaID;
		
		/* If no connection exists, initialize the local HyperSQL database for 
		 * analyses. */
		if (pgxdbConn == null) {
			PGXDB.initialize();
			pgxdbConn= PGXDB.getConnection();
		}
		
		getVariants();
	}
	
	
	/**
	 * Build the standard pharmacogenomic condition to be used when retrieving 
	 * variants for any patient's analysis.
	 * @return the ComboCondition to be used for all analyses
	 */
	private static ComboCondition buildCondition() {
		ComboCondition query= new ComboCondition(ComboCondition.Op.OR);
		
		// get all relevant genes
		List<String> genes= new LinkedList<String>();
		try {
			genes= PGXDBFunctions.getGenes();
		} catch (SQLException se) {
			se.printStackTrace();
		}
		
		// get all relevant markers
		List<String> markers= new LinkedList<String>();
		try {
			for (String g : genes) {
				markers.addAll(PGXDBFunctions.getMarkers(g));
			}
 		} catch (Exception pe) {
			pe.printStackTrace();
		}
		
		
		// add all markers to the ComboCondition
		// NOTE: this is hardcoded for now, but will need to be changed if the
		// dbSNP annotation DB is updated
		for (String m : markers) {
			query.addCondition(
				BinaryCondition.equalTo(ts.getDBColumn(columns.get(DBSNP_COLUMN)), m));
		}
		
		return query;		
	}
	
	
	/**
	 * Get all pharmacogenomic variants for this individual.
	 */
	private void getVariants() {
		ComboCondition query= new ComboCondition(ComboCondition.Op.AND);
		
		/* Take the standard combocondition and AND it to the DNA ID for this
		 * individual before submitting for variants. */		
		query.addCondition(
			BinaryCondition.equalTo(ts.getDBColumn(BasicVariantColumns.DNA_ID), dnaID));
		query.addCondition(standardPGXCondition);
				
		/* For each query, a VariantIterator will be returned. Iterate while
		 * this object hasNext() and store the Variant objects in an instance
		 * variable of List<Variant>
		 */
	}
	
	
	/** 
	 * Get the header for the table using the column aliases.
	 * @return a map of the column aliases to column names.
	 */
	public static Map<String, String> getDbToHumanReadableMap() {
		Map<String, String> dbAliasToNameMap = new HashMap<String, String>();

		try {
			AnnotationFormat[] afs = ProjectController.getInstance().getCurrentAnnotationFormats();
			for (AnnotationFormat af : afs) {
				for (CustomField field : af.getCustomFields()) {
					dbAliasToNameMap.put(field.getAlias(), field.getColumnName());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return dbAliasToNameMap;
	}
	
}
