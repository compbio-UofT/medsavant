/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import org.ut.biolab.medsavant.controller.ThreadController;
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
    private final String pageName;

    public OntologyPanelGenerator(String pageName) {
        this.pageName = pageName;
    }

    public String getName() {
        return "Ontology";
    }

    public JPanel getPanel() {
        if (panel == null) {
            panel = new OntologyPanel();
        } else {
            panel.update();
        }
        return panel;
    }

    //public void setUpdate(boolean update) {
    //}
    
    public void run(){
        if(panel != null)
            panel.update();
    }

    @Override
    public void setUpdateRequired(boolean required) {
        //
    }

    public class OntologyPanel extends JPanel {

        private final JPanel banner;
        private final JPanel content;
        private final JProgressBar progress;
        private final JComboBox options;
        private final HashMap<String, OntologySubPanel> map;
        private OntologySubPanel currentOntology;
//        private JButton applyButton;

        public OntologyPanel() {

            
            map = new HashMap<String,OntologySubPanel>();
            
            this.setLayout(new BorderLayout());
            banner = ViewUtil.getSubBannerPanel("Ontology");
            
            options = new JComboBox();

            content = new JPanel();
            content.setLayout(new BorderLayout());

            banner.add(options);           
            banner.add(ViewUtil.getMediumSeparator());        
            banner.add(Box.createHorizontalGlue());

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

            GOsubPanel gopanel = new GOsubPanel(this, pageName);
            //HPOsubPanel hpopanel = new HPOsubPanel(this, pageName);
            addOntologyAggregator(gopanel.getName(), gopanel);
            //addOntologyAggregator(hpopanel.getName(), hpopanel);
        }
        
        private void showOntology(String ontology) {
            OntologySubPanel o = map.get(ontology);
            
            if (currentOntology != null){
                currentOntology.stop();
                ThreadController.getInstance().cancelWorkers(pageName);
            }
                //currentOntology.setUpdate(false);
            
            //o.setUpdate(true);
            o.run();
            currentOntology = o;

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
        
        public void update(){
            if(currentOntology != null){
                currentOntology.run();
            }
        }
    }
    
}
