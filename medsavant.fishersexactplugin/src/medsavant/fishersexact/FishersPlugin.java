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

package medsavant.fishersexact;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.ut.biolab.medsavant.api.MedSavantSectionPlugin;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.listener.ProjectListener;
import org.ut.biolab.medsavant.listener.ReferenceListener;


/**
 *
 * @author Andrew
 */
public class FishersPlugin extends MedSavantSectionPlugin implements ReferenceListener, ProjectListener {
    
    private JPanel panel;
    private boolean update = true;
    private boolean isLoaded = false;
    
    /**
     * Create the user-interface which appears within the panel.
     *
     * @param panel provided by MedSavant
     */
    @Override
    public void init(JPanel panel) {
        this.panel = panel;
        ReferenceController.getInstance().addReferenceListener(this);
        ProjectController.getInstance().addProjectListener(this);
    }
    
    public void update(){
        
        panel.removeAll();
        
        if(!ReferenceController.getInstance().isReferenceSet()){
            panel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            panel.add(new JLabel("<html><i>Choose a project and reference table first.</i></html>"), gbc);
        } else {
            panel.setLayout(new BorderLayout());
            JScrollPane scroll = new JScrollPane();
            scroll.getViewport().add(new FishersPanel());
            panel.add(scroll, BorderLayout.CENTER); 
            update = false;
        }
    }

    /**
     * Title which will appear on plugin's tab in Savant user interface.
     */
    @Override
    public String getTitle() {
        return "Fisher's Exact Test";
    }

    @Override
    public void viewDidLoad() {
        isLoaded = true;
        if(update) update();
    }

    @Override
    public void viewDidUnload() {
        isLoaded = false;
    }

    @Override
    public void referenceAdded(String name) {}

    @Override
    public void referenceRemoved(String name) {}

    @Override
    public void referenceChanged(String prnameojectName) {
        update = true;
    }

    @Override
    public void projectAdded(String projectName) {}

    @Override
    public void projectRemoved(String projectName) {}

    @Override
    public void projectChanged(String projectName) {
        update = true;
        if(isLoaded) update();
    }

    @Override
    public void projectTableRemoved(int projid, int refid) {}
}
