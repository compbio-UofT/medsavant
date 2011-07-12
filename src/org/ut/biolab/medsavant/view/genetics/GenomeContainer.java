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
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.db.ConnectionController;
import org.ut.biolab.medsavant.db.MedSavantDatabase;
import org.ut.biolab.medsavant.db.QueryUtil;
import org.ut.biolab.medsavant.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.model.record.Chromosome;
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

        public GetNumVariantsSwingWorker() {}
        
        @Override
        protected Object doInBackground() {  
            try {
                int totalNum = QueryUtil.getNumFilteredVariants(ConnectionController.connect());                 
                int binsize = (int)Math.min(249250621, Math.max((long)totalNum * BINMULTIPLIER, MINBINSIZE));
                int maxRegion = 0;
                for(ChromosomePanel p : chrViews){
                    if(this.isCancelled()) return false;
                    int region = p.createBins(totalNum, binsize);
                    if(region > maxRegion) maxRegion = region;
                } 
                for(ChromosomePanel p : chrViews){
                    if(this.isCancelled()) return false;
                    p.updateAnnotations(maxRegion, binsize);
                } 
            } catch (SQLException ex) {
                Logger.getLogger(GenomeContainer.class.getName()).log(Level.SEVERE, null, ex);
            } catch(NonFatalDatabaseException ex){
                Logger.getLogger(GenomeContainer.class.getName()).log(Level.SEVERE, null, ex);
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
            /*try {
                boolean result = (Boolean)get();
                if(result) showShowCard();
            } catch (InterruptedException ex) {
                Logger.getLogger(GenomeContainer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(GenomeContainer.class.getName()).log(Level.SEVERE, null, ex);
            }*/
                        
        }      
    }
}
