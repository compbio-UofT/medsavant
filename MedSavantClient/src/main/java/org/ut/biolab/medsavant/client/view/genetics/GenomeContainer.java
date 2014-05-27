/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.view.genetics;

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
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.filter.FilterController;
import org.ut.biolab.medsavant.client.filter.FilterEvent;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.model.Chromosome;
import org.ut.biolab.medsavant.shared.model.Range;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.client.view.component.WaitPanel;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

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
        if (shouldUpdate) {
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
            try {
                long start = System.currentTimeMillis();
                final Map<String, Map<Range, Integer>> map = MedSavantClient.VariantManager.getChromosomeHeatMap(
                        LoginController.getSessionID(),
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
                for (ChromosomePanel p : chrViews) {
                    Map<Range, Integer> m = map.get(p.getChrName());
                    if (m == null) {
                        m = map.get(p.getShortChrName());
                    }
                    p.updateFrequencyCounts(m, max);
                }

                showShowCard();
                return true;
            } catch (SessionExpiredException ex) {
                MedSavantExceptionHandler.handleSessionExpiredException(ex);
                return null;
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
