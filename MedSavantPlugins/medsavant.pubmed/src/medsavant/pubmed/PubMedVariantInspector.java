package medsavant.pubmed;

import edu.iastate.jtm.assistant.PubMedAssistant;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.view.genetics.inspector.SubInspector;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;
import org.xml.sax.SAXException;

/**
 *
 * @author mfiume
 */
class PubMedVariantInspector extends SubInspector {
    private JPanel infoPanel;

    private JButton button;
    private VariantRecord vr;

    public PubMedVariantInspector() {
        init();
    }

    @Override
    public String getName() {
        return "PubMed";
    }

    @Override
    public JPanel getInfoPanel() {
        return infoPanel;
    }

    void setVariantRecord(VariantRecord vr) {
        this.vr = vr;
    }

    private void init() {
        infoPanel = new JPanel();
        button = new JButton("Search PubMed");
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    PubMedAssistant assistant = new PubMedAssistant();
                    assistant.setVisible(true);        // PuMA extends JFrame

                    if (vr != null) {
                        assistant.search(vr.getDbSNPID());
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        });
        infoPanel.add(button);
    }

}
