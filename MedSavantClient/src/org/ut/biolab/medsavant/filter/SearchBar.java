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
package org.ut.biolab.medsavant.filter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 * Panel which contains a collection of <code>QueryPanel</code>s, each of which contains a range of <code>Filter</code>s.
 *
 * @author Andrew
 */
public class SearchBar extends JPanel {

    private FilterController controller;
    List<QueryPanel> queryPanels = new ArrayList<QueryPanel>();
    private int nextQueryID = 1;
    private JPanel queryPanelContainer;
    private FilterHistoryPanel historyPanel;
    private JToggleButton historyButton;
    private SavedFiltersPanel savedFiltersPanel;
    private JToggleButton savedFiltersButton;

    /**
     * Creates search bar to contains our query panels.
     */
    public SearchBar() {
        controller = FilterController.getInstance();
        initComponents();
        createNewQueryPanel();
    }

    /**
     * Add a new query panel to the search area.  Currently, the UI provides no way to add additional query panels, so there can only
     * be one.
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

    private void loadFiltersFromFile(File file) throws Exception {

        if (file == null) {
            return;
        }

        //warn of overwrite
        if (controller.hasFiltersApplied() && DialogUtils.askYesNo("Confirm Load", "<html>Loading filters clears all existing filters. <br>Are you sure you want to continue?</html>") == JOptionPane.NO_OPTION) {
            return;
        }

        //read
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);

        doc.getDocumentElement().normalize();

        List<List<FilterState>> states = new ArrayList<List<FilterState>>();
        NodeList nodes = doc.getElementsByTagName("set");
        for (int i = 0; i < nodes.getLength(); i++) {

            Element set = (Element) nodes.item(i);
            NodeList filters = set.getElementsByTagName("filter");

            List<FilterState> list = new ArrayList<FilterState>();

            for (int j = 0; j < filters.getLength(); j++) {

                Element filter = (Element) filters.item(j);

                String name = filter.getAttribute("name");
                String id = filter.getAttribute("id");
                Filter.Type type = Filter.Type.valueOf(filter.getAttribute("type"));

                NodeList params = filter.getElementsByTagName("param");
                Map<String, String> values = new HashMap<String, String>();
                for (int k = 0; k < params.getLength(); k++) {
                    Element e = (Element) params.item(k);
                    values.put(e.getAttribute("key"), e.getAttribute("value"));
                }

                list.add(new FilterState(type, name, id, values));
            }

            if (!list.isEmpty()) {
                states.add(list);
            }
        }

        controller.removeAllFilters();
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
        setLayout(new GridBagLayout());

        FilterEffectivenessPanel effectivenessPanel = new FilterEffectivenessPanel();

        queryPanelContainer = ViewUtil.getClearPanel();
        queryPanelContainer.setLayout(new BoxLayout(queryPanelContainer, BoxLayout.Y_AXIS));

        JScrollPane scroll = ViewUtil.getClearBorderlessScrollPane(queryPanelContainer);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        historyButton = ViewUtil.getSoftToggleButton("Search History");
        historyButton.setSelected(false);
        ViewUtil.makeSmall(historyButton);

        historyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                boolean vis = historyButton.isSelected();
                historyPanel.setVisible(vis);
                if (vis) {
                    savedFiltersButton.setSelected(false);
                    savedFiltersPanel.setVisible(false);
                }
            }
        });

        savedFiltersButton = ViewUtil.getSoftToggleButton("Saved Filter Sets");
        savedFiltersButton.setSelected(false);
        ViewUtil.makeSmall(savedFiltersButton);

        savedFiltersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                boolean vis = savedFiltersButton.isSelected();
                savedFiltersPanel.setVisible(vis);
                if (vis) {
                    historyButton.setSelected(false);
                    historyPanel.setVisible(false);
                }
            }
        });

        historyPanel = new FilterHistoryPanel();
        historyPanel.setVisible(false);

        savedFiltersPanel = new SavedFiltersPanel();
        savedFiltersPanel.setVisible(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(effectivenessPanel, gbc);

        gbc.weighty = 1.0;
        gbc.gridy++;
        add(scroll, gbc);

        gbc.weighty = 0.0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        add(historyButton, gbc);
        gbc.gridx++;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        add(savedFiltersButton, gbc);
        
        gbc.gridx = 0;
        gbc.gridy++;
        add(historyPanel, gbc);
        add(savedFiltersPanel, gbc);
    }

    /**
     * Load the given list of new filters into all the query panels.
     */
    public void loadFilters(FilterState... states) {
        try {
            for (QueryPanel qp: queryPanels) {
                for (FilterState state: states) {
                    qp.loadFilterView(state);
                }
            }
        } catch (Exception ex) {
            ClientMiscUtils.reportError("Unable to apply requested filters: %s", ex);
        }
    }
}
