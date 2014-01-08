/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.ut.biolab.medsavant.client.view.dashboard.DashboardApp;
import org.ut.biolab.medsavant.client.view.images.IconFactory;

/**
 *
 * @author mfiume
 */
public class VCFImportApp implements DashboardApp {

    private JPanel view;

    @Override
    public JPanel getView() {
        return view;
    }

    private void initView() {
        if (view == null) {
            view = new JPanel();
            
            view.setLayout(new BorderLayout());

            view.setBackground(Color.red);
            
            new FileDrop(view, new FileDrop.Listener() {
                public void filesDropped(java.io.File[] files) {
                    for (File f : files) {
                        System.out.println("File dropped: " + f.getAbsolutePath());
                    }
                }   // end filesDropped
            }); // end FileDrop.Listener
        }
    }

    @Override
    public void viewWillUnload() {
    }

    @Override
    public void viewWillLoad() {
        initView();
    }

    @Override
    public void viewDidUnload() {
    }

    @Override
    public void viewDidLoad() {
    }

    @Override
    public ImageIcon getIcon() {
        return IconFactory.getInstance().getIcon(IconFactory.StandardIcon.APP_IMPORTVCF);
    }

    @Override
    public String getName() {
        return "VCF Import";
    }

}
