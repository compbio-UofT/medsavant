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

package medsavant.mendel;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.api.MedSavantSectionPlugin;
import medsavant.mendel.view.MendelPanel;

/**
 * Demonstration plugin to show how to do a simple panel.
 *
 * @author tarkvara
 */
public class MendelApp extends MedSavantSectionPlugin {
    private MendelPanel fmp;
    /**
     * Create the user-interface which appears within the panel.
     *
     * @param panel provided by MedSavant
     */
    @Override
    public void init(JPanel panel) {
        panel.setLayout(new BorderLayout());
        fmp = new MendelPanel();
        panel.add(fmp.getView(),BorderLayout.CENTER);
    }

    /**
     * Title which will appear on plugin's tab in Savant user interface.
     */
    @Override
    public String getTitle() {
        return "Mendel";
    }

    @Override
    public void viewDidLoad() {
        //do something
        fmp.refresh();
    }

    @Override
    public void viewDidUnload() {
        //do something
    }
}
