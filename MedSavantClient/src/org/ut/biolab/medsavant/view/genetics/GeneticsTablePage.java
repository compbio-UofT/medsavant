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

import org.ut.biolab.medsavant.view.genetics.inspector.InspectorPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyVetoException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.CollapsiblePanes;
import com.jidesoft.pane.OutlookTabbedPane;
import com.jidesoft.pane.event.CollapsiblePaneEvent;
import com.jidesoft.pane.event.CollapsiblePaneListener;
import com.jidesoft.plaf.UIDefaultsLookup;
import java.util.EnumMap;
import java.util.HashMap;
import javax.swing.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.api.Listener;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.util.ThreadController;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.Chromosome;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.model.event.VariantSelectionChangedListener;
import org.ut.biolab.medsavant.reference.ReferenceController;
import org.ut.biolab.medsavant.reference.ReferenceEvent;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.vcf.VariantRecord;
import org.ut.biolab.medsavant.view.genetics.inspector.GeneInspector;
import org.ut.biolab.medsavant.view.genetics.inspector.Inspector;
import org.ut.biolab.medsavant.view.genetics.inspector.VariantInspector;
import org.ut.biolab.medsavant.view.genetics.variantinfo.*;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.util.PeekingPanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class GeneticsTablePage extends SubSectionView implements FiltersChangedListener {

    private static final Log LOG = LogFactory.getLog(GeneticsTablePage.class);

    private JPanel panel;
    private TablePanel tablePanel;
    private GenomeContainer gp;
    private boolean isLoaded = false;
    private PeekingPanel genomeView;
    private Component[] settingComponents;
    private PeekingPanel detailView;
    private JTabbedPane _container;

    public GeneticsTablePage(SectionView parent) {
        super(parent);
        FilterController.addFilterListener(this);
        ReferenceController.getInstance().addListener(new Listener<ReferenceEvent>() {
            @Override
            public void handleEvent(ReferenceEvent event) {
                if (event.getType() == ReferenceEvent.Type.CHANGED) {
                    updateContents();
                }
            }
        });
    }

    @Override
    public String getName() {
        return "Spreadsheet";
    }

    @Override
    public Component[] getSubSectionMenuComponents() {
        if (settingComponents == null) {
            settingComponents = new Component[2];
            settingComponents[0] = PeekingPanel.getCheckBoxForPanel(detailView, "Inspector");
            settingComponents[1] = PeekingPanel.getCheckBoxForPanel(genomeView, "Browser");
        }
        return settingComponents;
    }

    @Override
    public JPanel getView(boolean update) {
        try {
            if (panel == null || update) {
                ThreadController.getInstance().cancelWorkers(getName());
                setPanel();
            } else {
                tablePanel.updateIfRequired();
                gp.updateIfRequired();
            }
        } catch (Exception ex) {
            ClientMiscUtils.reportError("Error generating genome view: %s", ex);
        }
        return panel;
    }

    private void setPanel() throws SQLException, RemoteException {
        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        Chromosome[] chroms = MedSavantClient.ReferenceManager.getChromosomes(LoginController.sessionId, ReferenceController.getInstance().getCurrentReferenceID());
        gp = new GenomeContainer(getName(), chroms);

        genomeView = new PeekingPanel("Genome", BorderLayout.SOUTH, (JComponent) gp, false, 225);
        genomeView.setToggleBarVisible(false);

        _container = InspectorPanel.getInstance();

        detailView = new PeekingPanel("Detail", BorderLayout.WEST, _container, false, 380);
        detailView.setToggleBarVisible(false);

        panel.add(genomeView, BorderLayout.NORTH);
        panel.add(detailView, BorderLayout.EAST);

        tablePanel = new TablePanel(getName());
        panel.add(tablePanel, BorderLayout.CENTER);
    }



    @Override
    public void viewDidLoad() {
        isLoaded = true;
        tablePanel.updateIfRequired();
        gp.updateIfRequired();
    }

    @Override
    public void viewDidUnload() {
        ThreadController.getInstance().cancelWorkers(getName());
        if (tablePanel != null && !tablePanel.isInit()) {
            setUpdateRequired(true);
        }
        isLoaded = false;
    }

    public void updateContents() {
        ThreadController.getInstance().cancelWorkers(getName());
        if (tablePanel == null || gp == null) {
            return;
        }
        tablePanel.setUpdateRequired(true);
        gp.setUpdateRequired(true);
        if (isLoaded) {
            tablePanel.updateIfRequired();
            gp.updateIfRequired();
        }
    }

    @Override
    public void filtersChanged() {
        updateContents();
    }



}
