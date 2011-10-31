/*
 *    Copyright 2011 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant;

import java.awt.Insets;
import ext.growl.GrowlException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.jidesoft.plaf.LookAndFeelFactory;
import ext.growl.Growl;
import ext.growl.GrowlUtils;

import org.ut.biolab.medsavant.controller.SettingsController;
import org.ut.biolab.medsavant.log.ClientLogger;
import org.ut.biolab.medsavant.util.MiscUtils;
import org.ut.biolab.medsavant.view.MainFrame;


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
        frame.setExtendedState(MainFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
        ClientLogger.log(Main.class, "MedSavant booted");
        
        // experimental integration with Growl
        /*
        if (MiscUtils.MAC) {
            try {
                Growl growl = GrowlUtils.getGrowlInstance("MedSavant");
            growl.addNotification("Something has happened", true);
            growl.register();

             // snip
            growl.sendNotification("Something has happened", "Notification!", "Wake up lazy bones!");
            } catch (GrowlException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
         */
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
