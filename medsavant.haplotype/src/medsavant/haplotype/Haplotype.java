package medsavant.haplotype;

/**
 * Perform genotype phasing to determine haplotypes and merge these results
 * with the input VCF to output a phased/partially phased VCF.
 * 
 * @author rammar
 */
public class Haplotype {
	
	private static String inputVCFName;
	private static String referencePanelVCFName;
	private static String tempDirectoryName;
	
	/**
	 * Testing method.
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		// sample argument call:
		// java -jar medsavant.haplotype.jar WGS-001-03.gatk.snp.indel.jv.vcf test_30k_pgx_subset_output.vcf .
		inputVCFName= args[0];
		referencePanelVCFName= args[1];
		tempDirectoryName= args[2];
		
		BEAGLEWrapper bw= new BEAGLEWrapper(tempDirectoryName, inputVCFName, referencePanelVCFName);
	}

}
