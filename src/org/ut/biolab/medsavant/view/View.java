/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view;

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
import org.ut.biolab.medsavant.util.ViewUtil;
import org.ut.biolab.medsavant.view.subview.PatientSubView;
import org.ut.biolab.medsavant.view.subview.SubView;
import org.ut.biolab.medsavant.view.subview.VariantSubView;

/**
 *
 * @author mfiume
 */
public class View extends JPanel {

    List<String> subViewNames;
    private JPanel viewContainer;
    private CardLayout viewContainerLayout;
    private JPanel bannerContainer;
    private CardLayout bannerContainerLayout;
    private JPanel menuPanel;
    private JPanel crossViewPanel;
    private ButtonGroup subViewButtonGroup;

    private static final String DEFAULT_SUBVIEW = "Variant";

    public View() {
        this.setBackground(Color.darkGray);
        init();
    }

    private void init() {
        subViewNames = new ArrayList<String>();
        this.setLayout(new BorderLayout());
        initMenu();
        initBannerContainer();
        initViewContainer();
        initViews();
        setSubView(DEFAULT_SUBVIEW);
    }

    private void addSubView(final SubView view) {

        viewContainer.add(view.getView(), view.getName());
        bannerContainer.add(view.getBanner(), view.getName());
        subViewNames.add(view.getName());

        JRadioButton switchCardButton = new JRadioButton(view.getName());

        if (view.getName().equals(DEFAULT_SUBVIEW)) { switchCardButton.setSelected(true); }
        
        switchCardButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setSubView(view.getName());
            }

        });

        subViewButtonGroup.add(switchCardButton);

        crossViewPanel.add(ViewUtil.clear(switchCardButton));
    }

    private void initViewContainer() {
        viewContainer = new JPanel();
        viewContainerLayout = new CardLayout();
        viewContainer.setLayout(viewContainerLayout);
        this.add(viewContainer, BorderLayout.CENTER);
    }

    private void initViews() {
        addSubView(new PatientSubView());
        addSubView(new VariantSubView());
    }

    private void setSubView(String subViewName) {
        viewContainerLayout.show(viewContainer, subViewName);
        bannerContainerLayout.show(bannerContainer, subViewName);
    }

    private void initBannerContainer() {
        bannerContainer = ViewUtil.createClearPanel();
        bannerContainerLayout = new CardLayout();
        bannerContainer.setLayout(bannerContainerLayout);
        menuPanel.add(bannerContainer);

        subViewButtonGroup = new ButtonGroup();
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

        crossViewPanel = ViewUtil.createClearPanel();
        crossViewPanel.setLayout(new BoxLayout(crossViewPanel,BoxLayout.X_AXIS));
        menuPanel.add(crossViewPanel);
        
        this.add(menuPanel, BorderLayout.NORTH);
    }

    
}
