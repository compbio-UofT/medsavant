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
package org.ut.biolab.medsavant.app.google;

import java.awt.BorderLayout;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.ut.biolab.medsavant.client.view.util.StandardFixableWidthAppPanel;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.shared.appapi.MedSavantDashboardApp;

/**
 * Demonstration plugin to show how to do a simple panel.
 *
 * @author mfiume
 */
public class GoogleGenomicsApp extends MedSavantDashboardApp {

    private JPanel content;
    
    private static final String iconroot = "/icons/";
    public static ImageIcon icon = getIcon(iconroot + "icon.png");
    private boolean initialized;
    
    public GoogleGenomicsApp() {
        initialized = false;
        content = new JPanel();
    }

    @Override
    public JPanel getContent() {
        return content;
    }

    /**
     * Title which will appear on plugin's tab in Savant user interface.
     */
    @Override
    public String getTitle() {
        return "Google Genomics";
    }

    @Override
    public void viewDidLoad() {
        initView();
    }

    @Override
    public void viewDidUnload() {
    }
    
    @Override
    public ImageIcon getIcon() {              
        return icon;
    }
    
    public static ImageIcon getIcon(String resourcePath) {
        return new ImageIcon(GoogleGenomicsApp.class.getResource(resourcePath));
    }

    private JPanel getSettingsTab() {
        StandardFixableWidthAppPanel p = new StandardFixableWidthAppPanel("Settings");
        
        p.addBlock("Authentication");
        
        return p;
    }

    private void initView() {
        if (!initialized) {
            content.setLayout(new BorderLayout());
            
            JTabbedPane tabs = ViewUtil.getMSTabedPane();
            tabs.add("Reads", new ReadsView());
            tabs.add("Settings", getSettingsTab());
            
            content.add(tabs,BorderLayout.CENTER);
        }
        initialized = true;
    }
}
