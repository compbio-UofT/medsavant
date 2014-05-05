/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.app.google;

import com.google.api.services.genomics.Genomics;
import com.google.api.services.genomics.Genomics.Datasets;
import com.google.api.services.genomics.Genomics.Datasets.List;
import com.google.api.services.genomics.model.Dataset;
import com.google.api.services.genomics.model.ListDatasetsResponse;
import com.google.api.services.genomics.model.Readset;
import com.google.api.services.genomics.model.SearchReadsetsRequest;
import com.google.api.services.genomics.model.SearchReadsetsResponse;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.app.google.original.GoogleAuthenticate;
import org.ut.biolab.medsavant.app.google.original.GoogleBAMDataSource;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.app.AppDirectory;
import org.ut.biolab.medsavant.client.view.app.builtin.SavantApp;
import org.ut.biolab.medsavant.client.view.component.WaitPanel;
import org.ut.biolab.medsavant.client.view.util.StandardFixableWidthAppPanel;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.client.view.util.list.NiceList;
import org.ut.biolab.medsavant.client.view.util.list.NiceListItem;
import org.ut.biolab.medsavant.client.view.util.list.WhiteNiceListColorScheme;
import savant.api.data.DataFormat;
import savant.api.util.DialogUtils;

/**
 *
 * @author mfiume
 */
class ReadsView extends JPanel {

    private Genomics genomics;
    private JPanel datasetBlock;

    private GoogleDataset dataset = null;
    private Thread fetcher;

    private class GoogleDataset {

        private final String id;
        private final String name;

        public GoogleDataset(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public ReadsView() {
        this.setLayout(new BorderLayout());
        StandardFixableWidthAppPanel p = new StandardFixableWidthAppPanel("Reads", false);

        JPanel datasetChooserBlock = p.addBlock("Dataset");
        datasetChooserBlock.setLayout(new MigLayout("insets 0, fillx, filly, wrap"));

        GoogleDataset defaultDataset = new GoogleDataset("376902546192", "1000 Genomes");

        final JComboBox datasetChooser = new JComboBox();
        datasetChooser.addItem(defaultDataset);
        datasetChooser.addItem(new GoogleDataset("383928317087", "PGP"));
        datasetChooser.addItem(new GoogleDataset("461916304629", "Simons Foundation"));
        //datasetChooser.addItem(new GoogleDataset("SRP034507", "SRP034507"));
        //datasetChooser.addItem(new GoogleDataset("SRP029392", "SRP029392"));
        datasetChooser.setSelectedItem(defaultDataset);

        datasetChooser.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                dataset = (GoogleDataset) datasetChooser.getSelectedItem();
                try {
                    refreshReadsets();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

        });

        datasetChooserBlock.add(datasetChooser);

        datasetBlock = ViewUtil.getClearPanel();
        datasetBlock.setLayout(new MigLayout("insets 0, fillx, filly"));
        datasetChooserBlock.add(datasetBlock,"growx 1.0, width 100%");
        
        try {
            genomics = GoogleAuthenticate.buildService();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        this.add(p, BorderLayout.CENTER);

        try {
            dataset = defaultDataset;
            refreshReadsets();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void refreshReadsets() throws IOException {

        datasetBlock.removeAll();

        if (dataset == null) {
            datasetBlock.add(ViewUtil.getGrayItalicizedLabel("Choose a dataset"));
            datasetBlock.invalidate();
            return;
        }

        final WaitPanel p = new WaitPanel("Retrieving readsets");
        datasetBlock.add(p, "growx 1.0, width 100%");
        datasetBlock.invalidate();

        if (fetcher != null) {
            fetcher.interrupt();
        }

        fetcher = new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    java.util.List<java.lang.String> datasetIds = new ArrayList<java.lang.String>();
                    datasetIds.add(dataset.getId());
                    SearchReadsetsRequest content = new SearchReadsetsRequest().setDatasetIds(datasetIds);
                    String pageToken = null;

                    final NiceList niceList = new NiceList();
                    niceList.setColorScheme(new WhiteNiceListColorScheme());
                    niceList.startTransaction();

                    int pageCount = 0;
                    while (true) {

                        if (Thread.currentThread().isInterrupted()) {
                            return;
                        }

                        content.setPageToken(pageToken);
                        Genomics.Readsets.Search req = genomics.readsets().search(content);
                        SearchReadsetsResponse o = req.execute();
                        java.util.List<Readset> readsets = o.getReadsets();

                        if (readsets == null || readsets.isEmpty()) {
                            break;
                        } else {
                            for (Readset rs : readsets) {
                                NiceListItem item = new NiceListItem(rs.getName(), rs.getId());
                                //item.setDescription(rs.getId());
                                niceList.addItem(item);
                            }

                            pageToken = o.getNextPageToken();
                            if (pageToken == null) {
                                break;
                            }
                        }

                        pageCount++;
                        p.setStatus(pageCount + " " + ((pageCount == 1) ? "page" : "pages") + " received...");
                    }

                    niceList.endTransaction();

                    final JButton b = new JButton("Refresh");
                    b.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                refreshReadsets();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    });

                    final JButton load = new JButton("Open in Genome Browser");
                    load.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                java.util.List<NiceListItem> selected = niceList.getSelectedItems();

                                if (selected != null && selected.size() > 0) {
                                    String readSetName = niceList.getSelectedItems().get(0).toString();
                                    String readsetID = niceList.getSelectedItems().get(0).getItem().toString();
                                    SavantApp app = AppDirectory.getGenomeBrowser();
                                    GoogleBAMDataSource ds = new GoogleBAMDataSource(readSetName, readsetID);
                                    app.addTrackFromDataSource(ds);
                                    MedSavantFrame.getInstance().getDashboard().launchApp(app);
                                } else {
                                    DialogUtils.displayMessage("Choose a readset to load");
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    });

                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            datasetBlock.removeAll();
                            datasetBlock.add(niceList.getSearchBar(), "width 100%, wrap");
                            datasetBlock.add(ViewUtil.getClearBorderlessScrollPane(niceList), "width 100%, height 500, growy 1.0, wrap");
                            datasetBlock.add(b, "split");
                            datasetBlock.add(load, "wrap, right");
                            datasetBlock.updateUI();
                        }

                    });

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

        }
        );
        fetcher.start();
    }
}
