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

package org.ut.biolab.medsavant.view.genetics.filter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.controller.ResultController;
import org.ut.biolab.medsavant.db.FatalDatabaseException;
import org.ut.biolab.medsavant.db.NonFatalDatabaseException;
import org.ut.biolab.medsavant.listener.ReferenceListener;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.view.component.ProgressPanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
public class FilterEffectivenessPanel extends JLayeredPane implements FiltersChangedListener, ReferenceListener {

    Color bg = new Color(139, 149, 164);
    private final ProgressPanel pp;
    private final JLabel labelVariantsRemaining;
    //private final FilterHistoryPanel historyPanel;
    private GridBagConstraints c;
    private WaitPanel waitPanel;
    private int waitCounter = 0;
    private JPanel panel;

    public FilterEffectivenessPanel() {

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;

        this.setBorder(ViewUtil.getMediumBorder());

        this.setLayout(new GridBagLayout());

        panel = ViewUtil.getClearPanel();
        //panel.setBackground(bg);
        //panel.setBorder(BorderFactory.createCompoundBorder(ViewUtil.getTopLineBorder(),ViewUtil.getBigBorder()));
        panel.setLayout(new BorderLayout());
        this.add(panel, c, JLayeredPane.DEFAULT_LAYER);

        waitPanel = new WaitPanel("Applying Filters");
        waitPanel.setVisible(false);
        this.add(waitPanel, c, JLayeredPane.DRAG_LAYER);

        labelVariantsRemaining = ViewUtil.getDetailTitleLabel("");
        labelVariantsRemaining.setForeground(Color.white);

        JPanel infoPanel = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(infoPanel);
        infoPanel.add(ViewUtil.center(labelVariantsRemaining));

        JLabel l = ViewUtil.getWhiteLabel("variants pass search conditions");
        ViewUtil.makeSmall(l);
        infoPanel.add(ViewUtil.center(l));
        infoPanel.setBorder(ViewUtil.getMediumTopHeavyBorder());

        panel.add(infoPanel,BorderLayout.NORTH);

        pp = new ProgressPanel();
        //pp.setBorder(ViewUtil.getBigBorder());
        panel.add(pp, BorderLayout.SOUTH);

        FilterController.addFilterListener(this);
        ReferenceController.getInstance().addReferenceListener(this);

        Thread t = new Thread(){
            @Override
            public void run() {
                setMaxValues();
            }
        };
        t.start();
    }

    @Override
    public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {

        //final IndeterminateProgressDialog dialog = new IndeterminateProgressDialog(
        //        "Applying Filter",
        //        "Filter is being applied. Please wait.",
        //        true);

        Thread thread = new Thread() {

            @Override
            public void run() {
                showWaitCard();
                try {
                    int numLeft = ResultController.getInstance().getNumFilteredVariants();
                    showShowCard();
                    //dialog.close();
                    setNumLeft(numLeft);
                } catch (NonFatalDatabaseException ex) {
                    showShowCard();
                    Logger.getLogger(FilterHistoryPanel.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        };

        thread.start();


        //dialog.setVisible(true);

    }

    long numLeft = 1;
    long numTotal = 1;

    private void setNumLeft(int num) {
        numLeft = num;
        refreshProgressLabel();

        pp.animateToValue(num);
    }

    private void setMaxValues() {

        labelVariantsRemaining.setText("Calculating...");
        updateUI();

        int maxRecords = -1;

        try {
            maxRecords = ResultController.getInstance().getNumTotalVariants();
        } catch (Exception ex) {
            Logger.getLogger(FilterHistoryPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        numTotal = maxRecords;

        if (maxRecords != -1) {
            pp.setMaxValue(maxRecords);
            pp.setToValue(maxRecords);
            //labelVariantsTotal.setText("TOTAL: " + ViewUtil.numToString(maxRecords));

            setNumLeft(maxRecords);
        }
    }

    @Override
    public void referenceAdded(String name) {}

    @Override
    public void referenceRemoved(String name) {}

    @Override
    public void referenceChanged(String name) {
        setMaxValues();
    }

    public synchronized void showWaitCard() {
        waitCounter++;
        waitPanel.setVisible(true);
        this.setLayer(waitPanel, JLayeredPane.DRAG_LAYER);
        waitPanel.repaint();
    }

    public synchronized void showShowCard() {
        waitCounter--;
        if(waitCounter <= 0){
            waitPanel.setVisible(false);
            waitCounter = 0;
        }
    }

    private void refreshProgressLabel() {
        long percent = 100;
        if (numTotal > 0) {
            percent = (numLeft * 100) / numTotal;
        }
        labelVariantsRemaining.setText(String.format("%,d (%d%%)", numLeft, percent));
    }
}
