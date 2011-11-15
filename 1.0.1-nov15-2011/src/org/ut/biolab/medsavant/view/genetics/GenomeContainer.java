/*
 *    Copyright 2011 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
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
import org.ut.biolab.medsavant.util.MedSavantWorker;

import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.db.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.model.Chromosome;
import org.ut.biolab.medsavant.db.util.query.VariantQueryUtil;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.model.record.Genome;
import org.ut.biolab.medsavant.view.ViewController;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
public class GenomeContainer extends JPanel implements FiltersChangedListener  {
    private static final Logger LOG = Logger.getLogger(GenomeContainer.class.getName());
        
    private Genome genome;
    private final JPanel chrContainer;
    private ArrayList<ChromosomePanel> chrViews;
    private static final String CARD_WAIT = "wait";
    private static final String CARD_SHOW = "show";
    private CardLayout cl;
    private static final int MINBINSIZE = 10000000;
    private static final int BINMULTIPLIER = 25;
    private final String pageName;
    
    private final Object updateLock = new Object();
    private boolean updateRequired = true;
    private boolean init = false;
    
    public GenomeContainer(String pageName) {
        this.pageName = pageName;
        
        cl = new CardLayout();
        this.setLayout(cl);
     
        chrContainer = ViewUtil.getClearPanel();
        chrContainer.setBorder(ViewUtil.getBigBorder());
        chrContainer.setLayout(new BoxLayout(chrContainer,BoxLayout.X_AXIS));
        this.add(chrContainer, CARD_SHOW);
               
        this.add(new WaitPanel("Generating Genome View", Color.WHITE), CARD_WAIT);

        FilterController.addFilterListener(this);
        init = true;
    }

    @Override
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

    /*public void filtersChanged() { 
        showWaitCard();
        GetNumVariantsSwingWorker gnv = new GetNumVariantsSwingWorker(pageName);
        gnv.execute();
    }*/
    
    public void filtersChanged() {        
        synchronized (updateLock){
            updateRequired = true;
        }
        if(ViewController.getInstance().getCurrentSectionView() != null && ViewController.getInstance().getCurrentSectionView().getName().equals(pageName)){
            updateIfRequired();
        }
    }
    
    public void updateIfRequired(){
        if(!init) return;
        boolean shouldUpdate = false;
        synchronized (updateLock){
            if(updateRequired){
                updateRequired = false;     
                shouldUpdate = true;
            }
        }
        if(shouldUpdate){
            showWaitCard();
            GetNumVariantsSwingWorker gnv = new GetNumVariantsSwingWorker(pageName);
            gnv.execute();
        }
    }
    
    private class GetNumVariantsSwingWorker extends MedSavantWorker implements FiltersChangedListener {

        private int maxRegion = 0;
        private int regionsDone = 0;
        private int activeThreads = 0;
        private final Object workerLock = new Object();
        
        public GetNumVariantsSwingWorker(String pageName) {
            super(pageName);
            FilterController.addActiveFilterListener(this);
        }
        
        @Override
        protected Object doInBackground() throws InterruptedException, SQLException {  
            final int totalNum = VariantQueryUtil.getNumFilteredVariants(
                                    ProjectController.getInstance().getCurrentProjectId(), 
                                    ReferenceController.getInstance().getCurrentReferenceId(), 
                                    FilterController.getQueryFilterConditions());           
            final int binsize = (int)Math.min(249250621, Math.max((long)totalNum * BINMULTIPLIER, MINBINSIZE));

            for (final ChromosomePanel p : chrViews){
                if(this.isThreadCancelled()) return null;

                //limit of 5 threads at a time
                synchronized (workerLock){
                    while(activeThreads > 5){
                        if(this.isThreadCancelled()) return null;
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
                    if(this.isThreadCancelled()) return null;
                    workerLock.wait();
                }
            }

            //actually draw chromosomes
            for(ChromosomePanel p : chrViews){
                if(this.isThreadCancelled()) return null;
                p.updateAnnotations(maxRegion, binsize);
            } 
            return true;            
        }
        
        /*@Override
        protected void done() {
            try {
                get();
                showShowCard();
            } catch (Exception x) {
                // TODO: #90
                LOG.log(Level.SEVERE, null, x);
            }
        } */     
        
        public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {
            if(!this.isDone()){
                this.cancel(true);
            }    
        }

        @Override
        protected void showProgress(double fraction) {
            //do nothing
        }

        @Override
        protected void showSuccess(Object result) {
            showShowCard();
        }
    }
}
