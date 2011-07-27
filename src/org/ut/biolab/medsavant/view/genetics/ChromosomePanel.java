/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.genetics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.sql.SQLException;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.model.record.Chromosome;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class ChromosomePanel extends JPanel {

    private final Chromosome chr;
    private final ChromosomeDiagramPanel cdp;

    public ChromosomePanel(Chromosome c) {
        this.setOpaque(false);
        this.chr = c;
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        JLabel shortLabel = new JLabel(chr.getShortname());
        shortLabel.setForeground(Color.white);
        this.add(shortLabel);
        this.add(ViewUtil.getSmallVerticalSeparator());
        cdp = new ChromosomeDiagramPanel(c);
        this.add(cdp);
        this.setPreferredSize(new Dimension(20,999));
        this.setMaximumSize(new Dimension(20,999));
    }

    void setScaleWithRespectToLength(long len) {
        cdp.setScaleWithRespectToLength(len);
    }
    
    /*public void update(int totalNum){
        cdp.update(totalNum);
    }*/
    
    public int createBins(int totalNum, int binsize){
        return cdp.createBins(totalNum, binsize);
    }
    
    public void updateAnnotations(int max, int binsize){
        cdp.updateAnnotations(max, binsize);
    }
    /*public int getNumBins(int binsize){
        return (int)(chr.getLength()/binsize + 1);
    }*/
    
    public String getChrName(){
        return chr.getName();
    }

}
