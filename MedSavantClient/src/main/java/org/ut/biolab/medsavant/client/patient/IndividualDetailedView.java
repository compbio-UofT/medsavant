/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.patient;

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
import java.util.concurrent.Semaphore;
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
import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.shared.model.Cohort;
import org.ut.biolab.medsavant.client.settings.DirectorySettings;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.component.BlockingPanel;
import org.ut.biolab.medsavant.client.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.client.view.dialog.ComboForm;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.list.DetailedView;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

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
    private String[] hospitalIDs;
    boolean pedigreeShown = false;
    private final JPanel pedigreeDetails;
    private PedigreeWorker pedigreeWorker;
    private Node overNode = null;
    private NodeView overNodeView = null;
    private List<Integer> selectedNodes;
    private String familyID;
    private Graph graph;
    private final CollapsiblePane collapsiblePane;
    private final BlockingPanel blockPanel;
    private static int pedigreeFontSize = 1;
    private final Semaphore csvSem = new Semaphore(1);
    private JPanel fontZoomButtonsPanel;

    public IndividualDetailedView(String page) throws RemoteException, SQLException {
        super(page);
        try {
            fieldNames = MedSavantClient.PatientManager.getPatientFieldAliases(LoginController.getInstance().getSessionID(), ProjectController.getInstance().getCurrentProjectID());
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
        }

        JPanel viewContainer = (JPanel) ViewUtil.clear(this.getContentPanel());
        viewContainer.setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setLayout(new BorderLayout());

        JPanel infoContainer = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(infoContainer);

        content.add(ViewUtil.getClearBorderlessScrollPane(infoContainer), BorderLayout.CENTER);

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
        collapsiblePane.add(infoContent, BorderLayout.CENTER);


        fontZoomButtonsPanel = ViewUtil.getClearPanel();
        fontZoomButtonsPanel.setLayout(new BoxLayout(fontZoomButtonsPanel, BoxLayout.X_AXIS));

        JButton zoomFont = new JButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.FONT_INCREASE));
        zoomFont.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (pedigreeFontSize < 20) {
                    pedigreeFontSize++;
                    pedigreeDetails.repaint();
                }
            }
        });

        JButton unZoomFont = new JButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.FONT_DECREASE));
        unZoomFont.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (pedigreeFontSize > 2) {
                    pedigreeFontSize--;
                    pedigreeDetails.repaint();
                }
            }
        });
        fontZoomButtonsPanel.add(Box.createHorizontalGlue());
        fontZoomButtonsPanel.add(zoomFont);
        fontZoomButtonsPanel.add(unZoomFont);

        infoDetails = ViewUtil.getClearPanel();
        pedigreeDetails = new JPanel();
        pedigreeDetails.setBackground(Color.white);
        pedigreeDetails.setLayout(new BoxLayout(pedigreeDetails, BoxLayout.Y_AXIS));
        pedigreeDetails.add(fontZoomButtonsPanel);
        ViewUtil.setBoxYLayout(infoContent);
        infoContent.add(infoDetails);

        menu = ViewUtil.getClearPanel();
        menu.add(addIndividualsButton());

        addBottomComponent(menu);

        blockPanel = new BlockingPanel("No individual selected", content);
        viewContainer.add(blockPanel, BorderLayout.CENTER);
    }

    public synchronized void showPedigree(File pedigreeCSVFile) {

        pedigreeDetails.removeAll();
///        pedigreeDetails.setLayout(new BorderLayout());

        pedigreeDetails.add(fontZoomButtonsPanel);//, BorderLayout.NORTH);
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

        view.getComponent().setMinimumSize(new Dimension(400, 400));
        view.getComponent().setPreferredSize(new Dimension(400, 400));

        //view.highlight(nodes);
        //view.setSelectionEnabled(true);


        view.addRule(new ShapeRule(GENDER, "1", new SymbolSexMale()));
        view.addRule(new ShapeRule(GENDER, "2", new SymbolSexFemale()));
        view.addRule(new ShapeRule(GENDER, "0", new SymbolSexUndesignated()));
        view.addRule(new ShapeRule(GENDER, "null", new SymbolSexUndesignated()));

        view.addRule(new PedigreeBasicRule(patientIDs));

        selectedNodes = new ArrayList<Integer>();
        for (Integer i : patientIDs) {
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
                    Integer patID = Integer.parseInt((String) overNode.getUserData(PATIENT_ID));
                    if (SwingUtilities.isRightMouseButton(e)) {
                        int[] patientIds = new int[selectedNodes.size()];
                        for (int i = 0; i < selectedNodes.size(); i++) {
                            patientIds[i] = selectedNodes.get(i);
                        }


                        JPopupMenu popup = org.ut.biolab.medsavant.client.patient.PatientUtils.createPopup(patientIds);
                        popup.show(e.getComponent(), e.getX(), e.getY());
                    } else if (SwingUtilities.isLeftMouseButton(e) && e.isControlDown()) {
                        if (!selectedNodes.contains(patID)) {
                            selectedNodes.add(patID);
                            overNodeView.setBorderColor(ViewUtil.detailSelectedBackground);
                        } else {
                            for (int i : patientIDs) {
                                if (i == patID) {
                                    return;
                                }
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
                        JPopupMenu popup = org.ut.biolab.medsavant.client.patient.PatientUtils.createPopup(familyID);
                        popup.show(e.getComponent(), e.getX(), e.getY());

                    }
                }
                pedigreeDetails.repaint();
            }
        });

        pedigreeDetails.add(view.getComponent());
