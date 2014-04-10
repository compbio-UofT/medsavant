package medsavant.pgx;

import java.util.LinkedList;
import java.util.List;
import org.ut.biolab.medsavant.shared.appdevapi.Variant;

/**
 * Stores a gene and variants associated with this gene
 * 
 * @author rammar
 */
public class PGXGeneAndVariants {
	
	private String gene;
	private List<Variant> variants;
	
	
	/**
	 * Create a new PGXGeneAndVariants with a gene and it's variants.
	 * @param gene the gene symbol/name
	 */
	public PGXGeneAndVariants(String gene) {
		this.gene= gene;
		this.variants= new LinkedList<Variant>();
	}
	
	
	/**
	 * Get the gene symbol.
	 * @return the gene symbol
	 */
	public String getGene() {
		return this.gene;
	}
	
	
	/**
	 * Get the list of Variants.
	 * @return the list of Variants
	 */
	public List<Variant> getVariants() {
		return this.variants;
	}
	
	
	/**
	 * Add a new variant to the list of Variants.
	 * @param var the new variant
	 */
	public void addVariant(Variant var) {
		variants.add(var);
	}
	
}
