/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.db.util.query.ReferenceQueryUtil;
import org.ut.biolab.medsavant.listener.ProjectListener;
import org.ut.biolab.medsavant.view.dialog.SavantExportForm;
import org.ut.biolab.medsavant.view.dialog.VCFUploadForm;
//import org.ut.biolab.medsavant.view.genetics.filter.FilterProgressPanel;
import org.ut.biolab.medsavant.view.genetics.filter.FilterProgressPanel;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.subview.SectionView;

/**
 *
 * @author mfiume
 */
public class GeneticsSection extends SectionView implements ProjectListener {

    private JPanel[] panels;
    private JComboBox referenceDropDown;

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
        pages[0] = new GeneticsFilterPage(this);
        pages[1] = new GeneticsTablePage(this);
        pages[2] = new GeneticsChartPage(this);
        pages[3] = new AggregatePage(this);
        return pages;
    }

    @Override
    public JPanel[] getPersistentPanels() {
        return panels;
    }

    public JButton createVcfButton() {
        JButton button = new JButton("Import Variants");

        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                new VCFUploadForm();
            }
        });

        return button;
    }

    private JButton addShowInSavantButton() {
        JButton button = new JButton("Show in Savant");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                new SavantExportForm();
            }
        });
        return button;
    }

    private JButton addSaveResultSetButton() {
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

        Component[] result = new Component[4];
        result[0] = new JLabel("Reference:");
        if(referenceDropDown == null){
            result[1] = getReferenceDropDown();
        } else {
            result[1] = referenceDropDown;
        }        
        result[2] = addShowInSavantButton();
        result[3] = createVcfButton();
        //result[0] = addSaveResultSetButton();

        return result;
    }

    private void setPersistencePanels() {
        try {
            /*panels = new JPanel[2];
            panels[0] = new FilterPanel();
            panels[1] = new FilterProgressPanel();
             * 
             */
            //TODO: account for exception in filter panel instead
        } catch (Exception ex) {
        }
    }

    private Component getReferenceDropDown() {
        referenceDropDown = new JComboBox();

        referenceDropDown.setMinimumSize(new Dimension(200, 23));
        referenceDropDown.setPreferredSize(new Dimension(200, 23));
        referenceDropDown.setMaximumSize(new Dimension(200, 23));
    
        refreshReferenceDropDown();
        
        ProjectController.getInstance().addProjectListener(this);

        return referenceDropDown;
    }

    private void refreshReferenceDropDown() {
        try {
            for(ActionListener l : referenceDropDown.getActionListeners()){
                referenceDropDown.removeActionListener(l);
            }
            referenceDropDown.removeAllItems();

            List<String> references = ReferenceQueryUtil.getReferencesForProject(
                    ProjectController.getInstance().getCurrentProjectId());

            for (String refname : references) {
                
                int refid = ReferenceController.getInstance().getReferenceId(refname);
                
                int numVariantsInTable = ProjectController.getInstance().getNumVariantsInTable(ProjectController.getInstance().getCurrentProjectId(),refid);
                
                referenceDropDown.addItem(refname); // + " (" + numVariantsInTable + " variants)");
            }
            referenceDropDown.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    String currentName = ReferenceController.getInstance().getCurrentReferenceName();
                    if(!ReferenceController.getInstance().setReference((String) referenceDropDown.getSelectedItem(), true)){
                        referenceDropDown.setSelectedItem(currentName);
                    }
                }
            });
           ReferenceController.getInstance().setReference((String) referenceDropDown.getSelectedItem());

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

    public void projectAdded(String projectName) {
        refreshReferenceDropDown();
    }

    public void projectRemoved(String projectName) {
        refreshReferenceDropDown();
    }

    public void projectChanged(String projectName) {
        refreshReferenceDropDown();
    }

    public void projectTableRemoved(int projid, int refid) {
        refreshReferenceDropDown();
    }

    public void referenceChanged(String referenceName) {}
}
