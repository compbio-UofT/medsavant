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
package org.ut.biolab.medsavant.client.view.variants;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.filter.FilterController;
import org.ut.biolab.medsavant.client.filter.FilterEvent;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.shared.model.Chromosome;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.reference.ReferenceEvent;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.ThreadController;
import org.ut.biolab.medsavant.client.view.component.GenericStringChooser;
import org.ut.biolab.medsavant.client.view.genetics.GenomeContainer;
import org.ut.biolab.medsavant.client.view.subview.SectionView;
import org.ut.biolab.medsavant.client.view.subview.SubSectionView;
import org.ut.biolab.medsavant.client.view.util.PeekingPanel;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.util.ServerRequest;
import savant.api.data.DataFormat;
import savant.api.event.GenomeChangedEvent;
import savant.controller.FrameController;
import savant.controller.GenomeController;
import savant.controller.TrackController;
import savant.exception.SavantTrackCreationCancelledException;
import savant.settings.PersistentSettings;
import savant.util.ColourKey;
import savant.view.swing.Savant;
import savant.view.tracks.Track;
import savant.view.tracks.TrackFactory;
import savant.view.variation.VariationController;

/**
 *
 * @author mfiume
 */
public class BrowserPage extends SubSectionView {

    private static final Log LOG = LogFactory.getLog(BrowserPage.class);
    private JPanel view;
    private JPanel browserPanel;
    private GenomeContainer genomeContainer;
    private PeekingPanel genomeView;
    private PeekingPanel variationPanel;
    private Component[] settingComponents;
    private boolean variantTrackLoaded = false;
    private static BrowserPage instance;
    private MedSavantDataSource msds;

    // Do not use unless you're sure BrowserPage has been initialized
    public static BrowserPage getInstance() {
        return instance;
    }
    private GenericStringChooser gsc;

