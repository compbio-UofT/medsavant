/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view;

import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.genetics.GeneticsListPage;
import org.ut.biolab.medsavant.view.genetics.GeneticsSummerizePage;
import org.ut.biolab.medsavant.view.menu.DualTab;
import org.ut.biolab.medsavant.view.menu.Tab;

/**
 *
 * @author mfiume
 */
public class SectionFactory {
    
    public static DualTab generatePatientsSection() {
        DualTab dt = new DualTab("Patients");
        
        Tab t0 = new Tab("Individuals", new JPanel());
        Tab t1 = new Tab("Cohort", new JPanel());
        
        dt.addTab(t0);
        dt.addTab(t1);
        
        return dt;
    }
    
    public static DualTab generateGeneticsSection() {
        DualTab dt = new DualTab("Genetics");
        
        Tab t0 = new Tab("Summary", (new GeneticsSummerizePage()).getView());
        Tab t1 = new Tab("List", (new GeneticsListPage()).getView());
        
        dt.addTab(t0);
        dt.addTab(t1);
        
        return dt;
    }
    
    public static DualTab generateAnnotateSection() {
        DualTab dt = new DualTab("Annotate");
        
        Tab t0 = new Tab("Gene Lists", new JPanel());
        Tab t1 = new Tab("Variant Harmfulness", new JPanel());
        
        dt.addTab(t0);
        dt.addTab(t1);
        
        return dt;
    }
}
