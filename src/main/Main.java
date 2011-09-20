/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package main;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent;
import com.apple.eawt.Application;
import com.apple.eawt.PreferencesHandler;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;
import com.jidesoft.plaf.LookAndFeelFactory;
import java.awt.Insets;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.ut.biolab.medsavant.oldcontroller.SettingsController;
import org.ut.biolab.medsavant.oldcontroller.ThreadController;
import org.ut.biolab.medsavant.view.MainFrame;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class Main {
    private static MainFrame frame;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        verifyJIDE();
        setLAF();
        SettingsController.getInstance();
        frame = MainFrame.getInstance();
        frame.setExtendedState(frame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }

    private static void setLAF() {
        try {
            
            if (ViewUtil.isMac()) {
                customizeForMacs();
            }
            
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0)); 
            LookAndFeelFactory.installJideExtension(LookAndFeelFactory.XERTO_STYLE_WITHOUT_MENU);

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void verifyJIDE() {
        com.jidesoft.utils.Lm.verifyLicense("Marc Fiume", "Savant Genome Browser", "1BimsQGmP.vjmoMbfkPdyh0gs3bl3932");
    }

    private static void customizeForMacs() {
        
            try {
                System.setProperty("apple.laf.useScreenMenuBar", "true");
                System.setProperty("com.apple.mrj.application.apple.menu.about.name", "MedSavant");
            
                
                UIManager.put("TitledBorder.border", UIManager.getBorder("TitledBorder.aquaVariant"));
                
                Application macOSXApplication = Application.getApplication();
                macOSXApplication.setAboutHandler(new AboutHandler() {

                    @Override
                    public void handleAbout(AppEvent.AboutEvent evt) {
                        JOptionPane.showMessageDialog(frame, "MedSavant " + 
                                ProgramInformation.getVersion() + " " + 
                                ProgramInformation.getReleaseType() + 
                                "\nCreated by Biolab at University of Toronto.");
                    }
                });
                macOSXApplication.setPreferencesHandler(new PreferencesHandler() {

                    @Override
                    public void handlePreferences(AppEvent.PreferencesEvent evt) {
                        throw new UnsupportedOperationException("Preferences not supported yet");
                    }
                });
                macOSXApplication.setQuitHandler(new QuitHandler() {

                    @Override
                    public void handleQuitRequestWith(AppEvent.QuitEvent evt, QuitResponse resp) {
                        //throw new UnsupportedOperationException("Preferences not supported yet");
                        System.exit(0);
                    }
                });
            } catch (Throwable x) {
                System.err.println("Warning: MedSavant requires Java for Mac OS X 10.6 Update 3 (or later).\nPlease check Software Update for the latest version.");
            }
    }

}
