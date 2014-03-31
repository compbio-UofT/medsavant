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
package org.ut.biolab.medsavant.client.view.splash;

import com.explodingpixels.macwidgets.*;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.*;

import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;


/**
 *
 * @author mfiume
 */
public class SplashFrame extends JFrame {

    private final JPanel primaryPanel;
    private final SplashLoginComponent loginComponent;
    private final SplashServerManagementComponent serverManager;

    public SplashFrame() {

        if (ClientMiscUtils.MAC) {
            MacUtils.makeWindowLeopardStyle(this.getRootPane());
        }

        this.setTitle("MedSavant");
        this.setResizable(false);
        this.setBackground(Color.white);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        this.setLayout(new MigLayout("filly, insets 0, gapx 0, height 530, width 750, hidemode 3"));

        primaryPanel = new JPanel();
        
        serverManager = new SplashServerManagementComponent(this);
        loginComponent = new SplashLoginComponent(this,serverManager);

        switchToLogin();

        this.add(primaryPanel, "width 100%, growy 1.0, growx 1.0");

        this.pack();
        this.setLocationRelativeTo(null);
    }

    void closeSplashFrame() {
        this.setVisible(false);
    }

    private void setPrimaryPanel(JPanel p) {

        if (p instanceof SplashLoginComponent) {
            loginComponent.setPageAppropriately();
        }

        primaryPanel.removeAll();
        primaryPanel.setLayout(new BorderLayout());
        primaryPanel.add(p, BorderLayout.CENTER);
        primaryPanel.updateUI();
    }

    public static void main(String[] a) {
        SplashFrame loginFrame = new SplashFrame();
        loginFrame.setVisible(true);
    }

    void switchToLogin() {
        setPrimaryPanel(loginComponent);
    }

    void switchToServerManager() {
        setPrimaryPanel(serverManager);
    }
}
