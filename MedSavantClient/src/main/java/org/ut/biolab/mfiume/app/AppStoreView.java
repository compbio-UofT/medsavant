package org.ut.biolab.mfiume.app;

import org.ut.biolab.mfiume.app.api.AppInfoFetcher;
import org.ut.biolab.mfiume.app.page.AppStoreLandingPage;
import org.ut.biolab.mfiume.app.page.AppStoreInstalledPage;
import com.explodingpixels.macwidgets.MacUtils;
import com.explodingpixels.widgets.WindowUtils;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JDialog;
import javax.swing.JFrame;
import org.ut.biolab.mfiume.app.api.AppInstaller;

/**
 *
 * @author mfiume
 */
class AppStoreView extends JDialog {

    private final AppInfoFetcher fetcher;
    private final AppInstaller installer;

    public AppStoreView(String title, AppInfoFetcher fetcher, AppInstaller installer) {
        //MacUtils.makeWindowLeopardStyle(this.getRootPane());
        //WindowUtils.createAndInstallRepaintWindowFocusListener(this);
        this.setTitle(title);
        this.setModal(true);
        //this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        this.setMinimumSize(new Dimension(600, 600));
        this.setLocationRelativeTo(null);
        this.fetcher = fetcher;
        this.installer = installer;
        initView();
        this.setLocationRelativeTo(null);
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
    }

    private void initView() {

        AppStoreViewManager avm = new AppStoreViewManager();

        AppStoreInstalledPage installedAppsPage = new AppStoreInstalledPage(installer);
        AppStoreLandingPage landingPage = new AppStoreLandingPage(fetcher, avm, installedAppsPage);

        AppStorePage[] pages = new AppStorePage[]{landingPage, installedAppsPage};
        AppStoreMenu menu = new AppStoreMenu(pages, avm);
        avm.setMenu(menu);

        this.setLayout(new BorderLayout());

        this.add(menu, BorderLayout.NORTH);
        this.add(avm.getView(), BorderLayout.CENTER);

        avm.switchToPage(landingPage);
    }
}
