/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import org.ut.biolab.medsavant.view.genetics.aggregates.AggregatePanelGenerator;
import org.ut.biolab.medsavant.view.genetics.aggregates.GOsubPanel;
import org.ut.biolab.medsavant.view.genetics.aggregates.HPOsubPanel;
import org.ut.biolab.medsavant.view.genetics.aggregates.OntologySubPanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class OntologyPanelGenerator implements AggregatePanelGenerator {
    
    private OntologyPanel panel;

    public OntologyPanelGenerator() {
    }

    public String getName() {
        return "Ontology";
    }

    public JPanel getPanel() {
        if (panel == null) {
            panel = new OntologyPanel();
        }
        return panel;
    }

    public void setUpdate(boolean update) {
    }

    public class OntologyPanel extends JPanel {

        private final JPanel banner;
        private final JPanel content;
        private final JProgressBar progress;
        private final JComboBox options;
        private final HashMap<String, OntologySubPanel> map;
        private OntologySubPanel currentOntology;
        private JButton applyButton;

        public OntologyPanel() {

            
            map = new HashMap<String,OntologySubPanel>();
            
            this.setLayout(new BorderLayout());
            banner = ViewUtil.getSubBannerPanel("Ontology");
            //banner.setLayout(new BoxLayout(banner,BoxLayout.X_AXIS));
            //banner.setBorder(ViewUtil.getMediumBorder());

            options = new JComboBox();

            content = new JPanel();
            content.setLayout(new BorderLayout());

            banner.add(options);
            
            /**
             * @author mfiume
             */
            /*
            banner.add(ViewUtil.getMediumSeparator());
            
            //banner.add(goButton);
            banner.add(Box.createHorizontalGlue());
             */
            
            /**
             * @author nnursimulu
             */
            banner.add(ViewUtil.getMediumSeparator());
            banner.add(Box.createHorizontalGlue());
            applyButton = new JButton("Apply");
            banner.add(applyButton);            
            banner.add(Box.createHorizontalGlue());
            banner.add(ViewUtil.getMediumSeparator());
            
            progress = new JProgressBar();
            progress.setStringPainted(true);
            
            banner.add(progress);
            

            this.add(banner, BorderLayout.NORTH);
            this.add(content, BorderLayout.CENTER);

            options.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    showOntology((String) options.getSelectedItem());
                }
            });

            GOsubPanel gopanel = new GOsubPanel(this);
            HPOsubPanel hpopanel = new HPOsubPanel(this);
            addOntologyAggregator(gopanel.getName(), gopanel);
            addOntologyAggregator(hpopanel.getName(), hpopanel);
        }
        
        // @author nnursimulu
        public JButton getApplyButton(){
            return applyButton;
        }

        private void showOntology(String ontology) {
            OntologySubPanel o = map.get(ontology);
            
            if (currentOntology != null)
                currentOntology.setUpdate(false);
            
            o.setUpdate(true);
            currentOntology = o;

            System.out.println("Showing ontology: " + ontology);
            this.content.removeAll();
            this.content.add(o,BorderLayout.CENTER);
            this.content.updateUI();
        }

        public void updateProgess(int value) {
            progress.setValue(value);
            progress.setString(value + "% done");
        }

        private void addOntologyAggregator(String name, OntologySubPanel os) {
            map.put(name,os);
            options.addItem(name);
        }
    }
    
}
