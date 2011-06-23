/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package main;

import com.jidesoft.plaf.LookAndFeelFactory;
import java.awt.Insets;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.ut.biolab.medsavant.controller.SettingsController;
import org.ut.biolab.medsavant.controller.ThreadController;
import org.ut.biolab.medsavant.view.Frame;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        verifyJIDE();
        setLAF();
        SettingsController.getInstance();
        Frame s = new Frame();
        s.setExtendedState(s.MAXIMIZED_BOTH);
        s.setVisible(true);
        
        ThreadController.getInstance().runInThread(new Runnable() {

            public void run() {
                System.out.println("Starting long thread");
                try {
                    Thread.sleep(12000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        });
    }

    private static void setLAF() {
        try {
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

}