//        pedigreeDetails.add(view.getComponent(), BorderLayout.NORTH);
        pedigreeDetails.updateUI();
    }

    private void selectIndividualInList(int patID) {
        Object[][] list = parent.getList();
        for (int i = 0; i < list.length; i++) {
            Object[] o = list[i];
            if (o != null && o.length >= 1 && (Integer) o[0] == patID) {
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
                        s = ClientMiscUtils.genderToString((Integer) result[i]);
                    } else if (result[i] instanceof Long) {
                        s = ClientMiscUtils.genderToString(ClientMiscUtils.safeLongToInt((Long) result[i]));
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

        final KeyValuePairPanel kvp = new KeyValuePairPanel(1, true);

        for (int i = 0; i < values.length; i++) {
            kvp.addKey(values[i][0]);
            kvp.setValue(values[i][0], values[i][1], true);
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

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        pedigreeDetails.repaint();
                    }
                });
            }
        });

        kvp.setValue(pedigreeKey, b);
        kvp.setDetailComponent(pedigreeKey, pedigreeDetails);
        if (pedigreeShown) {
            kvp.toggleDetailVisibility(pedigreeKey);
        }

        infoDetails.add(kvp);
        infoDetails.updateUI();
    }

    @Override
    public void setSelectedItem(Object[] item) {
        if (item.length == 0) {
            blockPanel.block();
        } else {
            int patientId = (Integer) item[0];
            String hospitalId = (String) item[2];
            setSelectedItem(patientId, hospitalId);
        }
    }

    public void setSelectedItem(int patientId, String hospitalId) {

        collapsiblePane.setTitle(hospitalId);

        hospitalIDs = new String[1];

        patientIDs = new int[1];
        patientIDs[0] = patientId;
        hospitalIDs[0] = hospitalId;

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

        if (items.isEmpty()) {
            blockPanel.block();
        } else {
            hospitalIDs = new String[items.size()];
            patientIDs = new int[items.size()];
            for (int i = 0; i < items.size(); i++) {
                patientIDs[i] = (Integer) items.get(i)[0];
                hospitalIDs[i] = (String) items.get(i)[2]; //BAD!
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
    }

    @Override
    public JPopupMenu createPopup() {
        //this.overNode.getId()



       // if (patientIDs != null && patientIDs.length > 0) {            
        // return PatientUtils.createPopup(patientIDs);
        //
        if (hospitalIDs != null && hospitalIDs.length > 0) {
            return PatientUtils.createPopup(hospitalIDs);
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
                        Cohort[] cohorts = MedSavantClient.CohortManager.getCohorts(LoginController.getInstance().getSessionID(), ProjectController.getInstance().getCurrentProjectID());
                        ComboForm form = new ComboForm(cohorts, "Select Cohort", "Select which cohort to add to:");
                        form.setVisible(true);
                        Cohort selected = (Cohort) form.getSelectedValue();
                        if (selected == null) {
                            return;
                        }
                        MedSavantClient.CohortManager.addPatientsToCohort(LoginController.getInstance().getSessionID(), patientIDs, selected.getId());
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
            try {
                return MedSavantClient.PatientManager.getPatientRecord(LoginController.getInstance().getSessionID(), ProjectController.getInstance().getCurrentProjectID(), patientID);
            } catch (SessionExpiredException ex) {
                MedSavantExceptionHandler.handleSessionExpiredException(ex);
                return null;
            }
        }

        @Override
        protected void showProgress(double ignored) {
        }

        @Override
        protected void showSuccess(Object[] result) {
            setPatientInformation(result);
            blockPanel.unblock();
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
            csvSem.acquire();
            List<Object[]> results = MedSavantClient.PatientManager.getFamilyOfPatient(LoginController.getInstance().getSessionID(), ProjectController.getInstance().getCurrentProjectID(), patientID);


            familyID = MedSavantClient.PatientManager.getFamilyIDOfPatient(LoginController.getInstance().getSessionID(), ProjectController.getInstance().getCurrentProjectID(), patientID);

            File outfile = new File(DirectorySettings.getTmpDirectory(), "pedigree" + patientID + ".csv");
            CSVWriter w = new CSVWriter(new FileWriter(outfile), ',', CSVWriter.NO_QUOTE_CHARACTER);
            w.writeNext(new String[]{HOSPITAL_ID, MOM, DAD, PATIENT_ID, GENDER, AFFECTED, DNA_ID});
            for (Object[] row : results) {
                String[] srow = new String[row.length];
                for (int i = 0; i < row.length; i++) {
                    String entry = row[i] == null ? "" : row[i].toString();
                    srow[i] = entry;
                    //      System.out.println("\t"+Thread.currentThread().getId()+": Adding to file");
                }
                w.writeNext(srow);
            }
            w.close();
            //System.out.println(Thread.currentThread().getId()+": File written");
            return outfile;
        }

        @Override
        protected void showProgress(double ignored) {
        }

        @Override
        protected void showSuccess(File result) {
            showPedigree(result);
            result.delete();
            csvSem.release();
        }
    }

    public static class HospitalSymbol extends Symbol2D {

        private final String hid;

        public HospitalSymbol(String hid) {
            this.hid = hid;
        }

        @Override
        public void drawSymbol(Graphics2D g2, Float position, float size, Color color, Color color1, NodeView nv) {
            g2.setFont(new Font("Arial", Font.PLAIN, pedigreeFontSize));
            FontMetrics fm = g2.getFontMetrics();
            int height = fm.getMaxAscent();
            float startX = (float) position.getX() - size / 2;// (float) (position.getX()-(double)width/2);
            float startY = (float) (position.getY() + (double) size / 2 + height);

            g2.drawString(hid, startX, startY);
        }

        @Override
        public int getPriority() {
            return 0;
        }
    }
}
