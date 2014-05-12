package medsavant.pgx;

/**
 * Simple class to store genotypes and the status of the genotype, including whether it was
 * directly measured or inferred from missing data. For example, most reference calls 
 * (if not all) are inferred when no genotype is present in a VCF file.
 * 
 * @author rammar
 */
public class PGXGenotype {
	
	private String genotype;
	private boolean isInferred;
	
	
	/**
	 * Create the genotype.
	 * @param genotype The genotype String
	 * @param isInferred true if inferred, false if measured directly from seq output
	 */
	public PGXGenotype(String genotype, boolean isInferred) {
		this.genotype= genotype;
		this.isInferred= isInferred;
	}
	
	
	/**
	 * Get the genotype String.
	 * @return the genotype String
	 */
	public String getGenotype() {
		return this.genotype;
	}
	
	
	/**
	 * Return true if genotype is inferred; false if directly measure from seq output.
	 * @return true if genotype is inferred; false if directly measure from seq output.
	 */
	public boolean getInferredStatus() {
		return this.isInferred;
	}
}
