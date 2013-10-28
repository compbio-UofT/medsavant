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
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import org.ut.biolab.medsavant.client.controller.AnalyticsController;
import org.ut.biolab.medsavant.client.controller.AnalyticsController.Analysis;
import org.ut.biolab.medsavant.client.view.component.BlockingPanel;
import org.ut.biolab.medsavant.client.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.client.view.list.DetailedView;
import org.ut.biolab.medsavant.client.view.list.SimpleDetailedListModel;
import org.ut.biolab.medsavant.client.view.list.SplitScreenView;
import org.ut.biolab.medsavant.client.view.subview.SectionView;
import org.ut.biolab.medsavant.client.view.subview.SubSectionView;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class AnalyticsPage extends SubSectionView {

    private JPanel view;
    //private FamilyMattersOptionView fo;
    public static final String PAGE_NAME = "Analytics";
    private final AnalyticsController controller;

    public AnalyticsPage(SectionView parent) {
        super(parent, PAGE_NAME);
        controller = AnalyticsController.getInstance();
    }

    @Override
    public JPanel getView() {
        if (view == null) {
            view = new SplitScreenView(
                    new SimpleDetailedListModel<Analysis>("Analyses") {
                        @Override
                        public Analysis[] getData() throws Exception {
                            List<Analysis> analytics = controller.getVariantAnalytics();
                            Analysis[] a = new Analysis[analytics.size()];
                            for (int i = 0; i < analytics.size(); i++) {
                                a[i] = analytics.get(i);
                            }
                            return a;
                        }
                    },
                    getDetailedView(),
                    getListEditor());
        }
        return view;
    }
    Analysis selectedAnalysis;

    private DetailedView getDetailedView() {
        return new DetailedView("Analyses") {
            @Override
            public void setSelectedItem(Object[] selectedRow) {
                stopCurrentAnalysis();
                if (selectedRow == null) {
                    block("No analysis selected");
                    selectedAnalysis = null;
                } else {
                    Analysis a = (Analysis) selectedRow[0];
                    selectedAnalysis = a;
                    setContent(a.getContent());
                }
            }

            @Override
            public void setMultipleSelections(List<Object[]> selectedRows) {
                stopCurrentAnalysis();
                if (selectedRows.isEmpty()) {
                    block("No analysis selected");
                } else {
                    block("Select one analysis");
                }
                selectedAnalysis = null;
            }

            @Override
            public JPopupMenu createPopup() {
                return new JPopupMenu();
            }

            private void setContent(JPanel content) {
                this.removeAll();
                this.setLayout(new BorderLayout());
                this.add(content, BorderLayout.CENTER);
                this.updateUI();
            }

            private void block(String msg) {
                this.removeAll();
                this.setLayout(new BorderLayout());
                BlockingPanel blockPanel = new BlockingPanel(msg, new JPanel());
                blockPanel.block();
                this.add(blockPanel, BorderLayout.CENTER);
                this.updateUI();
            }
        };
    }

    private DetailedListEditor getListEditor() {
        return new DetailedListEditor();
    }

    public void update() {
    }

    private void stopCurrentAnalysis() {
        if (selectedAnalysis != null) {
            selectedAnalysis.stop();
        }
    }

    @Override
    public void viewDidLoad() {
        super.viewDidLoad();
        if (selectedAnalysis != null) {
            selectedAnalysis.restart();
        }
    }

    @Override
    public void viewDidUnload() {
        super.viewDidUnload();
        stopCurrentAnalysis();
    }
}
