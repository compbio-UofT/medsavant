/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.aggregates;

import au.com.bytecode.opencsv.CSVWriter;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D.Float;
import java.sql.SQLException;
import org.ut.biolab.medsavant.db.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import com.jidesoft.utils.SwingWorker;
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
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.settings.DirectorySettings;
import org.ut.biolab.medsavant.util.MiscUtils;
import org.ut.biolab.medsavant.view.patients.individual.Pedigree;
import org.ut.biolab.medsavant.view.patients.individual.PedigreeBasicRule;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.util.WaitPanel;
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

/**
 *
 * @author mfiume
 */
public class FamilyPanelGenerator implements AggregatePanelGenerator {

    private static final Logger LOG = Logger.getLogger(GeneListPanelGenerator.class.getName());
    private FamilyPanel panel;

    public FamilyPanelGenerator() {
    }

    public String getName() {
        return "Family";
    }

    public JPanel getPanel() {
        if (panel == null) {
            panel = new FamilyPanel();
        }
        return panel;
    }

    public void run() {
        panel.run();
    }

    @Override
    public void setUpdateRequired(boolean required) {
        //
    }

    public class FamilyPanel extends JPanel implements FiltersChangedListener {

        private final JPanel banner;
        private final JComboBox familyLister;
        private final JButton goButton;
        private final JPanel pedigreePanel;
        private final JProgressBar progress;
        private TreeMap<String, Integer> individualToCountMap;
        private PedigreeGrabber pg;
        private FamilyVariantIntersectionAggregator fa;
        private Graph pedigree;
        private Node overNode = null;
        private NodeView overNodeView = null;
        private List<Integer> selectedNodes = new ArrayList<Integer>();
        private String familyId;

