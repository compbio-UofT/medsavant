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
package org.ut.biolab.medsavant.app.google;

import com.google.api.services.genomics.Genomics;
import com.google.api.services.genomics.model.Readset;
import com.google.api.services.genomics.model.SearchReadsetsRequest;
import com.google.api.services.genomics.model.SearchReadsetsResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.app.google.original.GoogleAuthenticate;
import org.ut.biolab.medsavant.app.google.original.GoogleBAMDataSource;
import org.ut.biolab.medsavant.client.settings.DirectorySettings;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.app.AppDirectory;
import org.ut.biolab.medsavant.client.view.app.builtin.SavantApp;
import org.ut.biolab.medsavant.client.view.component.WaitPanel;
import org.ut.biolab.medsavant.client.view.util.StandardFixableWidthAppPanel;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.client.view.util.form.NiceForm;
import org.ut.biolab.medsavant.client.view.util.form.NiceFormField;
import org.ut.biolab.medsavant.client.view.util.form.NiceFormFieldGroup;
import org.ut.biolab.medsavant.client.view.util.form.NiceFormModel;
import org.ut.biolab.medsavant.client.view.util.list.NiceList;
import org.ut.biolab.medsavant.client.view.util.list.NiceListItem;
import org.ut.biolab.medsavant.client.view.util.list.WhiteNiceListColorScheme;
import org.ut.biolab.medsavant.component.field.editable.StringEditableField;
import savant.api.util.DialogUtils;

/**
 *
 * @author mfiume
 */
class ReadsView extends JPanel {

    private File datasetFile;

    {
        File googleDir = new File(DirectorySettings.getCacheDirectory(), "google");
        googleDir.mkdirs();
        datasetFile = new File(googleDir, "datasets.ser");
    }

    private Genomics genomics;
    private JPanel datasetBlock;

    private GoogleDataset dataset = null;
    private Thread fetcher;

    private boolean isUpdatingChooser = false;

    private java.util.List<GoogleDataset> datasets;
    private JComboBox datasetChooser;

    void updateGenomicsService() {
        this.removeAll();
        this.initUI();
        this.updateUI();
    }

    private void initUI() {
        this.setLayout(new BorderLayout());
        StandardFixableWidthAppPanel p = new StandardFixableWidthAppPanel("Google Genomics", false);

        boolean successfullyAuthenticated = false;
        try {
            genomics = GoogleAuthenticate.buildService();
            successfullyAuthenticated = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        JPanel datasetChooserBlock = p.addBlock();

        if (successfullyAuthenticated) {

            datasetChooserBlock.setLayout(new MigLayout("insets 0, fillx, filly"));

            datasetChooser = new JComboBox();
            loadDatasets();
            updateChooser();

            datasetChooser.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (isUpdatingChooser) {
                        return;
                    }
                    dataset = (GoogleDataset) datasetChooser.getSelectedItem();
                    try {
                        refreshReadsets();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

            });

            JButton addButton = ViewUtil.getSoftButton("Add");
            JButton removeButton = ViewUtil.getSoftButton("Remove");

            ViewUtil.positionButtonFirst(addButton);
            ViewUtil.positionButtonLast(removeButton);

            addButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    final JDialog dialog = new JDialog(MedSavantFrame.getInstance(), "Add Dataset", true);

                    JPanel form = new JPanel(new MigLayout("wrap 2"));

                    final StringEditableField nameField = new StringEditableField();
                    nameField.setName("Name");
                    nameField.setAutonomousEditingEnabled(false);
                    nameField.setEditing(true);

                    final StringEditableField idField = new StringEditableField();
                    idField.setName("ID");
                    idField.setAutonomousEditingEnabled(false);
                    idField.setEditing(true);

                    form.add(new JLabel("Name:"));
                    form.add(nameField);
                    form.add(new JLabel("ID:"));
                    form.add(idField);

                    JButton addButton = new JButton("OK");
                    addButton.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {

                            String name = nameField.getValue();
                            String id = idField.getValue();

                            if (name.isEmpty() || id.isEmpty()) {
                                DialogUtils.displayError("Name and ID must not be empty");
                                return;
                            }

                            GoogleDataset ds = new GoogleDataset(id, name);
                            datasets.add(ds);
                            saveDatasets();
                            dataset = ds;
                            updateChooser();
                            refreshReadsets();

                            dialog.dispose();
                        }

                    });

