/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.genetics;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.dialog.SavantExportForm;
import org.ut.biolab.medsavant.view.dialog.VCFUploadForm;
import org.ut.biolab.medsavant.view.genetics.filter.FilterPanel;
//import org.ut.biolab.medsavant.view.genetics.filter.FilterProgressPanel;
import org.ut.biolab.medsavant.view.genetics.filter.FilterProgressPanel;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.subview.SectionView;

/**
 *
 * @author mfiume
 */
public class GeneticsSection extends SectionView {
    private JPanel[] panels;

    public GeneticsSection() {
        setPersistencePanels();
    }
    
    @Override
    public String getName() {
        return "Genetic Variants";
    }

    @Override
    public SubSectionView[] getSubSections() {
        SubSectionView[] pages = new SubSectionView[4];
        pages[0] = new GeneticsTablePage(this);
        pages[1] = new GeneticsChartPage(this);
        pages[2] = new GeneticsRegionsPage(this);
        pages[3] = new GeneticsTestPage(this);
        return pages;
    }

    @Override
    public JPanel[] getPersistentPanels() {
        return panels;
    }

    public JButton createVcfButton(){
        JButton button = new JButton("Import Variants");

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new VCFUploadForm();
            }
        });

        return button;
    }
    
    private JButton addShowInSavantButton(){
        JButton button = new JButton("Show in Savant");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new SavantExportForm();
            }
        }); 
        return button;
    }
    
    private JButton addSaveResultSetButton(){
        JButton button = new JButton("Save Variants");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //TODO
            }
        }); 
        return button;
    }

    @Override
    public Component[] getBanner() {

        Component[] result = new Component[3];
        result[2] = createVcfButton();
        result[1] = addShowInSavantButton();
        result[0] = addSaveResultSetButton();
        
        return result;
    }

    private void setPersistencePanels() {
        try {
            panels = new JPanel[2];
            panels[0] = new FilterPanel();
            panels[1] = new FilterProgressPanel();
            //TODO: account for exception in filter panel instead
        } catch (Exception ex) {
        }
    }

}
