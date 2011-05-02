/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view;

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
import org.ut.biolab.medsavant.view.subview.LibraryPage;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.subview.PatientPage;
import org.ut.biolab.medsavant.view.subview.Page;
import org.ut.biolab.medsavant.view.subview.VariantPage;

/**
 *
 * @author mfiume
 */
public class View extends JPanel {

    private JPanel bannerContainer;
    private CardLayout bannerContainerLayout;
    private JPanel menuPanel;

    private static final String DEFAULT_SUBVIEW = "Variant";
    private SplitView splitView;

    public View() {
        this.setBackground(Color.darkGray);
        init();
    }

    private void init() {
        this.setLayout(new BorderLayout());
        initMenu();
        initBannerContainer();
        initViewContainer();
        initViews();
        setSubView(DEFAULT_SUBVIEW);
    }

    private void addSubView(final Page view) {
        splitView.addSubsection(view.getName(), view.getView());
        bannerContainer.add(view.getBanner(), view.getName());
    }

    private void initViewContainer() {
        splitView = new SplitView();
        this.add(splitView, BorderLayout.CENTER);
    }

    private void initViews() {

        JPanel red = new JPanel(); red.setBackground(Color.red);
        JPanel green = new JPanel(); green.setBackground(Color.green);

        splitView.addSection("Library");
        splitView.addSubsection("Patients",new JPanel());
        splitView.addSubsection("Genes",red);
        splitView.addSubsection("Pathways",green);
        addSubView(new LibraryVariantsPage());
        splitView.addSection("Search");
        addSubView(new VariantPage());
    }

    private void setSubView(String subViewName) {
        bannerContainerLayout.show(bannerContainer, subViewName);
    }

    private void initBannerContainer() {
        bannerContainer = ViewUtil.createClearPanel();
        bannerContainerLayout = new CardLayout();
        bannerContainer.setLayout(bannerContainerLayout);
        menuPanel.add(bannerContainer);
    }

    private void initMenu() {
        menuPanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                GradientPaint p = new GradientPaint(0,0,Color.white,0,40,Color.lightGray);
                ((Graphics2D)g).setPaint(p);
                g.fillRect(0, 0, menuPanel.getWidth(), menuPanel.getHeight());
            }
        };
        menuPanel.setLayout(new BoxLayout(menuPanel,BoxLayout.Y_AXIS));
        
        this.add(menuPanel, BorderLayout.NORTH);
    }
}
