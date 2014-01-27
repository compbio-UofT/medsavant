/*
 * Copyright (C) 2014 University of Toronto, Computational Biology Lab.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package org.ut.biolab.medsavant.client.view.app.splash;

import com.explodingpixels.macwidgets.MacUtils;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class SplashFrame extends JFrame {
    private final JPanel secondaryPanel;
    
    public SplashFrame() {
        
        this.setResizable(false);
        
        MacUtils.makeWindowLeopardStyle(this.getRootPane());
        this.setLayout(new MigLayout("filly, insets 0, gapx 0, height 530"));
        
        LoginComponent lc = new LoginComponent();
        secondaryPanel = new JPanel();
        
        AdvertisingComponent ac = new AdvertisingComponent();
        setSecondaryPanel(ac);
        
        this.add(lc, "width 275, growy 1.0");
        this.add(secondaryPanel, "width 650, growy 1.0, growx 1.0");
        
        this.pack();
        this.setLocationRelativeTo(null);
    }
    
    public static void main(String[] argv) {
        SplashFrame f = new SplashFrame();
        f.setVisible(true);
    }

    private void setSecondaryPanel(JPanel p) {
        secondaryPanel.removeAll();
        secondaryPanel.setLayout(new BorderLayout());
        secondaryPanel.add(p,BorderLayout.CENTER);
        secondaryPanel.updateUI();
    }
}