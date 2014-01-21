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
import java.awt.Component;
import javax.swing.JPanel;

import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.filter.FilterController;
import org.ut.biolab.medsavant.client.filter.FilterEvent;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.reference.ReferenceEvent;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.ThreadController;
import org.ut.biolab.medsavant.client.view.genetics.charts.ChartView;
import org.ut.biolab.medsavant.client.view.app.MultiSectionApp;
import org.ut.biolab.medsavant.client.view.app.AppSubSection;


/**
 *
 * @author mfiume
 */
public class GeneticsChartPage extends AppSubSection {

    private JPanel view;
    private ChartView chartView;
    private boolean isLoaded = false;

    public GeneticsChartPage(MultiSectionApp parent) {
        super(parent, "Charts");
        FilterController.getInstance().addListener(new Listener<FilterEvent>() {
            @Override
            public void handleEvent(FilterEvent event) {
                ThreadController.getInstance().cancelWorkers(pageName);
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
    public JPanel getView() {
        if (view == null) {
            try {
                view = new JPanel();
                view.setLayout(new BorderLayout());

                //PeekingPanel detailView = new PeekingPanel("Filters", BorderLayout.EAST, new FilterPanel(), true,400);
                //panel.add(detailView, BorderLayout.WEST);

                chartView = new ChartView(pageName);
                view.add(chartView, BorderLayout.CENTER);
            } catch (Exception ex) {
                ClientMiscUtils.reportError("Error creating chart view: %s", ex);
            }
        }
        chartView.updateIfRequired();
        return view;
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
    public void viewWillLoad() {
        isLoaded = true;
    }

    @Override
    public void viewDidUnload() {
        isLoaded = false;
        super.viewDidUnload();
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
