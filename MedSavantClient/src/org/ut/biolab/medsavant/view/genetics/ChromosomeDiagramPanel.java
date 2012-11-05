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
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.api.Listener;
import org.ut.biolab.medsavant.model.Chromosome;
import org.ut.biolab.medsavant.model.Range;
import org.ut.biolab.medsavant.vcf.VariantRecord;
import org.ut.biolab.medsavant.view.genetics.inspector.stat.StaticVariantInspector;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume, AndrewBrook
 */
public class ChromosomeDiagramPanel extends JPanel implements Listener<VariantRecord>  {

    private long scaleWRTLength;
    private final Chromosome chr;
    private List<RangeAnnotation> annotations;
    //private static final int BINSIZE = 15000000;
    private static final int AMAXBINSIZE = 60000000;
    //private List<Integer> binValues;
    private VariantRecord selectedVariant;

    public ChromosomeDiagramPanel(Chromosome c) {
        this.chr = c;
        this.setOpaque(false);

        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.setPreferredSize(new Dimension(20,999));
        this.setMaximumSize(new Dimension(20,999));
        annotations = new ArrayList<RangeAnnotation>();

        StaticVariantInspector.addVariantSelectionChangedListener(this);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        long cent = chr.getCentromerePos();
        if (cent == -1) {
            cent = chr.getLength()/2;
        }

        int centView = translateModelToView(cent, chr.getLength(),this.getEffectiveHeight());

        int bend = 20;

        int width = this.getWidth()-1;

        g2.setColor(Color.white);
        g2.fillRoundRect(0, 0, width, centView,bend,bend);
        g2.setColor(Color.gray);
        RoundRectangle2D rec1 = new RoundRectangle2D.Double(0, 0, width, centView,bend,bend);
        g2.draw(rec1);

        g2.setColor(Color.white);
        g2.fillRoundRect(0, centView, width, this.getEffectiveHeight()-centView,bend,bend);
        g2.setColor(Color.gray);
        RoundRectangle2D rec2 = new RoundRectangle2D.Double(0, centView, width, this.getEffectiveHeight()-centView,bend,bend);
        g2.draw(rec2);

        Area shape = new Area(rec1);
        shape.add(new Area(rec2));

        g2.clip(shape);
        for (RangeAnnotation a : annotations) {
            int viewStart = translateModelToView(a.getStart(), chr.getLength(), this.getEffectiveHeight());
            int viewEnd = translateModelToView(a.getEnd(), chr.getLength(), this.getEffectiveHeight());
            if (viewEnd-viewStart < 1) { viewEnd = viewStart+1; }
            g2.setColor(a.getColor());
            g2.fillRect(0, viewStart, width, viewEnd-viewStart);
        }

        if (this.selectedVariant != null && this.selectedVariant.getChrom().equals(chr.getName())) {
            //g2.setColor(new Color(69,172,237));
            g2.setColor(new Color(0,148,54));
            int pos = translateModelToView(this.selectedVariant.getPosition(), chr.getLength(), this.getEffectiveHeight());
            g2.fillRect(1, pos, width-1, 3);
        }
    }

    protected void setScaleWithRespectToLength(long len) {
        this.scaleWRTLength = len;
        repaint();
    }

    private int getEffectiveHeight() {
        return (int) ((this.getHeight()*(long)chr.getLength())/scaleWRTLength)-1;
    }

    private static int translateModelToView(long modelPosition, long totalViewSize, int totalModelSize) {
        return (int) (modelPosition*totalModelSize/totalViewSize);
    }

    public synchronized void setAnnotations(List<RangeAnnotation> annotations) {
        this.annotations = annotations;
        //repaint();
    }

    /*public int update(int totalNum){
        return updateAnnotations(totalNum);
    }*/

    public void updateFrequencyCounts(Map<Range,Integer> binCounts, int max) {

        List<RangeAnnotation> as = new ArrayList<RangeAnnotation>();
        if (binCounts != null) {
            for (Range r : binCounts.keySet()) {
                int count = binCounts.get(r);
                Color c = getBlendColour(count,max);


                as.add(new RangeAnnotation((long) r.getMin(), (long) r.getMax(), c));
            }
        }
        setAnnotations(as);
    }

    /*private Color getBlendColour(int count, int max) {
        float alpha = 0.15f + (0.85f * (float)Math.min(1.0, (double)count/(double) max));
        return new Color(0.0F, 0.7F, 0.87F, alpha);
    }
    *
    */

    /*private Color getBlendColour(int count, int max) {
        return new Color(248,128,128,(int)(255*(double)count/max));
    }
    *
    */


    private Color getBlendColour(int count, int max) {
        return new Color(69,172,237,(int)(255*(double)count/max));
    }

    /*private Color getBlendColour(int count, int max) {
        return new Color(255,0,0,(int)(255*(double)count/max));
    }*/




    /*private Color getBlendColour(int count, int max) {
        return createBlend((double)count/max);
    }
    *
    */

    private static final Color[] HEATMAP_COLORS = {  new Color(126,243,125) /*green*/, new Color(248,128,128) /*red*/ };

    private static Color createBlend(double val) {
        int i0 = 0;
        int i1 = 1;
        double w1 = val;
        double w0 = 1.0f - w1;

        return new Color((int)(HEATMAP_COLORS[i0].getRed() * w0 + HEATMAP_COLORS[i1].getRed() * w1), (int)(HEATMAP_COLORS[i0].getGreen() * w0 + HEATMAP_COLORS[i1].getGreen() * w1), (int)(HEATMAP_COLORS[i0].getBlue() * w0 + HEATMAP_COLORS[i1].getBlue() * w1));
    }

    /*
    public void updateAnnotations(int max, int binsize){


        List<RangeAnnotation> as = new ArrayList<RangeAnnotation>();

        int pos = 0;
        for(int i = 0; i < chr.getLength(); i += binsize){
            if(binValues.get(pos) > 0){
                int newMax = max;
                if(i + binsize > chr.getLength()){
                    newMax = (int)((double)max * ((double)(chr.getLength() - i) / (double)binsize));
                }
                float alpha = 0.15f + (0.85f * (float)Math.min(1.0, (double)binValues.get(pos) / (double) newMax));
                as.add(new RangeAnnotation(i, i + binsize, new Color(0.0f, 0.7f, 0.87f, alpha)));
            }
            pos++;
        }

        setAnnotations(as);

    }
     *
     */

    @Override
    public void handleEvent(VariantRecord r) {
        this.selectedVariant = r;
        repaint();
    }



}
