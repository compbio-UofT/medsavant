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
package org.ut.biolab.medsavant.client.clinic;

import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.shared.appapi.MedSavantDashboardApp;
import org.ut.biolab.medsavant.client.plugin.AppController;
import org.ut.biolab.medsavant.shared.appapi.MedSavantApp;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.images.ImagePanel;

/**
 *
 * @author mfiume
 */
public class ClinicGalleryView extends GalleryView {

    private final List<GalleryItem> galleryItems;

    public ClinicGalleryView() {
        super("","Clinic","Clinic Apps are available on the MedSavant App Store");

        galleryItems = initGalleryItems();

        setGalleryItems(galleryItems);
    }

    private List<GalleryItem> initGalleryItems() {
        List<GalleryItem> items = new ArrayList<GalleryItem>();
        List<MedSavantApp> clinicApps = AppController.getInstance().getPluginsOfClass(MedSavantDashboardApp.class);

        for (int i = 0; i < clinicApps.size(); i++) {
            try {
                MedSavantDashboardApp app = (MedSavantDashboardApp) clinicApps.get(i);
                items.add(new GalleryItem(app.getIcon(), app.getContent(),app.getTitle()));
            } catch (Exception e) { e.printStackTrace(); }
        }

        return items;
    }

    private JPanel getHeroPanel() {
        ImageIcon heroPic = IconFactory.getInstance().getIcon("/org/ut/biolab/medsavant/client/view/images/icon/" + "clinic.png");

        int width = heroPic.getIconWidth();
        int height = heroPic.getIconHeight();
        ImagePanel ip = new ImagePanel(heroPic.getImage(),width,height);
        return ip;
    }

}