    public BrowserPage(SectionView parent) {
        super(parent, "Browser");

        instance = this;

        FilterController.getInstance().addListener(new Listener<FilterEvent>() {
            @Override
            public void handleEvent(FilterEvent event) {
                updateContents();
            }
        });

        ReferenceController.getInstance().addListener(new Listener<ReferenceEvent>() {
            @Override
            public void handleEvent(ReferenceEvent event) {
                if (event.getType() == ReferenceEvent.Type.CHANGED) {
                    updateContents();
                }
            }
        });

        GenomeController.getInstance()
                .addListener(new savant.api.util.Listener<GenomeChangedEvent>() {
            @Override
            public void handleEvent(GenomeChangedEvent event) {
                if (!variantTrackLoaded) {

                    // load a gene track if it exists
                    try {
                        System.out.println("Loading gene track");
                        String referenceName = ReferenceController.getInstance().getCurrentReferenceName();
                        String urlOfTrack = getTrackURL(referenceName, "gene");
                        addTrackFromURLString(urlOfTrack, DataFormat.GENERIC_INTERVAL);
                    } catch (Exception ex) {
                        LOG.info("Error loading gene track");
                        ex.printStackTrace();
                    }

                    // load the MedSavant variant track
                    try {
                        System.out.println("Loading MedSavant variant track");
                        msds = new MedSavantDataSource();
                        System.out.println("Subscribing selection change listener");
                        gsc.addListener(msds);
                        FrameController.getInstance().createFrame(new Track[]{TrackFactory.createTrack(msds)});

                    } catch (SavantTrackCreationCancelledException ex) {
                        LOG.info("Error loading MedSavant variant track");
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public Component[] getSubSectionMenuComponents() {
        if (settingComponents == null) {
            settingComponents = new Component[3];
            settingComponents[0] = PeekingPanel.getToggleButtonForPanel(genomeView, "Genome");
            settingComponents[1] = PeekingPanel.getToggleButtonForPanel(variationPanel, "Variation");

            try {
                List<String> dnaIDs = MedSavantClient.DBUtils.getDistinctValuesForColumn(
                        LoginController.getSessionID(),
                        ProjectController.getInstance().getCurrentVariantTableName(),
                        BasicVariantColumns.DNA_ID.getColumnName(),
                        true);
                gsc = new GenericStringChooser(dnaIDs, "Choose DNA IDs");

                JButton button = new JButton("Restrict DNA IDs");
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        gsc.setVisible(true);
                    }
                });

                settingComponents[2] = button;

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
        return settingComponents;
    }

    @Override
    public JPanel getView() {
        try {
            if (view == null) {
                view = new JPanel();
                view.setLayout(new BorderLayout());

                Chromosome[] chroms = MedSavantClient.ReferenceManager.getChromosomes(LoginController.getInstance().getSessionID(), ReferenceController.getInstance().getCurrentReferenceID());
                genomeContainer = new GenomeContainer(pageName, chroms);

                genomeView = new PeekingPanel("Genome", BorderLayout.SOUTH, genomeContainer, false, 225);
                genomeView.setToggleBarVisible(false);

                view.add(genomeView, BorderLayout.NORTH);

                browserPanel = new JPanel();
                browserPanel.setLayout(new BorderLayout());

                Savant savantInstance = Savant.getInstance(false, false);

                PersistentSettings.getInstance().setColor(ColourKey.GRAPH_PANE_BACKGROUND_BOTTOM, Color.white);
                PersistentSettings.getInstance().setColor(ColourKey.GRAPH_PANE_BACKGROUND_TOP, Color.white);
                PersistentSettings.getInstance().setColor(ColourKey.AXIS_GRID, new Color(240, 240, 240));

                savantInstance.setStartPageVisible(false);
                savantInstance.setTrackBackground(new Color(210, 210, 210));
                savantInstance.setBookmarksVisibile(false);
                savantInstance.setVariantsVisibile(false);

                GenomeController.getInstance().setGenome(null);

                String referenceName = ReferenceController.getInstance().getCurrentReferenceName();
                final String urlOfTrack = getTrackURL(referenceName, "sequence");

                browserPanel.add(savantInstance.getBrowserPanel(), BorderLayout.CENTER);

                view.add(browserPanel, BorderLayout.CENTER);

                variationPanel = new PeekingPanel("Variations", BorderLayout.WEST, VariationController.getInstance().getModule(), false, 325);
                variationPanel.setToggleBarVisible(false);

                view.add(variationPanel, BorderLayout.EAST);

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        addTrackFromURLString(urlOfTrack, DataFormat.SEQUENCE);
                    }
                });
                t.start();

            } else {
                genomeContainer.updateIfRequired();
            }
        } catch (Exception ex) {
            ClientMiscUtils.reportError("Error generating genome view: %s", ex);
        }
        return view;
    }

    public static void addTrackFromURLString(String urlString, DataFormat format) {
        try {
            if (!TrackController.getInstance().containsTrack(urlString)) {
                URL url = new URL(urlString);
                //TODO: get data format appropriately
                FrameController.getInstance().addTrackFromURI(url.toURI(), format, null);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static String getTrackURL(String referenceName, String trackName) throws Exception {
        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("reference", referenceName);
        requestMap.put("trackname", trackName);
        JSONObject o = ServerRequest.requestFromServer(ServerRequest.TRACK_PATH, requestMap);
        String urlOfTrack = (String) o.get("url");
        return urlOfTrack;
    }

    @Override
    public void viewDidLoad() {
        super.viewDidLoad();
        genomeContainer.updateIfRequired();
        MedSavantDataSource.setActive(true);

        // Refresh variants track
        if (msds != null) {
            if (variantTrackLoaded) {
                msds.refresh();
            }
        }
    }

    @Override
    public void viewDidUnload() {
        super.viewDidUnload();
        MedSavantDataSource.setActive(false);
    }

    public void updateContents() {
        ThreadController.getInstance().cancelWorkers(pageName);
        if (genomeContainer == null) {
            return;
        }
        genomeContainer.setUpdateRequired(true);
        if (loaded) {
            genomeContainer.updateIfRequired();
        }
    }
}
