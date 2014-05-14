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
package org.ut.biolab.medsavant.client.view.login;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.controller.ServerController;
import org.ut.biolab.medsavant.client.view.component.NiceMenu;
import org.ut.biolab.medsavant.client.view.list.DetailedListModel;
import org.ut.biolab.medsavant.client.view.list.SplitScreenView;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class SplashServerManagementComponent extends JPanel implements Listener<ServerController> {

    private NiceMenu topMenu;

    private SplitScreenView serverListScreen;
    private ServerDetailedView serverDetailView;
    private SplashFrame splash;

    public SplashServerManagementComponent(SplashFrame splash) {
        this.splash = splash;
        initUI();
        ServerController.getInstance().addListener(this);
    }

    public SplitScreenView getServerList() {
        return serverListScreen;
    }

    private void initUI() {

        this.setBackground(Color.white);
        this.setLayout(new BorderLayout());

        /**
         * MENUS
         */
        topMenu = new NiceMenu();
        topMenu.setTitle("Server Management");
        this.add(topMenu, BorderLayout.NORTH);

        /**
         * CENTRAL CONTAINER
         */
        JPanel container = ViewUtil.getClearPanel();
        container.setLayout(new MigLayout("insets 0, fillx, filly, center, hidemode 3"));
        this.add(container, BorderLayout.CENTER);

        /**
         * NORMAL FORM
         */
        serverDetailView = new ServerDetailedView(splash, this);
        final ServerDetailedListEditor serverDetailListEditor = new ServerDetailedListEditor(this);

        serverListScreen = new SplitScreenView(new DetailedListModel() {

            @Override
            public Object[][] getList(int limit) throws Exception {

                List<MedSavantServerInfo> servers = ServerController.getInstance().getServers();

                Object[][] results = new Object[servers.size()][];
                int counter = 0;
                for (MedSavantServerInfo server : servers) {
                    results[counter++] = new Object[]{server.getNickname(), server};
                }

                return results;
            }

            @Override
            public String[] getColumnNames() {
                return new String[]{"Server", "ServerObject"};
            }

            @Override
            public Class[] getColumnClasses() {
                return new Class[]{String.class, MedSavantServerInfo.class};
            }

            @Override
            public int[] getHiddenColumns() {
                return new int[0];
            }

        }, serverDetailView, serverDetailListEditor);

        container.add(serverListScreen, "width 100%, height 100%");

    }

    public void addServerAndEdit() {
        String baseName = "Untitled Server";
        String newName = baseName;
        int counter = 1;
        while (ServerController.getInstance().isServerNamed(newName)) {
            newName = String.format("%s (%d)", baseName, ++counter);
        }

        MedSavantServerInfo server = new MedSavantServerInfo();
        server.setNickname(newName);

        ServerController.getInstance().addServer(server);
        setSelectedServer(server);
    }

    private void setSelectedServer(MedSavantServerInfo server) {
        serverListScreen.selectItemWithKey(server.getNickname());
    }

    @Override
    public void handleEvent(ServerController event) {
        refreshList();
    }

    private void refreshList() {
        serverListScreen.refresh();
    }
}
