/*
 *    Copyright 2012 University of Toronto
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
package org.ut.biolab.medsavant.client.view.genetics.variantinfo;

import org.ut.biolab.medsavant.client.view.genetics.inspector.SubInspector;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.layout.CyLayoutAlgorithm;
import cytoscape.layout.CyLayouts;
import cytoscape.view.CyNetworkView;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.genemania.plugin.cytoscape2.layout.FilteredLayout;
import org.ut.biolab.medsavant.client.api.Listener;

import org.ut.biolab.medsavant.shared.model.Gene;
import org.ut.biolab.medsavant.shared.model.RegionSet;
import org.ut.biolab.medsavant.client.region.RegionController;
import org.ut.biolab.medsavant.client.settings.DirectorySettings;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.GenemaniaInfoRetriever.NoRelatedGenesInfoException;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author khushi
 */
public class GeneManiaInfoSubPanel extends SubInspector implements Listener<Gene> {

    private static final Log LOG = LogFactory.getLog(GeneManiaInfoSubPanel.class);
    private final String name;
    private GenemaniaInfoRetriever genemania;
    private JLabel label;
    private JPanel panel;
    private javax.swing.JProgressBar progressBar;
    private JLabel progressMessage;
    private KeyValuePairPanel kvp;
    private JPanel kvpPanel;
    private JPanel settingsPanel;
    private JButton settingsButton;
    private Gene gene;
    private GeneManiaSettingsDialog genemaniaSettings;
    private boolean rankByVarFreq;
    private Thread genemaniaAlgorithmThread;
    private boolean dataPresent;
    private int currSizeOfArray;
    private JPanel graph;
    private Listener<Object> geneListener;
    private Thread dlThread;
    
    private static String GM_URL = "http://genomesavant.com/serve/data/genemania/gmdata.zip";
    //private static String GM_URL = "http://zoidb.org/android/junk.zip";
    public GeneManiaInfoSubPanel() {
        name = "Related Genes";
        dataPresent = true;
    }

    public void setGeneListener(Listener<Object> geneListener) {
        this.geneListener = geneListener;
    }

    @Override
    public String getName() {
        return name;
    }

    public void dlGM(){
        panel.removeAll();

        try{
            String dstPath = DirectorySettings.getCacheDirectory().getAbsolutePath();
            
            FileDownloadPanel dlPanel = new FileDownloadPanel("Downloading GeneMANIA", new DownloadTask(GM_URL, dstPath){
                @Override
                public void done(){                     
                    panel.removeAll();
                    
                    JPanel innerPanel = new JPanel();
                    innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
                    
                    innerPanel.add(new JLabel("Extracting GeneMANIA files..."));
                    
                    JProgressBar jb = new JProgressBar();
                    jb.setIndeterminate(true);
                    innerPanel.add(jb);  
                    panel.add(innerPanel);
                    
                    new SwingWorker(){
                        public Void doInBackground(){
                            GenemaniaInfoRetriever.extractGM(getDestPath());        
                            return null;
                        }
                        
                        protected void done(){
                            panel.removeAll();
                            try{
                                dataPresent = true;                                
                                genemania = new GenemaniaInfoRetriever();
                                genemaniaSettings = new GeneManiaSettingsDialog(genemania);                                
                                buildPanel();
                                MedSavantFrame.getInstance().notificationMessage("GeneMANIA has finished downloading, and is ready to use!");
                                updateRelatedGenesPanel(gene);
                                panel.updateUI();                                
                            }catch(IOException e){
                                System.err.println(e);
                            }
                            //System.out.println("Exiting so you can debug");
                            //System.exit(1);
                        }
                    }.execute();                                                           
                }
            });               
            panel.add(dlPanel);
            dlPanel.download();            
            panel.revalidate();
        }catch(IOException e){
            DialogUtils.displayMessage("Error downloading GeneMANIA files "+e);
            dataPresent = false;
        }        
    }
    
