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

package org.ut.biolab.medsavant.view.patients;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
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
import java.util.concurrent.CancellationException;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import au.com.bytecode.opencsv.CSVWriter;
import com.jidesoft.utils.SwingWorker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.format.PatientFormat;
import org.ut.biolab.medsavant.model.Cohort;
import org.ut.biolab.medsavant.settings.DirectorySettings;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.view.component.CollapsiblePanel;
import org.ut.biolab.medsavant.view.dialog.ComboForm;
import org.ut.biolab.medsavant.view.list.DetailedView;
import org.ut.biolab.medsavant.view.pedigree.Pedigree;
import org.ut.biolab.medsavant.view.pedigree.PedigreeBasicRule;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class IndividualDetailedView extends DetailedView {
    private static final Log LOG = LogFactory.getLog(IndividualDetailedView.class);

    private List<String> fieldNames;
    private IndividualDetailsSW sw;
    private final JPanel infoContent;
    private final JPanel infoDetails;
    private final JPanel menu;
    private int[] patientIds;
    private final JPanel pedigreeContent;
    private final JPanel pedigreeDetails;
    private PedigreeSW sw2;
    private Node overNode = null;
    private NodeView overNodeView = null;
    private List<Integer> selectedNodes;
    private String familyId;

    private class IndividualDetailsSW extends SwingWorker {
        private final int pid;

        public IndividualDetailsSW(int pid) {
            this.pid = pid;
        }

        @Override
        protected Object doInBackground() throws RemoteException, SQLException {
            return MedSavantClient.PatientQueryUtilAdapter.getPatientRecord(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectId(), pid);
        }

        @Override
        protected void done() {
            try {
                Object[] result = (Object[]) get();
                setPatientInformation(result);
            } catch (CancellationException ex) {

            } catch (Exception x) {
                LOG.error("Error fetching individual details.", x);
            }
        }
    }


    private class PedigreeSW extends SwingWorker {
        private final int pid;

        public PedigreeSW(int pid) {
            this.pid = pid;
        }

        @Override
        protected Object doInBackground() throws Exception {

            List<Object[]> results = MedSavantClient.PatientQueryUtilAdapter.getFamilyOfPatient(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectId(), pid);
            familyId = MedSavantClient.PatientQueryUtilAdapter.getFamilyIdOfPatient(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectId(), pid);

            File outfile = new File(DirectorySettings.getTmpDirectory() ,"pedigree" + pid + ".csv");

                CSVWriter w = new CSVWriter(new FileWriter(outfile),',',CSVWriter.NO_QUOTE_CHARACTER);
                w.writeNext(new String[] {Pedigree.FIELD_HOSPITALID,Pedigree.FIELD_MOM,Pedigree.FIELD_DAD,
                    Pedigree.FIELD_PATIENTID,Pedigree.FIELD_GENDER,Pedigree.FIELD_AFFECTED});
                for (Object[] row : results) {
                    String[] srow = new String[row.length];
                    for (int i = 0; i < row.length; i++) {
                        srow[i] = row[i].toString();
                    }
                    w.writeNext(srow);
                }
                w.close();
            return outfile;
        }

        @Override
        protected void done() {
            try {
                File pedigreeCSVFile = (File) get();
                showPedigree(pedigreeCSVFile);
                pedigreeCSVFile.delete();
            } catch (CancellationException ex) {

            } catch (Exception x) {
                LOG.error("Error fetching pedigree details.", x);
            }
        }
    }

    public synchronized void showPedigree(File pedigreeCSVFile) {

        pedigreeDetails.removeAll();
        pedigreeDetails.setLayout(new BorderLayout());

        //Step 1
	final Graph graph = new Graph();
	CsvGraphLoader loader = new CsvGraphLoader(pedigreeCSVFile.getAbsolutePath(), ",");
	loader.setSettings(Pedigree.FIELD_HOSPITALID,Pedigree.FIELD_MOM,Pedigree.FIELD_DAD);
	loader.load(graph);

	//Step 2
	Sugiyama s = new Sugiyama(graph);
	s.run();

	//Step 3
	GraphView2D view = new GraphView2D(s.getLayoutedGraph());

        //view.highlight(nodes);
        //view.setSelectionEnabled(true);

        view.addRule(new ShapeRule(Pedigree.FIELD_GENDER, "1", new SymbolSexMale()));
	view.addRule(new ShapeRule(Pedigree.FIELD_GENDER, "2", new SymbolSexFemale()));
        view.addRule(new ShapeRule(Pedigree.FIELD_GENDER, "0", new SymbolSexUndesignated()));
	view.addRule(new ShapeRule(Pedigree.FIELD_GENDER, "null", new SymbolSexUndesignated()));

        view.addRule(new PedigreeBasicRule(patientIds));

        selectedNodes = new ArrayList<Integer>();
        for(Integer i : patientIds) {
            selectedNodes.add(i);
        }

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
                    String hospitalId = (String)overNode.getId();
                    Integer patientId = Integer.parseInt((String)overNode.getUserData(Pedigree.FIELD_PATIENTID));
                    if (SwingUtilities.isRightMouseButton(e)) {
                        int[] patientIds = new int[selectedNodes.size()];
                        for(int i = 0; i < selectedNodes.size(); i++) {
                            patientIds[i] = selectedNodes.get(i);
                        }
                        JPopupMenu popup = org.ut.biolab.medsavant.view.pedigree.Utils.createPopup(patientIds);
                        popup.show(e.getComponent(), e.getX(), e.getY());
                    } else if (SwingUtilities.isLeftMouseButton(e) && e.isControlDown()) {
                        if (!selectedNodes.contains(patientId)) {
                            selectedNodes.add(patientId);
                            overNodeView.setBorderColor(ViewUtil.detailSelectedBackground);
                        } else {
                            for(int i : patientIds) {
                                if (i == patientId) return;
                            }
                            selectedNodes.remove(patientId);
                            overNodeView.setBorderColor(Color.black);
                        }
                    } else if (SwingUtilities.isLeftMouseButton(e)) {
                        if (patientId != null && patientId > 0) {
                            selectIndividualInList(patientId);
                            //setSelectedItem(patientId, hospitalId);
                        }
                    }
                } else {
                    if (SwingUtilities.isRightMouseButton(e) && familyId != null) {
                        JPopupMenu popup = org.ut.biolab.medsavant.view.pedigree.Utils.createPopup(familyId);
                        popup.show(e.getComponent(), e.getX(), e.getY());

                    }
                }
                pedigreeDetails.repaint();
            }
        });

        pedigreeDetails.add(view.getComponent(),BorderLayout.CENTER);

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

    public static class HospitalSymbol extends Symbol2D {
        private final String hid;

                    public HospitalSymbol(String hid) {
                        this.hid = hid;
                    }

                    @Override
                    public void drawSymbol(Graphics2D gd, Float position, float size, Color color, Color color1, NodeView nv) {
                        gd.setFont(new Font("Arial",Font.PLAIN,1));
                        FontMetrics fm = gd.getFontMetrics();
                        int width = fm.stringWidth(hid);
                        int height = fm.getMaxAscent();

                        float startX = (float) position.getX()-size/2;// (float) (position.getX()-(double)width/2);
                        float startY = (float) (position.getY()+(double) size/2 +height);

                        gd.drawString(hid, startX, startY);
                    }

                    @Override
                    public int getPriority() {
                        return 0;
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
                if (values[i][0].equals(PatientFormat.ALIAS_OF_GENDER)) {
                    String s;
                    if (result[i] instanceof Long || result[i] instanceof Integer) {
                        s = ClientMiscUtils.genderToString(ClientMiscUtils.safeLongToInt((Long)result[i]));
                    } else {
                        s = ClientMiscUtils.GENDER_UNKNOWN;
                    }
                    values[i][1] = s;
                }
            }
        }


        infoDetails.removeAll();
        ViewUtil.setBoxYLayout(infoDetails);

        infoDetails.add(ViewUtil.getKeyValuePairList(values));

        infoDetails.updateUI();
    }

    public IndividualDetailedView() throws RemoteException, SQLException {

        fieldNames = MedSavantClient.PatientQueryUtilAdapter.getPatientFieldAliases(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectId());

        JPanel viewContainer = (JPanel) ViewUtil.clear(this.getContentPanel());
        viewContainer.setLayout(new BorderLayout());

        JPanel infoContainer = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(infoContainer);

        viewContainer.add(ViewUtil.getClearBorderlessJSP(infoContainer),BorderLayout.CENTER);

        CollapsiblePanel cp = new CollapsiblePanel("Patient Information");
        //cp.setContentPaneVisible(false);
        infoContainer.add(cp);

        infoContainer.add(ViewUtil.getLargeSeparator());

        CollapsiblePanel cpPed = new CollapsiblePanel("Pedigree");
        cpPed.setContentPaneVisible(false);
        infoContainer.add(cpPed);

        infoContainer.add(Box.createVerticalGlue());

        infoContent = cp.getContentPane();
        pedigreeContent = cpPed.getContentPane();

        infoDetails = ViewUtil.getClearPanel();
        pedigreeDetails = ViewUtil.getClearPanel();

        menu = ViewUtil.getClearPanel();

        menu.add(addIndividualsButton());
        menu.setVisible(false);


        ViewUtil.setBoxYLayout(infoContent);

        //infoContent.setLayout(new BorderLayout());
        pedigreeContent.setLayout(new BorderLayout());

        infoContent.add(infoDetails);
        pedigreeContent.add(pedigreeDetails,BorderLayout.CENTER);

        this.addBottomComponent(menu);
    }

    @Override
    public void setSelectedItem(Object[] item) {
        int patientId = (Integer) item[0];
        String hospitalId = (String) item[2];
        setSelectedItem(patientId, hospitalId);
    }

    public void setSelectedItem(int patientId, String hospitalId) {

        patientIds = new int[1];
        patientIds[0] = patientId;

        setTitle(hospitalId);

        infoDetails.removeAll();
        infoDetails.updateUI();

        if (sw != null) {
            sw.cancel(true);
        }
        sw = new IndividualDetailsSW(patientId);
        sw.execute();

        if (sw2 != null) {
            sw2.cancel(true);
        }

        sw2 = new PedigreeSW(patientId);
        sw2.execute();

        if (menu != null) menu.setVisible(true);
    }

    @Override
    public void setMultipleSelections(List<Object[]> items) {
        patientIds = new int[items.size()];
        for(int i = 0; i < items.size(); i++) {
            patientIds[i] = (Integer) items.get(i)[0];
        }
        if (items.isEmpty()) {
                setTitle("");
            } else {
        setTitle("Multiple individuals (" + items.size() + ")");
        }
        infoDetails.removeAll();
        infoDetails.updateUI();
        pedigreeDetails.removeAll();
        pedigreeDetails.updateUI();
    }


    @Override
    public void setRightClick(MouseEvent e) {
        if (patientIds != null && patientIds.length > 0) {
            JPopupMenu popup = org.ut.biolab.medsavant.view.pedigree.Utils.createPopup(patientIds);
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private JButton addIndividualsButton() {
        JButton button = new JButton("Add individual(s) to cohort");
        button.setBackground(ViewUtil.getDetailsBackgroundColor());
        button.setOpaque(false);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (patientIds != null && patientIds.length > 0) {
                    try {
                        List<Cohort> cohorts = MedSavantClient.CohortQueryUtilAdapter.getCohorts(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectId());
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
        return button;
    }

}
