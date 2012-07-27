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
package org.ut.biolab.medsavant.view.genetics.filter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.FilterController.FilterAction;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.settings.DirectorySettings;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author Andrew
 */
public class SearchBar extends JPanel {

    private List<SearchConditionsPanel> subs2 = new ArrayList<SearchConditionsPanel>();
    private int subNum = 1;
    private JPanel filterContainerContent;
    private JComboBox filterList;
    private boolean addingItems = false;
    private FilterHistoryPanel historyPanel;
    private JButton deleteButton;

    /** Creates new form FilterPanel */
    public SearchBar() {
        initComponents();
        createNewSubPanel();
    }

    public final SearchConditionsPanel createNewSubPanel() {

        SearchConditionsPanel cp = new SearchConditionsPanel(subNum++);
        filterContainerContent.add(cp);
        subs2.add(cp);

        refreshSubPanels();

        return cp;
    }

    public void refreshSubPanels() {
        filterContainerContent.removeAll();

        //refresh panel
        for (int i = 0; i < subs2.size(); i++) {
            filterContainerContent.add(subs2.get(i));
            filterContainerContent.add(Box.createVerticalStrut(5));
        }

        //filterContainer.add(createNewOrButton());
        filterContainerContent.add(Box.createVerticalGlue());
    }

    private void checkFilterListChanged() {
        if (!addingItems) {
            int index = filterList.getSelectedIndex();

            if (index != 0) {
                try {
                    loadFiltersFromFile(new File(DirectorySettings.getFiltersDirectory(), (String)filterList.getItemAt(index)));
                } catch (Exception ex) {
                    ClientMiscUtils.reportError("Error loading filters: %s", ex);
                }
            }
        }
    }

    public List<SearchConditionsPanel> getFilterPanelSubs() {
        return subs2;
    }

    public void clearAll() {
        subs2.clear();
        subNum = 1;
        createNewSubPanel();
    }

    private void removeCurrentSavedFilterFile() {
        int index = filterList.getSelectedIndex();
        if (index != 0) {
            File f = new File(DirectorySettings.getFiltersDirectory(), (String)filterList.getSelectedItem());
            if (f.exists()) {
                f.delete();
                updateFilterList();
            }
        }
    }

    private void saveFilters() throws Exception {

        String name = DialogUtils.displayInputMessage("Save filters", "Enter a name for these filters", "filters");
        File file = new File(DirectorySettings.getFiltersDirectory(), name + ".xml");

        if (file == null) {
            return;
        }
        if (name == null || name.isEmpty()) {
            return;
        }

        while (file.exists()) {

            int response;
            while (true) {
                response = DialogUtils.askYesNoCancel("Filters exist", "Filters with that name already exist. Overwrite?");
                if (response == DialogUtils.CANCEL) {
                    return;
                }
                if (response == DialogUtils.NO) {
                    saveFilters();
                    return;
                }

                file.delete();
                break;
            }
        }

        //write
        BufferedWriter out = new BufferedWriter(new FileWriter(file, false));

        out.write("<filters>\n");
        for (SearchConditionsPanel sub : subs2) {
            out.write("\t<set>\n");
            for (FilterHolder item : sub.getFilterHolders()) {
                out.write(item.getFilterView().saveState().generateXML() + "\n");
            }
            out.write("\t</set>\n");
        }
        out.write("</filters>");


        out.close();

        updateFilterList();

    }