                    JPanel p = new JPanel(new MigLayout("wrap"));
                    p.add(form);
                    p.add(addButton);

                    dialog.add(p);
                    dialog.pack();

                    dialog.setLocationRelativeTo(MedSavantFrame.getInstance());
                    dialog.setVisible(true);
                }

            });

            removeButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    String name = dataset.getName();
                    if (DialogUtils.askYesNo("<html>Are you sure you want to remove <i>" + name + "</i>?</html>") == DialogUtils.YES) {
                        datasets.remove(dataset);
                        saveDatasets();
                        dataset = datasets.get(0);
                        updateChooser();
                        refreshReadsets();
                    }
                }

            });

            datasetChooserBlock.add(new JLabel("<html><b>Read Dataset:</b></html>"), "split");
            datasetChooserBlock.add(datasetChooser);
            datasetChooserBlock.add(addButton, "gapx 0");
            datasetChooserBlock.add(removeButton, "wrap, gapx 0");

            datasetBlock = ViewUtil.getClearPanel();
            datasetBlock.setLayout(new MigLayout("insets 0, fillx, filly"));
            datasetChooserBlock.add(datasetBlock, "growx 1.0, width 100%");

            try {
                dataset = datasets.get(0);
                refreshReadsets();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            datasetChooserBlock.add(new JLabel("<html>In order to use this app, you need to grant access using your Google account.</html>"), "wrap");
            JButton b = new JButton("Authenticate");
            b.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    updateGenomicsService();
                }

            });
            datasetChooserBlock.add(b);
        }

        this.add(p, BorderLayout.CENTER);
    }

    private void loadDatasets() {

        if (!datasetFile.exists()) {
            datasets = new ArrayList<GoogleDataset>();
            datasets.add(new GoogleDataset("461916304629", "Simons Foundation"));
            datasets.add(new GoogleDataset("383928317087", "PGP"));
            datasets.add(new GoogleDataset("376902546192", "1000 Genomes"));
            saveDatasets();

        } else {
            System.out.println("Loading datasets");
            Reader reader = null;
            try {
                reader = new InputStreamReader(new FileInputStream(datasetFile), "UTF-8");
                Gson gson = new GsonBuilder().create();
                GoogleDataset[] datasetArray = gson.fromJson(reader, GoogleDataset[].class);
                datasets = new ArrayList<GoogleDataset>(Arrays.asList(datasetArray));

            } catch (Exception ex) {
                Logger.getLogger(ReadsView.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    reader.close();
                } catch (IOException ex) {
                    Logger.getLogger(ReadsView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void saveDatasets() {
        System.out.println("Saving datasets");
        Writer writer = null;
        try {
            writer = new FileWriter(datasetFile);
            Gson gson = new GsonBuilder().create();
            gson.toJson(datasets, writer);
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(ReadsView.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(ReadsView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void updateChooser() {
        isUpdatingChooser = true;
        datasetChooser.removeAllItems();
        for (GoogleDataset gd : datasets) {
            datasetChooser.addItem(gd);
        }
        if (dataset != null && datasets.contains(dataset)) {
            datasetChooser.setSelectedItem(dataset);
        }
        isUpdatingChooser = false;
    }

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
        this.updateGenomicsService();
    }

    private void refreshReadsets() {

        datasetBlock.removeAll();

        if (dataset == null) {
            datasetBlock.add(ViewUtil.getGrayItalicizedLabel("Choose a dataset"));
            datasetBlock.invalidate();
            return;
        }

        final WaitPanel p = new WaitPanel("Retrieving readsets");

        datasetBlock.add(p, "growx 1.0, width 100%");
        datasetBlock.invalidate();

        datasetBlock.updateUI();

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
                            } catch (Exception ex) {
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
                                    try {
                                        final SavantApp app = AppDirectory.getGenomeBrowser();
                                        final GoogleBAMDataSource ds = new GoogleBAMDataSource(readSetName, readsetID);

                                        MedSavantFrame.getInstance().getDashboard().launchApp(app);
                                        app.addTrackFromDataSource(ds);

                                    } catch (Exception ex) {
                                        DialogUtils.displayException("Problem Loading Track", "There was a problem loading track for readset " + readSetName, ex);
                                    }

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
                            //datasetBlock.add(b, "split");
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
