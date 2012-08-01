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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D.Float;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.*;

import au.com.bytecode.opencsv.CSVWriter;
import pedviz.algorithms.Sugiyama;
import pedviz.graph.Graph;
import pedviz.graph.Node;
import pedviz.loader.CsvGraphLoader;
import pedviz.view.GraphView2D;
import pedviz.view.NodeEvent;
import pedviz.view.NodeListener;
import pedviz.view.NodeView;
import pedviz.view.rules.Rule;
import pedviz.view.rules.ShapeRule;
import pedviz.view.symbols.Symbol2D;
import pedviz.view.symbols.SymbolSexFemale;
import pedviz.view.symbols.SymbolSexMale;
import pedviz.view.symbols.SymbolSexUndesignated;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.aggregate.AggregatePanel;
import org.ut.biolab.medsavant.aggregate.AggregatePanelGenerator;
import org.ut.biolab.medsavant.filter.FilterController;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.reference.ReferenceController;
import org.ut.biolab.medsavant.settings.DirectorySettings;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
public class FamilyPanelGenerator extends AggregatePanelGenerator {

    public FamilyPanelGenerator(String page) {
        super(page);
    }

    @Override
    public String getName() {
        return "Family";
    }

    @Override
    public AggregatePanel generatePanel() {
        return new FamilyPanel();
    }

    private class FamilyPanel extends AggregatePanel {

        private PedigreeGrabber pedigreeGrabber;
        private FamilyVariantIntersectionAggregator aggregator;
        private Graph pedigree;
        private Node overNode = null;
        private NodeView overNodeView = null;
        private List<Integer> selectedNodes = new ArrayList<Integer>();
        private GraphView2D graphView;
        private Map<String, Integer> individualVariantIntersection;
        private String familyID;

        private final JPanel banner;
        private final JComboBox familyLister;
        private final JPanel pedigreePanel;
        private final JProgressBar progress;

