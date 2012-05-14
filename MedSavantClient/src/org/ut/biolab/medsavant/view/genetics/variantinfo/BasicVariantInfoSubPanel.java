package org.ut.biolab.medsavant.view.genetics.variantinfo;

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

    private JLabel chromLabel;
    private JLabel positionLabel;
    private JLabel dnaLabel;
    private JLabel refLabel;
    private JLabel altLabel;
    private JLabel qualityLabel;
    private JLabel dbsnpLabel;
    private LinkButton ncbiButton;

    public BasicVariantInfoSubPanel() {
        VariantInfoPanel.addVariantSelectionChangedListener(this);
    }

    @Override
    public String getName() {
        return "Variant Details";
    }
    int indexOfPairLabel = 2;

    @Override
    public JPanel getInfoPanel() {

        JPanel p = new JPanel();
        ViewUtil.applyVerticalBoxLayout(p);

        int i = 1;

        JPanel dnaPanel = ViewUtil.getKeyValuePairPanelListItem("DNA ID", "", i++ % 2 == 0);
        dnaLabel = (JLabel) dnaPanel.getComponent(indexOfPairLabel);
        p.add(dnaPanel);

        addFilterToKVPPanel(dnaPanel);
        addCopyToKVPPanel(dnaPanel, dnaLabel);

        JPanel chromPanel = ViewUtil.getKeyValuePairPanelListItem("Chromosome", "", i++ % 2 == 0);
        chromLabel = (JLabel) chromPanel.getComponent(indexOfPairLabel);
        p.add(chromPanel);

        addFilterToKVPPanel(chromPanel);

        JPanel posPanel = ViewUtil.getKeyValuePairPanelListItem("Position", "", i++ % 2 == 0);
        positionLabel = (JLabel) posPanel.getComponent(indexOfPairLabel);
        p.add(posPanel);

        JPanel refPanel = ViewUtil.getKeyValuePairPanelListItem("Reference", "", i++ % 2 == 0);
        refLabel = (JLabel) refPanel.getComponent(indexOfPairLabel);
        p.add(refPanel);

        JPanel altPanel = ViewUtil.getKeyValuePairPanelListItem("Alternate", "", i++ % 2 == 0);
        altLabel = (JLabel) altPanel.getComponent(indexOfPairLabel);
        p.add(altPanel);

        JPanel dbSNPPanel = ViewUtil.getKeyValuePairPanelListItem("dbSNP", "", i++ % 2 == 0);
        dbsnpLabel = (JLabel) dbSNPPanel.getComponent(indexOfPairLabel);
        p.add(dbSNPPanel);

        ncbiButton = new LinkButton("NCBI");
        ncbiButton.addActionListener(new ActionListener() {

            String baseUrl = "http://www.ncbi.nlm.nih.gov/SNP/snp_ref.cgi?searchType=adhoc_search&rs=";
            @Override
            public void actionPerformed(ActionEvent ae) {
                try {
                    URL url = new URL(baseUrl + URLEncoder.encode(dbsnpLabel.getText(), charset));
                    java.awt.Desktop.getDesktop().browse(url.toURI());
                } catch (Exception ex) {
                    DialogUtils.displayError("Problem launching website.");
                }
            }

        });
        addToKVPPanel(ncbiButton, dbSNPPanel);

        addFilterToKVPPanel(dbSNPPanel);
        addCopyToKVPPanel(dbSNPPanel,dbsnpLabel);

        JPanel qualPanel = ViewUtil.getKeyValuePairPanelListItem("Quality", "", i++ % 2 == 0);
        qualityLabel = (JLabel) qualPanel.getComponent(indexOfPairLabel);
        p.add(qualPanel);


        return p;
    }

    static String charset = "UTF-8";

    @Override
    public void variantSelectionChanged(VariantRecord r) {
        dnaLabel.setText(r.getDnaID());
        chromLabel.setText(r.getChrom());
        positionLabel.setText(ViewUtil.numToString(r.getPosition()));
        refLabel.setText(r.getRef());
        altLabel.setText(r.getAlt());
        qualityLabel.setText(ViewUtil.numToString(r.getQual()));
        dbsnpLabel.setText(r.getDbSNPID());

        ncbiButton.setEnabled(!dbsnpLabel.getText().equals(""));
    }

    private void addToKVPPanel(LinkButton component, JPanel kvpPanel) {
        kvpPanel.add(component, kvpPanel.getComponentCount() - 1);
        kvpPanel.add(Box.createHorizontalStrut(3), kvpPanel.getComponentCount() - 1);
    }

    private void addFilterToKVPPanel(JPanel kvpPanel) {
        LinkButton filterButton = new LinkButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.FILTER).getImage());
        filterButton.setToolTipText("Add filter condition");
        addToKVPPanel(filterButton, kvpPanel);
    }

    private void addCopyToKVPPanel(JPanel kvpPanel, final JLabel label) {
        LinkButton button = new LinkButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.COPY).getImage());
        button.setToolTipText("Copy");
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                String selection = label.getText();
                StringSelection data = new StringSelection(selection);
                Clipboard clipboard =
                        Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(data, data);
                DialogUtils.displayMessage("Copied \"" + selection + "\" to clipboard.");
            }
        });
        addToKVPPanel(button, kvpPanel);
    }
}
