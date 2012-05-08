package org.ut.biolab.medsavant.view.genetics;

import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.model.event.VariantSelectionChangedListener;
import org.ut.biolab.medsavant.vcf.VariantRecord;
import org.ut.biolab.medsavant.view.genetics.variantinfo.InfoSubPanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
class BasicVariantInfoSubPanel extends InfoSubPanel implements VariantSelectionChangedListener {
    private JLabel chromLabel;
    private JLabel positionLabel;
    private JLabel dnaLabel;
    private JLabel refLabel;
    private JLabel altLabel;
    private JLabel qualityLabel;
    private JLabel dbsnpLabel;

    public BasicVariantInfoSubPanel() {
        TablePanel.addVariantSelectionChangedListener(this);
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

        int i = 0;

        JPanel dnaPanel = ViewUtil.getKeyValuePairPanelListItem("DNA ID", "", i++%2==0);
        dnaLabel = (JLabel) dnaPanel.getComponent(indexOfPairLabel);
        p.add(dnaPanel);

        JPanel chromPanel = ViewUtil.getKeyValuePairPanelListItem("Chromosome", "", i++%2==0);
        chromLabel = (JLabel) chromPanel.getComponent(indexOfPairLabel);
        p.add(chromPanel);


        JPanel posPanel = ViewUtil.getKeyValuePairPanelListItem("Position", "", i++%2==0);
        positionLabel = (JLabel) posPanel.getComponent(indexOfPairLabel);
        p.add(posPanel);

        JPanel refPanel = ViewUtil.getKeyValuePairPanelListItem("Reference", "", i++%2==0);
        refLabel = (JLabel) refPanel.getComponent(indexOfPairLabel);
        p.add(refPanel);

        JPanel altPanel = ViewUtil.getKeyValuePairPanelListItem("Alternate", "", i++%2==0);
        altLabel = (JLabel) altPanel.getComponent(indexOfPairLabel);
        p.add(altPanel);

        JPanel dbSNPPanel = ViewUtil.getKeyValuePairPanelListItem("dbSNP", "", i++%2==0);
        dbsnpLabel = (JLabel) dbSNPPanel.getComponent(indexOfPairLabel);
        p.add(dbSNPPanel);

        JPanel qualPanel = ViewUtil.getKeyValuePairPanelListItem("Quality", "", i++%2==0);
        qualityLabel = (JLabel) qualPanel.getComponent(indexOfPairLabel);
        p.add(qualPanel);


        return p;
    }

    @Override
    public void variantSelectionChanged(VariantRecord r) {
        dnaLabel.setText(r.getDnaID());
        chromLabel.setText(r.getChrom());
        positionLabel.setText(ViewUtil.numToString(r.getPosition()));
        refLabel.setText(r.getRef());
        altLabel.setText(r.getAlt());
        qualityLabel.setText(ViewUtil.numToString(r.getQual()));
        dbsnpLabel.setText(r.getDbSNPID());
    }

}
