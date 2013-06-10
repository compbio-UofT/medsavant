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
package org.ut.biolab.medsavant.client.filter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.ut.biolab.medsavant.client.settings.DirectorySettings;

import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.variant.ExportVCFWizard;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.genetics.GeneticsFilterPage;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.mfiume.query.medsavant.MedSavantConditionViewGenerator;
import org.ut.biolab.mfiume.query.QueryViewController;
import org.ut.biolab.mfiume.query.SearchConditionGroupItem;

/**
 * Panel which contains a collection of
 * <code>QueryPanel</code>s, each of which contains a range of
 * <code>Filter</code>s.
 *
 * @author Andrew
 */
public class SearchBar extends JPanel {
    private static final String SAVED_SEARCH_EXTENSION = "cond";
    private static SearchBar instance;

    private FilterController controller;
    List<QueryPanel> queryPanels = new ArrayList<QueryPanel>();
    private int nextQueryID = 1;
    private JPanel queryPanelContainer;
    private FilterHistoryPanel historyPanel;
    //private JToggleButton historyButton;
    //private SavedFiltersPanel savedFiltersPanel;
    //private JToggleButton savedFiltersButton;
    private QueryViewController queryViewController;

    /**
     * Creates search bar to contains our query panels.
     */
    private SearchBar() {
        controller = FilterController.getInstance();
        initComponents();
        createNewQueryPanel();
    }

    public static SearchBar getInstance() {
        if (instance == null) {
            instance = new SearchBar();
        }
        return instance;
    }

    /**
     * Add a new query panel to the search area. Currently, the UI provides no
     * way to add additional query panels, so there can only be one.
     */
    public final QueryPanel createNewQueryPanel() {

        QueryPanel cp = new QueryPanel(nextQueryID++);
        queryPanelContainer.add(cp);
        queryPanels.add(cp);

        refreshSubPanels();

        return cp;
    }

    public void refreshSubPanels() {
        queryPanelContainer.removeAll();

        //refresh panel
        for (int i = 0; i < queryPanels.size(); i++) {
            queryPanelContainer.add(queryPanels.get(i));
            queryPanelContainer.add(Box.createVerticalStrut(5));
        }

        queryPanelContainer.add(Box.createVerticalGlue());
    }

    public void clearAll() {
        queryPanels.clear();
        nextQueryID = 1;
        createNewQueryPanel();
    }

    void loadFiltersFromFiles(Collection<File> files) throws Exception {

        List<List<FilterState>> states = FilterState.loadFiltersFromFiles(files);

        controller.removeAllFilters();
        queryPanels.clear();
        nextQueryID = 1;

        for (int i = 0; i < states.size(); i++) {
            QueryPanel qp = createNewQueryPanel();
            List<FilterState> filters = states.get(i);
            for (FilterState state : filters) {
                qp.loadFilterView(state);
            }
        }
        refreshSubPanels();
    }

