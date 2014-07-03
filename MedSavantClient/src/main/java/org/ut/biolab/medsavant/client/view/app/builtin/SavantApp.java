/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 *
 * All rights reserved. No warranty, explicit or implicit, provided. THE
 * SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE FOR
 * ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.ut.biolab.medsavant.client.view.app.builtin;

import edu.toronto.cs.medsavant.medsavant.app.api.appcomm.AppCommRegistry;
import edu.toronto.cs.medsavant.medsavant.app.api.appcomm.AppCommHandler;
import edu.toronto.cs.medsavant.medsavant.app.api.appcomm.BAMFileComm;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.filter.FilterController;
import org.ut.biolab.medsavant.client.filter.FilterEvent;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.reference.ReferenceEvent;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.ThreadController;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.component.GenericStringChooser;
import org.ut.biolab.medsavant.client.view.component.SelectableListView;
import org.ut.biolab.medsavant.client.view.component.WaitPanel;
import org.ut.biolab.medsavant.client.view.dashboard.LaunchableApp;
import org.ut.biolab.medsavant.client.view.genetics.GenomeContainer;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.notify.Notification;
import org.ut.biolab.medsavant.client.view.util.PeekingPanel;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.client.view.variants.BrowserPage;
import org.ut.biolab.medsavant.client.view.variants.MedSavantDataSource;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.model.Chromosome;
import org.ut.biolab.medsavant.shared.util.ServerRequest;
import savant.api.adapter.DataSourceAdapter;
import savant.api.data.DataFormat;
import savant.api.event.GenomeChangedEvent;
import savant.api.util.DialogUtils;
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
public class SavantApp implements LaunchableApp, AppCommHandler<BAMFileComm> {

    private String pageName = "Savant";
    private boolean initialized;
    private final Queue<DataSourceAdapter> queue = new LinkedList<DataSourceAdapter>();
    private boolean sequenceLoaded = false;

    private void initView() {
        if (!initialized) {
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

            startMappingDNAIdsToBAMURLs();

            GenomeController.getInstance()
                    .addListener(new savant.api.util.Listener<GenomeChangedEvent>() {
                        @Override
                        public void handleEvent(GenomeChangedEvent event) {
                            if (!variantTrackLoaded) {
                                // load a gene track if it exists
                                try {
                                    LOG.debug("Loading gene track");
                                    String referenceName = ReferenceController.getInstance().getCurrentReferenceName();
                                    String urlOfTrack = getTrackURL(referenceName, "gene");
                                    addTrackFromURLString(urlOfTrack, DataFormat.GENERIC_INTERVAL, false);
                                } catch (Exception ex) {
                                    LOG.error("Error loading gene track", ex);
                                }

                                // load the MedSavant variant track
                                try {
                                    LOG.debug("Loading MedSavant variant track");
                                    msds = new MedSavantDataSource();
                                    addTrackFromDataSource(msds);
                                    variantTrackLoaded = true;
                                } catch (Exception ex) {
                                    LOG.error("Misc. error loading MedSavant variant track", ex);
                                }
                            }
                            
                            LOG.info("GENOME CHANGED TO " + event.getNewGenome().getName());
                            sequenceLoaded = true;
                            processTrackBacklog();
                        }
                    });
        }
        initialized = true;
    }

    @Override
    public void viewWillUnload() {
    }

    @Override
    public void viewWillLoad() {
        initView();
    }

    @Override
    public ImageIcon getIcon() {
        return IconFactory.getInstance().getIcon(IconFactory.StandardIcon.APP_SAVANT);
    }

    @Override
    public String getName() {
        return "Genome Browser";
    }

    private static final Log LOG = LogFactory.getLog(SavantApp.class);
    private JPanel view;
    private JPanel browserPanel;
    private GenomeContainer genomeContainer;
    private PeekingPanel genomeView;
    private PeekingPanel variationPanel;
    private Component[] settingComponents;
    private boolean variantTrackLoaded = false;
    private static SavantApp instance;
    private MedSavantDataSource msds;
    //private final Semaphore trackAdditionLock = new Semaphore(1, true);

