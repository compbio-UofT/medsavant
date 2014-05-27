package org.ut.biolab.medsavant.shared.appdevapi;

/**
 * Stores the names of annotation columns so that users don't have to hard-code
 * any column names. All app column name changes should be made here and apps
 * should exclusively use these fields when referencing columns in the DB.
 * NOTE: These are IN ADDITION to the BasicVariantColumns columns. If the column
 * is missing here, it's in BasicVariantColumns
 * 
 * @author rammar
 */
public class DBAnnotationColumns {
	
	public static final String AF1000g= "1000g2012apr_all, Score";
	public static final String AF6500ex= "esp6500_all, Score";
	public static final String POLYPHEN2HUMVAR= "ljb23_pp2hvar, Score";
	public static final String SIFT= "ljb23_sift, Score";
	public static final String PHYLOP= "ljb23_phylop, Score";
	public static final String HGMD_RSID_TEXT= "hgmd_pro_allmut, dbsnp";
	public static final String HGMD_PMID_TEXT= "hgmd_pro_allmut, pmid";
	public static final String HGMD_OMIM_TEXT= "hgmd_pro_allmut, omimid";
	public static final String HGMD_DISEASE_TEXT= "hgmd_pro_allmut, disease";
	public static final String HGMD_DESCRIPTION_TEXT= "hgmd_pro_allmut, descr";
	public static final String HGMD_ACC_TEXT= "hgmd_pro_allmut, acc_num";
	public static final String HGMD_COMMENTS_TEXT= "hgmd_pro_allmut, comments";
	public static final String CLINVAR_RSID_TEXT= "Clinvar, rsID";
	public static final String CLINVAR_INFO_TEXT= "Clinvar, info";
	public static final String DBSNP_TEXT= "snp138, RSID";
	
}