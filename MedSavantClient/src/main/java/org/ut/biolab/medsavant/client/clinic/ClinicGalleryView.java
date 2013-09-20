package org.ut.biolab.medsavant.client.clinic;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.api.MedSavantClinicApp;
import org.ut.biolab.medsavant.client.plugin.AppController;
import org.ut.biolab.medsavant.client.plugin.MedSavantApp;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.images.ImagePanel;
import org.ut.biolab.medsavant.client.view.subview.SubSectionView;
import savant.plugin.PluginController;

/**
 *
 * @author mfiume
 */
public class ClinicGalleryView extends GalleryView {

    private final List<GalleryItem> galleryItems;

    public ClinicGalleryView() {
        super("","Clinic");

        galleryItems = initGalleryItems();

        setGalleryItems(galleryItems);
        setMenuHeroPanel(getHeroPanel());
    }

    private List<GalleryItem> initGalleryItems() {
        List<GalleryItem> items = new ArrayList<GalleryItem>();
        List<MedSavantApp> clinicApps = AppController.getInstance().getPluginsOfClass(MedSavantClinicApp.class);

        for (int i = 0; i < clinicApps.size(); i++) {
            MedSavantClinicApp app = (MedSavantClinicApp) clinicApps.get(i);
            items.add(new GalleryItem(app.getIcon(), app.getContent(),app.getTitle()));
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