        private FamilyPanel() {

            setLayout(new BorderLayout());
            banner = ViewUtil.getSubBannerPanel("Family");

            familyLister = new JComboBox();

            pedigreePanel = new JPanel();
            pedigreePanel.setLayout(new BorderLayout());

            banner.add(familyLister);
            banner.add(ViewUtil.getMediumSeparator());

            banner.add(Box.createHorizontalGlue());

            progress = new JProgressBar();
            progress.setStringPainted(true);

            banner.add(progress);

            add(banner, BorderLayout.NORTH);
            add(pedigreePanel, BorderLayout.CENTER);

            familyLister.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showFamilyAggregates((String) familyLister.getSelectedItem());
                }
            });

            new FamilyListGetter().execute();

        }

        public void updateFamilyDropDown(List<String> familyList) {
            for (String fam : familyList) {
                familyLister.addItem(fam);
            }
        }

        private void stopThreads() {
            try {
                pedigreeGrabber.cancel(true);
            } catch (Exception e) {
            }

            try {
                aggregator.cancel(true);
            } catch (Exception e) {
            }

            progress.setString("stopped");
        }

        @Override
        public void recalculate() {
            showFamilyAggregates((String)familyLister.getSelectedItem());
        }

        private class FamilyListGetter extends MedSavantWorker<List<String>> {

            public FamilyListGetter() {
                super(pageName);
            }

            @Override
            protected List<String> doInBackground() throws Exception {
                return MedSavantClient.PatientManager.getFamilyIDs(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectID());
            }

            @Override
            protected void showProgress(double fraction) {}

            @Override
            protected void showSuccess(List<String> result) {
                updateFamilyDropDown(result);
            }
        }

        private void showFamilyAggregates(String famID) {

            pedigreePanel.removeAll();
            pedigreePanel.add(new WaitPanel("Getting pedigree"));
            familyID = famID;


            stopThreads();

            progress.setIndeterminate(true);

            pedigreeGrabber = new PedigreeGrabber(famID);
            pedigreeGrabber.execute();

            aggregator = new FamilyVariantIntersectionAggregator(famID);
            aggregator.execute();

        }

        private class FamilyVariantIntersectionAggregator extends MedSavantWorker<Map<String, Integer>> {

            private final String familyId;

            public FamilyVariantIntersectionAggregator(String familyId) {
                super(pageName);
                this.familyId = familyId;
            }

            @Override
            protected Map<String, Integer> doInBackground() throws Exception {
                return MedSavantClient.VariantManager.getNumVariantsInFamily(
                        LoginController.sessionId,
                        ProjectController.getInstance().getCurrentProjectID(),
                        ReferenceController.getInstance().getCurrentReferenceID(),
                        familyId, FilterController.getInstance().getAllFilterConditions());
            }

            @Override
            protected void showProgress(double fraction) {}

            @Override
            protected void showSuccess(Map<String, Integer> result) {
                setIndividualVariantIntersection(result);
            }
        }

        private class PedigreeGrabber extends MedSavantWorker<File> {

            private final String familyId;

            public PedigreeGrabber(String familyId) {
                super(pageName);
                this.familyId = familyId;
            }

            @Override
            protected File doInBackground() throws Exception {

                List<Object[]> results = MedSavantClient.PatientManager.getFamily(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectID(), familyId);

                File outfile = new File(DirectorySettings.getTmpDirectory(), "pedigree" + familyId + ".csv");

                CSVWriter w = new CSVWriter(new FileWriter(outfile), ',', CSVWriter.NO_QUOTE_CHARACTER);
                w.writeNext(new String[]{Pedigree.FIELD_HOSPITALID,
                            Pedigree.FIELD_MOM,
                            Pedigree.FIELD_DAD,
                            Pedigree.FIELD_PATIENTID,
                            Pedigree.FIELD_GENDER,
                            Pedigree.FIELD_AFFECTED});
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
            protected void showProgress(double fraction) {}

            @Override
            protected void showSuccess(File result) {

                Graph pedigree = new Graph();
                CsvGraphLoader loader = new CsvGraphLoader(result.getAbsolutePath(), ",");
                loader.setSettings(Pedigree.FIELD_HOSPITALID, Pedigree.FIELD_MOM, Pedigree.FIELD_DAD);
                loader.load(pedigree);

                setPedigree(pedigree);
            }
        }

        private synchronized void setIndividualVariantIntersection(Map<String, Integer> map) {
            this.individualVariantIntersection = map;
            updateResultView();
        }

        private synchronized void setPedigree(Graph pedigree) {


            Sugiyama s = new Sugiyama(pedigree);
            s.run();

            GraphView2D view = new GraphView2D(s.getLayoutedGraph());

            view.addRule(new ShapeRule(Pedigree.FIELD_GENDER, "1", new SymbolSexMale()));
            view.addRule(new ShapeRule(Pedigree.FIELD_GENDER, "2", new SymbolSexFemale()));
            view.addRule(new ShapeRule(Pedigree.FIELD_GENDER, "0", new SymbolSexUndesignated()));
            view.addRule(new ShapeRule(Pedigree.FIELD_GENDER, "null", new SymbolSexUndesignated()));

            view.addRule(new PedigreeBasicRule());
            view.addRule(new NumVariantRule());

            this.pedigree = pedigree;
            this.graphView = view;

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
                            int[] patientIds;
                            if (selectedNodes != null && !selectedNodes.isEmpty()) {
                                patientIds = new int[selectedNodes.size()];
                                for(int i = 0; i < selectedNodes.size(); i++) {
                                    patientIds[i] = selectedNodes.get(i);
                                }
                            } else {
                                patientIds = new int[]{patientId};
                            }
                            JPopupMenu popup = org.ut.biolab.medsavant.patient.PatientUtils.createPopup(patientIds);
                            popup.show(e.getComponent(), e.getX(), e.getY());
                        } else if (SwingUtilities.isLeftMouseButton(e)) {
                            if (!selectedNodes.contains(patientId)) {
                                selectedNodes.add(patientId);
                                overNodeView.setBorderColor(ViewUtil.detailSelectedBackground);
                            } else {
                                selectedNodes.remove(patientId);
                                overNodeView.setBorderColor(Color.black);
                            }
                        }
                    } else {
                        if (SwingUtilities.isRightMouseButton(e) && familyID != null) {
                            JPopupMenu popup = org.ut.biolab.medsavant.patient.PatientUtils.createPopup(familyID);
                            popup.show(e.getComponent(), e.getX(), e.getY());

                        }
                    }
                    pedigreePanel.repaint();
                }
            });


            updateResultView();
        }
        public static final String FIELD_NUMVARIANTS = "VARIANTS";

        private synchronized void updateResultView() {
            if (graphView != null) {
                this.pedigreePanel.removeAll();
                this.pedigreePanel.setLayout(new BorderLayout());
                this.pedigreePanel.add(graphView.getComponent(), BorderLayout.CENTER);
                this.pedigreePanel.updateUI();

                if (this.individualVariantIntersection != null) {

                    for (Node n : pedigree.getAllNodes()) {
                        String id = n.getId().toString();
                        n.setUserData(FIELD_NUMVARIANTS, individualVariantIntersection.get(id));
                    }

                    this.pedigreePanel.updateUI();

                    progress.setIndeterminate(false);
                    progress.setValue(100);
                    progress.setString("complete");
                }
            }
        }
    }

    public static class NumVariantRule extends Rule {

        @Override
        public void applyRule(NodeView nv) {
            nv.addSymbol(new NumVariantsSymbol());

        }

        private static class NumVariantsSymbol extends Symbol2D {

            @Override
            public int getPriority() {
                return 0;
            }

            @Override
            public void drawSymbol(Graphics2D gd, Float position, float size, Color color, Color color1, NodeView nv) {

                Object o = nv.getNode().getUserData(FamilyPanelGenerator.FamilyPanel.FIELD_NUMVARIANTS);

                String toWrite;

                if (o != null) {
                    Integer count = (Integer) o;
                    toWrite = ViewUtil.numToString(count);
                } else {
                    toWrite = "no DNA";
                }

                gd.setFont(new Font("Arial", Font.BOLD, 1));
                FontMetrics fm = gd.getFontMetrics();
                int width = fm.stringWidth(toWrite);
                int height = fm.getAscent();

                float startX = (float) position.getX() - size / 2;// (float) (position.getX()-(double)width/2);
                float startY = (float) (position.getY() + (double) size / 2 + height + 0.1 + height);

                float pad = 0.07F;

                gd.setColor(Color.red);
                gd.fill(new RoundRectangle2D.Float(startX - pad, startY - height - pad + 0.1F, size + 2*pad, height + 2*pad,1F,1F));

                gd.setColor(Color.white);
                gd.drawString(toWrite, startX+ size*0.1F, startY);

            }
        }
    }
}
