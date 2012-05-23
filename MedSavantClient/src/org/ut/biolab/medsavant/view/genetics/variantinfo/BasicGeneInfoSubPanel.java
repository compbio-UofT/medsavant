package org.ut.biolab.medsavant.view.genetics.variantinfo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.net.URLEncoder;
import javax.swing.*;
import org.ut.biolab.medsavant.model.Gene;
import org.ut.biolab.medsavant.model.event.GeneSelectionChangedListener;
import org.ut.biolab.medsavant.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.view.component.LinkButton;
import org.ut.biolab.medsavant.view.genetics.GeneIntersectionGenerator;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class BasicGeneInfoSubPanel extends InfoSubPanel implements GeneSelectionChangedListener {

    private static String KEY_NAME = "Name";
    private static String KEY_CHROM = "Chrom";
    private static String KEY_START = "Start";
    private static String KEY_END = "End";
    private KeyValuePairPanel p;

    public BasicGeneInfoSubPanel() {
        GeneIntersectionGenerator.addGeneSelectionChangedListener(this);
    }

    @Override
    public String getName() {
        return "Gene Details";
    }
    static String charset = "UTF-8";

    @Override
    public JPanel getInfoPanel() {
        if (p == null) {
            p = new KeyValuePairPanel(2);
            p.addKey(KEY_NAME);

            JButton filterButton2 = ViewUtil.getTexturedButton("Card", IconFactory.getInstance().getIcon(IconFactory.StandardIcon.LINKOUT));
            filterButton2.setToolTipText("Lookup Gene Card");
            p.setAdditionalColumn(KEY_NAME, 1, filterButton2);

            filterButton2.addActionListener(new ActionListener() {

                String baseUrl = "http://www.genecards.org/cgi-bin/carddisp.pl?gene=";

                @Override
                public void actionPerformed(ActionEvent ae) {
                    try {

                        JComboBox jcb = (JComboBox) p.getComponent(KEY_NAME);
                        URL url = new URL(baseUrl + URLEncoder.encode(jcb.getSelectedItem().toString(), charset));
                        System.out.println(url.getPath());

                        java.awt.Desktop.getDesktop().browse(url.toURI());
                    } catch (Exception ex) {
                        DialogUtils.displayError("Problem launching website.");
                    }
                }
            });

            p.addKey(KEY_CHROM);
            p.addKey(KEY_START);
            p.addKey(KEY_END);
        }
        return p;
    }

    public boolean showHeader() {
        return false;
    }

    @Override
    public void geneSelectionChanged(Gene g) {
        if (p == null) {
            return;
        }
        if (g == null) {
            // TODO show other card
            return;
        }

        JComponent c = GeneIntersectionGenerator.getInstance().getGeneDropDown();
        ViewUtil.makeSmall(c);

        p.setValue(KEY_NAME, c);
        p.setValue(KEY_CHROM, g.getChrom());
        p.setValue(KEY_START, ViewUtil.numToString(g.getStart()));
        p.setValue(KEY_END, ViewUtil.numToString(g.getEnd()));
    }
}
