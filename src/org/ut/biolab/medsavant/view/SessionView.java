/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view;

import org.ut.biolab.medsavant.view.util.ComponentUtil;
import org.ut.biolab.medsavant.model.event.SectionChangedEvent;
import java.awt.Color;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.JTabbedPane;
import org.ut.biolab.medsavant.model.event.SectionChangedEventListener;
import org.ut.biolab.medsavant.view.annotations.AnnotationsSubView;
import org.ut.biolab.medsavant.view.genetics.GeneticsSubView;
import org.ut.biolab.medsavant.view.patients.PatientsSubView;
import org.ut.biolab.medsavant.view.subview.SubView;
import org.ut.biolab.medsavant.view.util.PaintUtil;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class SessionView extends JPanel implements SectionChangedEventListener {

    private JTabbedPane panes;

    //private static final String DEFAULT_SUBVIEW = "Variants";
   // private SplitView splitView;

    public SessionView() {
        //this.setBackground(ViewUtil.getDarkColor());
        init();
    }

    private void init() {
        this.setLayout(new BorderLayout());
        initViewContainer();
        initViews();
    }

    private void addSubView(SubView view) {
        panes.addTab(view.getName(), view);
        //splitView.addSubsection(view.getName(), view);
    }

    private void initViewContainer() {
        panes = ComponentUtil.getH1TabbedPane();
        panes.setBorder(ViewUtil.getSmallBorder());
        //panes.setFont(new Font("Arial", Font.BOLD, 12));
        this.add(panes, BorderLayout.CENTER);
        //splitView = new SplitView();
        //splitView.addSectionChangedListener(this);
        //this.add(splitView, BorderLayout.CENTER);
    }

    private void initViews() {

        //splitView.addSection("Library");
        //addSubView(new LibraryVariantsPage());
        //splitView.addSection("Search");
        //addSubView(new PatientsPage());
        addSubView(new PatientsSubView());
        addSubView(new GeneticsSubView());
        addSubView(new AnnotationsSubView());
        
        //addSubView(new AnnotationsPage());
        panes.setSelectedIndex(0);
        //splitView.setSubsection(DEFAULT_SUBVIEW);
    }

    public void sectionChangedEventReceived(SectionChangedEvent e) {
        //System.out.println("View received section changed to " + e.getSection());
    }
    
    public void paintComponent(Graphics g) {
        PaintUtil.paintSky(g, this);
    }
}
