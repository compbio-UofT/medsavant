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

import java.awt.Dimension;
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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.db.util.shared.ExtensionFileFilter;
import org.ut.biolab.medsavant.db.util.shared.ExtensionsFileFilter;
import org.ut.biolab.medsavant.util.MiscUtils;
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

    //private List<FilterPanelSub> subs = new ArrayList<FilterPanelSub>();
    private List<FilterPanelSub> subs2 = new ArrayList<FilterPanelSub>();
    private int subNum = 1;

    /** Creates new form FilterPanel */
    public FilterPanel() {
        initComponents();

        container.setBorder(BorderFactory.createLineBorder(container.getBackground(), 10));
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        createNewSubPanel();
    }

    private JPanel createNewOrButton(){

        final JLabel addLabel = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.ADD));
        addLabel.setToolTipText("Add filter set");
        addLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                createNewSubPanel();
            }
        });


        JPanel tmp1 = ViewUtil.getSecondaryBannerPanel();//ViewUtil.getClearPanel();
        tmp1.setBorder(BorderFactory.createCompoundBorder(
                          ViewUtil.getTinyLineBorder(),
                          ViewUtil.getMediumBorder()));
        tmp1.add(addLabel);
        tmp1.setMaximumSize(new Dimension(9999,40));
        tmp1.add(Box.createHorizontalStrut(5));
        JLabel addLabelText = new JLabel("Add filter set");
        tmp1.add(addLabelText);
        tmp1.add(Box.createHorizontalGlue());

        return tmp1;
    }

    public FilterPanelSub createNewSubPanel(){

        /*
        FilterPanelSub newSub = new FilterPanelSub(this, subNum++);
        subs.add(newSub);
        */

        FilterPanelSub cp = new FilterPanelSub(this, subNum++);
        container.add(cp);
        subs2.add(cp);

        refreshSubPanels();

        return cp;
    }

    public void refreshSubPanels(){
        container.removeAll();

        JButton saveButton = new JButton("Save Filters");
        saveButton.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                try {
                    saveFilters();
                } catch (IOException ex) {
                    Logger.getLogger(FilterPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        JButton loadButton = new JButton("Load Filters");
        loadButton.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                try {
                    loadFilters();
                } catch (ParserConfigurationException ex) {
                    Logger.getLogger(FilterPanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SAXException ex) {
                    Logger.getLogger(FilterPanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(FilterPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        JLabel l = new JLabel("Filter variants by:");

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.X_AXIS));
        topContainer.add(l);
        topContainer.add(Box.createHorizontalGlue());
        topContainer.add(saveButton);
        topContainer.add(loadButton);
        container.add(topContainer);

        container.add(Box.createVerticalStrut(5));
        //check for removed items
        for(int i = subs2.size()-1; i >= 0; i--){
            if(subs2.get(i).isRemoved()){
                subs2.remove(i);
            }
        }

        //refresh panel
        for(int i = 0; i < subs2.size(); i++){
            container.add(subs2.get(i));
            container.add(Box.createVerticalStrut(5));
        }
        container.add(createNewOrButton());
        container.add(Box.createVerticalGlue());

        this.updateUI();
    }

    public List<FilterPanelSub> getFilterPanelSubs(){
        return this.subs2;
    }

    public void clearAll(){
        this.subs2.clear();
        subNum = 1;
        refreshSubPanels();
    }

    private void saveFilters() throws IOException{

        //choose save file
        File file = DialogUtils.chooseFileForSave("Save Filters", "saved_filters.xml", ExtensionFileFilter.createFilters(new String[]{"xml"}), null);
        if(file == null) return;

        //write
        BufferedWriter out = new BufferedWriter(new FileWriter(file, false));

        out.write("<filters>\n");
        for(FilterPanelSub sub : subs2){
            out.write("\t<set>\n");
            for(FilterPanelSubItem item : sub.getSubItems()){
               out.write(item.getFilterView().saveState().generateXML() + "\n");
            }
            out.write("\t</set>\n");
        }
        out.write("</filters>");


        out.close();

    }

    private void loadFilters() throws ParserConfigurationException, SAXException, IOException{

        //warn of overwrite
        if(FilterController.hasFiltersApplied() && DialogUtils.askYesNo("Confirm Load", "<html>Loading filters clears all existing filters. <br>Are you sure you want to continue?</html>") == JOptionPane.NO_OPTION) return;

        //choose open file
        File file = DialogUtils.chooseFileForOpen("Load Filters", new ExtensionsFileFilter("xml"), null);
        if(file == null) return;

        //read
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);

        doc.getDocumentElement().normalize();

        List<List<FilterState>> states = new ArrayList<List<FilterState>>();
        NodeList nodes = doc.getElementsByTagName("set");
        for(int i = 0; i < nodes.getLength(); i++){

            Element set = (Element) nodes.item(i);
            NodeList filters = set.getElementsByTagName("filter");

            List<FilterState> list = new ArrayList<FilterState>();

            for(int j = 0; j < filters.getLength(); j++){

                Element filter = (Element) filters.item(j);

                String name = filter.getAttribute("name");
                String id = filter.getAttribute("id");
                FilterType type = FilterType.valueOf(filter.getAttribute("type"));

                NodeList params = filter.getElementsByTagName("param");
                Map<String, String> values = new HashMap<String, String>();
                for(int k = 0; k < params.getLength(); k++){
                    Element e = (Element)params.item(k);
                    values.put(e.getAttribute("key"), e.getAttribute("value"));
                }

                list.add(new FilterState(type, name, id, values));
            }

            if(!list.isEmpty()){
                states.add(list);
            }
        }

        //generate
        FilterUtils.clearFilterSets();
        for(int i = 0; i < states.size(); i++){
            FilterPanelSub fps = createNewSubPanel();
            List<FilterState> filters = states.get(i);
            for(FilterState state : filters){
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        container = new javax.swing.JPanel();

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jPanel1.setBackground(new java.awt.Color(255, 204, 204));
        jPanel1.setLayout(new java.awt.GridLayout(1, 3));

        javax.swing.GroupLayout containerLayout = new javax.swing.GroupLayout(container);
        container.setLayout(containerLayout);
        containerLayout.setHorizontalGroup(
            containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 657, Short.MAX_VALUE)
        );
        containerLayout.setVerticalGroup(
            containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 509, Short.MAX_VALUE)
        );

        jPanel1.add(container);

        jScrollPane1.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 661, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 513, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel container;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
