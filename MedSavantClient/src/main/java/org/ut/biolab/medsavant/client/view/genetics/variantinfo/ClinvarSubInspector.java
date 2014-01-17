package org.ut.biolab.medsavant.client.view.genetics.variantinfo;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.client.view.genetics.inspector.SubInspector;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 * Clinvar SubInspector to display variant details.
 * 
 * @author rammar
 */
public class ClinvarSubInspector extends SubInspector {
	
	private static final String URL_CHARSET = "UTF-8";
	private final String CLINVAR_RSID_TEXT= "clinvar_20131105b, rsID";
	private final String CLINVAR_INFO_TEXT= "clinvar_20131105b, info";
	private final String baseDBSNPUrl= "http://www.ncbi.nlm.nih.gov/SNP/snp_ref.cgi?searchType=adhoc_search&rs=";
	private final String baseClinvarUrl= "http://www.ncbi.nlm.nih.gov/clinvar/";
	private final String baseOMIMUrl= "http://www.omim.org/entry/";
	
	private final int KEY_VALUE_PAIR_PANEL_ADDITIONAL_COLUMN_NUMBER= 4;
	private final String KEY_CLINVAR_RSID= "dbSNP ID";
	private final String KEY_CLINVAR_SIG= "Clinical Significance";
	private final String KEY_CLINVAR_SRC= "OMIM Allelic Variant ID";
	private final String KEY_CLINVAR_DSDB= "OMIM Disease ID";
	private final String KEY_CLINVAR_DBN= "Disease Name";
	private final String KEY_CLINVAR_ACC= "Clinvar Accession";
	
	private Object[] currentLine;
	private int CLINVAR_RSID_INDEX;
	private int CLINVAR_INFO_INDEX;
	private KeyValuePairPanel p;
	
	// Clinvar info field properties
	private String rsID;
	private String clnSig;
	private String omimID;
	private String omimAllelicVariantID;
	private String disease;
	private String accession;
	
	private Map variantClinicalSignificance= new HashMap<String, String>();
	
	public ClinvarSubInspector() {
		initializeSignificance();
    }

    @Override
    public String getName() {
        return "Clinvar Information";
    }
	
