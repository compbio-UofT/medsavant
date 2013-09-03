package medsavant.inspect;

import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.view.genetics.inspector.SubInspector;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;

/**
 *
 * @author mfiume
 */
class ExampleVariantInspector extends SubInspector {
    private JPanel infoPanel;
    private JLabel label;

    public ExampleVariantInspector() {
        init();
    }

    @Override
    public String getName() {
        return "Example Inspector";
    }

    @Override
    public JPanel getInfoPanel() {
        return infoPanel;
    }

    void setVariantRecord(VariantRecord vr) {
        System.out.println("App Setting variant to " + vr.toString());
        label.setText(vr.getChrom() + ":" + vr.getPosition());
    }

    private void init() {
        infoPanel = new JPanel();
        label = new JLabel("No variant selected");
        infoPanel.add(label);
    }

}
