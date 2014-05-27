package medsavant.pathways;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.shared.appapi.MedSavantDashboardApp;


/**
 * Pathways app.
 *
 * @author rammar
 */
public class PathwaysApp extends MedSavantDashboardApp {

    private static final String iconroot= "/medsavant/pathways/icon/";
    
    private PathwaysPanel p;

    @Override
    public JPanel getContent() {
        if (p  == null) {
            p = new PathwaysPanel();
        }
        return p.getView();
    }

    /**
     * Title which will appear on plugin's tab in Savant user interface.
     */
    @Override
    public String getTitle() {
        return "Pathwayz";
    }

    @Override
    public void viewDidLoad() {
    }

    @Override
    public void viewDidUnload() {
    }

    @Override
    public ImageIcon getIcon() {
        return getIcon(iconroot + "icon.jpg");
    }

    public ImageIcon getIcon(String resourcePath) {
        return new ImageIcon(getClass().getResource(resourcePath));
    }
}