    private void loadFiltersFromFile(File file) throws ParserConfigurationException, SAXException, IOException, SQLException {

        //warn of overwrite
        if (FilterController.hasFiltersApplied() && DialogUtils.askYesNo("Confirm Load", "<html>Loading filters clears all existing filters. <br>Are you sure you want to continue?</html>") == JOptionPane.NO_OPTION) {
            return;
        }

        if (file == null) {
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

        //generate
        FilterUtils.clearFilterSets();
        FilterController.setAutoCommit(false);
        for (int i = 0; i < states.size(); i++) {
            SearchConditionsPanel fps = createNewSubPanel();
            List<FilterState> filters = states.get(i);
            for (FilterState state : filters) {
                FilterUtils.loadFilterView(state, fps);
            }
        }
        FilterController.commit(file.getName(), FilterAction.REPLACED);
        FilterController.setAutoCommit(true);
        refreshSubPanels();

    }

    private void initComponents() {
        setOpaque(false);
        setLayout(new GridBagLayout());

        JPanel filterAndToolbarContainer = ViewUtil.getClearPanel();
        filterAndToolbarContainer.setLayout(new BorderLayout());
        //filterAndToolbarContainer.setBorder(ViewUtil.getMediumBorder());

        filterContainerContent = ViewUtil.getClearPanel();
        filterContainerContent.setLayout(new BoxLayout(filterContainerContent, BoxLayout.Y_AXIS));

        JPanel topContainer = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(topContainer);


        JScrollPane scroll = ViewUtil.getClearBorderlessScrollPane(filterContainerContent);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel addFilterPanel = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(addFilterPanel);

        FilterEffectivenessPanel hp = new FilterEffectivenessPanel();

        JButton saveButton = new JButton("Save");
        ViewUtil.makeSmall(saveButton);
        //saveButton.setFontSize(11);
        //saveButton.setContentAreaFilled(false);
        saveButton.setToolTipText("Save filter set");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    saveFilters();
                } catch (Exception ex) {
                    ClientMiscUtils.reportError("Error saving filter set: %s", ex);
                }
            }
        });

        deleteButton = new JButton("Forget");
        ViewUtil.makeMini(deleteButton);
        //deleteButton.setFontSize(11);
        deleteButton.setToolTipText("Forget this search");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeCurrentSavedFilterFile();
            }
        });


        filterList = new JComboBox();
        filterList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                checkFilterListChanged();
            }
        });
        updateFilterList();



        JToolBar saveContainer = new JToolBar();// ViewUtil.getClearPanel();
        saveContainer.setBackground(ViewUtil.getTertiaryMenuColor());
        saveContainer.setFloatable(false);
        //ViewUtil.applyMenuStyleInset(saveContainer);


        saveContainer.setLayout(new BoxLayout(saveContainer, BoxLayout.X_AXIS));
        saveContainer.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        filterList.putClientProperty( "JComponent.sizeVariant", "small" );

        saveContainer.add(filterList);
        saveContainer.add(Box.createHorizontalGlue());
        saveContainer.add(saveButton);
        saveContainer.add(Box.createHorizontalStrut(3));
        saveContainer.add(deleteButton);

        //topContainer.add(saveContainer);
        topContainer.add(hp);


        JPanel bottomContainer = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(bottomContainer);

        historyPanel = new FilterHistoryPanel();

        final JToggleButton showHistoryButton = ViewUtil.getSoftToggleButton("Search History");
        showHistoryButton.setSelected(false);
        ViewUtil.makeSmall(showHistoryButton);
        //showHistoryButton.setFont(new Font("Arial", Font.BOLD, 12));
        //showHistoryButton.setForeground(Color.white);

        showHistoryButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                historyPanel.setVisible(showHistoryButton.isSelected());
            }

        });

        JPanel historyButtonPanel = ViewUtil.alignLeft(showHistoryButton);
        historyButtonPanel.setBorder(ViewUtil.getMediumBorder());

        historyPanel.setVisible(showHistoryButton.isSelected());
        bottomContainer.add(historyButtonPanel);
        bottomContainer.add(historyPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(topContainer, gbc);

        gbc.weighty = 1.0;
        add(scroll, gbc);

        gbc.weighty = 0.0;
        add(bottomContainer, gbc);
    }

    private void updateFilterList() {
        File filterDir = DirectorySettings.getFiltersDirectory();

        addingItems = true;
        filterList.removeAllItems();

        if (filterDir.list().length == 0) {
            filterList.addItem("No saved searches");
            filterList.setEnabled(false);
            deleteButton.setEnabled(false);
        } else {
            filterList.addItem("Load a saved search");
            for (String fn: filterDir.list()) {
                filterList.addItem(fn);
            }
            filterList.setEnabled(true);
            deleteButton.setEnabled(true);
        }
        addingItems = false;
    }
}
