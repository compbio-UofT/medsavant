package medsavant.pgx;

import java.sql.SQLException;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.api.MedSavantClinicApp;


/**
 * PGx app.
 *
 * @author rammar
 */
public class PGXApp extends MedSavantClinicApp {

    private static final String iconroot= "/medsavant/pgx/icon/";
    
    private PGXPanel p;

    @Override
    public JPanel getContent() {
        if (p  == null) {
            p = new PGXPanel();
        }
        return p.getView();
    }

    /**
     * Title which will appear on plugin's tab in Savant user interface.
     */
    @Override
    public String getTitle() {
        return "Pharmacogenomics";
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
