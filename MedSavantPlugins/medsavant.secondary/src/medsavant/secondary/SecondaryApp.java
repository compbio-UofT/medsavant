package medsavant.secondary;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.shared.appapi.MedSavantDashboardApp;


public class SecondaryApp extends MedSavantDashboardApp {

    private static final String iconroot= "/medsavant/secondary/icon/";
    
    private SecondaryPanel p;

    @Override
    public JPanel getContent() {
        if (p  == null) {
            p = new SecondaryPanel();
        }
        return p.getView();
    }

    /**
     * Title which will appear on plugin's tab in Savant user interface.
     */
    @Override
    public String getTitle() {
        return "Secondary";
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
