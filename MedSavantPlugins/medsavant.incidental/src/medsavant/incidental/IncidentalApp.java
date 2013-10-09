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
package medsavant.incidental;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.api.MedSavantClinicApp;


/**
 * Demonstration plugin to show how to do a simple panel.
 *
 * @author tarkvara
 */
public class IncidentalApp extends MedSavantClinicApp {

    private JPanel p;

    @Override
    public JPanel getContent() {
        if (p == null) {
            p = new JPanel();
            p.add(new JLabel("Incidentalome"));
        }
        return p;
    }

    /**
     * Title which will appear on plugin's tab in Savant user interface.
     */
    @Override
    public String getTitle() {
        return "Incidentalome";
    }

    @Override
    public void viewDidLoad() {
    }

    @Override
    public void viewDidUnload() {
    }

    private static final String iconroot = "/medsavant/incidental/icon/";

    @Override
    public ImageIcon getIcon() {
        return getIcon(iconroot + "icon.png");
    }

    public ImageIcon getIcon(String resourcePath) {
        return new ImageIcon(getClass().getResource(resourcePath));
    }
}
