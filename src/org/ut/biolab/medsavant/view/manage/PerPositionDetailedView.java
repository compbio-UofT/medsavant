/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.manage;

import java.awt.BorderLayout;
import java.util.List;
import java.util.Vector;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.patients.DetailedView;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
class PerPositionDetailedView extends DetailedView {
    private final JPanel details;
    private final JPanel menu;
    private final JPanel content;

    public PerPositionDetailedView() {
        
        content = this.getContentPanel();
        
        details = ViewUtil.getClearPanel();
        menu = ViewUtil.getButtonPanel();
        
        //menu.add(setDefaultCaseButton());
        //menu.add(setDefaultControlButton());
        //menu.add(removeIndividualsButton());
        //menu.add(deleteCohortButton());
        menu.setVisible(false);
        
        content.setLayout(new BorderLayout());
        
        content.add(details,BorderLayout.CENTER);
        content.add(menu,BorderLayout.SOUTH);
    }

    @Override
    public void setSelectedItem(Vector item) {
        String annotationName = (String) item.get(0);
        setTitle(annotationName);
        
        details.removeAll();
        details.setLayout(new BorderLayout());
        
        setDetailsForAnnotation(annotationName);
        
        details.updateUI();
        
        if(menu != null) menu.setVisible(true);
    }

    @Override
    public void setMultipleSelections(List<Vector> selectedRows) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    private void setDetailsForAnnotation(String annotationName) {
        if (annotationName.equals(PerPositionAnnotationListModel.ANNOTATION_GATK)) {
            String[][] kvp = new String[3][2];
            kvp[0][0] = "Description";
            kvp[0][1] = "General annotations of known human protein-coding and non-protein-coding genes taken from the NCBI RNA reference sequences collection (RefSeq).";
            kvp[1][0] = "URL";
            kvp[1][1] = "http://www.broadinstitute.org/gsa/wiki/index.php/Genomic_Annotator_Data_Tables";
            kvp[2][0] = "Available Annotations";
            kvp[2][1] = "Homo Sapiens (hg19 exome)";
            details.add(ViewUtil.getKeyValuePairPanel(kvp),BorderLayout.NORTH);
        } else if (annotationName.equals(PerPositionAnnotationListModel.ANNOTATION_SIFT)) {
            String[][] kvp = new String[3][2];
            kvp[0][0] = "Description";
            kvp[0][1] = "SIFT predicts whether an amino acid substitution affects protein function.";
            kvp[1][0] = "URL";
            kvp[1][1] = "http://sift.jcvi.org/";
            kvp[2][0] = "Available Annotations";
            kvp[2][1] = "Homo Sapiens (hg19 exome)";
            details.add(ViewUtil.getKeyValuePairPanel(kvp),BorderLayout.NORTH);
        } else if (annotationName.equals(PerPositionAnnotationListModel.ANNOTATION_POLYPHEN)) {
            String[][] kvp = new String[3][2];
            kvp[0][0] = "Description";
            kvp[0][1] = "Polyphen-2 predicts possible impact of an amino acid substitution on the structure and function of a human protein using straightforward physical and comparative considerations.";
            kvp[1][0] = "URL";
            kvp[1][1] = "http://genetics.bwh.harvard.edu/pph2/";
            kvp[2][0] = "Available Annotations";
            kvp[2][1] = "Homo Sapiens (hg19 exome)";
            details.add(ViewUtil.getKeyValuePairPanel(kvp),BorderLayout.NORTH);
        }
    }
    
}
