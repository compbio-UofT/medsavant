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

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JPanel;

import org.ut.biolab.medsavant.api.Listener;
import org.ut.biolab.medsavant.filter.FilterController;
import org.ut.biolab.medsavant.filter.FilterEvent;
import org.ut.biolab.medsavant.reference.ReferenceController;
import org.ut.biolab.medsavant.reference.ReferenceEvent;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.util.ThreadController;
import org.ut.biolab.medsavant.view.genetics.charts.ChartView;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;


/**
 *
 * @author mfiume
 */
public class GeneticsChartPage extends SubSectionView {

    private JPanel panel;
    private ChartView chartView;
    private boolean isLoaded = false;

    public GeneticsChartPage(SectionView parent) {
        super(parent);
        FilterController.getInstance().addListener(new Listener<FilterEvent>() {
            @Override
            public void handleEvent(FilterEvent event) {
                ThreadController.getInstance().cancelWorkers(getName());
                tryUpdate();
            }
        });
        ReferenceController.getInstance().addListener(new Listener<ReferenceEvent>() {
            @Override
            public void handleEvent(ReferenceEvent event) {
                if (event.getType() == ReferenceEvent.Type.CHANGED) {
                    tryUpdate();
                }
            }
        });
    }

    @Override
    public String getName() {
        return "Chart";
    }

    @Override
    public JPanel getView(boolean update) {
        try {
            if (panel == null || update) {
                setPanel();
            }
            chartView.updateIfRequired();
        } catch (Exception ex) {
            ClientMiscUtils.reportError("Error creating chart view: %s", ex);
        }
        return panel;
    }

    private void setPanel() throws RemoteException, SQLException {

        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        //PeekingPanel detailView = new PeekingPanel("Filters", BorderLayout.EAST, new FilterPanel(), true,400);
        //panel.add(detailView, BorderLayout.WEST);

        chartView = new ChartView(getName());
        panel.add(chartView, BorderLayout.CENTER);
    }

    @Override
    public Component[] getSubSectionMenuComponents() {
        /*
        Component[] cs = new Component[1];
        JButton addButton = new JButton("Add chart");
        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                cc.addChart();
                cc.updateUI();
            }
        });
        cs[0] = addButton;
        return cs;
         *
         */
        return null;
    }

    @Override
    public void viewDidLoad() {
        isLoaded = true;
    }

    @Override
    public void viewDidUnload() {
        isLoaded = false;
        ThreadController.getInstance().cancelWorkers(getName());
    }

    private void tryUpdate() {
        if (chartView != null) {
            chartView.setUpdateRequired(true);
            if (isLoaded) {
                chartView.updateIfRequired();
            }
        }
    }
}
