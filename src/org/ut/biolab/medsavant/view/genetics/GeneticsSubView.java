/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.genetics;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.db.DBUtil;
import org.ut.biolab.medsavant.util.ExtensionFileFilter;
import org.ut.biolab.medsavant.view.genetics.filter.FilterPanel;
import org.ut.biolab.medsavant.view.subview.Page;
import org.ut.biolab.medsavant.view.subview.SubView;
import org.ut.biolab.medsavant.view.util.PaintUtil;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class GeneticsSubView extends SubView {

    @Override
    public String getName() {
        return "Genetic Variants";
    }

    @Override
    public Page[] getPages() {
        Page[] pages = new Page[2];
        pages[0] = new GeneticsSummerizePage();
        pages[1] = new GeneticsListPage();
        return pages;
    }

    @Override
    public JPanel[] getPersistentPanels() {
        try {
            JPanel[] panels = new JPanel[1];
            panels[0] = new FilterPanel();
            //TODO: account for exception in filter panel instead
            return panels;
        } catch (Exception ex) {
            return null;
        }
    }

    public JButton createVcfButton(){
        JButton button = new JButton("Add VCF");

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Add VCF");
                fc.setDialogType(JFileChooser.OPEN_DIALOG);
                fc.addChoosableFileFilter(new ExtensionFileFilter("vcf"));
                int result = fc.showDialog(null, null);
                if (result == JFileChooser.CANCEL_OPTION || result == JFileChooser.ERROR_OPTION) {
                    return;
                }
                String path = fc.getSelectedFile().getAbsolutePath();
                try {
                    DBUtil.addVcfToDb(path);
                } catch (SQLException ex) {
                    Logger.getLogger(GeneticsSubView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        return button;
    }

    @Override
    public Component getBanner() {
        JPanel p = new JPanel() {
            public void paintComponent(Graphics g) {
                PaintUtil.paintDarkMenu(g, this);
            }
        };
        p.setBackground(Color.red);
        p.setLayout(new BoxLayout(p,BoxLayout.X_AXIS));
       // p.add(Box.createHorizontalStrut(10));
        //p.add(new JButton("Add VCF"));
        p.add(createVcfButton());
        p.add(Box.createHorizontalGlue());
        //p.add(new JButton("Show in Savant"));
        p.add(Box.createHorizontalStrut(10));
        return p;
    }

}
