package medsavant.incidental;

import java.sql.SQLException;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.api.MedSavantClinicApp;


/**
 * Demonstration plugin to show how to do a simple panel.
 *
 * @author rammar
 */
public class IncidentalApp extends MedSavantClinicApp {

    private static final String iconroot= "/medsavant/incidental/icon/";
    
    private IncidentalPanel p;

    @Override
    public JPanel getContent() {
        if (p == null) {
            p = new IncidentalPanel();
        }
        return p.getView();
    }

    /**
     * Title which will appear on plugin's tab in Savant user interface.
     */
    @Override
    public String getTitle() {
        return "Incidentalome";
    }

    @Override
    public void viewDidLoad() {
    }

    @Override
    public void viewDidUnload() {
    }

    @Override
    public ImageIcon getIcon() {
        return getIcon(iconroot + "icon.png");
    }

    public ImageIcon getIcon(String resourcePath) {
        return new ImageIcon(getClass().getResource(resourcePath));
    }
}
