/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.util;

import com.jidesoft.docking.DockableFrame;
import com.jidesoft.docking.DockingManager;

/**
 *
 * @author Andrew
 */
public class MiscUtils {
    
     public static void setFrameVisibility(String frameKey, boolean isVisible, DockingManager m) {
        DockableFrame f = m.getFrame(frameKey);
        if (isVisible) {
            m.showFrame(frameKey);
        } else {
            m.hideFrame(frameKey);
        }
    }
    
}
