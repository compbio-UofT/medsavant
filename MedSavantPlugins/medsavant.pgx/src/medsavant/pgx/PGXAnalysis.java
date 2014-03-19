package medsavant.pgx;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import java.util.LinkedList;
import java.util.List;
import org.ut.biolab.medsavant.shared.appdevapi.Variant;

/**
 * Performs a pharmacogenomic analysis for this individual.
 * 
 * @author rammar
 */
public class PGXAnalysis {
	
	private static ComboCondition standardCondition= buildCondition();
	
	private String dnaID;
	private List<Variant> pgxVariants= new LinkedList<Variant>();
	
	/**
	 * Initiate a pharacogenomic analysis.
	 * @param dnaID the DNA ID for this individual
	 */
	public PGXAnalysis(String dnaID) {
		this.dnaID= dnaID;
		getVariants();
	}
	
	
	/**
	 * Build the standard pharmacogenomic condition to be used when retrieving 
	 * variants for any patient's analysis.
	 * @return the ComboCondition to be used for all analyses
	 */
	private static ComboCondition buildCondition() {
		ComboCondition output;
		
		/*
		 * This method will have to go to the local HSQL DB and retrieve the 
		 * markers and their positions in the genome. These will have to be put
		 * together into a combo condition that is OR-ed together to find all
		 * variants that are relevant to our pgx genes of interest.
		 */
		
		return output;		
	}
	
	
	/**
	 * Get all pharmacogenomic variants for this individual.
	 */
	private void getVariants() {
		/*
		 * Take the standard combocondition and AND it to the DNA ID for this
		 * individual before submitting for variants.
		 * For each query, a VariantIterator will be returned. Iterate while
		 * this object hasNext() and store the Variant objects in an instance
		 * variable of List<Variant>
		 */
	}
	
	
	
	
}
