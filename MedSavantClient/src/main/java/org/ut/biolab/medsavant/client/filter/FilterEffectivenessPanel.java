/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.filter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.concurrent.Semaphore;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.MedSavantClient;

import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.controller.ResultController;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.reference.ReferenceEvent;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.client.view.component.WaitPanel;

/**
 *
 * @author mfiume
 */
public class FilterEffectivenessPanel extends JLayeredPane {

    private static final Log LOG = LogFactory.getLog(FilterEffectivenessPanel.class);
    int numLeft = 1;
    int numTotal = 1;
    private int waitCounter = 0;
    //private final ProgressPanel progressPanel;
    private final JLabel labelVariantsRemaining;
    private WaitPanel waitPanel;
    private JPanel panel;

    private static Listener<FilterEvent> filterEventListener = null;
    private static Listener<ReferenceEvent> referenceEventListener = null;

    //private static int sid = 0; //debug variable
    public FilterEffectivenessPanel() {
        this(Color.black);

    }

    public FilterEffectivenessPanel(Color foregroundColor) {

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        this.setPreferredSize(new Dimension(280, 80));
        this.setMaximumSize(new Dimension(280, 80));

        setLayout(new GridBagLayout());

        waitPanel = new WaitPanel("Applying Filters");
        waitPanel.setVisible(false);
        add(waitPanel, gbc, JLayeredPane.DRAG_LAYER);

        panel = ViewUtil.getClearPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(ViewUtil.getMediumBorder());
        //panel.setPreferredSize(waitPanel.getPreferredSize());
        add(panel, gbc, JLayeredPane.DEFAULT_LAYER);

        labelVariantsRemaining = ViewUtil.getDetailTitleLabel("");
        labelVariantsRemaining.setForeground(foregroundColor);

        JPanel infoPanel = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(infoPanel);

        final JLabel a = new JLabel("");
        a.setForeground(foregroundColor);
        ViewUtil.makeSmall(a);
        infoPanel.add(ViewUtil.centerHorizontally(a));

        Listener<FilterEvent> fe = new Listener<FilterEvent>() {
            @Override
            public void handleEvent(FilterEvent event) {
                try {

                    if (MedSavantClient.VariantManager.willApproximateCountsForConditions(
                            LoginController.getSessionID(),
                            ProjectController.getInstance().getCurrentProjectID(),
                            ReferenceController.getInstance().getCurrentReferenceID(),
                            FilterController.getInstance().getAllFilterConditions())) {
                        a.setText("approximately");
                    } else {
                        a.setText("");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

        fe.handleEvent(null);
        FilterController.getInstance().addListener(fe);

        infoPanel.add(ViewUtil.centerHorizontally(labelVariantsRemaining));

        JLabel l = new JLabel("of all variants pass search conditions");
        l.setForeground(foregroundColor);
        ViewUtil.makeSmall(l);
        infoPanel.add(ViewUtil.centerHorizontally(l));
        infoPanel.setBorder(ViewUtil.getMediumTopHeavyBorder());

        panel.add(infoPanel, BorderLayout.NORTH);

        initListeners();

    }

    private synchronized void initListeners() {
        //progressPanel = new ProgressPanel();
        //pp.setBorder(ViewUtil.getBigBorder());
        //panel.add(progressPanel, BorderLayout.SOUTH);
        //Clear out old event listeners.
        if (filterEventListener != null) {
            FilterController.getInstance().removeListener(filterEventListener);
        }
        if (referenceEventListener != null) {
            ReferenceController.getInstance().removeListener(referenceEventListener);
        }
        setMaxValues();
        updateNumRemaining();
        filterEventListener = new Listener<FilterEvent>() {
            //private int i = sid++;
            @Override
            public void handleEvent(FilterEvent event) {
                //System.out.println(i + ": Got filterevent " + event);
                updateNumRemaining();
            }
        };

        FilterController.getInstance().addListener(filterEventListener);

        referenceEventListener = new Listener<ReferenceEvent>() {
            @Override
            public void handleEvent(ReferenceEvent event) {
                if (event.getType() == ReferenceEvent.Type.CHANGED) {
                    setMaxValues();
                }
            }
        };
        ReferenceController.getInstance().addListener(referenceEventListener);
    }

    private Semaphore updateSem = new Semaphore(1);

    public final void updateNumRemaining() {
        showWaitCard();

        new MedSavantWorker<Integer>("Filters") {
            @Override
            protected Integer doInBackground() throws Exception {
                updateSem.acquire();
                return ResultController.getInstance().getFilteredVariantCount();
            }

            @Override
            protected void showProgress(double fraction) {
            }

            @Override
            protected void showSuccess(Integer result) {
                showShowCard();
                setNumLeft(result);
                updateSem.release();
            }

            @Override
            protected void showFailure(Throwable ex) {
                showShowCard();
                LOG.error("Error getting filtered variant count.", ex);
                updateSem.release();
            }
        }.execute();
    }

    private void setNumLeft(int num) {
        numLeft = num;
        refreshProgressLabel();
    }

    private void setMaxValues() {
        labelVariantsRemaining.setText("Calculating...");

        new MedSavantWorker<Integer>("Filters") {
            @Override
            protected void showFailure(Throwable ex) {
                showShowCard();
                LOG.error("Error getting total variant count.", ex);
                updateSem.release();
            }

            @Override
            protected void showProgress(double fraction) {
            }

            @Override
            protected void showSuccess(Integer result) {
                numTotal = result;
                //progressPanel.setMaxValue(numTotal);
                //progressPanel.setToValue(numTotal);
                setNumLeft(numTotal);
                updateSem.release();
            }

            @Override
            protected Integer doInBackground() throws Exception {
                updateSem.acquire();
                return ResultController.getInstance().getTotalVariantCount();
            }
        }.execute();

    }

    public synchronized void showWaitCard() {
        waitCounter++;
        waitPanel.setVisible(true);
        setLayer(waitPanel, JLayeredPane.DRAG_LAYER);
        waitPanel.repaint();
    }

    public synchronized void showShowCard() {
        waitCounter--;
        if (waitCounter <= 0) {
            waitPanel.setVisible(false);
            waitCounter = 0;
        }
    }

    private void refreshProgressLabel() {
        //System.out.println("Refresh progress label " + numTotal + ", " + numLeft);
        double percent = 100.0;
        if (numTotal > 0) {
            percent = (numLeft * 100.0) / numTotal;
        }
        labelVariantsRemaining.setText(String.format("%,d (%.1f%%)", numLeft, percent));
        panel.revalidate();
        panel.repaint();

    }
}
