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
package org.ut.biolab.medsavant.client.app.page;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.io.FileUtils;
import org.ut.biolab.medsavant.client.plugin.AppDescriptor;
import org.ut.biolab.medsavant.client.app.api.AppInfoFetcher;
import org.ut.biolab.medsavant.client.app.AppInfo;
import org.ut.biolab.medsavant.client.app.AppInstallUtils;
import org.ut.biolab.medsavant.client.app.AppStorePage;
import org.ut.biolab.medsavant.client.view.component.LazyPanel;
import org.ut.biolab.medsavant.client.view.util.StandardAppContainer;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.client.app.api.AppInstaller;
import org.ut.biolab.medsavant.client.settings.DirectorySettings;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.app.AppDirectory;
import org.ut.biolab.medsavant.client.view.notify.Notification;


/**
 * Landing page for the App Store.
 * 
 * @author mfiume, rammar
 */
public class AppStoreLandingPage implements AppStorePage {

    private final AppStoreInstalledPage installedPage;
    private final AppInfoFetcher fetcher;
    private JPanel appListView;
    private final JTabbedPane parent;
    private final AppInstaller installer;

    public AppStoreLandingPage(AppInfoFetcher fetcher, AppInstaller installer, JTabbedPane parent, AppStoreInstalledPage installedPage) {
        this.fetcher = fetcher;
        this.installer = installer;
        this.parent = parent;
        this.installedPage = installedPage;
    }

    @Override
    public String getName() {
        return "Apps";
    }

    @Override
    public LazyPanel getView() {
        LazyPanel p = new LazyPanel() {

            @Override
            public void viewDidLoad() {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final List<AppInfo> appInfo = fetcher.fetchApplicationInformation(null);
                            SwingUtilities.invokeAndWait(new Runnable() {
                                @Override
                                public void run() {
                                    setAppInfo(appInfo);
                                }
                            });
                        } catch (Exception ex) {
                            Logger.getLogger(AppStoreLandingPage.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
                t.start();
            }

            @Override
            public void viewDidUnload() {
            }

        };

        p.setOpaque(false);
        p.setLayout(new BorderLayout());
        
        JPanel container = ViewUtil.getClearPanel();
        container.setLayout(new MigLayout("fillx,insets 0"));

        appListView = ViewUtil.getClearPanel();
        appListView.setLayout(new MigLayout("wrap, fillx, insets 0"));
        
        JLabel titleLabel = ViewUtil.getLargeSerifLabel("Available Apps");
        
		JButton appFromFile= new JButton("Install app from file...");
		appFromFile.addActionListener(getAppFromFileAL());
				
        container.add(titleLabel,"wrap");
		container.add(appFromFile, "wrap");
        container.add(appListView,"growx 1.0");
        
        JPanel fixedWidth = ViewUtil.getDefaultFixedWidthPanel(container);
        
        StandardAppContainer sac = new StandardAppContainer(fixedWidth, true);
        p.add(sac,BorderLayout.CENTER);
        sac.setBackground(ViewUtil.getLightGrayBackgroundColor());

        return p;
    }

    private synchronized void setAppInfo(List<AppInfo> info) {
        appListView.removeAll();

        Set<AppInfo> installRegistry = installer.getInstallRegistry();

        for (AppInfo i : info) {
            boolean installedAlready = installRegistry.contains(i);
            boolean canUpdate = false;
            if (installedAlready) {
                AppInfo installedApp = AppInstallUtils.getAppWithName(installer, i.getName());
                if (new AppDescriptor.AppVersion(i.getVersion()).isNewerThan(new AppDescriptor.AppVersion(installedApp.getVersion()))) {
                    canUpdate = true;
                }
            }
            AppInfoFlowView infoBox = new AppInfoFlowView(i, parent, installedPage, installedAlready, canUpdate);
            appListView.add(infoBox, "width 100%");
        }

        appListView.updateUI();
    }
	
	
	/**
	 * Action when the app from file button is selected.
	 * @return The ActionListener
	 */
	private ActionListener getAppFromFileAL() {
		ActionListener outputAL= new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {				
				// Get the user's jar file
				final JFileChooser chooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("JAR file", "jar");
				chooser.setFileFilter(filter);
				int chooserValue= chooser.showOpenDialog(MedSavantFrame.getInstance());
				
				// only copy the file if the approve button ("ok" rather than "cancel") has been clicked
				if(chooserValue != JFileChooser.APPROVE_OPTION) {
					return;
				}

				File chooserFile= new File(chooser.getSelectedFile().getPath());
				try {
					FileUtils.copyFileToDirectory(chooserFile, new File(
						DirectorySettings.getPluginsDirectory().getAbsolutePath()));
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}

				// Restart MedSavant
				Notification n = new Notification();
				n.setIcon(AppDirectory.getAppStore().getIcon());
				n.setName("App installed");
				n.setDescription("Restart to complete installation");
				MedSavantFrame.getInstance().showNotification(n);
				n.setAction("Restart", new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						MedSavantFrame.getInstance().requestLogoutAndRestart();
					}
				});
			}
		};
		
		return outputAL;
	}
	
}
