/*
 *    Copyright 2011 University of Toronto
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

package medsavant.wikipathways.app;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.Border;

import com.healthmarketscience.sqlbuilder.Condition;
import java.awt.Color;

import org.ut.biolab.medsavant.client.view.MedSavantFrame;

/**
 * Object which holds the UI associated with a given instance of the filter.
 *
 * @author tarkvara
 */
public class FilterInstance {
    private PathwaysTab pathwaysTab;
    private JLabel appliedLabel;
    private JLabel currentLabel;
    private JButton applyButton;
    private JButton removeButton;
    private int queryID;
    private boolean filterApplied = false;
    private String pathwayString;
    private final Object lock = new Object();

    /**
     * Create the user-interface which appears within the panel.
     *
     * @param panel provided by MedSavant to host our plugin
     */
    FilterInstance(JPanel panel, int queryID) {
        this.queryID = queryID;
        pathwaysTab = new PathwaysTab();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JButton chooseButton = new JButton("Choose Pathway");
        chooseButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                pathwaysTab.setVisible(true);
            }
        });
        chooseButton.setPreferredSize(new Dimension(140,22));
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
        p1.add(chooseButton);
        p1.add(Box.createHorizontalGlue());
        panel.add(p1);

        panel.add(Box.createRigidArea(new Dimension(5,5)));

        appliedLabel = new JLabel("<HTML>Applied Filter: (none)<BR>Genomic Regions: 0</HTML>");
        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));
        p2.add(appliedLabel);
        p2.add(Box.createHorizontalGlue());
        Border paddingBorder1 = BorderFactory.createEmptyBorder(4,4,4,4);
        Border border1 = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        p2.setBorder(BorderFactory.createCompoundBorder(border1,paddingBorder1));
        panel.add(p2);

        panel.add(Box.createRigidArea(new Dimension(5,5)));

        currentLabel = new JLabel("Current Filter: (none)");
        JPanel p3 = new JPanel();
        p3.setLayout(new BoxLayout(p3, BoxLayout.X_AXIS));
        p3.add(currentLabel);
        p3.add(Box.createHorizontalGlue());
        Border paddingBorder2 = BorderFactory.createEmptyBorder(4,4,4,4);
        Border border2 = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        p3.setBorder(BorderFactory.createCompoundBorder(border2,paddingBorder2));
        panel.add(p3);

        panel.add(Box.createRigidArea(new Dimension(5,5)));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        removeButton = new JButton("Remove Filter");
        removeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                removeFilter();
            }
        });
        removeButton.setPreferredSize(new Dimension(140,22));
        removeButton.setEnabled(false);
        buttonPanel.add(removeButton);

        applyButton = new JButton("Apply Current Filter");

        applyButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                apply();
            }
        });
        applyButton.setPreferredSize(new Dimension(140,22));
        applyButton.setEnabled(false);
        buttonPanel.add(applyButton);
        buttonPanel.add(Box.createHorizontalGlue());

        panel.add(buttonPanel);
    }

    public void setAppliedFilter(String s, int numGenes){
        appliedLabel.setText("<HTML>Applied Filter: " + s + "<BR> Genomic Regions: " + numGenes +"</HTML>");
        applyButton.setEnabled(true);
    }

    public void setCurrentFilter(String s){
        currentLabel.setText("Current Filter: " + s);
        applyButton.setEnabled(true);

        //someone may be waiting on lock
        synchronized(lock){
            lock.notifyAll();
        }
    }

    private void removeFilter(){
        //ProjectCont.removeFilter("medsavant.pathways", queryID);
        setAppliedFilter("(none)", 0);
        applyButton.setEnabled(true);
        removeButton.setEnabled(false);
        setFilterApplied(false);
    }

    public void addFilter(Condition[] conditions, String pathwayString, int size) {
        //ProjectUtils.addFilterConditions(conditions, "WikiPathways", "medsavant.pathways", queryID);
        setAppliedFilter(pathwayString, size);
        setFilterApplied(true);
        this.pathwayString = pathwayString;
    }

    public void enableApply(boolean enable){
        applyButton.setEnabled(enable);
    }

    public void apply(){
        pathwaysTab.applyFilter();
        pathwaysTab.setVisible(false);
        applyButton.setEnabled(false);
        removeButton.setEnabled(true);
    }

    /**
     * Called when MedSavant has removed this filter.
     */
    public void cleanup() {
        pathwaysTab.setVisible(false);
        pathwaysTab = null;
        System.out.println("Pretending to clean up " + queryID);
    }

    public boolean isFilterApplied(){
        return filterApplied;
    }

    public void setFilterApplied(boolean set){
        filterApplied = set;
    }

    public String getPathwayName(){
        return pathwayString;
    }

    public void goToPathwayAndApply(final String pathwayId){

        final JDialog d = new JDialog(MedSavantFrame.getInstance(), "WikiPathways - Getting pathway data. Please wait.", true);
        Thread t = new Thread(){

            @Override
            public void run(){
                pathwaysTab.getById(pathwayId);
                d.setVisible(true);
                synchronized(lock){
                    try {
                        lock.wait();
                    } catch (InterruptedException ex) {}
                }
                d.dispose();
                apply();
            }
        };
        t.start();
        d.setVisible(true);
    }

}
