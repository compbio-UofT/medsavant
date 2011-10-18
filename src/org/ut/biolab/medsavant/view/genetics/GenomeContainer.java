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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.util.query.VariantQueryUtil;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.db.model.Chromosome;
import org.ut.biolab.medsavant.model.record.Genome;
import org.ut.biolab.medsavant.util.MedSwingWorker;
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
    private static final int MINBINSIZE = 10000000;
    private static final int BINMULTIPLIER = 25;

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
    
    private class GetNumVariantsSwingWorker extends MedSwingWorker {

        private int maxRegion = 0;
        private int regionsDone = 0;
        private int activeThreads = 0;
        private final Object workerLock = new Object();
        
        public GetNumVariantsSwingWorker() {}
        
        @Override
        protected Object doInBackground() {  
            try {
                final int totalNum = VariantQueryUtil.getNumFilteredVariants(
                                        ProjectController.getInstance().getCurrentProjectId(), 
                                        ReferenceController.getInstance().getCurrentReferenceId(), 
                                        FilterController.getQueryFilterConditions());           
                final int binsize = (int)Math.min(249250621, Math.max((long)totalNum * BINMULTIPLIER, MINBINSIZE));

                for(final ChromosomePanel p : chrViews){
                    if(this.isCancelled()) return false;
                    
                    //limit of 5 threads at a time
                    synchronized(workerLock){
                        while(activeThreads > 5){
                            workerLock.wait();
                        }
                        activeThreads++;
                    }

                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            int region = p.createBins(totalNum, binsize);
                            synchronized(workerLock){
                                if(region > maxRegion) maxRegion = region;
                                regionsDone++;
                                activeThreads--;
                                workerLock.notifyAll();
                            }          
                        }
                    };
                    thread.start();
                } 

                //wait until all threads completed
                synchronized(workerLock){
                    while(regionsDone < chrViews.size()){
                        workerLock.wait();
                    }
                }

                //actually draw chromosomes
                for(ChromosomePanel p : chrViews){
                    if(this.isCancelled()) return false;
                    p.updateAnnotations(maxRegion, binsize);
                } 
            } catch (SQLException ex) {
                Logger.getLogger(GenomeContainer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                //
            }

            return true;            
        }
        
        @Override
        protected void done() {
            if (this.isCancelled()) {
                return;
            } else {
                showShowCard();
            }                        
        }      
    }
}
