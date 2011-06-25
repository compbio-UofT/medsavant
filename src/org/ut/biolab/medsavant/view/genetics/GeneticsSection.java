/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.genetics;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
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
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.util.PaintUtil;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class GeneticsSection extends SectionView {

    @Override
    public String getName() {
        return "Genetic Variants";
    }

    @Override
    public SubSectionView[] getSubSections() {
        SubSectionView[] pages = new SubSectionView[4];
        pages[0] = new GeneticsListPage(this);
        pages[1] = new GeneticsChartPage(this);
        pages[2] = new GeneticsRegionsPage(this);
        pages[3] = new GeneticsTestPage(this);
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
        JButton button = new JButton("Import Variants");

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Import Variants");
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
                    Logger.getLogger(GeneticsSection.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        return button;
    }

    @Override
    public Component[] getBanner() {

        Component[] result = new Component[1];
        result[0] = createVcfButton();
        
        return result;
    }

}
