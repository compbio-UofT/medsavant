package medsavant.discovery;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.shared.appapi.MedSavantDashboardApp;


public class DiscoveryApp extends MedSavantDashboardApp {

    private static final String iconroot= "/medsavant/discovery/icon/";
    
    private DiscoveryPanel p;

    @Override
    public JPanel getContent() {
        if (p  == null) {
            p = new DiscoveryPanel();
        }
        return p.getView();
    }

    /**
     * Title which will appear on plugin's tab in Savant user interface.
     */
    @Override
    public String getTitle() {        
        return "Discovery";
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