    // Do not use unless you're sure BrowserPage has been initialized
    public static SavantApp getInstance() {
        return instance;
    }
    //private GenericStringChooser gsc;

    boolean isDoneMappingIdsToBAMURLs = false;
    private List<String> dnaIDs;
    private ArrayList<String> sampleIdsHavingBams;
    private HashMap<String, String> dnaIDToURLMap;

    public SavantApp() {
        AppCommRegistry.getInstance().registerHandler(this, BAMFileComm.class);
    }

    public Component[] getSubSectionMenuComponents() {
        if (settingComponents == null) {

            while (genomeView == null && variationPanel == null) {
                try {
                    LOG.debug("Waiting for panels...");
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(BrowserPage.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            settingComponents = new Component[2];
            settingComponents[0] = PeekingPanel.getToggleButtonForPanel(genomeView, "Genome");
            settingComponents[1] = PeekingPanel.getToggleButtonForPanel(variationPanel, "Variation");
        }
        return settingComponents;
    }

    private void setupToolbarButtons(Savant savantInstance) {

        // Removed temporarily 06-08-2013, in preparation for 1.1 release.
        /*
         JButton button = new JButton(IconFactory.getInstance().getIcon(StandardIcon.FILTER));
         button.setToolTipText("Restrict DNA IDs");
         button.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent ae) {
         gsc.setLocationRelativeTo(view);
         gsc.setVisible(true);
         }
         });
         */
        JPanel pluginToolbar = savantInstance.getPluginToolbar();
        pluginToolbar.setLayout(new MigLayout("insets 0"));

        // Removed temporarily 06-08-2013, in preparation for 1.1 release.
        // pluginToolbar.add(button);
        try {

            String buttonStyle = "segmentedCapsule";

            JButton dnaButton = ViewUtil.getTexturedButton("Open Patient BAMs");
            //JButton dnaButton = new JButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.BAMFILE));
            dnaButton.setToolTipText("Open BAM File(s)");
            dnaButton.putClientProperty("JButton.buttonType", buttonStyle);
            dnaButton.putClientProperty("JButton.segmentPosition", "only");
            dnaButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    if (isDoneMappingIdsToBAMURLs) {
                        GenericStringChooser bamFileChooser = new GenericStringChooser(sampleIdsHavingBams, "Open BAM File(s)");
                        bamFileChooser.setLocationRelativeTo(view);

                        bamFileChooser.addListener(new Listener<SelectableListView.SelectionEvent>() {
                            @Override
                            public void handleEvent(SelectableListView.SelectionEvent event) {
                                List selections = event.getSelections();
                                for (Object o : selections) {
                                    String url = dnaIDToURLMap.get(o.toString());
                                    addTrackFromURLString(url, DataFormat.ALIGNMENT, false);
                                }
                            }
                        });

                        bamFileChooser.setVisible(true);

                    } else {
                        DialogUtils.displayMessage("Still collecting alignment file URLs, try again soon.");
                    }
                }
            });

            ViewUtil.makeMini(dnaButton);

            //pluginToolbar.add(dnaButton);
            //pluginToolbar.setVisible(true);
        } catch (Exception e) {
            LOG.error("ERROR ", e);
        }
    }

