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
package org.ut.biolab.medsavant.client.app;

import java.awt.Color;
import java.awt.Container;
import org.ut.biolab.medsavant.client.app.MedSavantAppInstaller;
import org.ut.biolab.medsavant.client.app.api.AppInfoFetcher;
import org.ut.biolab.medsavant.client.app.MedSavantAppFetcher;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import org.ut.biolab.medsavant.client.app.api.AppInstaller;


/**
 *
 * @author mfiume
 */
public class jAppStore {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        MedSavantAppFetcher maf = new MedSavantAppFetcher();
        MedSavantAppInstaller mai = new MedSavantAppInstaller();

        jAppStore as = new jAppStore("MedSavant App Store",maf,mai);
        as.showStore();
    }
    private final AppStoreView view;
    private final AppInfoFetcher fetcher;

    public jAppStore(String title, AppInfoFetcher fetcher, AppInstaller installer) {
        this.view = new AppStoreView(title,fetcher,installer);
        this.fetcher = fetcher;
    }

    public void showStore() {
        this.view.setVisible(true);
    }
    
    public Container getContentPane() {
        return this.view.getContentPane();
    }

    public static void wrapComponentWithLineBorder(JComponent c) {
        c.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(235,235,235), 1), c.getBorder()));
    }

}
