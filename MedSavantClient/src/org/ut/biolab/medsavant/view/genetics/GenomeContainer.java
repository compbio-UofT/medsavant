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

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.db.FatalDatabaseException;
import org.ut.biolab.medsavant.db.NonFatalDatabaseException;
import org.ut.biolab.medsavant.model.Chromosome;
import org.ut.biolab.medsavant.model.Range;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.model.record.Genome;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
public class GenomeContainer extends JLayeredPane {

    private static final Logger LOG = Logger.getLogger(GenomeContainer.class.getName());
    private Genome genome;
    private final JPanel chrContainer;
    private ArrayList<ChromosomePanel> chrViews;
    private WaitPanel waitPanel;
    private JPanel chrPlusButtonContainer;
    /*private static final int MINBINSIZE = 1000000;
    private static final int BINMULTIPLIER = 10;*/
    private final String pageName;
    private final Object updateLock = new Object();
    private boolean updateRequired = true;
    private boolean init = false;
    private GridBagConstraints c;
    private GetNumVariantsSwingWorker gnv;

    public GenomeContainer(String pageName, Genome g) {
        this.pageName = pageName;

        this.setLayout(new GridBagLayout());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;

        chrPlusButtonContainer = new JPanel();
        chrPlusButtonContainer.setBackground(ViewUtil.getTertiaryMenuColor());
        chrPlusButtonContainer.setLayout(new BorderLayout());

        chrContainer = ViewUtil.getClearPanel();
        chrContainer.setBorder(ViewUtil.getBigBorder());
        chrContainer.setLayout(new BoxLayout(chrContainer, BoxLayout.X_AXIS));

        chrPlusButtonContainer.add(chrContainer, BorderLayout.CENTER);

        /*
        JButton savantButton = new JButton("Export to Savant Genome Browser");
        savantButton.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
        new SavantExportForm();
        }
        });

        chrPlusButtonContainer.add(ViewUtil.alignRight(ViewUtil.alignLeft(savantButton)),BorderLayout.SOUTH);
         *
         */

        this.add(chrPlusButtonContainer, c, JLayeredPane.DEFAULT_LAYER);

        waitPanel = new WaitPanel("Generating Genome View");
        this.add(waitPanel, c, JLayeredPane.PALETTE_LAYER);

        init = true;
        setGenome(g);

        updateIfRequired();

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent ce) {
                updateIfRequired();
            }

            @Override
            public void componentHidden(ComponentEvent ce) {
                if (gnv != null && !gnv.isDone()) {
                    gnv.cancel(true);
                }
            }
        });
    }

    public final void setGenome(Genome g) {
        this.genome = g;
        setChromosomeViews();
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
        waitPanel.setVisible(true);
        this.setLayer(waitPanel, JLayeredPane.MODAL_LAYER);
        waitPanel.repaint();
    }

    private synchronized void showShowCard() {
        waitPanel.setVisible(false);
        chrPlusButtonContainer.repaint();
    }

    /*public void filtersChanged() {
    showWaitCard();
    GetNumVariantsSwingWorker gnv = new GetNumVariantsSwingWorker(pageName);
    gnv.execute();
    }*/
    void setUpdateRequired(boolean b) {
        updateRequired = b;
    }

    public final void updateIfRequired() {
        if (!init) {
            return;
        }

        boolean shouldUpdate = false;
        synchronized (updateLock) {
            if (updateRequired && this.isVisible() && this.getSize().getWidth() != 0) {
                updateRequired = false;
                shouldUpdate = true;
            }
        }
        if(shouldUpdate){
            showWaitCard();
            gnv = new GetNumVariantsSwingWorker(pageName);
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
        protected Object doInBackground() throws InterruptedException, SQLException, RemoteException {
            long start = System.currentTimeMillis();
            final Map<String, Map<Range, Integer>> map = MedSavantClient.VariantQueryUtilAdapter.getChromosomeHeatMap(
                    LoginController.sessionId,
                    ProjectController.getInstance().getCurrentProjectId(),
                    ReferenceController.getInstance().getCurrentReferenceId(),
                    FilterController.getQueryFilterConditions(),
                    3000000);
            long time = System.currentTimeMillis() - start;

            int mmax = 0;
            for (String s : map.keySet()) {
                for (Range r : map.get(s).keySet()) {
                    int val = map.get(s).get(r);
                    mmax = (val > mmax) ? val : mmax;
                }
            }

            final int max = mmax;

            for(ChromosomePanel p : chrViews) {
                Map<Range, Integer> m = map.get(p.getChrName());
                if(m == null) m = map.get(p.getShortChrName());
                p.updateFrequencyCounts(m, max);
            }

            showShowCard();
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
        @Override
        public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {
            if (!this.isDone()) {
                this.cancel(true);
            }
        }

        @Override
        protected void showProgress(double fraction) {
            //do nothing
        }

        @Override
        protected void showSuccess(Object result) {
            //TODO: why isn't this always called??
            showShowCard();
        }
    }
}
