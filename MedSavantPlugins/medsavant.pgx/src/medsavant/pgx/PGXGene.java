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
	private Map<String, PGXGenotype> maternalGenotypes;
	private Map<String, PGXGenotype> paternalGenotypes;
	private String maternalActivity;
	private String paternalActivity;
	private boolean isPhased= true; // default is phased
	private String metabolizerClass;
	private List<Variant> novelVariants;
	
	
	/**
	 * Create a new PGXGeneAndVariants with a gene and it's variants.
	 * @param gene the gene symbol/name
	 */
	public PGXGene(String gene) {
		this.gene= gene;
		this.variants= new LinkedList<Variant>();
		this.novelVariants= new LinkedList<Variant>();
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
	 * @precondition the key is an rsID, the value is the genotype
	 */
	public void setMaternalGenotypes(Map<String, PGXGenotype> genotypesMap) {
		this.maternalGenotypes= genotypesMap;
	}
	
	
	/**
	 * Set the phased paternal genotypes.
	 * @param genotypesMap the Map of genotypes.
	 * @precondition the key is an rsID, the value is the genotype
	 */
	public void setPaternalGenotypes(Map<String, PGXGenotype> genotypesMap) {
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
	 * The key is an rsID, the value is the genotype.
	 * @return the Map of genotypes.
	 */
	public Map<String, PGXGenotype> getMaternalGenotypes() {
		return this.maternalGenotypes;
	}
	
	
	/**
	 * Get the phased paternal genotypes.
	 * The key is an rsID, the value is the genotype.
	 * @return the Map of genotypes.
	 */
	public Map<String, PGXGenotype> getPaternalGenotypes() {
		return this.paternalGenotypes;
	}
	
	
	/**
	 * Set this gene's genotypes as unphased. They are marked as phased by default.
	 */
	public void setUnphased() {
		this.isPhased= false;
	}
	
	
	/**
	 * Find out if this gene's genotypes are phased.
	 * @return true is phased, false otherwise
	 */
	public boolean isPhased() {
		return this.isPhased;
	}
	
	
	/**
	 * Set maternal activity.
	 * @param activity the activity string
	 */
	public void setMaternalActivity(String activity) {
		this.maternalActivity= activity;
	}
	
	
	/**
	 * Set paternal activity.
	 * @param activity the activity string
	 */
	public void setPaternalActivity(String activity) {
		this.paternalActivity= activity;
	}
	
	
	/** 
	 * Get the maternal activity.
	 * @return the activity string
	 */
	public String getMaternalActivity() {
		return this.maternalActivity;
	}
	
	
	/** 
	 * Get the paternal activity.
	 * @return the activity string
	 */
	public String getPaternalActivity() {
		return this.paternalActivity;
	}
	
	
	/**
	 * Set the metabolizer class.
	 * @param metabolizer The metabolizer class string
	 */
	public void setMetabolizerClass(String metabolizer) {
		this.metabolizerClass= metabolizer;
	}
	
	
	/**
	 * Get the metabolizer class.
	 * @return The metabolizer class string
	 */
	public String getMetabolizerClass() {
		return this.metabolizerClass;
	}
	
	
	/**
	 * Get the list of novel Variants.
	 * @return the list of novel Variants
	 */
	public List<Variant> getNovelVariants() {
		return this.novelVariants;
	}
	
	
	/**
	 * Add a new variant to the list of novel Variants.
	 * @param novelVar the new variant
	 */
	public void addNovelVariant(Variant novelVar) {
		novelVariants.add(novelVar);
	}
}