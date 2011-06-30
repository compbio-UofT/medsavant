/*
 *    Copyright 2011 University of Toronto
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

package pathways;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import org.bridgedb.DataSource;
import org.bridgedb.bio.Organism;
import org.pathvisio.wikipathways.WikiPathwaysClient;
import org.pathvisio.wikipathways.webservice.WSPathwayInfo;
import org.pathvisio.wikipathways.webservice.WSSearchResult;
//import org.ut.biolab.medsavant.controller.SettingsController;
//import savant.api.util.DialogUtils;
//import savant.settings.DirectorySettings;

/**
 *
 * @author AndrewBrook
 */

public class PathwaysBrowser extends JPanel{

    private JLabel messageLabel;
    private JTable table;
    private WikiPathwaysClient wpclient;
    //private SVGViewer svgPanel;
    private Viewer svgPanel;
    private Loader loader;
    
    private static final String ALL_ORGANISMS = "All Organisms";
    private static final String SELECT_ORGANISM = "Select an organism to display pathways:";
    private static final String SELECT_PATHWAY = "Select a pathway to display:";
    private static final String SEARCH_RESULTS = "Search Results: ";
    private static final String ERROR_MESSAGE = "There was an error...";
    private static final String LOADING = "Your request is being processed...";

    private enum location { ORGANISMS, PATHWAYS, SEARCH};
    private location loc = location.ORGANISMS;

    private boolean used = false;
    
    public PathwaysBrowser(WikiPathwaysClient client, Viewer svgPanel, Loader loader) {

        this.wpclient = client;
        this.svgPanel = svgPanel;
        this.loader = loader;

        setLayout(new BorderLayout());

        messageLabel = new JLabel(SELECT_ORGANISM);
        messageLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        add(messageLabel, BorderLayout.NORTH);

        table = new JTable();
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt){
                if (evt.getClickCount() == 2) {
                    int row = table.rowAtPoint(evt.getPoint());
                    row = table.getRowSorter().convertRowIndexToModel(row);
                    if(loc == location.ORGANISMS){
                        listPathways(((OrganismTableModel)table.getModel()).getEntry(row));
                    } else if (loc == location.PATHWAYS){
                        WSPathwayInfo selection = ((PathwayTableModel)table.getModel()).getEntry(row);
                        if(selection == null){
                            listOrganisms();
                        } else {
                            loadPathway(selection.getId());
                        }
                    } else if (loc == location.SEARCH){
                        WSSearchResult selection = ((SearchTableModel)table.getModel()).getEntry(row);
                        loadPathway(selection.getId());
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);

        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }

    public boolean hasBeenUsed(){
        return used;
    }

    public void startBrowse(){
        used = true;
        listOrganisms();
    }

    public void startText(final String query, final String organismString){
        used = true;
        startLoad();
        final PathwaysBrowser instance = this;
        Thread thread = new Thread() {
            public void run() {
                try {
                    WSSearchResult[] search = null;
                    if(organismString == null || organismString.equals("") || organismString.equals("All")){
                        search = wpclient.findPathwaysByText(query);
                    } else {
                        search = wpclient.findPathwaysByText(query, Organism.fromLatinName(organismString));
                    }
                    if(search.length == 0){
                        JOptionPane.showMessageDialog(instance, "Your search returned no results. ", "No Results", JOptionPane.ERROR_MESSAGE);
                    }
                    table.setModel(new SearchTableModel(search));
                    messageLabel.setText(SEARCH_RESULTS);
                    loc = location.SEARCH;
                } catch (RemoteException ex) {
                    Logger.getLogger(PathwaysBrowser.class.getName()).log(Level.SEVERE, null, ex);
                }
                endLoad();
            }
        };
        thread.start();

        
    }

    private void listOrganisms(){    
        startLoad();
        Thread thread = new Thread() {
            public void run() {
                String[] organisms = new String[0];
                try {
                    organisms = wpclient.listOrganisms();
                    table.setModel(new OrganismTableModel(organisms, false));
                    loc = location.ORGANISMS;
                    messageLabel.setText(SELECT_ORGANISM);
                } catch (RemoteException ex) {
                    //DialogUtils.displayException("WikiPathways Error", "Unable to process request.", ex);
                }
                endLoad();
            }
        };
        thread.start();
    }

    private void listPathways(final String organism){
        startLoad();
        Thread thread = new Thread() {
            public void run() {
                WSPathwayInfo[] pathways = new WSPathwayInfo[0];
                try {
                    if(organism.equals(ALL_ORGANISMS)){
                        pathways = wpclient.listPathways();
                    } else {
                        pathways = wpclient.listPathways(Organism.fromLatinName(organism));
                    }
                    table.setModel(new PathwayTableModel(pathways, true));
                    loc = location.PATHWAYS;
                    messageLabel.setText(SELECT_PATHWAY);
                } catch (RemoteException ex) {
                    //DialogUtils.displayException("WikiPathways Error", "Unable to process request.", ex);
                }
                endLoad();
            }
        };
        thread.start();
    }

    public void loadPathway(final String pathwayID){
        startLoad();
        final PathwaysBrowser instance = this;
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {

                    //TODO: parallelize data retrieval!

                    //get svg
                    //TODO: these paths should be retreived from medsavant!!!
                    String filename = System.getProperty("user.home") + System.getProperty("file.separator") + "medsavant" + System.getProperty("file.separator") + pathwayID + ".svg";
                    //String filename = SettingsController.getTempDirectory() + System.getProperty("file.separator") + pathwayID + ".svg";
                    byte[] svgByte = wpclient.getPathwayAs("svg", pathwayID, 0);
                    OutputStream out;
                    out = new FileOutputStream(filename);
                    out.write(svgByte);
                    out.close();

                    //get gpml
                    String filename1 = System.getProperty("user.home") + System.getProperty("file.separator") + "medsavant" + System.getProperty("file.separator") + pathwayID + ".gpml";
                    //String filename1 = SettingsController.getTempDirectory() + System.getProperty("file.separator") + pathwayID + ".gpml";
                    byte[] gpmlByte = wpclient.getPathwayAs("gpml", pathwayID, 0);
                    OutputStream out1;
                    out1 = new FileOutputStream(filename1);
                    out1.write(gpmlByte);
                    out1.close();

                    //set pathway
                    svgPanel.setPathway(new File(filename).toURI(), new File(filename1).toURI());
                    setVisible(false);
                    svgPanel.setVisible(true);
                    loader.setVisible(false);
                                        
                } catch (RemoteException ex) {
                    JOptionPane.showMessageDialog(instance, "The pathway '" + pathwayID + "' could not be found.", "Error", JOptionPane.ERROR_MESSAGE);
                    loader.setVisible(false);
                    svgPanel.setVisible(false);
                    setVisible(false);
                    //Logger.getLogger(PathwaysBrowser.class.getName()).log(Level.SEVERE, null, ex);
                } catch (FileNotFoundException ex){
                    Logger.getLogger(PathwaysBrowser.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex){
                    Logger.getLogger(PathwaysBrowser.class.getName()).log(Level.SEVERE, null, ex);
                }                
            }
        };
        thread.start();     
    }