        public FamilyPanel() {

            this.setLayout(new BorderLayout());
            banner = ViewUtil.getSubBannerPanel("Family");

            familyLister = new JComboBox();

            goButton = new JButton("Aggregate");

            pedigreePanel = new JPanel();
            pedigreePanel.setLayout(new BorderLayout());

            banner.add(familyLister);
            banner.add(ViewUtil.getMediumSeparator());

            banner.add(Box.createHorizontalGlue());

            progress = new JProgressBar();
            progress.setStringPainted(true);

            banner.add(progress);

            this.add(banner, BorderLayout.NORTH);
            this.add(pedigreePanel, BorderLayout.CENTER);

            familyLister.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    showFamilyAggregates((String) familyLister.getSelectedItem());
                }
            });

            (new FamilyListGetter()).execute();

            FilterController.addFilterListener(this);
        }

        public void updateFamilyDropDown(List<String> familyList) {
            for (String fam : familyList) {
                familyLister.addItem(fam);
            }
        }

        public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {
            stopThreads();
            showFamilyAggregates((String) familyLister.getSelectedItem());
        }

        private void stopThreads() {
            try {
                this.pg.cancel(true);
            } catch (Exception e) {
            }

            try {
                this.fa.cancel(true);
            } catch (Exception e) {
            }

            progress.setString("stopped");
        }

        private void run() {
            this.showFamilyAggregates((String) familyLister.getSelectedItem());
        }

        private class FamilyListGetter extends SwingWorker<List<String>, String> {

            @Override
            protected List<String> doInBackground() throws Exception {
                try {
                    return MedSavantClient.PatientQueryUtilAdapter.getFamilyIds(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectId());
                } catch (SQLException ex) {
                    MiscUtils.checkSQLException(ex);
                    throw ex;
                }
            }

            @Override
            protected void done() {
                try {
                    updateFamilyDropDown(get());
                } catch (Exception x) {
                    // TODO: #90
                    LOG.log(Level.SEVERE, null, x);
                }
            }
        }

        private void showFamilyAggregates(String familyId) {

            this.pedigreePanel.removeAll();
            this.pedigreePanel.add(new WaitPanel("Getting pedigree"));
            this.pedigreePanel.updateUI();
            this.familyId = familyId;

            stopThreads();

            progress.setIndeterminate(true);

            pg = new PedigreeGrabber(familyId);
            pg.execute();

            fa = new FamilyVariantIntersectionAggregator(familyId);
            fa.execute();

        }
        private GraphView2D graphView;
        private Map<String, Integer> individualVariantIntersection;

        private class FamilyVariantIntersectionAggregator extends SwingWorker<Map<String, Integer>, Map<String, Integer>> {

            private final String familyId;

            public FamilyVariantIntersectionAggregator(String familyId) {
                this.familyId = familyId;
            }

            @Override
            protected Map<String, Integer> doInBackground() throws Exception {
                try {
                    return MedSavantClient.VariantQueryUtilAdapter.getNumVariantsInFamily(
                            LoginController.sessionId, 
                            ProjectController.getInstance().getCurrentProjectId(),
                            ReferenceController.getInstance().getCurrentReferenceId(),
                            familyId, FilterController.getQueryFilterConditions());
                } catch (SQLException ex) {
                    MiscUtils.checkSQLException(ex);
                    throw ex;
                }
            }

            protected void done() {
                try {
                    Map<String, Integer> map = get();
                    setIndividualVariantIntersection(map);
                } catch (Exception ex) {
                    // TODO: report error if necessary
                    return;
                }
            }
        }

        private class PedigreeGrabber extends SwingWorker<File, File> {

            private final String familyId;

            public PedigreeGrabber(String familyId) {
                this.familyId = familyId;
            }

            @Override
            protected File doInBackground() throws Exception {

                List<Object[]> results;
                try {
                    results = MedSavantClient.PatientQueryUtilAdapter.getFamily(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectId(), familyId);
                } catch (SQLException ex) {
                    MiscUtils.checkSQLException(ex);
                    throw ex;
                }

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

            protected void done() {

                File pedigreeCSVFile;
                try {
                    pedigreeCSVFile = get();
                } catch (Exception ex) {
                    return;
                }

                Graph pedigree = new Graph();
                CsvGraphLoader loader = new CsvGraphLoader(pedigreeCSVFile.getAbsolutePath(), ",");
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
                public void onNodeEvent(NodeEvent ne) {
                    if(ne.getType() == NodeEvent.MOUSE_ENTER){
                        overNode = ne.getNode();
                        overNodeView = ne.getNodeView();
                    } else if (ne.getType() == NodeEvent.MOUSE_LEAVE){
                        overNode = null;
                        overNodeView = null;
                    }
                }
            });

            view.getComponent().addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if(overNode != null){
                        String hospitalId = (String)overNode.getId();
                        Integer patientId = Integer.parseInt((String)overNode.getUserData(Pedigree.FIELD_PATIENTID));
                        if(SwingUtilities.isRightMouseButton(e)){
                            int[] patientIds;
                            if(selectedNodes != null && !selectedNodes.isEmpty()){
                                patientIds = new int[selectedNodes.size()];
                                for(int i = 0; i < selectedNodes.size(); i++){
                                    patientIds[i] = selectedNodes.get(i);
                                }  
                            } else {
                                patientIds = new int[]{patientId};
                            }
                            JPopupMenu popup = org.ut.biolab.medsavant.view.pedigree.Utils.createPopup(patientIds);
                            popup.show(e.getComponent(), e.getX(), e.getY());
                        } else if (SwingUtilities.isLeftMouseButton(e)){ 
                            if(!selectedNodes.contains(patientId)){
                                selectedNodes.add(patientId);
                                overNodeView.setBorderColor(ViewUtil.detailSelectedBackground);
                            } else {
                                selectedNodes.remove(patientId);
                                overNodeView.setBorderColor(Color.black);
                            }
                        } 
                    } else {
                        if(SwingUtilities.isRightMouseButton(e) && familyId != null){
                            JPopupMenu popup = org.ut.biolab.medsavant.view.pedigree.Utils.createPopup(familyId);
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
