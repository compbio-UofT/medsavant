package org.ut.biolab.mfiume.app.page;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import org.ut.biolab.mfiume.app.AppInfo;
import org.ut.biolab.mfiume.app.api.AppInstaller;
import org.ut.biolab.mfiume.app.jAppStore;

/**
 *
 * @author mfiume
 */
class AppInstallInstalledView extends JPanel {

    private final AppInfo appInfo;
    private final AppInstaller installer;
    private final AppStoreInstalledPage installPage;

    public AppInstallInstalledView(AppInfo i, AppInstaller installer, AppStoreInstalledPage parent) {
        this.appInfo = i;
        this.installer = installer;
        this.installPage = parent;

        this.setBackground(Color.white);
        int padding = 10;
        this.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        this.add(new JLabel("<html><b>" + i.getName() + "</b> " + i.getVersion() + " <small> by " + i.getAuthor() + "</small></html>"));
        this.add(Box.createHorizontalGlue());
        this.add(getUninstallButton());

        jAppStore.wrapComponentWithLineBorder(this);
    }

    public static JButton getSoftButton(String string) {
        JButton b = new JButton(string);
        b.putClientProperty("JButton.buttonType", "segmentedRoundRect");
        b.putClientProperty("JButton.segmentPosition", "only");
        b.setFocusable(false);
        b.putClientProperty("JComponent.sizeVariant", "small");
        return b;
    }

    private JButton getUninstallButton() {
        JButton b = getSoftButton("Uninstall");
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean success = installer.uninstallApp(appInfo);
                        if (success) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    installPage.updateInstalledList();
                                }
                            });
                        }
                    }
                });
                t.start();
            }
        });
        return b;

    }
}