    private void initComponents() {
        setOpaque(false);
        setLayout(new BorderLayout());

        FilterEffectivenessPanel effectivenessPanel = new FilterEffectivenessPanel(new Color(20, 20, 20));

        queryPanelContainer = ViewUtil.getClearPanel();
        queryPanelContainer.setLayout(new BoxLayout(queryPanelContainer, BoxLayout.Y_AXIS));

        JLabel historyLabel = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.HISTORY_ON_TOOLBAR));
        historyLabel.setToolTipText("Show search history");

        JLabel loadLabel = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.LOAD_ON_TOOLBAR));
        loadLabel.setToolTipText("Load search conditions");

        JLabel saveLabel = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.SAVE_ON_TOOLBAR));
        saveLabel.setToolTipText("Save search");

        final JLabel actionLabel = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.ACTION_ON_TOOLBAR));
        actionLabel.setToolTipText("Actions");


        historyPanel = new FilterHistoryPanel();
        //historyPanel.setVisible(false);


        //savedFiltersPanel.setVisible(false);

        final Dimension dialogDimensions = new Dimension(400, 400);

        historyPanel.setMinimumSize(dialogDimensions);
        historyPanel.setPreferredSize(dialogDimensions);

        final JDialog dHistory = new JDialog(MedSavantFrame.getInstance(), true);
        dHistory.setTitle("Search History");
        dHistory.add(historyPanel);
        dHistory.pack();
        dHistory.setLocationRelativeTo(MedSavantFrame.getInstance());
        dHistory.setResizable(false);

        historyLabel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent me) {
                dHistory.setVisible(true);
            }

            @Override
            public void mousePressed(MouseEvent me) {
            }

            @Override
            public void mouseReleased(MouseEvent me) {
            }

            @Override
            public void mouseEntered(MouseEvent me) {
            }

            @Override
            public void mouseExited(MouseEvent me) {
            }
        });

        final JFileChooser fileChooser = new JFileChooser();
        
        final JDialog dSave = new JDialog(MedSavantFrame.getInstance(), true);
        dSave.setTitle("Load Search Conditions");
        dSave.setResizable(false);

        loadLabel.addMouseListener(new MouseListener() {
            SavedFiltersPanel savedFiltersPanel;

            @Override
            public void mouseClicked(MouseEvent me) {                
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Saved Searches", SAVED_SEARCH_EXTENSION);
                fileChooser.setFileFilter(filter);
        
                if (fileChooser.showSaveDialog(MedSavantFrame.getInstance()) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                            
                    if(file.exists()){                      
                        getQueryViewController().loadConditions(file);                      
                    }else{
                        DialogUtils.displayError("File "+file.getPath()+" does not exist!");
                    }  
                }      
                
                
               /* if (savedFiltersPanel != null) {
                    dSave.remove(savedFiltersPanel);
                }
                savedFiltersPanel = new SavedFiltersPanel();
                savedFiltersPanel.setMinimumSize(dialogDimensions);
                savedFiltersPanel.setPreferredSize(dialogDimensions);
                dSave.add(savedFiltersPanel);
                dSave.pack();
                dSave.setLocationRelativeTo(MedSavantFrame.getInstance());
                dSave.setVisible(true);*/
                
            }

            @Override
            public void mousePressed(MouseEvent me) {
            }

            @Override
            public void mouseReleased(MouseEvent me) {
            }

            @Override
            public void mouseEntered(MouseEvent me) {
            }

            @Override
            public void mouseExited(MouseEvent me) {
            }
        });

        saveLabel.addMouseListener(new MouseListener() {
            private boolean validateName(String name) {
                File[] existingFilterSets = DirectorySettings.getFiltersDirectory().listFiles();
                for (File f : existingFilterSets) {
                    if (f.getName().equals(name)) {
                        return DialogUtils.askYesNo("Replace Filter Set", "<html>A filter set named <i>%s</i> already exists.<br>Do you want to overwrite it?</html>", name) == DialogUtils.YES;
                    }
                }
                return true;
            }

            private void saveFilterSet(String name) throws Exception {

                File file = new File(DirectorySettings.getFiltersDirectory(), name);

                BufferedWriter out = new BufferedWriter(new FileWriter(file, false));
                out.write("<filters>\n");
                for (QueryPanel sub : GeneticsFilterPage.getSearchBar().queryPanels) {
                    out.write("\t<set>\n");
                    for (FilterHolder item : sub.getFilterHolders()) {
                        if (item.hasFilterView()) {
                            out.write(item.getFilterView().saveState().generateXML() + "\n");
                        }
                    }
                    out.write("\t</set>\n");
                }
                out.write("</filters>\n");
                out.close();
            }

            private void saveFile(File file){
                getQueryViewController().saveConditions(file);
                JOptionPane.showMessageDialog(MedSavantFrame.getInstance(), "Search conditions saved to "+file.getPath());
            }
            
            @Override
            public void mouseClicked(MouseEvent me) {                
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Saved Searches", SAVED_SEARCH_EXTENSION);
                fileChooser.setFileFilter(filter);
        
                if (fileChooser.showSaveDialog(MedSavantFrame.getInstance()) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    if(!file.getPath().toLowerCase().endsWith("."+SAVED_SEARCH_EXTENSION)){
                        file = new File(file.getPath() + "."+SAVED_SEARCH_EXTENSION);
                    }            
                    if(file.exists()){
                        int r = JOptionPane.showConfirmDialog(fileChooser, "The file "+file.getPath()+" already exists.  Overwrite?","Warning",JOptionPane.YES_NO_OPTION);
                        if(r == JOptionPane.YES_OPTION){
                            saveFile(file);
                        }
                    }else{
                        saveFile(file);
                    }    
                }                
                /*
                if (FilterController.getInstance().hasFiltersApplied()) {
                    String name = "Untitled";
                    do {
                        name = DialogUtils.displayInputMessage("Save Search", "Provide a name for this set of search conditions:", name);
                        if (name == null) {
                            return;     // User cancelled input dialog.
                        }
                        // If there's no extension, add ".xml".
                        if (name.indexOf('.') < 0) {
                            name += ".xml";
                        }
                    } while (!validateName(name));
                    try {
                        saveFilterSet(name);
                    } catch (Exception ex) {
                        ClientMiscUtils.reportError("Unable to save filter set: %s", ex);
                    }
                } else {
                    DialogUtils.displayError("Can't save search", "No filters are applied.");
                }
                */
            }

            @Override
            public void mousePressed(MouseEvent me) {
            }

            @Override
            public void mouseReleased(MouseEvent me) {
            }

            @Override
            public void mouseEntered(MouseEvent me) {
            }

            @Override
            public void mouseExited(MouseEvent me) {
            }
        });

        final JPopupMenu actionPopup = new JPopupMenu();
        JMenuItem exportAction = new JMenuItem("Export VCF");
        exportAction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                try {
                    new ExportVCFWizard().setVisible(true);
                } catch (Exception ex) {
                    ClientMiscUtils.reportError("Unable to launch Variant Export wizard: %s", ex);
                }
            }
        });
        actionPopup.add(exportAction);


        actionLabel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent me) {
                System.out.println("Showing actions");
                actionPopup.show(actionLabel, 0, -(actionLabel.getHeight()));
            }

            @Override
            public void mousePressed(MouseEvent me) {
            }

            @Override
            public void mouseReleased(MouseEvent me) {
            }

            @Override
            public void mouseEntered(MouseEvent me) {
            }

            @Override
            public void mouseExited(MouseEvent me) {
            }
        });

        JPanel bottomBar = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(bottomBar);
        /*bottomBar.add(saveLabel);
        bottomBar.add(Box.createRigidArea(new Dimension(3, 1)));
        bottomBar.add(loadLabel);*/
        bottomBar.add(Box.createRigidArea(new Dimension(10, 1)));
        bottomBar.add(historyLabel);
        bottomBar.add(Box.createRigidArea(new Dimension(10, 1)));
        bottomBar.add(actionLabel);

        JPanel instead = getSearchComponent();

        //JScrollPane scroll = ViewUtil.getClearBorderlessScrollPane(instead);
        //scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.setFocusable(true);
        add(effectivenessPanel, BorderLayout.NORTH);
        add(instead, BorderLayout.CENTER);
        add(ViewUtil.centerHorizontally(bottomBar), BorderLayout.SOUTH);
    }

    /**
     * Load the given list of new filters into all the query panels.
     */
    public void loadFilters(FilterState... states) {
        try {
            for (QueryPanel qp : queryPanels) {
                for (FilterState state : states) {
                    qp.loadFilterView(state);
                }
            }
        } catch (Exception ex) {
            ClientMiscUtils.reportError("Unable to apply requested filters: %s", ex);
        }
    }

    private JPanel getSearchComponent() {

        final SearchConditionGroupItem entireQueryModel = new SearchConditionGroupItem(null);
        queryViewController = new QueryViewController(entireQueryModel,MedSavantConditionViewGenerator.getInstance());

        JPanel p = new JPanel();
        p.setFocusable(true);
        p.setOpaque(false);
        p.setLayout(new BorderLayout());
        p.add(queryViewController,BorderLayout.CENTER);
        return p;
    }

    public QueryViewController getQueryViewController() {
        return queryViewController;
    }
}