	@Override
    public JPanel getInfoPanel() {
		if (p == null) {
			p = new KeyValuePairPanel(KEY_VALUE_PAIR_PANEL_ADDITIONAL_COLUMN_NUMBER);
			p.addKey(KEY_CLINVAR_RSID);
			p.addKey(KEY_CLINVAR_SIG);
			p.addKey(KEY_CLINVAR_DBN);
			p.addKey(KEY_CLINVAR_SRC);
			p.addKey(KEY_CLINVAR_DSDB);
			p.addKey(KEY_CLINVAR_ACC);
		}
		return p;
	}
	
	
	/**
	 * Initialize the map of variant clinical significance.
	 * These values were obtained from the Clinvar header (in the 20131105b VCF file).
	 */
	private void initializeSignificance() {
		variantClinicalSignificance.put("0", "unknown");
		variantClinicalSignificance.put("1", "untested");
		variantClinicalSignificance.put("2", "non-pathogenic");
		variantClinicalSignificance.put("3", "probable-non-pathogenic");
		variantClinicalSignificance.put("4", "probable-pathogenic");
		variantClinicalSignificance.put("5", "pathogenic");
		variantClinicalSignificance.put("6", "drug-response");
		variantClinicalSignificance.put("7", "histocompatibility");
		variantClinicalSignificance.put("255", "other");
	}
	
	
	/**
	 * Sets the current variant line from the table for this subinspector
	 * @param line the current line from the table (unedited and unsliced)
	 * @param header the header for the table (unedited and unsliced)
	 */
	public void setVariantLine(Object[] line, List<String> header) {
		currentLine= line;
		CLINVAR_RSID_INDEX= header.indexOf(CLINVAR_RSID_TEXT);
		CLINVAR_INFO_INDEX= header.indexOf(CLINVAR_INFO_TEXT);
		
		
		// Extract Clinvar details from table
		if (currentLine[CLINVAR_RSID_INDEX] != null)
			rsID= (String) currentLine[CLINVAR_RSID_INDEX];
		else
			rsID= "";
		
		if (currentLine[CLINVAR_INFO_INDEX] != null)
			parseClinvarInfo((String) currentLine[CLINVAR_INFO_INDEX]);
		else
			resetClinvarFields();
		
		
		// Create inspector table
		int buttonNumber;
		
		p.setValue(KEY_CLINVAR_RSID, rsID);
		buttonNumber= 0;
		p.setAdditionalColumn(KEY_CLINVAR_RSID, buttonNumber++, KeyValuePairPanel.getCopyButton(KEY_CLINVAR_RSID, p));
		p.setAdditionalColumn(KEY_CLINVAR_RSID, buttonNumber++, 
			getKeyValuePairPanelButton(KEY_CLINVAR_RSID, baseDBSNPUrl, p.getValue(KEY_CLINVAR_RSID), true));
		
		String clnSigTranslation= "";
		if (variantClinicalSignificance.containsKey(clnSig))
			clnSigTranslation= (String) variantClinicalSignificance.get(clnSig);
		p.setValue(KEY_CLINVAR_SIG, clnSigTranslation);
		
		p.setValue(KEY_CLINVAR_DBN, disease);
		
		p.setValue(KEY_CLINVAR_ACC, accession);
		// get the accession root for the URL
		Matcher m= Pattern.compile("([^\\.]+)\\.").matcher(accession);
		String accessionRoot= "";
		if (m.find())
			accessionRoot= m.group(1);
		buttonNumber= 0;
		p.setAdditionalColumn(KEY_CLINVAR_ACC, buttonNumber++, KeyValuePairPanel.getCopyButton(KEY_CLINVAR_ACC, p));
		p.setAdditionalColumn(KEY_CLINVAR_ACC, buttonNumber++, 
			getKeyValuePairPanelButton(KEY_CLINVAR_ACC, baseClinvarUrl, accessionRoot, true));
		
		p.setValue(KEY_CLINVAR_SRC, omimAllelicVariantID);
		String omimAllelicVariantID_url= omimAllelicVariantID.replaceAll("\\.", "#");		
		buttonNumber= 0;
		p.setAdditionalColumn(KEY_CLINVAR_SRC, buttonNumber++, KeyValuePairPanel.getCopyButton(KEY_CLINVAR_SRC, p));
		p.setAdditionalColumn(KEY_CLINVAR_SRC, buttonNumber++, 
			getKeyValuePairPanelButton(KEY_CLINVAR_SRC, baseOMIMUrl, omimAllelicVariantID_url, false));
		
		p.setValue(KEY_CLINVAR_DSDB, omimID);
		buttonNumber= 0;
		p.setAdditionalColumn(KEY_CLINVAR_DSDB, buttonNumber++, KeyValuePairPanel.getCopyButton(KEY_CLINVAR_DSDB, p));
		p.setAdditionalColumn(KEY_CLINVAR_DSDB, buttonNumber++, 
			getKeyValuePairPanelButton(KEY_CLINVAR_DSDB, baseOMIMUrl, omimID, true));
	}
	
	
	/**
	 * Parses the clinvar info text and assigns values to the relevant instance
	 * variables.
	 * @param clinvarInfoText The clinvar info text field
	 */
	private void parseClinvarInfo(String clinvarInfoText) {
		resetClinvarFields();
		
		/* Extract the variant clinical significance */
		Pattern clnSigPattern= Pattern.compile(";?CLNSIG=([^;]+);?", Pattern.CASE_INSENSITIVE);
		Matcher clnSigMatcher= clnSigPattern.matcher(clinvarInfoText);
		if (clnSigMatcher.find())
			clnSig= clnSigMatcher.group(1);
		
		/* Extract the variant disease name */
		Pattern diseasePattern= Pattern.compile(";?CLNDBN=([^;]+);?", Pattern.CASE_INSENSITIVE);
		Matcher diseaseMatcher= diseasePattern.matcher(clinvarInfoText);
		if (diseaseMatcher.find())
			disease= diseaseMatcher.group(1);
		// Convert all "_" to spaces, "|" to "; " and "\x2c" to ","
		disease= disease.replaceAll("_", " ");
		disease= disease.replaceAll("\\|", "; ");
		disease= disease.replaceAll("\\x2c", ", ");
		if (disease.length() > 15)
			disease= disease.substring(0, 15) + "..."; // TESTING for now
		
		
		/* Extract the Clivar accession */
		Pattern accessionPattern= Pattern.compile(";?CLNACC=([^;]+);?", Pattern.CASE_INSENSITIVE);
		Matcher accessionMatcher= accessionPattern.matcher(clinvarInfoText);
		// Assign the first accession, if there is a list
		if (accessionMatcher.find()) {
			String s= accessionMatcher.group(1);
			List<String> l= Arrays.asList(s.split("\\|"));
			accession= l.get(0); // get the first accession, if there is > 1
		}
		
		/* Extract the OMIM allelic variant ID */
		Matcher omimAllelicMatcher= Pattern.compile(";?CLNSRC=([^;]+);?", Pattern.CASE_INSENSITIVE).matcher(clinvarInfoText);
		if (omimAllelicMatcher.find()) {
			String s= omimAllelicMatcher.group(1);
			List<String> l= Arrays.asList(s.split(":"));
			int index= l.indexOf("OMIM_Allelic_Variant");
			
			if (index != -1) { //only match if found
				Matcher omimAllelicVariantIDMatcher= 
					Pattern.compile(";?CLNSRCID=([^;]+);?", Pattern.CASE_INSENSITIVE).matcher(clinvarInfoText);
				if (omimAllelicVariantIDMatcher.find()) {
					String s2= omimAllelicVariantIDMatcher.group(1);
					List<String> l2= Arrays.asList(s2.split(":"));
					omimAllelicVariantID= l2.get(index);
				}
			}			
		}
		
		/* Extract the OMIM ID */
		Matcher omimMatcher= Pattern.compile(";?CLNDSDB=([^;]+);?", Pattern.CASE_INSENSITIVE).matcher(clinvarInfoText);
		if (omimMatcher.find()) {
			String s= omimMatcher.group(1);
			List<String> l= Arrays.asList(s.split(":"));
			int index= l.indexOf("OMIM");
			
			if (index != -1) { //only match if found
				Matcher omimIDMatcher= 
					Pattern.compile(";?CLNDSDBID=([^;]+);?", Pattern.CASE_INSENSITIVE).matcher(clinvarInfoText);
				if (omimIDMatcher.find()) {
					String s2= omimIDMatcher.group(1);
					List<String> l2= Arrays.asList(s2.split(":"));
					omimID= l2.get(index);
				}
			}			
		}
		
	}
	
