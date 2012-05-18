package org.ut.biolab.medsavant.view.genetics.variantinfo;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.net.URLEncoder;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.model.event.VariantSelectionChangedListener;
import org.ut.biolab.medsavant.vcf.VariantRecord;
import org.ut.biolab.medsavant.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.view.component.LinkButton;
import org.ut.biolab.medsavant.view.genetics.GeneticsTablePage.VariantInfoPanel;
import org.ut.biolab.medsavant.view.genetics.TablePanel;
import org.ut.biolab.medsavant.view.genetics.variantinfo.InfoSubPanel;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class BasicVariantInfoSubPanel extends InfoSubPanel implements VariantSelectionChangedListener {

    private static String KEY_DNAID = "DNA ID";
    private static String KEY_CHROM = "Chromosome";
    private static String KEY_POSITION = "Position";
    private static String KEY_REF = "Reference";
    private static String KEY_ALT = "Alternate";
    private static String KEY_QUAL = "Quality";
    private static String KEY_DBSNP = "dbSNP ID";

    private KeyValuePairPanel p;

    public BasicVariantInfoSubPanel() {
        VariantInfoPanel.addVariantSelectionChangedListener(this);
    }

    @Override
    public String getName() {
        return "Variant Details";
    }

    static String charset = "UTF-8";

    @Override
    public JPanel getInfoPanel() {
        if (p == null) {
            p = new KeyValuePairPanel(4);
            p.addKey(KEY_DNAID);
            p.addKey(KEY_CHROM);
            p.addKey(KEY_POSITION);
            p.addMoreRow();
            p.addKey(KEY_REF);
            p.addKey(KEY_ALT);
            p.addKey(KEY_QUAL);
            p.addKey(KEY_DBSNP);

            JLabel l = new JLabel("This will eventually show a chart");
            p.setDetailComponent(KEY_QUAL,l);

            int col = 0;

            p.setAdditionalColumn(KEY_DNAID, col, getFilterButton(KEY_DNAID));
            p.setAdditionalColumn(KEY_DBSNP, col, getFilterButton(KEY_DBSNP));
            p.setAdditionalColumn(KEY_QUAL, col, getFilterButton(KEY_QUAL));

            col++;
            p.setAdditionalColumn(KEY_DNAID, col, getCopyButton(KEY_DNAID));
            p.setAdditionalColumn(KEY_DBSNP, col, getCopyButton(KEY_DBSNP));
            p.setAdditionalColumn(KEY_QUAL, col, getChartButton(KEY_QUAL));

            col++;
            p.setAdditionalColumn(KEY_DBSNP, col, getNCBIButton(KEY_DBSNP));

        }
        return p;
    }

    public boolean showHeader() {
        return false;
    }

    @Override
    public void variantSelectionChanged(VariantRecord r) {
        if (p == null) { return; }
        if (r == null) {
            // TODO show other card
            return;
        }

        p.setValue(KEY_DNAID, r.getDnaID());
        p.setValue(KEY_CHROM, r.getChrom());
        p.setValue(KEY_POSITION, ViewUtil.numToString(r.getPosition()));
        p.setValue(KEY_REF, r.getRef());
        p.setValue(KEY_ALT, r.getAlt());

        p.setValue(KEY_QUAL, ViewUtil.numToString(r.getQual()));
        p.setValue(KEY_DBSNP, r.getDbSNPID());
    }

    private Component getCopyButton(final String key) {
        LinkButton button = new LinkButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.COPY).getImage());
        button.setToolTipText("Copy " + key);
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                String selection = p.getValue(key);
                StringSelection data = new StringSelection(selection);
                Clipboard clipboard =
                        Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(data, data);
                DialogUtils.displayMessage("Copied \"" + selection + "\" to clipboard.");
            }
        });
        return button;
    }

    private Component getFilterButton(final String key) {
        LinkButton button = new LinkButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.FILTER).getImage());
        button.setToolTipText("Filter " + key);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
            }
        });
        return button;
    }

    private Component getChartButton(final String key) {
        LinkButton button = new LinkButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.CHART_SMALL).getImage());
        button.setToolTipText("Chart " + key);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                p.toggleDetailVisibility(key);
            }
        });
        return button;
    }

    private Component getNCBIButton(final String key) {
        LinkButton ncbiButton = new LinkButton("NCBI");
        ncbiButton.setToolTipText("Lookup " + key + " at NCBI");
        ncbiButton.addActionListener(new ActionListener() {

            String baseUrl = "http://www.ncbi.nlm.nih.gov/SNP/snp_ref.cgi?searchType=adhoc_search&rs=";
            @Override
            public void actionPerformed(ActionEvent ae) {
                try {
                    URL url = new URL(baseUrl + URLEncoder.encode(p.getValue(key), charset));
                    java.awt.Desktop.getDesktop().browse(url.toURI());
                } catch (Exception ex) {
                    DialogUtils.displayError("Problem launching website.");
                }
            }

        });

        return ncbiButton;
    }
}