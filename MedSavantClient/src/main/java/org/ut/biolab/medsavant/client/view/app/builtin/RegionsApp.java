package org.ut.biolab.medsavant.client.view.app.builtin;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.region.RegionController;
import org.ut.biolab.medsavant.client.region.RegionDetailedListEditor;
import org.ut.biolab.medsavant.client.region.RegionDetailedView;
import org.ut.biolab.medsavant.client.view.dashboard.LaunchableApp;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.list.SimpleDetailedListModel;
import org.ut.biolab.medsavant.client.view.list.SplitScreenView;
import org.ut.biolab.medsavant.shared.model.RegionSet;

/**
 *
 * @author mfiume
 */
public class RegionsApp implements LaunchableApp {

    private SplitScreenView view;

    public RegionsApp() {
    }

    @Override
    public JPanel getView() {
        return view;
    }

    @Override
    public void viewWillUnload() {
    }

    @Override
    public void viewWillLoad() {
        initView();

    }

    @Override
    public void viewDidUnload() {
    }

    @Override
    public void viewDidLoad() {
    }

    private void initView() {
        try {
            view = new SplitScreenView(
                    new SimpleDetailedListModel("Gene List") {
                        @Override
                        public RegionSet[] getData() throws Exception {
                            return RegionController.getInstance().getRegionSets().toArray(new RegionSet[0]);
                        }
                    },
                    new RegionDetailedView("Regions"),
                    new RegionDetailedListEditor());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public ImageIcon getIcon() {
        return IconFactory.getInstance().getIcon(IconFactory.StandardIcon.APP_REGIONS);
    }

    @Override
    public String getName() {
        return "Regions";
    }
    
    @Override
    public void didLogout() {
    }

    @Override
    public void didLogin() {
    }
}
