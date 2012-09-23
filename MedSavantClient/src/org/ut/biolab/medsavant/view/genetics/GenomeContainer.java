/*
 *    Copyright 2011-2012 University of Toronto
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

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.api.Listener;
import org.ut.biolab.medsavant.filter.FilterController;
import org.ut.biolab.medsavant.filter.FilterEvent;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.Chromosome;
import org.ut.biolab.medsavant.model.Range;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.reference.ReferenceController;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.util.WaitPanel;


/**
 *
 * @author mfiume
 */
public class GenomeContainer extends JLayeredPane {

    private Chromosome[] genome;
    private final JPanel chrContainer;
    private ArrayList<ChromosomePanel> chrViews;
    private WaitPanel waitPanel;
    private JPanel chrPlusButtonContainer;
    private final String pageName;
    private final Object updateLock = new Object();
    private boolean updateRequired = true;
    private boolean init = false;
    private GetNumVariantsSwingWorker gnv;

    public GenomeContainer(String pageName, Chromosome[] g) {
        this.pageName = pageName;

        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        chrPlusButtonContainer = new JPanel();
        //chrPlusButtonContainer.setBackground(ViewUtil.getTertiaryMenuColor());
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

        add(chrPlusButtonContainer, gbc, JLayeredPane.DEFAULT_LAYER);

        waitPanel = new WaitPanel("Generating Genome View");
        add(waitPanel, gbc, JLayeredPane.PALETTE_LAYER);

        init = true;
        genome = g;
        setChromosomeViews();

        updateIfRequired();

        addComponentListener(new ComponentAdapter() {
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

    private void setChromosomeViews() {
        chrContainer.removeAll();

        chrViews = new ArrayList<ChromosomePanel>();
        long max = Long.MIN_VALUE;
        for (Chromosome c : genome) {
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
    public void setUpdateRequired(boolean b) {
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

    private class GetNumVariantsSwingWorker extends MedSavantWorker {

        GetNumVariantsSwingWorker(String pageName) {
            super(pageName);
            FilterController.getInstance().addListener(new Listener<FilterEvent>() {
                @Override
                public void handleEvent(FilterEvent event) {
                    if (!isDone()) {
                        cancel(true);
                    }
                }
            });
        }

        @Override
        protected Object doInBackground() throws InterruptedException, SQLException, RemoteException {
            long start = System.currentTimeMillis();
            final Map<String, Map<Range, Integer>> map = MedSavantClient.VariantManager.getChromosomeHeatMap(
                    LoginController.sessionId,
                    ProjectController.getInstance().getCurrentProjectID(),
                    ReferenceController.getInstance().getCurrentReferenceID(),
                    FilterController.getInstance().getAllFilterConditions(),
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
