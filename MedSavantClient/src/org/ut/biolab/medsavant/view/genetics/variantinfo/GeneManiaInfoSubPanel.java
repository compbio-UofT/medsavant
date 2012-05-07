/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.variantinfo;


import java.awt.Color;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.vcf.VariantRecord;
import org.ut.biolab.medsavant.view.util.ViewUtil;
/**
 *
 * @author khushi
 */
public class GeneManiaInfoSubPanel extends InfoSubPanel {
    private final String name;

    public GeneManiaInfoSubPanel(){
        this.name= "Gene Inspector";
    }

    @Override
    public String getName(){
        return this.name;
    }

    @Override
    //What's the point of this??
    public boolean showHeader() {
        return false;
    }

     @Override
     public JPanel getInfoPanel() {
         JPanel p = new JPanel();
         return p;
     }

     @Override
    public void setInfoFor(VariantRecord r) {
        if (r.getDbSNPID() == null || r.getDbSNPID().equals("")) {
            //field.setText(r.getChrom() + " " + r.getPosition());
        } else {
            //field.setText(r.getDbSNPID());
        }
    }
}
