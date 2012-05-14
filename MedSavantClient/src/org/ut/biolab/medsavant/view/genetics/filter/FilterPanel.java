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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
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
import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.FilterController.FilterAction;
import org.ut.biolab.medsavant.settings.DirectorySettings;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.view.component.HoverButton;
import org.ut.biolab.medsavant.view.genetics.filter.FilterState.FilterType;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author Andrew
 */
public class FilterPanel extends JPanel {

    private static final Log LOG = LogFactory.getLog(FilterPanel.class);

    private JPanel container;
    private List<FilterPanelSub> subs2 = new ArrayList<FilterPanelSub>();
    private int subNum = 1;
    private JPanel filterContainerContent;
    private JComboBox filterList;
    private boolean addingItems = false;
    private FilterHistoryPanel historyPanel;
    private HoverButton deleteButton;

    /** Creates new form FilterPanel */
    public FilterPanel() {
        initComponents();
        createNewSubPanel();
    }

    public final FilterPanelSub createNewSubPanel() {

        FilterPanelSub cp = new FilterPanelSub(this, subNum++);
        filterContainerContent.add(cp);
        subs2.add(cp);

        refreshSubPanels();

        return cp;
    }

    public void refreshSubPanels() {
        filterContainerContent.removeAll();

        //check for removed items
        for (int i = subs2.size() - 1; i >= 0; i--) {
            if (subs2.get(i).isRemoved()) {
                subs2.remove(i);
            }
        }

        //refresh panel
        for (int i = 0; i < subs2.size(); i++) {
            filterContainerContent.add(subs2.get(i));
            filterContainerContent.add(Box.createVerticalStrut(5));
        }

        //filterContainer.add(createNewOrButton());
        filterContainerContent.add(Box.createVerticalGlue());

        this.updateUI();
    }

    private void checkFilterListChanged() {
        if (!addingItems) {
            int index = filterList.getSelectedIndex();

            if (index != 0) {
                try {
                    loadFiltersFromFile(new File(DirectorySettings.getFiltersDirectory(), (String) filterList.getItemAt(index)));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    DialogUtils.displayError("Problem loading filters");
                }
            }
        }
    }

    public List<FilterPanelSub> getFilterPanelSubs() {
        return this.subs2;
    }

    public void clearAll() {
        this.subs2.clear();
        subNum = 1;
        createNewSubPanel();
    }

    private void removeCurrentSavedFilterFile() {
        int index = this.filterList.getSelectedIndex();
        if (index != 0) {
            File f = new File(DirectorySettings.getFiltersDirectory(),(String) this.filterList.getSelectedItem());
            if (f.exists()) {
                f.delete();
                this.updateFilterList();
            }
        }
    }

    private void saveFilters() throws IOException {

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
        for (FilterPanelSub sub : subs2) {
            out.write("\t<set>\n");
            for (FilterPanelSubItem item : sub.getSubItems()) {
                out.write(item.getFilterView().saveState().generateXML() + "\n");
            }
            out.write("\t</set>\n");
        }
        out.write("</filters>");


        out.close();

        updateFilterList();

    }

    private void loadFiltersFromFile(File file) throws ParserConfigurationException, SAXException, IOException {

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
                FilterType type = FilterType.valueOf(filter.getAttribute("type"));

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
            FilterPanelSub fps = createNewSubPanel();
            List<FilterState> filters = states.get(i);
            for (FilterState state : filters) {
                try {
                    FilterUtils.loadFilterView(state, fps);
                } catch (SQLException ex) {
                    ClientMiscUtils.checkSQLException(ex);
                    LOG.error("Error loading filters.", ex);
                }
            }
        }
        FilterController.commit(file.getName(), FilterAction.REPLACED);
        FilterController.setAutoCommit(true);
        refreshSubPanels();

    }

    private void initComponents() {

        container = this;
        this.setOpaque(false);

        container.setLayout(new BorderLayout());

        JPanel filterAndToolbarContainer = ViewUtil.getClearPanel();
        filterAndToolbarContainer.setLayout(new BorderLayout());
        //filterAndToolbarContainer.setBorder(ViewUtil.getMediumBorder());

        filterContainerContent = ViewUtil.getClearPanel();
        filterContainerContent.setLayout(new BoxLayout(filterContainerContent, BoxLayout.Y_AXIS));

         JPanel topContainer = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(topContainer);


        JScrollPane scroll = ViewUtil.getClearBorderlessJSP(filterContainerContent);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel addFilterPanel = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(addFilterPanel);
        final JLabel addLabel = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.ADD));
        addLabel.setToolTipText("Add new filter set");
        addLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                createNewSubPanel();
            }
        });


        container.add(scroll, BorderLayout.CENTER);

        FilterEffectivenessPanel hp = new FilterEffectivenessPanel();

        HoverButton saveButton = new HoverButton("Save");
        saveButton.setFontSize(11);

        saveButton.setContentAreaFilled(false);
        saveButton.setToolTipText("Save filter set");
        saveButton.putClientProperty( "JComponent.sizeVariant", "small" );
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    saveFilters();
                } catch (IOException ex) {
                    LOG.error("Error saving filters.", ex);
                }
            }
        });

        deleteButton = new HoverButton("Forget");
        deleteButton.setFontSize(11);
        deleteButton.setToolTipText("Forget this search");
        deleteButton.putClientProperty( "JComponent.sizeVariant", "small" );
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

        topContainer.add(saveContainer);
        topContainer.add(hp);

        container.add(topContainer, BorderLayout.NORTH);

        JPanel bottomContainer = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(bottomContainer);

        historyPanel = new FilterHistoryPanel();

        final JCheckBox showHistoryButton = new JCheckBox("Show query effectiveness");
        showHistoryButton.setSelected(false);
        showHistoryButton.setFont(new Font("Arial", Font.BOLD, 12));
        showHistoryButton.setForeground(Color.white);

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

        container.add(bottomContainer,BorderLayout.SOUTH);
    }

    private void updateFilterList() {
        File filterDir = DirectorySettings.getFiltersDirectory();

        addingItems = true;
        filterList.removeAllItems();

        if (filterDir.list().length == 0) {
            this.filterList.addItem("No saved searches");
            this.filterList.setEnabled(false);
            deleteButton.setEnabled(false);
        } else {
            this.filterList.addItem("Load a saved search");
            for (String fn : filterDir.list()) {
                this.filterList.addItem(fn);
            }
            this.filterList.setEnabled(true);
            deleteButton.setEnabled(true);
        }
        addingItems = false;
    }
}
