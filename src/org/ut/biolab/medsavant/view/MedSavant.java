/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view;

import fiume.vcf.VCFParser;
import fiume.vcf.VariantSet;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.view.View;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.ResultController;

/**
 *
 * @author mfiume
 */
public class MedSavant extends JFrame {

    public MedSavant() {
        super("MedSavant");

        this.setLayout(new BorderLayout());

        initVariantCollection();

        JPanel view = new View();
        this.add(view, BorderLayout.CENTER);

        JMenuBar menu = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem closeItem = new JMenuItem("Close");
        final MedSavant instance = this;
        closeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                instance.requestClose();
            }
        });
        fileMenu.add(closeItem);
        menu.add(fileMenu);
        this.add(menu, BorderLayout.NORTH);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initVariantCollection() {
        try {
            VariantSet variants = VCFParser.parseVariants(new File("C:\\calls.vcf"));
            ResultController.clearVariants();
            ResultController.addVariantSet(variants);

        } catch (IOException ex) {
        }
    }

    public void requestClose() {
        System.exit(0);
    }

}
