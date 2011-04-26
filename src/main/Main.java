/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package main;

import com.jidesoft.plaf.LookAndFeelFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.ut.biolab.medsavant.view.MedSavant;

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
        MedSavant s = new MedSavant();
        s.setExtendedState(s.MAXIMIZED_BOTH);
        s.setVisible(true);
    }

    private static void setLAF() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            //LookAndFeelFactory.installDefaultLookAndFeelAndExtension();
            LookAndFeelFactory.installJideExtension(LookAndFeelFactory.VSNET_STYLE_WITHOUT_MENU);
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
