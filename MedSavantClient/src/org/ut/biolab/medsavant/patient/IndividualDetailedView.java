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

package org.ut.biolab.medsavant.patient;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D.Float;
import java.io.File;
import java.io.FileWriter;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

import au.com.bytecode.opencsv.CSVWriter;
import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.CollapsiblePanes;
import pedviz.algorithms.Sugiyama;
import pedviz.graph.Graph;
import pedviz.graph.Node;
import pedviz.loader.CsvGraphLoader;
import pedviz.view.GraphView2D;
import pedviz.view.NodeEvent;
import pedviz.view.NodeListener;
import pedviz.view.NodeView;
import pedviz.view.rules.ShapeRule;
import pedviz.view.symbols.Symbol2D;
import pedviz.view.symbols.SymbolSexFemale;
import pedviz.view.symbols.SymbolSexMale;
import pedviz.view.symbols.SymbolSexUndesignated;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.format.BasicPatientColumns;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.model.Cohort;
import org.ut.biolab.medsavant.settings.DirectorySettings;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.view.dialog.ComboForm;
import org.ut.biolab.medsavant.view.list.DetailedView;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class IndividualDetailedView extends DetailedView implements PedigreeFields {

    private List<String> fieldNames;
    private DetailsWorker detailsWorker;
    private final JPanel infoContent;
    private final JPanel infoDetails;
    private final JPanel menu;
    private int[] patientIDs;
    boolean pedigreeShown = false;

    private final JPanel pedigreeDetails;
    private PedigreeWorker pedigreeWorker;
    private Node overNode = null;
    private NodeView overNodeView = null;
    private List<Integer> selectedNodes;
    private String familyID;
    private Graph graph;
    private final CollapsiblePane collapsiblePane;

    public IndividualDetailedView(String page) throws RemoteException, SQLException {
        super(page);

        fieldNames = MedSavantClient.PatientManager.getPatientFieldAliases(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectID());

        JPanel viewContainer = (JPanel) ViewUtil.clear(getContentPanel());
        viewContainer.setLayout(new BorderLayout());

        JPanel infoContainer = ViewUtil.getClearPanel();
        //infoContainer.setLayout(new BorderLayout());
        ViewUtil.applyVerticalBoxLayout(infoContainer);

        viewContainer.add(ViewUtil.getClearBorderlessScrollPane(infoContainer), BorderLayout.CENTER);

        CollapsiblePanes panes = new CollapsiblePanes();
        panes.setOpaque(false);
        infoContainer.add(panes);

        collapsiblePane = new CollapsiblePane();
        collapsiblePane.setStyle(CollapsiblePane.TREE_STYLE);
        collapsiblePane.setCollapsible(false);
        panes.add(collapsiblePane);

        panes.addExpansion();

        infoContent = new JPanel();
        infoContent.setLayout(new BorderLayout());
        collapsiblePane.setLayout(new BorderLayout());
        collapsiblePane.add(infoContent,BorderLayout.CENTER);

        infoDetails = ViewUtil.getClearPanel();
        pedigreeDetails = new JPanel();
        pedigreeDetails.setBackground(Color.white);

        ViewUtil.setBoxYLayout(infoContent);
        infoContent.add(infoDetails);

        menu = ViewUtil.getClearPanel();
        menu.add(addIndividualsButton());

        addBottomComponent(menu);
    }

    public synchronized void showPedigree(File pedigreeCSVFile) {

        pedigreeDetails.removeAll();
        pedigreeDetails.setLayout(new BorderLayout());

        //Step 1
	graph = new Graph();
	CsvGraphLoader loader = new CsvGraphLoader(pedigreeCSVFile.getAbsolutePath(), ",");
	loader.setSettings(HOSPITAL_ID, MOM, DAD);
	loader.load(graph);

        //numIndivids = graph.getSize();

	//Step 2
	Sugiyama s = new Sugiyama(graph);
	s.run();

	//Step 3
	GraphView2D view = new GraphView2D(s.getLayoutedGraph());

        view.setZoomEnabled(false);
        view.setMovingEnabled(false);

        view.getComponent().setMinimumSize(new Dimension(400,400));
        view.getComponent().setPreferredSize(new Dimension(400,400));

        //view.highlight(nodes);
        //view.setSelectionEnabled(true);


        view.addRule(new ShapeRule(GENDER, "1", new SymbolSexMale()));
	view.addRule(new ShapeRule(GENDER, "2", new SymbolSexFemale()));
        view.addRule(new ShapeRule(GENDER, "0", new SymbolSexUndesignated()));
	view.addRule(new ShapeRule(GENDER, "null", new SymbolSexUndesignated()));

        view.addRule(new PedigreeBasicRule(patientIDs));

        selectedNodes = new ArrayList<Integer>();
        for(Integer i : patientIDs) {
            selectedNodes.add(i);
        }

        view.centerGraph();

        //add ability to click
        view.addNodeListener(new NodeListener() {
            @Override
            public void onNodeEvent(NodeEvent ne) {
                if (ne.getType() == NodeEvent.MOUSE_ENTER) {
                    overNode = ne.getNode();
                    overNodeView = ne.getNodeView();
                } else if (ne.getType() == NodeEvent.MOUSE_LEAVE) {
                    overNode = null;
                    overNodeView = null;
                }
            }
        });

        view.getComponent().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (overNode != null) {
                    Integer patID = Integer.parseInt((String)overNode.getUserData(PATIENT_ID));
                    if (SwingUtilities.isRightMouseButton(e)) {
                        int[] patientIds = new int[selectedNodes.size()];
                        for(int i = 0; i < selectedNodes.size(); i++) {
                            patientIds[i] = selectedNodes.get(i);
                        }
                        JPopupMenu popup = org.ut.biolab.medsavant.patient.PatientUtils.createPopup(patientIds);
                        popup.show(e.getComponent(), e.getX(), e.getY());
                    } else if (SwingUtilities.isLeftMouseButton(e) && e.isControlDown()) {
                        if (!selectedNodes.contains(patID)) {
                            selectedNodes.add(patID);
                            overNodeView.setBorderColor(ViewUtil.detailSelectedBackground);
                        } else {
                            for(int i : patientIDs) {
                                if (i == patID) return;
                            }
                            selectedNodes.remove(patID);
                            overNodeView.setBorderColor(Color.black);
                        }
                    } else if (SwingUtilities.isLeftMouseButton(e)) {
                        if (patID != null && patID > 0) {
                            selectIndividualInList(patID);
                            //setSelectedItem(patientId, hospitalId);
                        }
                    }
                } else {
                    if (SwingUtilities.isRightMouseButton(e) && familyID != null) {
                        JPopupMenu popup = org.ut.biolab.medsavant.patient.PatientUtils.createPopup(familyID);
                        popup.show(e.getComponent(), e.getX(), e.getY());

                    }
                }
                pedigreeDetails.repaint();
            }
        });

        pedigreeDetails.add(view.getComponent(),BorderLayout.NORTH);

        pedigreeDetails.updateUI();
    }

    private void selectIndividualInList(int patID) {
        Object[][] list = parent.getList();
        for(int i = 0; i < list.length; i++) {
            Object[] o = list[i];
            if (o != null && o.length>=1 && (Integer)o[0] == patID) {
                parent.selectInterval(i, i);
                return;
            }
        }
    }

    public synchronized void setPatientInformation(Object[] result) {

        String[][] values = new String[fieldNames.size()][2];
        for (int i = 0; i < fieldNames.size(); i++) {
            values[i][0] = fieldNames.get(i);
            values[i][1] = "";
            if (result[i] != null) {
                values[i][1] = result[i].toString();

                //special case for gender
                if (values[i][0].equals(BasicPatientColumns.GENDER.getAlias())) {
                    String s;
                    if (result[i] instanceof Integer) {
                        s = ClientMiscUtils.genderToString((Integer)result[i]);
                    } else if (result[i] instanceof Long) {
                        s = ClientMiscUtils.genderToString(ClientMiscUtils.safeLongToInt((Long)result[i]));
                    } else {
                        s = ClientMiscUtils.GENDER_UNKNOWN;
                    }
                    values[i][1] = s;
                }

                //special case for affected
                if (values[i][0].equals(BasicPatientColumns.AFFECTED.getAlias())) {
                    String s;
                    if (result[i] instanceof Boolean) {
                        Boolean b = (Boolean) result[i];
                        s = b ? "Yes" : "No";
                    } else {
                        s = "Unknown";
                    }
                    values[i][1] = s;
                }
            }
        }

        infoDetails.removeAll();
        ViewUtil.setBoxYLayout(infoDetails);

        final KeyValuePairPanel kvp = new KeyValuePairPanel(1,true);

        for (int i = 0 ; i < values.length; i++) {
            kvp.addKey(values[i][0]);
            kvp.setValue(values[i][0], "<html>" + values[i][1].replace(",", "<br>") + "</html>");
        }

        final String pedigreeKey = "Pedigree";
        kvp.addKey(pedigreeKey);
        final JToggleButton b = ViewUtil.getTexturedToggleButton(pedigreeShown ? "Hide" : "Show");
        b.setSelected(pedigreeShown);

        b.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                kvp.toggleDetailVisibility(pedigreeKey);
                pedigreeShown = b.isSelected();
                if (b.isSelected()) {
                    b.setText("Hide");
                } else {
                    b.setText("Show");
                }
                pedigreeDetails.updateUI();
            }

        });

        kvp.setValue(pedigreeKey, b);
        kvp.setDetailComponent(pedigreeKey, pedigreeDetails);
        if (pedigreeShown) {
            kvp.toggleDetailVisibility(pedigreeKey);
        }

        /*
        final String cohortKey = "Cohort";
        kvp.addKey(cohortKey);
        final JButton cohortAddButton = new JButton("Assign individual to cohort");
        ViewUtil.makeSmall(cohortAddButton);
        cohortAddButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                if (patientIds != null && patientIds.length > 0) {
                    try {
                        List<Cohort> cohorts = MedSavantClient.CohortQueryUtilAdapter.getCohorts(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectID());
                        ComboForm form = new ComboForm(cohorts.toArray(), "Select Cohort", "Select which cohort to add to:");
                        Cohort selected = (Cohort) form.getSelectedValue();
                        if (selected == null) {
                            return;
                        }
                        MedSavantClient.CohortQueryUtilAdapter.addPatientsToCohort(LoginController.sessionId, patientIds, selected.getId());
                    } catch (Exception x) {
                        LOG.error("Error fetching cohorts.", x);
                    }
                    parent.refresh();
                }
            }

        });

        kvp.setAdditionalColumn(cohortKey, 0, cohortAddButton);
        *
        */

        infoDetails.add(kvp);

        infoDetails.updateUI();
    }

    @Override
    public void setSelectedItem(Object[] item) {
        int patientId = (Integer) item[0];
        String hospitalId = (String) item[2];
        setSelectedItem(patientId, hospitalId);
    }

    public void setSelectedItem(int patientId, String hospitalId) {

        collapsiblePane.setTitle(hospitalId);

        patientIDs = new int[1];
        patientIDs[0] = patientId;

        infoDetails.removeAll();
        infoDetails.updateUI();

        if (pedigreeWorker != null) {
            pedigreeWorker.cancel(true);
        }

        pedigreeWorker = new PedigreeWorker(patientId);
        pedigreeWorker.execute();

        if (detailsWorker != null) {
            detailsWorker.cancel(true);
        }
        detailsWorker = new DetailsWorker(patientId);
        detailsWorker.execute();

        //if (menu != null) menu.setVisible(true);
    }

    @Override
    public void setMultipleSelections(List<Object[]> items) {
        patientIDs = new int[items.size()];
        for(int i = 0; i < items.size(); i++) {
            patientIDs[i] = (Integer) items.get(i)[0];
        }
        if (items.isEmpty()) {
            collapsiblePane.setTitle("");
        } else {
            collapsiblePane.setTitle("Multiple individuals (" + items.size() + ")");
        }
        infoDetails.removeAll();
        infoDetails.updateUI();
        pedigreeDetails.removeAll();
        pedigreeDetails.updateUI();
    }


    @Override
    public JPopupMenu createPopup() {
        if (patientIDs != null && patientIDs.length > 0) {
            return PatientUtils.createPopup(patientIDs);
        }
        return null;
    }

    private JButton addIndividualsButton() {
        JButton button = new JButton("Assign individual(s) to cohort");
        button.setBackground(ViewUtil.getDetailsBackgroundColor());
        button.setOpaque(false);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (patientIDs != null && patientIDs.length > 0) {
                    try {
                        Cohort[] cohorts = MedSavantClient.CohortManager.getCohorts(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectID());
                        ComboForm form = new ComboForm(cohorts, "Select Cohort", "Select which cohort to add to:");
                        Cohort selected = (Cohort)form.getSelectedValue();
                        if (selected == null) {
                            return;
                        }
                        MedSavantClient.CohortManager.addPatientsToCohort(LoginController.sessionId, patientIDs, selected.getId());
                    } catch (Exception ex) {
                        ClientMiscUtils.reportError("Error adding individuals to cohort: %s", ex);
                    }
                    parent.refresh();
                }
            }
        });
        return button;
    }

    private class DetailsWorker extends MedSavantWorker<Object[]> {
        private final int patientID;

        private DetailsWorker(int patID) {
            super(getPageName());
            patientID = patID;
        }

        @Override
        protected Object[] doInBackground() throws RemoteException, SQLException {
            return MedSavantClient.PatientManager.getPatientRecord(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectID(), patientID);
        }

        @Override
        protected void showProgress(double ignored) {
        }

        @Override
        protected void showSuccess(Object[] result) {
            setPatientInformation(result);
        }
    }


    private class PedigreeWorker extends MedSavantWorker<File> {
        private final int patientID;

        public PedigreeWorker(int patID) {
            super(getPageName());
            this.patientID = patID;
        }

        @Override
        protected File doInBackground() throws Exception {

            List<Object[]> results = MedSavantClient.PatientManager.getFamilyOfPatient(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectID(), patientID);


            familyID = MedSavantClient.PatientManager.getFamilyIDOfPatient(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectID(), patientID);

            File outfile = new File(DirectorySettings.getTmpDirectory() ,"pedigree" + patientID + ".csv");

                CSVWriter w = new CSVWriter(new FileWriter(outfile),',',CSVWriter.NO_QUOTE_CHARACTER);
                w.writeNext(new String[] { HOSPITAL_ID, MOM, DAD, PATIENT_ID, GENDER, AFFECTED });
                for (Object[] row : results) {
                    String[] srow = new String[row.length];
                    for (int i = 0; i < row.length; i++) {
                        String entry = row[i] == null ? "" : row[i].toString();
                        srow[i] = entry;
                    }
                    w.writeNext(srow);
                }
                w.close();
            return outfile;
        }

        @Override
        protected void showProgress(double ignored) {
        }

        @Override
        protected void showSuccess(File result) {
            showPedigree(result);
            result.delete();
        }
    }
    public static class HospitalSymbol extends Symbol2D {
        private final String hid;

        public HospitalSymbol(String hid) {
            this.hid = hid;
        }

        @Override
        public void drawSymbol(Graphics2D g2, Float position, float size, Color color, Color color1, NodeView nv) {
            g2.setFont(new Font("Arial",Font.PLAIN,1));
            FontMetrics fm = g2.getFontMetrics();
            int height = fm.getMaxAscent();
            float startX = (float) position.getX()-size/2;// (float) (position.getX()-(double)width/2);
            float startY = (float) (position.getY()+(double) size/2 +height);

            g2.drawString(hid, startX, startY);
        }

        @Override
        public int getPriority() {
            return 0;
        }

    }
}
