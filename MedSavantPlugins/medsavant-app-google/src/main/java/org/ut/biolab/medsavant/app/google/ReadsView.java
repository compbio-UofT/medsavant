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

    public ReadsView() {
        this.setLayout(new BorderLayout());
        StandardFixableWidthAppPanel p = new StandardFixableWidthAppPanel("Reads", false);
        datasetBlock = p.addBlock("Alignment Datasets");
        datasetBlock.setLayout(new MigLayout("insets 0, fillx, filly"));

        try {
            genomics = GoogleAuthenticate.buildService();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        this.add(p, BorderLayout.CENTER);

        try {
            refreshReadsets();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void refreshReadsets() throws IOException {

        datasetBlock.removeAll();
        datasetBlock.add(new WaitPanel("Retrieving readsets"), "growx 1.0, width 100%");
        datasetBlock.invalidate();

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    java.util.List<java.lang.String> datasetIds = new ArrayList<java.lang.String>();
                    datasetIds.add("376902546192");
                    SearchReadsetsRequest content = new SearchReadsetsRequest().setDatasetIds(datasetIds);
                    String pageToken = null;

                    final NiceList niceList = new NiceList();
                    niceList.setColorScheme(new WhiteNiceListColorScheme());
                    niceList.startTransaction();

                    while (true) {

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
        t.start();
    }
}
