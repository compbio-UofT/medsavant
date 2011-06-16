/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view;

import org.ut.biolab.medsavant.model.event.SectionChangedEvent;
import org.ut.biolab.medsavant.view.subview.LibraryVariantsPage;
import org.ut.biolab.medsavant.view.subview.LibraryVariantsPage;
import org.ut.biolab.medsavant.controller.FilterController;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import org.ut.biolab.medsavant.model.event.SectionChangedEventListener;
import org.ut.biolab.medsavant.view.subview.LibraryPage;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.subview.PatientPage;
import org.ut.biolab.medsavant.view.subview.Page;
import org.ut.biolab.medsavant.view.subview.ResultsPage;
import org.ut.biolab.medsavant.view.subview.VariantPage;

/**
 *
 * @author mfiume
 */
public class View extends JPanel implements SectionChangedEventListener {

    private static final String DEFAULT_SUBVIEW = "Variants";
    private SplitView splitView;

    public View() {
        this.setBackground(Color.darkGray);
        init();
    }

    private void init() {
        this.setLayout(new BorderLayout());
        initViewContainer();
        initViews();
    }

    private void addSubView(final Page view) {
        splitView.addSubsection(view.getName(), view);
    }

    private void initViewContainer() {
        splitView = new SplitView();
        splitView.addSectionChangedListener(this);
        this.add(splitView, BorderLayout.CENTER);
    }

    private void initViews() {

        splitView.addSection("Library");
        addSubView(new LibraryVariantsPage());
        splitView.addSection("Search");
        addSubView(new ResultsPage());
        splitView.setSubsection(DEFAULT_SUBVIEW);
    }

    public void sectionChangedEventReceived(SectionChangedEvent e) {
        //System.out.println("View received section changed to " + e.getSection());
    }
}
