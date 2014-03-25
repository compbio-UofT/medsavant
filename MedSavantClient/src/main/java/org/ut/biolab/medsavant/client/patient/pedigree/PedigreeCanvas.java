/*
 * Copyright (C) 2014 University of Toronto, Computational Biology Lab.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.ut.biolab.medsavant.client.patient.pedigree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;
import static org.ut.biolab.medsavant.client.patient.pedigree.PedigreeFields.DAD;
import static org.ut.biolab.medsavant.client.patient.pedigree.PedigreeFields.GENDER;
import static org.ut.biolab.medsavant.client.patient.pedigree.PedigreeFields.HOSPITAL_ID;
import static org.ut.biolab.medsavant.client.patient.pedigree.PedigreeFields.MOM;
import static org.ut.biolab.medsavant.client.patient.pedigree.PedigreeFields.PATIENT_ID;
import org.ut.biolab.medsavant.client.view.component.WaitPanel;
import org.ut.biolab.medsavant.client.view.font.FontFactory;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import pedviz.algorithms.Sugiyama;
import pedviz.graph.Graph;
import pedviz.graph.Node;
import pedviz.loader.CsvGraphLoader;
import pedviz.view.GraphView;
import pedviz.view.GraphView2D;
import pedviz.view.NodeEvent;
import pedviz.view.NodeListener;
import pedviz.view.NodeView;
import pedviz.view.rules.ShapeRule;
import pedviz.view.symbols.Symbol2D;
import pedviz.view.symbols.SymbolSexFemale;
import pedviz.view.symbols.SymbolSexMale;
import pedviz.view.symbols.SymbolSexUndesignated;

/**
 *
 * @author mfiume
 */
public class PedigreeCanvas extends JPanel {

    private Graph graph;
    private PedigreeWorker pedigreeWorker;
    private String familyName;
    private JLabel familyNameLabel;
    private GraphView2D view;
    private PedigreeBasicRule rule;

    public PedigreeCanvas() {

        this.setBackground(ViewUtil.getDefaultBackgroundColor());

        initCanvas();

        this.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                if (view != null) {
                    view.updateGraphView();
                }
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }

        });
    }

    private void initCanvas() {
        showWaitPanel();
    }

    public void showPedigreeFor(int patientID) {
        if (pedigreeWorker != null) {
            pedigreeWorker.cancel(true);
        }
        showWaitPanel();
        pedigreeWorker = new PedigreeWorker(patientID, this);
        pedigreeWorker.execute();
    }

    synchronized void showPedigree(File pedigreeCSVFile, int patientID) {

        System.out.println("Drawing pedigree from file " + pedigreeCSVFile.getAbsolutePath());

        //Step 1
        graph = new Graph();
        
        CsvGraphLoader loader = new CsvGraphLoader(pedigreeCSVFile.getAbsolutePath(), ",");
        loader.setSettings(HOSPITAL_ID, MOM, DAD);
        loader.load(graph);

        //Step 2
        Sugiyama s = new Sugiyama(graph);
        s.run();

        //Step 3
        view = new GraphView2D(s.getLayoutedGraph());

        view.setSelectionEnabled(false);
        view.setZoomEnabled(true);
        view.setMovingEnabled(true);

        view.addRule(new ShapeRule(GENDER, "1", new SymbolSexMale()));
        view.addRule(new ShapeRule(GENDER, "2", new SymbolSexFemale()));
        view.addRule(new ShapeRule(GENDER, "0", new SymbolSexUndesignated()));
        view.addRule(new ShapeRule(GENDER, "null", new SymbolSexUndesignated()));

        rule = new PedigreeBasicRule(patientID);
        
        view.addRule(rule);
        view.setScale(1);

        view.centerGraph();

        final float fontStepSize = 0.2f;

        JButton zoomFont = ViewUtil.getIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.FONT_INCREASE), 3);
        zoomFont.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (rule.getFontSize() < 10f) {
                    rule.setFontSize(rule.getFontSize()+fontStepSize);
                    view.updateRules();
                    view.updateGraphView();

                }
            }
        });

        JButton unZoomFont = ViewUtil.getIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.FONT_DECREASE), 3);
        unZoomFont.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (rule.getFontSize() >= 0.4) {
                    rule.setFontSize(rule.getFontSize()-fontStepSize);
                    view.updateRules();
                    view.updateGraphView();
                }
            }
        });

        final JCheckBox patientLabels = new JCheckBox("Patient Labels");
        patientLabels.setSelected(true);
        patientLabels.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                rule.setPatientLabelsShown(patientLabels.isSelected());
                view.updateRules();
                view.updateGraphView();
            }

        });
        
        JButton help = ViewUtil.getHelpButton("Pedigree Help", "Using the mouse, scroll to zoom and click-and-drag to pan pedigree.", true);

        System.out.println("Placing pedigree in view...");

        this.removeAll();

        JPanel topBar = ViewUtil.getClearPanel();

        familyNameLabel = ViewUtil.getLargeGrayLabel(familyName);

        topBar.setLayout(new MigLayout("fillx, insets 0"));

        topBar.add(familyNameLabel, "gpx 10000, growx 10000");
        topBar.add(patientLabels, "growx 0, right, split");
        topBar.add(help);
        //topBar.add(zoomFont, "growx 0, right");
        //topBar.add(unZoomFont, "growx 0, right");

        JPanel placeholder = ViewUtil.getWhiteLineBorderedPanel();
        placeholder.setLayout(new BorderLayout());
        placeholder.add(view.getComponent(), BorderLayout.CENTER);

        this.setBackground(ViewUtil.getLightGrayBackgroundColor());
        int padding = 30;
        this.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
        this.setLayout(new BorderLayout());

        this.add(topBar, BorderLayout.NORTH);
        this.add(placeholder, BorderLayout.CENTER);

        this.updateUI();
    }

    private void showWaitPanel() {
        this.removeAll();
        this.setLayout(new BorderLayout());
        WaitPanel wp = new WaitPanel("Drawing pedigree");
        this.add(wp, BorderLayout.CENTER);
        this.updateUI();
    }

    public void setFamilyName(String familyID) {
        familyName = familyID;
        if (familyNameLabel != null) {
            familyNameLabel.setText(familyID);
        }
    }

    

    public static void main(String[] argv) {
        JFrame f = new JFrame();
        PedigreeCanvas pc = new PedigreeCanvas();
        pc.showPedigree(new File("/Users/mfiume/.medsavant/tmp/pedigree1.csv"), 1);
        f.add(pc);
        f.pack();
        f.setVisible(true);
    }

}
