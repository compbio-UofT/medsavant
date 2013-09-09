package org.ut.biolab.mfiume.app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 *
 * @author mfiume
 */
public class AppStoreViewManager {
    private final JPanel content;
    private AppStorePage previousPage;
    private AppStoreMenu menu;

    public AppStoreViewManager() {
        this.content = new JPanel();
        content.setLayout(new BorderLayout());
        content.setBackground(Color.white);

        int padding = 25;
        content.setBorder(BorderFactory.createEmptyBorder(padding, SIDEPADDING, padding, SIDEPADDING));
    }

    public static final int SIDEPADDING = 50;

    public void switchToPage(AppStorePage page) {
        switchToPage(page,true);
    }

    public void switchToPage(AppStorePage page, boolean programmatically) {

        content.removeAll();

        if (previousPage != null) {
            previousPage.viewDidUnload();
        }

        content.add(page.getView(),BorderLayout.CENTER);
        content.updateUI();

        page.viewDidLoad();

        previousPage = page;

        if (menu != null && programmatically) {
            menu.pageChangedTo(page);
        }
    }

    protected Component getView() {
        return content;
    }

    void setMenu(AppStoreMenu menu) {
        this.menu = menu;
    }

}
