package medsavant.pgx;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.ut.biolab.medsavant.shared.appdevapi.Variant;

/**
 * Stores a gene and variants associated with this gene
 * 
 * @author rammar
 */
public class PGXGene {
	
	private String gene;
	private List<Variant> variants;
	private String diplotype;
	private String maternalHaplotype;
	private String paternalHaplotype;
	private Map<String, String> maternalGenotypes;
	private Map<String, String> paternalGenotypes;
	
	
	/**
	 * Create a new PGXGeneAndVariants with a gene and it's variants.
	 * @param gene the gene symbol/name
	 */
	public PGXGene(String gene) {
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
	
	
	/**
	 * Set the diplotype of this gene.
	 * @param diplotype the String diplotype
	 */
	public void setDiplotype(String diplotype) {
		this.diplotype= diplotype;
	}
	
	
	/**
	 * Set the maternal haplotype.
	 * @param haplotype the String haplotype
	 */
	public void setMaternalHaplotype(String haplotype) {
		this.maternalHaplotype= haplotype;
	}
	
	
	/**
	 * Set the paternal haplotype.
	 * @param haplotype the String haplotype
	 */
	public void setPaternalHaplotype(String haplotype) {
		this.paternalHaplotype= haplotype;
	}
	
	
	/**
	 * Set the phased maternal genotypes.
	 * @param genotypesMap the Map of genotypes.
	 */
	public void setMaternalGenotypes(Map<String, String> genotypesMap) {
		this.maternalGenotypes= genotypesMap;
	}
	
	
	/**
	 * Set the phased paternal genotypes.
	 * @param genotypesMap the Map of genotypes.
	 */
	public void setPaternalGenotypes(Map<String, String> genotypesMap) {
		this.paternalGenotypes= genotypesMap;
	}
	
	
	/**
	 * Get the diplotype of this gene.
	 * @return the diplotype String
	 */
	public String getDiplotype() {
		return this.diplotype;
	}
	

	/**
	 * Get the maternal haplotype of this gene.
	 * @return the haplotype String
	 */
	public String getMaternalHaplotype() {
		return this.maternalHaplotype;
	}
	
	
	/**
	 * Get the paternal haplotype of this gene.
	 * @return the haplotype String
	 */
	public String getPaternalHaplotype() {
		return this.paternalHaplotype;
	}
	
	
	/**
	 * Get the phased maternal genotypes.
	 * @return the Map of genotypes.
	 */
	public Map<String, String> getMaternalGenotypes() {
		return this.maternalGenotypes;
	}
	
	
	/**
	 * Get the phased paternal genotypes.
	 * @return the Map of genotypes.
	 */
	public Map<String, String> getPaternalGenotypes() {
		return this.paternalGenotypes;
	}
}
