/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view;

import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.pane.FloorTabbedPane;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.model.Filter;

/**
 *
 * @author mfiume
 */
class FilterPanel extends JDialog {

    private ButtonPanel buttonPanel;
    private FloorTabbedPane filterViewTabbedPanel;
    private JButton applyButton;
    private final ArrayList<FilterView> filterViews;

    FilterPanel(Frame owner, String title) {
        super(owner,title,true);
        filterViews = new ArrayList<FilterView>();
        initGUI();
    }

    private void initGUI() {

        this.setLayout(new BorderLayout());

        buttonPanel = new ButtonPanel();
        buttonPanel.setSizeConstraint(ButtonPanel.NO_LESS_THAN);
        JButton button = new JButton("Cancel");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                closeWithoutApplyingFilter();
            }

        });
        buttonPanel.add(button, ButtonPanel.CANCEL_BUTTON);
        applyButton = new JButton("Apply");
        applyButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                applyCurrentFilterAndClose();
            }

        });
        setApplyButtonEnabled(false);
        buttonPanel.add(applyButton, ButtonPanel.AFFIRMATIVE_BUTTON);
        //button = new JButton("Help");
        //buttonPanel.add(button, ButtonPanel.HELP_BUTTON);

        this.add(this.buttonPanel,BorderLayout.SOUTH);

        filterViewTabbedPanel = new FloorTabbedPane();
        filterViewTabbedPanel.setPreferredSize(new Dimension(300,450));
        filterViewTabbedPanel.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateCurrentFilterViewIndex();
                
            }
        });

        this.add(this.filterViewTabbedPanel,BorderLayout.CENTER);

        this.pack();
    }

    private void updateCurrentFilterViewIndex() {
        if (filterViewTabbedPanel.getTabCount()-1 == 0) { return; }
        int currentViewIndex = filterViewTabbedPanel.getSelectedIndex();
        setApplyButtonEnabled(false);
    }

    public void addFilterViews(List<FilterView> filterViews) {
        for (FilterView view : filterViews) {
            addFilterView(view);
        }
    }

    private void addFilterView(FilterView view) {
        filterViews.add(view);
        filterViewTabbedPanel.addTab(view.getTitle(),view.getComponent());
    }

    private void setApplyButtonEnabled(boolean b) {
        this.applyButton.setEnabled(b);
    }

    private void applyCurrentFilterAndClose() {

        FilterView fv = filterViews.get(filterViewTabbedPanel.getSelectedIndex());
        System.out.println("Selected filter: " + fv.getTitle());
        Filter f = fv.getFilterGenerator().generateFilter();
        FilterController.addFilter(f);

        this.setVisible(false);
    }

    private void closeWithoutApplyingFilter() {
        this.setVisible(false);
    }

    void listenToComponent(final JRadioButton c) {
        c.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setApplyButtonEnabled(true);
            }

        });
    }


}
