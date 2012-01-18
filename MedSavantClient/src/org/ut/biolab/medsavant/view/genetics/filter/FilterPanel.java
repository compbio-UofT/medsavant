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

/*
 * FilterPanel.java
 *
 * Created on 19-Oct-2011, 4:16:02 PM
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.db.util.shared.ExtensionFileFilter;
import org.ut.biolab.medsavant.db.util.shared.ExtensionsFileFilter;
import org.ut.biolab.medsavant.settings.DirectorySettings;
import org.ut.biolab.medsavant.util.MiscUtils;
import org.ut.biolab.medsavant.view.component.ProgressPanel;
import org.ut.biolab.medsavant.view.genetics.filter.FilterState.FilterType;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Andrew
 */
public class FilterPanel extends javax.swing.JPanel {

    private javax.swing.JPanel container;
    //private List<FilterPanelSub> subs = new ArrayList<FilterPanelSub>();
    private List<FilterPanelSub> subs2 = new ArrayList<FilterPanelSub>();
    private int subNum = 1;
    private JPanel filterContainer;
    private JComboBox filterList;
    private ActionListener comboBoxListener;
    private boolean addingItems = false;

    /** Creates new form FilterPanel */
    public FilterPanel() {
        initComponents();
        createNewSubPanel();
    }

    private JPanel createNewOrButton() {

        final JLabel addLabel = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.ADD));
        addLabel.setToolTipText("Add exlusive set of filters");
        addLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                createNewSubPanel();
            }
        });


        JPanel tmp1 = ViewUtil.getClearPanel();//ViewUtil.getSecondaryBannerPanel();//ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(tmp1);
        /*tmp1.setBorder(BorderFactory.createCompoundBorder(
                ViewUtil.getTinyLineBorder(),
                ViewUtil.getMediumBorder()));
         *
         */
        tmp1.add(addLabel);
        tmp1.setMaximumSize(new Dimension(9999, 40));
        tmp1.add(Box.createHorizontalStrut(5));

        JLabel addLabelText = new JLabel("Add filter set");
        tmp1.add(addLabelText);
        tmp1.add(Box.createHorizontalGlue());

        return tmp1;
    }

    public FilterPanelSub createNewSubPanel() {

        /*
        FilterPanelSub newSub = new FilterPanelSub(this, subNum++);
        subs.add(newSub);
         */

        FilterPanelSub cp = new FilterPanelSub(this, subNum++);
        filterContainer.add(cp);
        subs2.add(cp);

        refreshSubPanels();

        return cp;
    }

    public void refreshSubPanels() {
        filterContainer.removeAll();

        //check for removed items
        for (int i = subs2.size() - 1; i >= 0; i--) {
            if (subs2.get(i).isRemoved()) {
                subs2.remove(i);
            }
        }

        //refresh panel
        for (int i = 0; i < subs2.size(); i++) {
            filterContainer.add(subs2.get(i));
            filterContainer.add(Box.createVerticalStrut(5));
        }

        //filterContainer.add(createNewOrButton());
        filterContainer.add(Box.createVerticalGlue());

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
        refreshSubPanels();
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
        for (int i = 0; i < states.size(); i++) {
            FilterPanelSub fps = createNewSubPanel();
            List<FilterState> filters = states.get(i);
            for (FilterState state : filters) {
                try {
                    FilterUtils.loadFilterView(state, fps);
                } catch (SQLException ex) {
                    MiscUtils.checkSQLException(ex);
                    Logger.getLogger(FilterPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        refreshSubPanels();

    }

    private void initComponents() {

        container = this;
        container.setLayout(new BorderLayout());
        container.setBackground(ViewUtil.getMenuColor());

        JPanel filterAndToolbarContainer = ViewUtil.getClearPanel();
        filterAndToolbarContainer.setLayout(new BorderLayout());
        filterAndToolbarContainer.setBorder(ViewUtil.getMediumBorder());


        filterContainer = ViewUtil.getClearPanel();

        container.add(filterAndToolbarContainer,BorderLayout.CENTER);
        filterAndToolbarContainer.add(filterContainer, BorderLayout.CENTER);
        filterAndToolbarContainer.add(createNewOrButton(),BorderLayout.SOUTH);


        filterContainer.setLayout(new BoxLayout(filterContainer, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(filterContainer);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        container.add(scroll, BorderLayout.CENTER);

        //container.add(new ProgressPanel());

        FilterEffectivenessPanel hp = new FilterEffectivenessPanel();
        container.add(hp, BorderLayout.SOUTH);


        JButton saveButton = new JButton();
        saveButton.setIcon(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.SAVE));
        saveButton.setToolTipText("Save filter set");
        saveButton.putClientProperty( "JButton.buttonType", "segmentedRoundRect" );
        saveButton.putClientProperty( "JButton.segmentPosition", "only" );
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    saveFilters();
                } catch (IOException ex) {
                    Logger.getLogger(FilterPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        JButton  deleteButton = new JButton();
        deleteButton.setIcon(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.TRASH));
        deleteButton.setToolTipText("Remove this set from list");
        deleteButton.putClientProperty( "JButton.buttonType", "segmentedRoundRect" );
        deleteButton.putClientProperty( "JButton.segmentPosition", "only" );
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

        //JLabel l = new JLabel("Filter variants by:");

        JPanel topContainer = new JPanel();
        //topContainer.setOpaque(false);
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.X_AXIS));
        //topContainer.add(l);


        //topContainer.add(Box.createHorizontalGlue());

        topContainer.add(filterList);
        topContainer.add(Box.createHorizontalGlue());
        topContainer.add(saveButton);
        topContainer.add(deleteButton);

        container.add(topContainer, BorderLayout.NORTH);
    }

    private void updateFilterList() {
        File filterDir = DirectorySettings.getFiltersDirectory();

        addingItems = true;
        filterList.removeAllItems();
        this.filterList.addItem("Saved Filters");
        for (String fn : filterDir.list()) {
            this.filterList.addItem(fn);
        }
        addingItems = false;
    }
}
