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
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.db.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.db.model.Chromosome;
import org.ut.biolab.medsavant.db.model.Range;
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
    
    public void updateFrequencyCounts(Map<Range,Integer> binCounts,int max){
        cdp.updateFrequencyCounts(binCounts,max);
    }
    
    public String getChrName(){
        return chr.getName();
    }
    
    public String getShortChrName(){
        return chr.getShortname();
    }

}
