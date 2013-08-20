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
package org.ut.biolab.medsavant.client.view.genetics;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.filter.FilterController;
import org.ut.biolab.medsavant.client.filter.FilterEvent;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.reference.ReferenceEvent;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.ThreadController;
import org.ut.biolab.medsavant.client.view.SplitScreenPanel;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;
import org.ut.biolab.medsavant.client.view.genetics.inspector.ComprehensiveInspector;
import org.ut.biolab.medsavant.client.view.genetics.inspector.stat.StaticInspectorPanel;
import org.ut.biolab.medsavant.client.view.subview.SectionView;
import org.ut.biolab.medsavant.client.view.subview.SubSectionView;
import org.ut.biolab.medsavant.client.view.util.PeekingPanel;
import org.ut.biolab.medsavant.client.view.component.WaitPanel;

/**
 *
 * @author mfiume
 */
public class GeneticsTablePage extends SubSectionView{

    private static final Log LOG = LogFactory.getLog(GeneticsTablePage.class);
    private Thread viewPreparationThread;
    private JPanel view;    
    private JPanel outerTablePanel;
    private TablePanel tablePanel;
    private Component[] settingComponents;
    private PeekingPanel detailView;        
    
    //SplitScreenAdapter(JPanel panel, SplitScreenAdapter.Location location){
    @Override
    public void clearSelection(){
        if(tablePanel != null){
            tablePanel.clearSelection();
        }
    }
    
    public GeneticsTablePage(SectionView parent) {
        super(parent, "Spreadsheet");
        FilterController.getInstance().addListener(new Listener<FilterEvent>() {
            @Override
            public void handleEvent(FilterEvent event) {
                queueTableUpdate();
            }
        });
        ReferenceController.getInstance().addListener(new Listener<ReferenceEvent>() {
            @Override
            public void handleEvent(ReferenceEvent event) {
                if (event.getType() == ReferenceEvent.Type.CHANGED) {
                    queueTableUpdate();
                }
            }
        });
    }

    @Override
    public Component[] getSubSectionMenuComponents() {
        if (settingComponents == null) {
            settingComponents = new Component[1];
            try {
                viewPreparationThread.join();
            } catch (Exception e) {
                System.err.println(e);
            }

            if (detailView == null) {
                System.err.println("detailView is null!");
            }
            settingComponents[0] = PeekingPanel.getToggleButtonForPanel(detailView, "Inspector");
        }
        return settingComponents;
    }
    
    
    /**
     * Splits the main table view into an upper and lower section.
     * The upper section contains the main table pane, and the lower section
     * contains the given JPanel.     
     */    
    /*
    @Override
    public void splitScreen(JPanel p){
        split = true;
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tablePanel, p);
        splitPane.setResizeWeight(1);        
        outerTablePanel.removeAll();
        outerTablePanel.add(splitPane);
        outerTablePanel.revalidate();
        outerTablePanel.repaint();
    }
    
    @Override
    public void unsplitScreen(){
        split = false;
        outerTablePanel.removeAll();
        outerTablePanel.add(tablePanel);
        outerTablePanel.revalidate();
        outerTablePanel.repaint();
    }
    
    @Override
    public boolean isSplit(){
        return split;
    }*/

    @Override
    public JPanel getView() {
        try {
            if (view == null) {

                view = new JPanel();
                view.setLayout(new BorderLayout());
                view.add(new WaitPanel("Preparing Spreadsheet..."));

                Runnable prepareViewInBackground = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            LOG.debug("Running thread prepareViewINBackground!");
                            final JPanel tmpView = new JPanel();
                            tmpView.setLayout(new BorderLayout());

                            tablePanel = new TablePanel(pageName);
                            SplitScreenPanel ssp = new SplitScreenPanel(tablePanel);
                            
                            final ComprehensiveInspector inspectorPanel = 
                                    new ComprehensiveInspector(true, true, true, true, true, true, true, ssp);
                            
                            inspectorPanel.addSelectionListener(new Listener<Object>(){
                                @Override
                                public void handleEvent(Object event) {
                                    clearSelection();
                                }                                
                            });
                                                        
                            TablePanel.addVariantSelectionChangedListener(new Listener<VariantRecord>() {
                                @Override
                                public void handleEvent(final VariantRecord r) {
                                    inspectorPanel.setVariantRecord(r);
                                }
                            });
                            LOG.debug("Constructing detailView");
                            detailView = new PeekingPanel("Detail", BorderLayout.WEST, inspectorPanel, false, StaticInspectorPanel.INSPECTOR_WIDTH);
                            detailView.setToggleBarVisible(false);

                            tmpView.add(detailView, BorderLayout.EAST);
                            //outerTablePanel = new JPanel();
                            //outerTablePanel.setLayout(new BoxLayout(outerTablePanel, BoxLayout.Y_AXIS));
                            
                            //outerTablePanel.add(tablePanel);
                            //tmpView.add(outerTablePanel, BorderLayout.CENTER);
                            tmpView.add(ssp, BorderLayout.CENTER);

                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    view.removeAll();
                                    view.add(tmpView, BorderLayout.CENTER);
                                    view.updateUI();
                                }
                            });
                                                        
                        } catch (Exception ex) {
                            LOG.error(ex);
                            System.out.println("Caught spreadsheet loading error: "+ex);
                            ex.printStackTrace();
                            view.removeAll();
                            WaitPanel p = new WaitPanel("Error loading Spreadsheet");
                            p.setComplete();
                            view.add(p);
                            
                        }
                    }
                };

                viewPreparationThread = new Thread(prepareViewInBackground);
                viewPreparationThread.start();


            }

            return view;

        } catch (Exception ex) {
            ClientMiscUtils.reportError("Error generating genome view: %s", ex);
        }
        return view;
    }

    @Override
    public void viewDidLoad() {
        super.viewDidLoad();
        tablePanel.setTableShowing(true);
    }

    @Override
    public void viewDidUnload() {
        super.viewDidUnload();
        tablePanel.setTableShowing(false);
    }

    public void queueTableUpdate() {
        ThreadController.getInstance().cancelWorkers(pageName);
        if (tablePanel == null) {
            return;
        }
        tablePanel.queueUpdate();
    }
}