    private void startLoad(){
        loader.setMessage(LOADING);
        loader.setVisible(true);
        svgPanel.setVisible(false);
        this.setVisible(false);
    }

    private void endLoad(){
        loader.setVisible(false);
        setVisible(true);
    }

    private class OrganismTableModel extends AbstractTableModel {
        private List<String> names = new ArrayList<String>();
        private String[] headers = {"Latin Name", "English Name"};

        OrganismTableModel(String[] names, boolean hasParent){
            if(hasParent) this.names.add(null);
            this.names.add(ALL_ORGANISMS);
            this.names.addAll(Arrays.asList(names));
        }

        @Override
        public int getRowCount() {
            return names.size();
        }

        @Override
        public int getColumnCount() {
            return headers.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            String s = names.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return s == null ? ".." : s;
                case 1:
                    if(s == null) return "..";
                    else if (s.equals(ALL_ORGANISMS)) return "";
                    else return Organism.fromLatinName(s).shortName();
            }
            return null;
        }

        @Override
        public String getColumnName(int column) {
            return headers[column];
        }

        public String getEntry(int row){
            return names.get(row);
        }
    }

    private class PathwayTableModel extends AbstractTableModel {
        private List<WSPathwayInfo> pathways = new ArrayList<WSPathwayInfo>();
        private String[] headers = {"ID", "Name", "Species", "Revision", "URL"};

        PathwayTableModel(WSPathwayInfo[] pathways, boolean hasParent){
            if(hasParent) this.pathways.add(null);
            this.pathways.addAll(Arrays.asList(pathways));
        }

        @Override
        public int getRowCount() {
            return pathways.size();
        }

        @Override
        public int getColumnCount() {
            return headers.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            WSPathwayInfo pathway = pathways.get(rowIndex);
            switch (columnIndex){
                case 0:
                    return pathway == null ? ".." : pathway.getId();
                case 1:
                    return pathway == null ? "" : pathway.getName();
                case 2:
                    return pathway == null ? "" : pathway.getSpecies();
                case 3:
                    return pathway == null ? "" : pathway.getRevision();
                case 4:
                    return pathway == null ? "" : pathway.getUrl();
            }
            return null;
        }
        
        @Override
        public String getColumnName(int column) {
            return headers[column];
        }

        public WSPathwayInfo getEntry(int row){
            return pathways.get(row);
        }
    }

    private class SearchTableModel extends AbstractTableModel {
        private List<WSSearchResult> pathways = new ArrayList<WSSearchResult>();
        private String[] headers = {"Search Relevance", "ID", "Name", "Species", "Revision", "URL"};

        SearchTableModel(WSSearchResult[] pathways){
            this.pathways.addAll(Arrays.asList(pathways));
        }

        @Override
        public int getRowCount() {
            return pathways.size();
        }

        @Override
        public int getColumnCount() {
            return headers.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            WSSearchResult pathway = pathways.get(rowIndex);
            switch (columnIndex){
                case 0:
                    return pathway.getScore();
                case 1:
                    return pathway.getId();
                case 2:
                    return pathway.getName();
                case 3:
                    return pathway.getSpecies();
                case 4:
                    return pathway.getRevision();
                case 5:
                    return pathway.getUrl();
            }
            return null;
        }

        @Override
        public String getColumnName(int column) {
            return headers[column];
        }

        public WSSearchResult getEntry(int row){
            return pathways.get(row);
        }
    }
}
