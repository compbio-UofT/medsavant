package org.ut.biolab.medsavant.shared.appdevapi;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;

/**
 * Variant representation of individual variant rows from VariantManagerAdapter.getVariants(...).
 * 
 * @author rammar
 */
public class Variant {
	
	private static final String JANNOVAR_TYPE= "EFFECT";
	private static final String JANNOVAR_EFFECT= "HGVS";
	private static final String DP4= "DP4";
	private static final String AD= "AD";
	private static final String AO= "AO";
	private static final String DP= "DP";
	private static final String GT= "GT";
	private static final Pattern geneSymbolPattern= Pattern.compile("^([^:]+)");
	
	private Object[] row;
	private String chromosome;
	private String reference;
	private String alternate;
	private long start;
	private long end;
	private String zygosity;
	private int alternateDepth= -1;
	private int referenceDepth= -1;	
	private String geneSymbol;
	private String infoColumn;
	private String formatColumn;
	private String sampleInfoColumn;
	private String gtField; // Corresponds to "GT" in FORMAT column
	private String variantEffect;
	private String variantType;
	
	
	/**
	 * Variant object based on a row from the table.
	 * @param currentRow a single row from VariantManagerAdapter.getVariants(...) 
	 */
	public Variant(Object[] currentRow) {
		row= currentRow;
		chromosome= (String) row[BasicVariantColumns.INDEX_OF_CHROM];
		reference= (String) row[BasicVariantColumns.INDEX_OF_REF];
		alternate= (String) row[BasicVariantColumns.INDEX_OF_ALT];
		start= ((Integer) row[BasicVariantColumns.INDEX_OF_START_POSITION]).longValue();
		end= ((Integer) row[BasicVariantColumns.INDEX_OF_END_POSITION]).longValue();
		zygosity= (String) row[BasicVariantColumns.INDEX_OF_ZYGOSITY];
		infoColumn= (String) row[BasicVariantColumns.INDEX_OF_CUSTOM_INFO];
		formatColumn= extractFromInfoColumn(BasicVariantColumns.FORMAT.getAlias());
		sampleInfoColumn= extractFromInfoColumn(BasicVariantColumns.SAMPLE_INFO.getAlias());
	}
	
	
	/**
	 * Thrown when fields are not found within custom info, sample_info or
	 * format columns.
	 */
	private class FieldNotFoundException extends Exception {
		public FieldNotFoundException(String message) {
			super(message);
		}
	}
	
	
	/**
	 * Return the alternate allelic depth of coverage.
	 * @return the alternate allelic depth of coverage
	 */
	public int getAlternateDepth() {
		try {
			if (alternateDepth == -1) // not initialized yet
				extractCoverage();
		} catch (FieldNotFoundException fnfe) {
			System.err.println(fnfe.getMessage());
			fnfe.printStackTrace();
		}
		
		return alternateDepth;
	}
	
	
	/**
	 * Return the reference allelic depth of coverage.
	 * @return the reference allelic depth of coverage
	 */
	public int getReferenceDepth() {
		try {
			if (referenceDepth == -1) // not initialized yet
				extractCoverage();
		} catch (FieldNotFoundException fnfe) {
			System.err.println(fnfe.getMessage());
			fnfe.printStackTrace();
		}
		
		return referenceDepth;
	}
	
	
	/**
	 * Return the reference allele.
	 * @return the reference allele.
	 */
	public String getReference() {
		return reference;
	}
	
	
	/**
	 * Return the alternate allele.
	 * @return the alternate allele.
	 */
	public String getAlternate() {
		return alternate;
	}
	
	
	/**
	 * Return the chromosome for this variant
	 * @return the chromosome
	 */
	public String getChromosome() {
		return chromosome;
	}
	
	
	/**
	 * Return the start position for this variant.
	 * @return the start position
	 */
	public long getStart() {
		return start;
	}
	
	
	/**
	 * Return the first gene symbol for this variant.
	 * @return the gene symbol
	 */
	public String getGene() {
		if (geneSymbol == null)
			extractGene();
		
		return geneSymbol;
	}
	
	
	/**
	 * Get the GT field corresponding to the genotype and haplotype for this variant.
	 * @return the GT field value
	 */
	public String getGT() {
		try {
			if (gtField == null)
				extractFromFormatColumn(GT);
		} catch (FieldNotFoundException fnfe) {
			System.err.println(fnfe.getMessage());
			fnfe.printStackTrace();
		}
		
		return gtField;
	}
	
	
	/**
	 * Get the biological effect of this variant.
	 * @return biological effect of this variant
	 */
	public String getVariantEffect() {
		if (variantEffect == null)
			variantEffect= extractFromInfoColumn(JANNOVAR_EFFECT);
		
		return variantEffect;
	}
	
	
	/**
	 * Get the mutation category of this variant.
	 * @return the mutation type/category
	 */
	public String getVariantType() {
		if (variantType == null)
			variantType= extractFromInfoColumn(JANNOVAR_TYPE);
		
		return variantType;
	}
	
	
	/**
	 * Get value for a specific column.
	 * @param columnName the String name of this column (in the header)
	 * @return the value corresponding to this column
	 */
	public Object getColumn(String columnName) {
		int index= VariantIterator.header.indexOf(columnName);
		return row[index];
	}
	
	
	/**
	 * Get the full table row for this variant.
	 * @return row for this variant
	 */
	public Object[] getRow() {
		return row;
	}
	
	
	/**
	 * Get the table header.
	 * @return table header
	 */
	public List<String> getHeader() {
		return VariantIterator.header;
	}
			
	
	/**
	 * Extract field from the VCF CUSTOM_INFO column.
	 * @param key the key for the field within the info column
	 * @return the value corresponding to the key, null if key is absent
	 */
	private String extractFromInfoColumn(String key) {
		String regex= ";?" + key + "=([^;]+);?";
		Pattern keyPattern= Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher keyMatcher= keyPattern.matcher(infoColumn);
		
		String value= null;
		if (keyMatcher.find()) { // NOTE: need to run find() to get group() below
			keyMatcher.group(1);
		}
		
		return value;
	}
	
	
	/**
	 * Extract field from FORMAT and SAMPLE_INFO columns.
	 * @param key the key for the field within the FORMAT column
	 * @return the value corresponding to the key from the SAMPLE_INFO column, null if key is absent
	 */
	private String extractFromFormatColumn(String key) throws FieldNotFoundException {
		List<String> formatKeys= Arrays.asList(formatColumn.split(":"));
		int index= formatKeys.indexOf(key);
		List<String> sampleInfoKeys= Arrays.asList(sampleInfoColumn.split(":"));
		
		// Check that format and sample info columns have same number of fields
		if (formatKeys.size() != sampleInfoKeys.size()) {
			throw new FieldNotFoundException("FORMAT and SAMPLE_INFO columns "+
				"have a different number of fields; should be identical size.");
		}
		
		String value= null;
		if (index != -1) {
			value= sampleInfoKeys.get(index);
		}
		
		return value;
	}
	
	
	/**
	 * Extract the gene symbol for this variant.
	 */
	private void extractGene() {
		Matcher geneMatcher= geneSymbolPattern.matcher(infoColumn);
		if (geneMatcher.find()) {
			geneSymbol= geneMatcher.group(1);
		}
	}
	
	
	/** 
	 * Extracts the reference and alternate allelic coverage for this variant.
	 */
	private void extractCoverage() throws FieldNotFoundException {		
		String dp4Text= extractFromInfoColumn(DP4);
		String adText= extractFromFormatColumn(AD);
		String aoText= extractFromFormatColumn(AO);
		String dpText= extractFromFormatColumn(DP);
		
		/* Process DP4 or AD or AO and DP text (from VCF INFO or Format columns) if present. */
		if (dp4Text != null) {
			/* From the samtools definition of the DP4 field:
			 * Number of: 
			 * 1) forward ref alleles; 
			 * 2) reverse ref; 
			 * 3) forward non-ref; 
			 * 4) reverse non-ref alleles, used in variant calling. 
			 * Sum can be smaller than DP because low-quality bases are not counted.
			 * 
			 * URL: http://samtools.sourceforge.net/mpileup.shtml
			 */
			String[] delimited= dp4Text.split(",");
			referenceDepth= Integer.parseInt(delimited[0]) + Integer.parseInt(delimited[1]);
			alternateDepth= Integer.parseInt(delimited[2]) + Integer.parseInt(delimited[3]);
			
		} else if (adText != null) {
			String[] adCoverageDelimited= adText.split(",");
			referenceDepth= Integer.parseInt(adCoverageDelimited[0]);
			alternateDepth= Integer.parseInt(adCoverageDelimited[1]);
			
		} else if (aoText != null && dpText != null) {
			int totalCount= Integer.parseInt(dpText);
			String[] aoCoverageDelimited= aoText.split(",");
			
			////////////// FIX WHEN DB IS UPDATED TO NEW FORMAT - deals with multiple alleles/vcf line
			System.err.println("Fix covereage BUG here: " + this.getClass().getSimpleName());
			/* Sometimes the AO count can be comma separated for multiple alternate
			 * alleles. In this case, due to the way we import these in MedSavant,
			 * I don't know which allele corresponds to which depth, so take the
			 * minimum. This may have to be modified later. */		
			int[] aoCoverageInt= new int[aoCoverageDelimited.length];
			for (int i= 0; i != aoCoverageInt.length; ++i) {
				aoCoverageInt[i]= Integer.parseInt(aoCoverageDelimited[i]);
			}
			Arrays.sort(aoCoverageInt); // sort the array in ascending order
			
			alternateDepth= aoCoverageInt[0];
			referenceDepth= totalCount - alternateDepth;
		}
	}
	
}