    @Override
    public JPanel getView() {
        try {
            if (view == null) {
                //trackAdditionLock.acquire();
                view = new JPanel();
                view.setLayout(new BorderLayout());
                view.add(new WaitPanel("Starting Genome Browser"));

                Chromosome[] chroms = MedSavantClient.ReferenceManager.getChromosomes(LoginController.getSessionID(), ReferenceController.getInstance().getCurrentReferenceID());
                genomeContainer = new GenomeContainer(pageName, chroms); // what's the first param for?
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
                            setupToolbarButtons(savantInstance);

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
                                    addTrackFromURLString(urlOfTrack, DataFormat.SEQUENCE, false);
                                    //trackAdditionLock.release();
                                }
                            });
                            t.start();

                        } catch (Exception ex) {
                            LOG.error("Got exception: " + ex);
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

    public void addTrackFromURLString(String urlString, final DataFormat format) {
        addTrackFromURLString(urlString, format, true);
    }

    private void processTrackBacklog() {
        while (!queue.isEmpty()) {
            addTrackFromDataSource(queue.poll());
        }
    }
    
    public void addTrackFromDataSource(DataSourceAdapter dsa) {
        try {
            if (!sequenceLoaded) {
                queue.add(dsa);
                return;
            }
            //trackAdditionLock.acquire();
            
            LOG.info("ADDING TRACK " + dsa.getName());
            Track t = TrackFactory.createTrack(dsa);
            FrameController c = FrameController.getInstance();
            c.createFrame(new Track[]{t});
            //trackAdditionLock.release();
        } catch (Exception ex) {
            LOG.error(ex);
        }
    }

    public void addTrackFromURLString(String urlString, final DataFormat format, boolean showNotification) {

        LOG.info("Adding track from " + urlString);

        final Notification n = new Notification();
        n.setName("Importing Track...");
        n.setDescription(urlString);
        n.setIcon(this.getIcon());
        n.setIsIndeterminateProgress(true);
        n.setHideDoesClose(true);

        ActionListener openApp = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                MedSavantFrame.getInstance().getDashboard().launchApp(SavantApp.this);
            }

        };
        n.setAction("Open", openApp);

        if (showNotification && MedSavantFrame.getInstance().getDashboard().getCurrentApp() != this) {
            MedSavantFrame.getInstance().showNotification(n);
        }

        try {
            final URL url = new URL(urlString);
            if (!TrackController.getInstance().containsTrack(urlString)) {
                if (view == null) {
                    getView();
                }
                Thread t = new Thread(new Runnable() {
                    public void run() {
                        try {
                            LOG.info("ACQUIRING URL LOCK");
                            //trackAdditionLock.acquire();
                            FrameController.getInstance().addTrackFromURI(url.toURI(), format, null);
                            //trackAdditionLock.release();
                            LOG.info("RELEASING URL LOCK");
                            n.setIsIndeterminateProgress(false);
                            n.setProgress(1.0);
                        } catch (Exception ex) {
                            
                            LOG.error(ex);
                        }
                    }
                });
                t.start();
            }
        } catch (Exception ex) {
            LOG.error(ex);
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
    }

    public void updateContents() {
        ThreadController.getInstance().cancelWorkers(pageName);
        if (genomeContainer == null) {
            return;
        }
        genomeContainer.setUpdateRequired(true);
        if (initialized) {
            genomeContainer.updateIfRequired();
        }
    }

    private void startMappingDNAIdsToBAMURLs() {

        dnaIDs = java.util.Arrays.asList(new String[]{});
        sampleIdsHavingBams = new ArrayList<String>();
        dnaIDToURLMap = new HashMap<String, String>();

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    // TODO: This takes a long time, do it faster or at least threaded
                    dnaIDs = MedSavantClient.DBUtils.getDistinctValuesForColumn(
                            LoginController.getSessionID(),
                            ProjectController.getInstance().getCurrentVariantTableName(),
                            BasicVariantColumns.DNA_ID.getColumnName(),
                            false);
                    //gsc = new GenericStringChooser(dnaIDs, "Choose DNA IDs");

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

                    isDoneMappingIdsToBAMURLs = true;
                } catch (Exception ex) {
                    LOG.error(ex);
                }
            }

        });

    }

    @Override
    public void didLogout() {
    }

    @Override
    public void didLogin() {
    }

    @Override
    public void handleCommEvent(BAMFileComm value) {
        System.out.println("Launching genome browser and openning BAM file " + value.getEventData());
        try {
            addTrackFromURLString(value.getEventData().toString(), DataFormat.ALIGNMENT, false);
            MedSavantFrame.getInstance().getDashboard().launchApp(this);
        } catch (NullPointerException e) {
            DialogUtils.displayError("Error loading BAM file at URL: null");
        } catch (Exception e) {
            DialogUtils.displayError("Error loading BAM file at URL: " + value.getEventData().getPath());
        }
    }

    @Override
    public String getHandlerName() {
        return "Savant Genome Browser";
    }

    @Override
    public ImageIcon getHandlerIcon() {
        return this.getIcon();
    }

}
