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
	
	private static final String JANNOVAR_EFFECT= BasicVariantColumns.JANNOVAR_EFFECT.getColumnName();
	private static final String JANNOVAR_SYMBOL= BasicVariantColumns.JANNOVAR_SYMBOL.getColumnName();
	private static final String DP4= "DP4";
	private static final String AD= "AD";
	private static final String AO= "AO";
	private static final String DP= "DP";
	private static final String GT= "GT";
	private static final Pattern geneSymbolPattern= Pattern.compile("^([^:]+)");
	private static final String VCF_MISSING_VALUE= ".";
	
	private Object[] row;
	private String chromosome;
	private String reference;
	private String alternate;
	private int alternateNumber;
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
		alternateNumber= ((Integer) row[BasicVariantColumns.INDEX_OF_ALT_NUMBER]).intValue();
		start= ((Integer) row[BasicVariantColumns.INDEX_OF_START_POSITION]).longValue();
		end= ((Integer) row[BasicVariantColumns.INDEX_OF_END_POSITION]).longValue();
		zygosity= (String) row[BasicVariantColumns.INDEX_OF_ZYGOSITY];
		infoColumn= (String) row[BasicVariantColumns.INDEX_OF_CUSTOM_INFO];
		formatColumn= extractFromInfoColumn(BasicVariantColumns.FORMAT.getColumnName());
		sampleInfoColumn= extractFromInfoColumn(BasicVariantColumns.SAMPLE_INFO.getColumnName());
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
	 * Return the alternate allele number. The first alternate allele is 1, the 
	 * second is 2, etc.
	 * @return the alternate allele number
	 */
	public int getAlternateNumber() {
		return alternateNumber;
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
	 * Return the end position for this variant.
	 * @return the end position
	 */
	public long getEnd() {
		return end;
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
				gtField= extractFromFormatColumn(GT);
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
	public String getMutationSymbols() {
		if (variantEffect == null)
			variantEffect= extractFromInfoColumn(JANNOVAR_SYMBOL);
		
		return variantEffect;
	}
	
	
	/**
	 * Get the mutation category of this variant.
	 * @return the mutation type/category
	 */
	public String getMutationType() {
		if (variantType == null)
			variantType= extractFromInfoColumn(JANNOVAR_EFFECT);
		
		return variantType;
	}
	
	
	/**
	 * Get the zygosity of this variant.
	 * @return the zygosity String
	 */
	public String getZygosity() {
		return zygosity;
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
	public String extractFromInfoColumn(String key) {
		String regex= ";?" + key + "=([^;]+);?";
		Pattern keyPattern= Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher keyMatcher= keyPattern.matcher(infoColumn);
		
		String value= null;
		if (keyMatcher.find()) { // NOTE: need to run find() to get group() below
			value= keyMatcher.group(1);
		}
		
		return value;
	}
	
	
	/**
	 * * Extract field from FORMAT and SAMPLE_INFO columns.
	 * @param key the key for the field within the FORMAT column
	 * @return the value corresponding to the key from the SAMPLE_INFO column, null if key is absent
	 * @throws org.ut.biolab.medsavant.shared.appdevapi.Variant.FieldNotFoundException 
	 *	if the number of format columns differs from the sample_info columns, which is
	 *	acceptible by VCF 4.1 spec. the only field that has to be present of all the
	 *	format columns is the GT field; all others can be missing.
	 */
	public String extractFromFormatColumn(String key) throws FieldNotFoundException {
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
		String geneString= extractFromInfoColumn(JANNOVAR_SYMBOL);
		Matcher geneMatcher= geneSymbolPattern.matcher(geneString);
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
			
		} else if (adText != null && adText != VCF_MISSING_VALUE) {
			String[] adCoverageDelimited= adText.split(",");
			referenceDepth= Integer.parseInt(adCoverageDelimited[0]);
			alternateDepth= Integer.parseInt(adCoverageDelimited[1]);
			
		} else if (aoText != null && dpText != null && aoText != VCF_MISSING_VALUE && dpText != VCF_MISSING_VALUE) {
			int totalCount= Integer.parseInt(dpText);
			String[] aoCoverageDelimited= aoText.split(",");
			
			// UPDATE WHEN DB IS UPDATED TO NEW FORMAT - deals with multiple alleles/vcf line
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