    private void buildPanel(){
        System.out.println("Inside getInfoPanel");
        kvp = new KeyValuePairPanel(2);
        kvp.setKeysVisible(false);
        kvpPanel = new JPanel();
        kvpPanel.setLayout(new BorderLayout());
        kvpPanel.add(kvp, BorderLayout.CENTER);
        JPanel currGenePanel = ViewUtil.getClearPanel();
        JPanel pMessagePanel = ViewUtil.getClearPanel();
        settingsPanel = ViewUtil.getClearPanel();
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        progressMessage = new JLabel();
        progressMessage.setVisible(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        settingsButton = new JButton("Settings");
        settingsButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                genemaniaSettings.showSettings();
                if (genemaniaSettings.getUpdateQueryNeeded()) {
                    updateRelatedGenesPanel(gene);
                }
            }
        });
        label = new JLabel("Selected gene: ");
        //currGenePanel.add(l);
        pMessagePanel.add(progressMessage);
        settingsPanel.setLayout(new BorderLayout());
        settingsPanel.add(settingsButton, BorderLayout.EAST);
        graph = ViewUtil.getClearPanel();
        panel.add(kvpPanel);
        panel.add(currGenePanel);
        panel.add(pMessagePanel);
        panel.add(progressBar);

        //p.add(new javax.swing.JSeparator(JSeparator.HORIZONTAL));
        panel.add(settingsPanel);
        panel.add(graph);
    }
    
    @Override
    public JPanel getInfoPanel() {
        label = new JLabel("Selected gene: ");
        panel = ViewUtil.getClearPanel();
        try {
            if (GenemaniaInfoRetriever.hasGeneManiaData()) {
                System.out.println("Found genemania data");
                genemania = new GenemaniaInfoRetriever();
                genemaniaSettings = new GeneManiaSettingsDialog(genemania);
            } else {
                System.out.println("didn't find gene mania data, setting up button");
                JButton dlGm = new JButton("Download GeneMANIA");
                dlGm.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        dlGM();
                    }
                });
                //progressMessage = new JLabel("<html><center>" + ex.getMessage() + "<br>Please try again after data has been downloaded.</center></html>", SwingConstants.CENTER);
                //panel.add(progressMessage);
                panel.add(dlGm);
                dataPresent = false;
                System.out.println("Returning panel "+(panel == null));
                return panel;
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }

        buildPanel();
        return panel;
        
        /*
        System.out.println("Inside getInfoPanel");
        kvp = new KeyValuePairPanel(2);
        kvp.setKeysVisible(false);
        kvpPanel = new JPanel();
        kvpPanel.setLayout(new BorderLayout());
        kvpPanel.add(kvp, BorderLayout.CENTER);
        JPanel currGenePanel = ViewUtil.getClearPanel();
        JPanel pMessagePanel = ViewUtil.getClearPanel();
        settingsPanel = ViewUtil.getClearPanel();
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        progressMessage = new JLabel();
        progressMessage.setVisible(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        settingsButton = new JButton("Settings");
        settingsButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                genemaniaSettings.showSettings();
                if (genemaniaSettings.getUpdateQueryNeeded()) {
                    updateRelatedGenesPanel(gene);
                }
            }
        });
        label = new JLabel("Selected gene: ");
        //currGenePanel.add(l);
        pMessagePanel.add(progressMessage);
        settingsPanel.setLayout(new BorderLayout());
        settingsPanel.add(settingsButton, BorderLayout.EAST);
        graph = ViewUtil.getClearPanel();
        panel.add(kvpPanel);
        panel.add(currGenePanel);
        panel.add(pMessagePanel);
        panel.add(progressBar);

        //p.add(new javax.swing.JSeparator(JSeparator.HORIZONTAL));
        panel.add(settingsPanel);
        panel.add(graph);
        return panel;*/
    }

    @Override
    public void handleEvent(Gene g) {
      
        if (g == null || !dataPresent) {
            if(g != null){
                gene = g;
            }
            label.setText("None");            
        } else {
            System.out.println("Received gene " + g.getName());
            label.setText(g.getName());
            if (gene == null || !g.getName().equals(gene.getName())) {
                updateRelatedGenesPanel(g);
            }

        }
    }

    private JPopupMenu getRegionSetsMenu(Gene gene) {
        final JPopupMenu regionSets = new JPopupMenu();
        final RegionController regionController = RegionController.getInstance();
        final Gene g = gene;
        try {
            for (RegionSet s : regionController.getRegionSets()) {
                final RegionSet finalRegionSet = s;
                JMenuItem menuItem = new JMenuItem(s.getName());
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        try {
                            regionController.addToRegionSet(finalRegionSet, g.getChrom(), g.getStart(), g.getEnd(), g.getName());
                            DialogUtils.displayMessage(String.format("Successfully added %s to %s list", g.getName(), finalRegionSet.getName()));
                        } catch (Exception ex) {
                            ClientMiscUtils.reportError(String.format("Unable to add %s to %s list: %%s", g.getName(), finalRegionSet.getName()), ex);
                        }
                    }
                });
                regionSets.add(menuItem);
            }
        } catch (Exception ex) {
            return new JPopupMenu();
        }
        return regionSets;
    }

    private void updateRelatedGenesPanel(Gene g) {
        gene = g;
        kvpPanel.removeAll();
        kvpPanel.invalidate();
        kvpPanel.updateUI();
        kvp = new KeyValuePairPanel(5);
        kvp.setKeysVisible(false);
        kvpPanel.add(kvp);
        progressBar.setVisible(true);
        progressMessage.setVisible(true);
        progressBar.setIndeterminate(true);
        progressMessage.setText("Querying GeneMANIA for related genes");

        final Object lock = new Object();

        Runnable r = new Runnable() {
            @Override
            public void run() {



                boolean setMsgOff = true;
                boolean buildGraph = true;
                if (!Thread.interrupted()) {
                    try {
                        List<String> geneNames = new ArrayList();
                        geneNames.add(gene.getName());
                        List<String> notInGenemania = new ArrayList<String>(geneNames);
                        notInGenemania.removeAll(GenemaniaInfoRetriever.getValidGenes(geneNames));
                        geneNames = GenemaniaInfoRetriever.getValidGenes(geneNames);
                        genemania.setGenes(geneNames);
                        System.out.println("Thread: " + Thread.currentThread().getId());
                        System.out.println("notingenemania size " + notInGenemania.size());
                        System.out.println("genemania getgenes size " + genemania.getGenes().size());
                        System.out.println("rankbyvarfreq " + rankByVarFreq);

                        if (notInGenemania.size() > 0) {
                            String message = "<html><center>Following gene(s) not found in GeneMANIA: ";
                            for (String invalidGene : notInGenemania) {
                                message += "<br>" + invalidGene;
                            }
                            message += "</center></html>";
                            progressMessage.setText(message);
                            setMsgOff = false;
                            buildGraph = false;
                        }
                        GeneSetFetcher geneSetFetcher = GeneSetFetcher.getInstance();
                        if (genemania.getGenes().size() > 0) {
                            int i = 1;
                            String zero = Integer.toString(0);
                            Font HEADER_FONT = new Font("Arial", Font.BOLD, 10);
                            kvp.addKey(zero);
                            JLabel geneHeader = new JLabel("Gene".toUpperCase());
                            geneHeader.setFont(HEADER_FONT);
                            kvp.setValue(zero, geneHeader);
                            JLabel varFreqHeader = new JLabel("<html>VARIATION<br>FREQUENCY<br>(var/kb)</html>");
                            varFreqHeader.setFont(HEADER_FONT);
                            kvp.setAdditionalColumn(zero, 0, varFreqHeader);
                            JLabel genemaniaHeader = new JLabel("<html>GENEMANIA<br>SCORE</html>");
                            genemaniaHeader.setFont(HEADER_FONT);
                            //kvp.setAdditionalColumn(zero, 1, genemaniaHeader);
                            if (rankByVarFreq) {
                                Iterator<org.ut.biolab.medsavant.shared.model.Gene> itr = geneSetFetcher.getGenesByNumVariants(genemania.getRelatedGeneNamesByScore()).iterator();
                                org.ut.biolab.medsavant.shared.model.Gene currGene;
                                itr.next();//skip the first one (it's the name of selected gene already displayed)

                                if (Thread.interrupted()) {
                                    throw new InterruptedException();
                                }
                                while (itr.hasNext()) {
                                    currGene = itr.next();
                                    final org.ut.biolab.medsavant.shared.model.Gene finalGene = currGene;
                                    kvp.addKey(Integer.toString(i));
                                    JLabel geneName = new JLabel(currGene.getName());
                                    EntrezButton geneLinkButton = new EntrezButton(currGene.getName());
                                    kvp.setValue(Integer.toString(i), geneName);

                                    JButton geneInspectorButton = ViewUtil.getTexturedButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.INSPECTOR));
                                    geneInspectorButton.setToolTipText("Inspect this gene");
                                    geneInspectorButton.addActionListener(new ActionListener() {
                                        @Override
                                        public void actionPerformed(ActionEvent ae) {
                                            if (geneListener != null) {
                                                geneListener.handleEvent(finalGene);
                                            }
                                        }
                                    });

                                    final JButton addToRegionListButton = ViewUtil.getTexturedButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.ADDKVP));
                                    addToRegionListButton.setToolTipText("Add to Region List");
                                    addToRegionListButton.addActionListener(new ActionListener() {
                                        @Override
                                        public void actionPerformed(ActionEvent ae) {
                                            getRegionSetsMenu(finalGene).show(addToRegionListButton, 0, addToRegionListButton.getHeight());
                                        }
                                    });

                                    kvp.setAdditionalColumn(Integer.toString(i), 2, geneInspectorButton);
                                    kvp.setAdditionalColumn(Integer.toString(i), 3, addToRegionListButton);
                                    kvp.setAdditionalColumn(Integer.toString(i), 4, geneLinkButton);
                                    i++;
                                }
                                currSizeOfArray = i - 1;
                            } else {
                                Iterator<String> itr = genemania.getRelatedGeneNamesByScore().iterator();
                                String currGene;
                                itr.next();//skip the first one (it's the name of selected gene already displayed)

                                if (Thread.interrupted()) {
                                    throw new InterruptedException();
                                }

                                System.out.println("start populating table" + System.currentTimeMillis());

                                while (itr.hasNext()) {
                                    currGene = itr.next();
                                    final org.ut.biolab.medsavant.shared.model.Gene finalGene = GeneSetFetcher.getInstance().getGene(currGene);
                                    if (finalGene != null) {
                                        kvp.addKey(Integer.toString(i));
                                        //System.err.println("get link out button" + System.currentTimeMillis());
                                        EntrezButton geneLinkButton = new EntrezButton(currGene);
                                        JLabel geneName = new JLabel(currGene);
                                        kvp.setValue(Integer.toString(i), geneName);
                                        //System.err.println("get inspector button" + System.currentTimeMillis());
                                        JButton geneInspectorButton = ViewUtil.getTexturedButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.INSPECTOR));
                                        geneInspectorButton.setToolTipText("Inspect this gene");
                                        geneInspectorButton.addActionListener(new ActionListener() {
                                            @Override
                                            public void actionPerformed(ActionEvent ae) {
                                                if (geneListener != null) {
                                                    geneListener.handleEvent(finalGene);
                                                }
                                            }
                                        });
                                        //System.err.println("get region sets" + System.currentTimeMillis());
                                        final JButton addToRegionListButton = ViewUtil.getTexturedButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.ADDKVP));
                                        addToRegionListButton.setToolTipText("Add to Region List");
                                        addToRegionListButton.addActionListener(new ActionListener() {
                                            @Override
                                            public void actionPerformed(ActionEvent e) {
                                                getRegionSetsMenu(finalGene).show(addToRegionListButton, 0, addToRegionListButton.getHeight());
                                            }
                                        });
                                        kvp.setAdditionalColumn(Integer.toString(i), 2, geneInspectorButton);
                                        kvp.setAdditionalColumn(Integer.toString(i), 3, addToRegionListButton);
                                        kvp.setAdditionalColumn(Integer.toString(i), 4, geneLinkButton);
                                        i++;
                                    }
                                }
                                System.err.println("done thread" + System.currentTimeMillis());

                                currSizeOfArray = i - 1;
                            }//end else

                        }//end if

                    } catch (InterruptedException e) {
                        System.out.println(e);
                        buildGraph = false;
                    } catch (NoRelatedGenesInfoException e) {
                        System.out.println(e);
                        progressMessage.setText(e.getMessage());
                        setMsgOff = false;
                        buildGraph = false;
                    } catch (Exception ex) {
                        System.out.println(ex);
                        buildGraph = false;
                        ClientMiscUtils.reportError("Error retrieving data from GeneMANIA: %s", ex);
                    } catch(Error e){
                        System.out.println("Caught error "+e);
                        System.out.println("getMessage: "+e.getMessage());
                        System.out.println("getLocalizedMessage: "+e.getLocalizedMessage());
                        e.printStackTrace();
                    }finally {
                        progressBar.setIndeterminate(false);
                        progressBar.setValue(0);
                        progressBar.setVisible(false);
                        graph.removeAll();
                        if (setMsgOff) {
                            progressMessage.setVisible(false);
                        }

                        if (buildGraph) {
                            graph.add(buildGraph());
                        }

                        graph.invalidate();
                        graph.updateUI();
                    }

                }

                synchronized (lock) {
                    lock.notify();
                }
            }
        };
        if (genemaniaAlgorithmThread == null) {
            genemaniaAlgorithmThread = new Thread(r);
        } else {
            genemaniaAlgorithmThread.interrupt();
            genemaniaAlgorithmThread = new Thread(r);

        }

        final Runnable geneDescriptionFetcher = new Runnable() {
            @Override
            public void run() {
                for (int j = 1; j <= currSizeOfArray; j++) {
                    try {
                        String geneName = kvp.getValue(Integer.toString(j));
                        Gene gene = GeneSetFetcher.getInstance().getGene(geneName);
                        String d = gene.getDescription();
                        kvp.setToolTipForValue(Integer.toString(j), d);
                    } catch (Exception e) {
                        //do nothing (don't set tool tip to anything)
                    }
                }
            }
        };
        //}


        genemaniaAlgorithmThread.start();

        Runnable r2 = new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (lock) {
                        lock.wait();
                        Thread toolTipGenerator = new Thread(geneDescriptionFetcher);
                        Thread varFreqCalculator = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("THIS IS THE CURRSIZEOFARRAY: " + currSizeOfArray);
                                for (int i = 1; i <= currSizeOfArray; i++) {
                                    try {
                                        String geneName = kvp.getValue(Integer.toString(i));
                                        Gene gene = GeneSetFetcher.getInstance().getGene(geneName);
                                        kvp.setAdditionalColumn(Integer.toString(i), 0, new JLabel(Double.toString(GeneSetFetcher.getInstance().getNormalizedVariantCount(gene))));
                                        kvp.invalidate();
                                        kvp.updateUI();
                                    } catch (Exception ex) {
                                        //don't put in any variation frequency
                                    }
                                }
                            }
                        });
                        toolTipGenerator.start();
                        varFreqCalculator.start();
                    }
                } catch (Exception e) {
                }
            }
        };

        Thread t2 = new Thread(r2);
        t2.start();



    }

    // final static Object lock;
    public JPanel buildGraph() {
        CyNetwork network = genemania.getGraph();
        System.out.println("Nodes " + network.getNodeCount());
        System.out.println("Edges " + network.getEdgeCount());
        //for (int i = 0; i < network.getEdgeCount(); i++) {
        //System.out.println(network.getEdge(i));
        //}

        CytoscapeUtils cy = new CytoscapeUtils(genemania.getNetworkUtils());
        Cytoscape.firePropertyChange(Cytoscape.NETWORK_MODIFIED, null, null);
        Cytoscape.firePropertyChange(Cytoscape.ATTRIBUTES_CHANGED, null, null);
        CyNetworkView view = cy.getNetworkView(network);
        CyLayoutAlgorithm layout = CyLayouts.getLayout(FilteredLayout.ID);
        if (layout == null) {
            layout = CyLayouts.getDefaultLayout();
        }
        layout.doLayout(view);
        //NetworkViewManager viewManager = Cytoscape.getDesktop().getNetworkViewManager();
        //JInternalFrame frame = viewManager.getInternalFrame(view);
        JPanel p = new JPanel();
        p.add(view.getComponent());
        return p;
    }
    /*
     * private JComponent getFrameFromView(CyNetworkView view) { final
     * JInternalFrame iframe = new JInternalFrame(view.getTitle(), true, true,
     * true, true);
     *
     *
     * // code added to support layered canvas for each CyNetworkView if (view
     * instanceof DGraphView) { final InternalFrameComponent internalFrameComp =
     * new InternalFrameComponent(iframe.getLayeredPane(), (DGraphView) view);
     *
     * iframe.getContentPane().add(internalFrameComp);
     *
     * } else { logger.info("NetworkViewManager.createContainer() - DGraphView
     * not found!"); iframe.getContentPane().add(view.getComponent()); }
     *
     * iframe.pack();
     *
     * int x = 0; int y = 0; JInternalFrame refFrame = null; JInternalFrame[]
     * allFrames = desktopPane.getAllFrames();
     *
     * if (allFrames.length > 1) { refFrame = allFrames[0]; }
     *
     * if (refFrame != null) { x = refFrame.getLocation().x + 20; y =
     * refFrame.getLocation().y + 20; }
     *
     * if (x > (desktopPane.getWidth() - MINIMUM_WIN_WIDTH)) { x =
     * desktopPane.getWidth() - MINIMUM_WIN_WIDTH; }
     *
     * if (y > (desktopPane.getHeight() - MINIMUM_WIN_HEIGHT)) { y =
     * desktopPane.getHeight() - MINIMUM_WIN_HEIGHT; }
     *
     * if (x < 0) { x = 0; }
     *
     * if (y < 0) { y = 0; }
     *
     * iframe.setBounds(x, y, 400, 400);
     *
     * // maximize the frame if the specified property is set try { String max
     * = CytoscapeInit.getProperties().getProperty("maximizeViewOnCreate");
     *
     * if ((max != null) && Boolean.parseBoolean(max)) iframe.setMaximum(true);
     * } catch (PropertyVetoException pve) { //logger.warn("Unable to maximize
     * internal frame: "+pve.getMessage()); }
     *
     * iframe.setVisible(true); //iframe.addInternalFrameListener(this);
     * iframe.setResizable(true);
     *
     * return iframe; }
     *
     */
}