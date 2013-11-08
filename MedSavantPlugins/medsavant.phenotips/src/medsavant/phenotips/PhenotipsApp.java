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
package medsavant.phenotips;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.api.MedSavantClinicApp;

/**
 * @author mfiume
 */
public class PhenotipsApp extends MedSavantClinicApp {

    private JPanel fmp;

    @Override
    public JPanel getContent() {
        if (fmp == null) {
            fmp = new JPanel();
        }
        return fmp;
    }

    /**
     * Title which will appear on plugin's tab in Savant user interface.
     */
    @Override
    public String getTitle() {
        return "Phenotips";
    }

    @Override
    public void viewDidLoad() {
    }

    @Override
    public void viewDidUnload() {
    }

    private static final String iconroot = "/medsavant/phenotips/icon/";

    @Override
    public ImageIcon getIcon() {
        return getIcon(iconroot + "icon.png");
    }

    public ImageIcon getIcon(String resourcePath) {
        return new ImageIcon(getClass().getResource(resourcePath));
    }
}
