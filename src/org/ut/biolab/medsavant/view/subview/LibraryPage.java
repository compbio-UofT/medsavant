/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.subview;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class LibraryPage implements Page {

    public LibraryPage() {
    }

    public String getName() {
        return "Library";
    }

    public Component getView() {
        return new JPanel();
    }

    public Component getBanner() {
        JPanel p = ViewUtil.createClearPanel();
        p.setBackground(Color.black);
        return p;
    }

}
