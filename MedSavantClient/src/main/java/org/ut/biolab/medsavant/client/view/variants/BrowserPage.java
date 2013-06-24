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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
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
import org.ut.biolab.medsavant.client.view.ViewController;
import org.ut.biolab.medsavant.client.view.component.GenericStringChooser;
import org.ut.biolab.medsavant.client.view.component.SelectableListView;
import org.ut.biolab.medsavant.client.view.component.WaitPanel;
import org.ut.biolab.medsavant.client.view.genetics.GenomeContainer;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
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
    private List<String> dnaIDs;
    private final ArrayList<String> sampleIdsHavingBams;
    private final HashMap<String, String> dnaIDToURLMap;

   
    
    
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

        dnaIDs = java.util.Arrays.asList(new String[]{});
        sampleIdsHavingBams = new ArrayList<String>();
        dnaIDToURLMap = new HashMap<String, String>();

        try {


            dnaIDs = MedSavantClient.DBUtils.getDistinctValuesForColumn(
                    LoginController.getSessionID(),
                    ProjectController.getInstance().getCurrentVariantTableName(),
                    BasicVariantColumns.DNA_ID.getColumnName(),
                    false);
            gsc = new GenericStringChooser(dnaIDs, "Choose DNA IDs");


            for (String s : dnaIDs) {
                String url = MedSavantClient.PatientManager.getReadAlignmentPathForDNAID(
                        LoginController.getSessionID(),
                        ProjectController.getInstance().getCurrentProjectID(),
                        s);
                if (url != null && !url.isEmpty()) {
                    sampleIdsHavingBams.add(s);
                    String[] splitUrls = url.split(","); // can specify multiple urls, take the first one
                    dnaIDToURLMap.put(s, splitUrls[0]);
                }
            }

        } catch (Exception ex) {
            LOG.error(ex);
        }

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
                        Track t = TrackFactory.createTrack(msds);
                        FrameController c = FrameController.getInstance();
                        c.createFrame(new Track[]{t});
                        variantTrackLoaded = true;

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

            while (genomeView == null && variationPanel == null) {
                try {
                    System.out.println("Waiting for panels...");
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(BrowserPage.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            settingComponents = new Component[5];
            settingComponents[0] = PeekingPanel.getToggleButtonForPanel(genomeView, "Genome");
            settingComponents[1] = PeekingPanel.getToggleButtonForPanel(variationPanel, "Variation");

            JButton button = new JButton("Restrict DNA IDs");
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    gsc.setVisible(true);
                }
            });

            settingComponents[2] = button;

            try {

                final GenericStringChooser bamFileChooser = new GenericStringChooser(sampleIdsHavingBams, "Open BAM File(s)");

                JButton dnaButton = new JButton("Open BAM File(s)");
                dnaButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        bamFileChooser.setVisible(true);
                    }
                });

                bamFileChooser.addListener(new Listener<SelectableListView.SelectionEvent>() {
                    @Override
                    public void handleEvent(SelectableListView.SelectionEvent event) {
                        List selections = event.getSelections();
                        for (Object o : selections) {
                            String url = dnaIDToURLMap.get(o.toString());
                            addTrackFromURLString(url, DataFormat.ALIGNMENT);
                        }
                    }
                });

                settingComponents[3] = dnaButton;
            } catch (Exception e) {
                e.printStackTrace();
            }

            ImageIcon img = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.ACTION_ON_TOOLBAR); 
            JButton undockButton = new JButton("Undock", img);
            undockButton.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent ae){
                   ViewController.getInstance().undock();
               } 
            });
            settingComponents[4] = undockButton;
        }
        return settingComponents;
    }

    @Override
    public JPanel getView() {
        try {
            if (view == null) {

                view = new JPanel();
                view.setLayout(new BorderLayout());
                view.add(new WaitPanel("Preparing Savant Genome Browser..."));

                Chromosome[] chroms = MedSavantClient.ReferenceManager.getChromosomes(LoginController.getInstance().getSessionID(), ReferenceController.getInstance().getCurrentReferenceID());
                genomeContainer = new GenomeContainer(pageName, chroms);
                genomeView = new PeekingPanel("Genome", BorderLayout.SOUTH, genomeContainer, false, 225);

                final JPanel variationPlaceHolder = new JPanel();
                variationPlaceHolder.setLayout(new BorderLayout());
                variationPlaceHolder.add(new WaitPanel("Initializing variant views..."), BorderLayout.CENTER);
                variationPanel = new PeekingPanel("Variations", BorderLayout.WEST, variationPlaceHolder, false, 325);
                variationPanel.setToggleBarVisible(false);

                Runnable prepareBrowserThread = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final JPanel tmpView = new JPanel();
                            tmpView.setLayout(new BorderLayout());

                            genomeView.setToggleBarVisible(false);

                            tmpView.add(genomeView, BorderLayout.NORTH);

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

                            variationPlaceHolder.removeAll();
                            variationPlaceHolder.add(VariationController.getInstance().getModule(), BorderLayout.CENTER);


                            GenomeController.getInstance().setGenome(null);

                            String referenceName = ReferenceController.getInstance().getCurrentReferenceName();
                            final String urlOfTrack = getTrackURL(referenceName, "sequence");

                            browserPanel.add(savantInstance.getBrowserPanel(), BorderLayout.CENTER);

                            tmpView.add(browserPanel, BorderLayout.CENTER);

                            tmpView.add(variationPanel, BorderLayout.EAST);

                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    view.removeAll();
                                    view.add(tmpView, BorderLayout.CENTER);
                                    view.updateUI();
                                }
                            });

                            Thread t = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    addTrackFromURLString(urlOfTrack, DataFormat.SEQUENCE);
                                }
                            });
                            t.start();


                        } catch (Exception ex) {
                            view.removeAll();
                            WaitPanel p = new WaitPanel("Error loading genome browser");
                            p.setComplete();
                            view.add(p);
                        }
                    }
                };

                new Thread(prepareBrowserThread).start();

            } else {
                if (genomeContainer != null) {
                    genomeContainer.updateIfRequired();
                }
            }
        } catch (Exception ex) {
            ClientMiscUtils.reportError("Error generating genome view: %s", ex);
        }
        return view;
    }

    public void addTrackFromURLString(String urlString, DataFormat format) {
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
        //MedSavantDataSource.setActive(true);

        genomeContainer.updateIfRequired();

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
        //MedSavantDataSource.setActive(false);
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
