/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.mfiume.app;

import java.awt.Color;
import java.awt.Container;
import org.ut.biolab.medsavant.client.app.MedSavantAppInstaller;
import org.ut.biolab.mfiume.app.api.AppInfoFetcher;
import org.ut.biolab.medsavant.client.app.MedSavantAppFetcher;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import org.ut.biolab.mfiume.app.api.AppInstaller;


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
