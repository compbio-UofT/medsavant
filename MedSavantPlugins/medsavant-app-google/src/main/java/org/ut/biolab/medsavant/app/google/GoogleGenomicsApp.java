/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 * 
 * All rights reserved. No warranty, explicit or implicit, provided.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
 * FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.ut.biolab.medsavant.app.google;

import java.awt.BorderLayout;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.ut.biolab.medsavant.client.view.util.PathField;
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

    private void initView() {
        if (!initialized) {
            content.setLayout(new BorderLayout());
            
            ReadsView rv = new ReadsView();
            
            /*JTabbedPane tabs = ViewUtil.getMSTabedPane();
            
            
            
            tabs.add("Reads", rv);*/
            
            content.add(rv,BorderLayout.CENTER);
        }
        initialized = true;
    }
}
