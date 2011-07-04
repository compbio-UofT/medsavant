/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.genetics;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.db.ConnectionController;
import org.ut.biolab.medsavant.db.MedSavantDatabase;
import org.ut.biolab.medsavant.db.QueryUtil;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.model.record.Chromosome;
import org.ut.biolab.medsavant.model.record.Genome;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
public class GenomeContainer extends JPanel implements FiltersChangedListener  {
        
    private Genome genome;
    private final JPanel chrContainer;
    private ArrayList<ChromosomePanel> chrViews;
    private static final String CARD_WAIT = "wait";
    private static final String CARD_SHOW = "show";
    private CardLayout cl;

    public GenomeContainer() {
        cl = new CardLayout();
        this.setLayout(cl);
     
        chrContainer = ViewUtil.getClearPanel();
        chrContainer.setBorder(ViewUtil.getBigBorder());
        chrContainer.setLayout(new BoxLayout(chrContainer,BoxLayout.X_AXIS));
        this.add(chrContainer, CARD_SHOW);
               
        this.add(new WaitPanel("Generating Genome View", Color.WHITE), CARD_WAIT);

        FilterController.addFilterListener(this);
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        GradientPaint p = new GradientPaint(0,0,Color.darkGray,0, this.getHeight(), Color.black);
        g2.setPaint(p);
        g2.fillRect(0, 0, this.getWidth(), this.getHeight());
    }

    public void setGenome(Genome g) {
        this.genome = g;
        setChromosomeViews();
        this.filtersChanged();
    }

    private void setChromosomeViews() {
        chrContainer.removeAll();

        chrViews = new ArrayList<ChromosomePanel>();
        long max = Long.MIN_VALUE;
        for (Chromosome c : genome.getChromosomes()) {
            chrViews.add(new ChromosomePanel(c));
            max = Math.max(max, c.getLength());
        }
        chrContainer.add(Box.createHorizontalGlue());
        for (ChromosomePanel cv : chrViews) {
            cv.setScaleWithRespectToLength(max);
            chrContainer.add(cv);
            chrContainer.add(ViewUtil.getMediumSeparator());
        }
        chrContainer.add(Box.createHorizontalGlue());

    }
    
    private synchronized void showWaitCard() {
        cl.show(this, CARD_WAIT);
              
    }

    private synchronized void showShowCard() {
        cl.show(this, CARD_SHOW);
    }

    public void filtersChanged() { 
        showWaitCard();
        GetNumVariantsSwingWorker gnv = new GetNumVariantsSwingWorker();
        gnv.execute();
    }
    
    private class GetNumVariantsSwingWorker extends SwingWorker {

        public GetNumVariantsSwingWorker() {}
        
        @Override
        protected Object doInBackground() throws Exception {
            int totalNum = QueryUtil.getNumFilteredVariants(ConnectionController.connect(), MedSavantDatabase.getInstance().getVariantTableSchema());
            for(ChromosomePanel p : chrViews){
                p.update(totalNum);
            }     
            return null;            
        }
        
        @Override
        protected void done() {
            showShowCard();
        }      
    }
}
