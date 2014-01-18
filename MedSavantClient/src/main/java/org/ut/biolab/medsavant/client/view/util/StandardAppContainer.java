/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.util;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 * A helper class to assist in creating App containers with 
 * standard scroll behaviour and padding. An instance of this class
 * should  be returned by all getView methods of apps.
 * @author mfiume
 */
public class StandardAppContainer extends JPanel {

    public StandardAppContainer(JPanel view) {
        this(view,false);
    }
    
    public StandardAppContainer(JPanel view, boolean doesScroll) {
        
        JPanel paddedContainer = ViewUtil.getClearPanel();
        paddedContainer.setLayout(new MigLayout("fillx, filly"));

        this.setBackground(Color.white);
        this.setLayout(new BorderLayout());

        if (doesScroll) {
            JScrollPane p = ViewUtil.getClearBorderlessScrollPane(paddedContainer);
            p.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            
            paddedContainer.add(view,"growy 1.0");
            this.add(p, BorderLayout.CENTER);
        } else {
            paddedContainer.add(view,"growy 1.0, width 100%");
            this.add(paddedContainer,BorderLayout.CENTER);
        }
    }

}
