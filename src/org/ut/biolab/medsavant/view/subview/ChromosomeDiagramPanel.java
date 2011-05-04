/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.subview;

import fiume.vcf.VariantRecord;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ResultController;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.model.record.Chromosome;

/**
 *
 * @author mfiume
 */
public class ChromosomeDiagramPanel extends JPanel implements FiltersChangedListener {

    private long scaleWRTLength;
    private final Chromosome chr;
    private List<RangeAnnotation> annotations;

    public ChromosomeDiagramPanel(Chromosome c) {
        this.chr = c;
        this.setOpaque(false);
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.setPreferredSize(new Dimension(20,999));
        this.setMaximumSize(new Dimension(20,999));
        updateAnnotations();
        FilterController.addFilterListener(this);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        long cent = chr.getCentromerepos();
        if (cent == -1) {
            cent = chr.getLength()/2;
        }
        
        int centView = translateModelToView(cent, chr.getLength(), this.getEffectiveHeight());
        GradientPaint p = new GradientPaint(0,0,Color.white,0, this.getHeight(), Color.lightGray);

        g2.setPaint(p);
        g2.fillRoundRect(0, 0, this.getWidth(), centView,20,20);
        g2.setColor(Color.gray);
        g2.drawRoundRect(0, 0, this.getWidth(), centView,20,20);

        g2.setPaint(p);
        g2.fillRoundRect(0, centView, this.getWidth(), this.getEffectiveHeight()-centView,20,20);
        g2.setColor(Color.gray);
        g2.drawRoundRect(0, centView, this.getWidth(), this.getEffectiveHeight()-centView,20,20);

        for (RangeAnnotation a : annotations) {
            int viewStart = translateModelToView(a.getStart(), chr.getLength(), this.getEffectiveHeight());
            int viewEnd = translateModelToView(a.getEnd(), chr.getLength(), this.getEffectiveHeight());
            if (viewEnd-viewStart < 1) { viewEnd = viewStart+1; }
            g2.setColor(a.getColor());
            g2.fillRect(0, viewStart, this.getWidth(), viewEnd);
        }
    }

    protected void setScaleWithRespectToLength(long len) {
        this.scaleWRTLength = len;
        repaint();
    }

    private int getEffectiveHeight() {
        return (int) ((this.getHeight()*chr.getLength())/scaleWRTLength);
    }

    private static int translateModelToView(long modelPosition, long totalViewSize, int totalModelSize) {
        return (int) (modelPosition*totalModelSize/totalViewSize);
    }

    public void setAnnotations(List<RangeAnnotation> annotations) {
        this.annotations = annotations;
    }

    public void filtersChanged() {
        updateAnnotations();
    }

    private void updateAnnotations() {
        List<VariantRecord> rs = ResultController.getInstance().getFilteredVariantRecords();
        List<RangeAnnotation> as = new ArrayList<RangeAnnotation>();
        for (VariantRecord r : rs) {
            if (r.getChrom().equals(chr.getName())) {
                as.add(new RangeAnnotation(r.getPos(),r.getPos(),new Color(0,178,222,50)));
            }
        }
        setAnnotations(as);
        repaint();
    }

}
