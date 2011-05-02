/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.subview;

import org.ut.biolab.medsavant.view.SplitView;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
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
        SplitView p = new SplitView();
        JPanel red = new JPanel(); red.setBackground(Color.red);
        JPanel green = new JPanel(); green.setBackground(Color.green);
        
        p.addSection("Library");
        p.addSubsection("Patients",new JPanel());
        p.addSubsection("Genes",red);
        p.addSubsection("Pathways",green);
        p.addSubsection("Variants",new JPanel());
        p.addSection("Search");
        p.addSubsection("Filter variants",new JPanel());
        return p;
    }

    public Component getBanner() {
        JPanel p = ViewUtil.createClearPanel();
        p.setBackground(Color.black);
        return p;
    }

}