	/**
	 * Reset the clinvar fields.
	 */
	private void resetClinvarFields() {
		// Reset to empty strings
		clnSig= "";
		omimID= "";
		omimAllelicVariantID= "";
		disease= "";
		accession= "";
	}
	
	
	/**
	 * Create a button to search the web for this property.
	 * @param key
	 * @param baseUrl
	 * @param appendToUrl
	 * @return 
	 */
	private Component getKeyValuePairPanelButton(final String key, final String baseUrl, 
			final String appendToUrl, final boolean doEncode) {
		
		JButton ncbiButton = ViewUtil.getTexturedButton("", IconFactory.getInstance().getIcon(IconFactory.StandardIcon.LINKOUT));
		ncbiButton.setToolTipText("Lookup " + key + " on the web");
		ncbiButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent ae) {
				try {
					URL url;
					if (doEncode)
						url = new URL(baseUrl + URLEncoder.encode(appendToUrl, URL_CHARSET));
					else
						url = new URL(baseUrl + appendToUrl);
					
					java.awt.Desktop.getDesktop().browse(url.toURI());
				} catch (Exception ex) {
					ClientMiscUtils.reportError("Problem launching website: %s", ex);
				}
			}
		});

		return ncbiButton;
    }
	
	/**
	 * Creates a button to search dbSNP.
	 * @param key
	 * @return 
	 */
	private Component getDBSNPButton(final String key) {
		JButton ncbiButton = ViewUtil.getTexturedButton("", IconFactory.getInstance().getIcon(IconFactory.StandardIcon.LINKOUT));
		ncbiButton.setToolTipText("Lookup " + key + " at NCBI");
		ncbiButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent ae) {
				try {
					URL url = new URL(baseDBSNPUrl + URLEncoder.encode(p.getValue(key), URL_CHARSET));
					java.awt.Desktop.getDesktop().browse(url.toURI());
				} catch (Exception ex) {
					ClientMiscUtils.reportError("Problem launching NCBI website: %s", ex);
				}
			}
		});

		return ncbiButton;
    }
}