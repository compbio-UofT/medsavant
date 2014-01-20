/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.filter;

import com.explodingpixels.macwidgets.SourceListControlBar;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.variant.ExportVCFWizard;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.mfiume.query.medsavant.MedSavantConditionViewGenerator;
import org.ut.biolab.mfiume.query.QueryViewController;
import org.ut.biolab.mfiume.query.SearchConditionGroupItem;

/**
 * Panel which contains a collection of <code>QueryPanel</code>s, each of which
 * contains a range of <code>Filter</code>s.
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
    private QueryViewController queryViewController;
    private final FileNameExtensionFilter filenameFilter = new FileNameExtensionFilter("Saved Searches", SAVED_SEARCH_EXTENSION);

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

        this.setBackground(ViewUtil.getSecondaryMenuColor());
        this.setBorder(ViewUtil.getSideLineBorder());

        setLayout(new BorderLayout());

        FilterEffectivenessPanel effectivenessPanel = new FilterEffectivenessPanel(new Color(20, 20, 20));

        queryPanelContainer = ViewUtil.getClearPanel();
        queryPanelContainer.setLayout(new BoxLayout(queryPanelContainer, BoxLayout.Y_AXIS));

        historyPanel = new FilterHistoryPanel();

        final Dimension dialogDimensions = new Dimension(400, 400);

        historyPanel.setMinimumSize(dialogDimensions);
        historyPanel.setPreferredSize(dialogDimensions);

        // history dialog
        final JDialog dHistory = new JDialog(MedSavantFrame.getInstance(), true);
        dHistory.setTitle("Search History");
        dHistory.add(historyPanel);
        dHistory.pack();
        dHistory.setLocationRelativeTo(MedSavantFrame.getInstance());
        dHistory.setResizable(false);

        // save dialog
        final JFileChooser fileChooser = new JFileChooser();

        final JDialog dSave = new JDialog(MedSavantFrame.getInstance(), true);
        dSave.setTitle("Load Search Conditions");
        dSave.setResizable(false);

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

        final SourceListControlBar controlbar = new SourceListControlBar();

        // add clear button
        controlbar.createAndAddButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.CLEAR_ON_TOOLBAR), new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (DialogUtils.askYesNo("Clear current search", "Clear current search?") == JOptionPane.YES_OPTION) {
                    getQueryViewController().clearSearch();
                }
            }
        });

        // add save button
        controlbar.createAndAddButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.SAVE_ON_TOOLBAR), new ActionListener() {

            private void saveFile(File file) {
                getQueryViewController().saveConditions(file);
                JOptionPane.showMessageDialog(MedSavantFrame.getInstance(), "Search conditions saved to " + file.getPath());
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser.setFileFilter(filenameFilter);

                if (fileChooser.showSaveDialog(MedSavantFrame.getInstance()) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    if (!file.getPath().toLowerCase().endsWith("." + SAVED_SEARCH_EXTENSION)) {
                        file = new File(file.getPath() + "." + SAVED_SEARCH_EXTENSION);
                    }
                    if (file.exists()) {
                        int r = JOptionPane.showConfirmDialog(fileChooser, "The file " + file.getPath() + " already exists.  Overwrite?", "Warning", JOptionPane.YES_NO_OPTION);
                        if (r == JOptionPane.YES_OPTION) {
                            saveFile(file);
                        }
                    } else {
                        saveFile(file);
                    }
                }
            }
        });

        // add load button
        controlbar.createAndAddButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.LOAD_ON_TOOLBAR), new ActionListener() {

            SavedFiltersPanel savedFiltersPanel;

            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser.setFileFilter(filenameFilter);

                if (fileChooser.showOpenDialog(MedSavantFrame.getInstance()) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();

                    if (file.exists()) {
                        getQueryViewController().loadConditions(file);
                    } else {
                        DialogUtils.displayError("File " + file.getPath() + " does not exist!");
                    }
                }
            }

        });

        // add history button
        controlbar.createAndAddButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.HISTORY_ON_TOOLBAR), new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                dHistory.setVisible(true);
            }

        });

        // add action button
        controlbar.createAndAddButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.ACTION_ON_TOOLBAR), new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                actionPopup.show(controlbar.getComponent(), 0, -(actionPopup.getHeight()));
            }

        });

        JPanel instead = getSearchComponent();

        //JScrollPane scroll = ViewUtil.getClearBorderlessScrollPane(instead);
        //scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.setFocusable(true);
        add(effectivenessPanel, BorderLayout.NORTH);
        add(instead, BorderLayout.CENTER);
        add(controlbar.getComponent(), BorderLayout.SOUTH);

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
        queryViewController = new QueryViewController(entireQueryModel, MedSavantConditionViewGenerator.getInstance());

        JPanel p = new JPanel();
        p.setFocusable(true);
        p.setOpaque(false);
        p.setLayout(new BorderLayout());
        p.add(queryViewController, BorderLayout.CENTER);
        return p;
    }

    public QueryViewController getQueryViewController() {
        return queryViewController;
    }
}
