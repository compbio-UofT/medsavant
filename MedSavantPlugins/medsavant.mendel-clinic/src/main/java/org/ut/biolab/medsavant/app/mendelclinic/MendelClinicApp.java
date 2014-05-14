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
package org.ut.biolab.medsavant.app.mendelclinic;

import edu.toronto.cs.medsavant.medsavant.app.api.appcomm.AppCommHandler;
import edu.toronto.cs.medsavant.medsavant.app.api.appcomm.AppCommRegistry;
import edu.toronto.cs.medsavant.medsavant.app.api.appcomm.VariantResultComm;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.app.mendelclinic.view.MendelPanel;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.app.DashboardSectionFactory;
import org.ut.biolab.medsavant.shared.appapi.MedSavantDashboardApp;

/**
 * Demonstration plugin to show how to do a simple panel.
 *
 * @author tarkvara
 */
public class MendelClinicApp extends MedSavantDashboardApp implements AppCommHandler<VariantResultComm> {

    private MendelPanel fmp;
    
    private static final String iconroot = "/icons/";
    public static ImageIcon icon = getIcon(iconroot + "mendel-icon.png");
    
    public MendelClinicApp() {
        AppCommRegistry.getInstance().registerHandler(this, VariantResultComm.class);
    }

    @Override
    public JPanel getContent() {
        if (fmp == null) {
            fmp = new MendelPanel();
        }
        return fmp.getView();
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
        fmp.refresh();
    }

    @Override
    public void viewDidUnload() {
    }
    
    @Override
    public ImageIcon getIcon() {              
        return icon;
    }
    
    public static ImageIcon getIcon(String resourcePath) {
        return new ImageIcon(MendelClinicApp.class.getResource(resourcePath));
    }

    @Override
    public String getHandlerName() {
        return this.getTitle();
    }

    @Override
    public ImageIcon getHandlerIcon() {
        return this.getIcon();
    }

    @Override
    public void handleCommEvent(VariantResultComm value) {
        // horrible hack
        MedSavantFrame.getInstance().getDashboard().launchApp(this.getTitle());
    }
}
