/*
 * Copyright (C) 2014 University of Toronto, Computational Biology Lab.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.ut.biolab.medsavant.client.view.splash;

import java.util.List;
import org.ut.biolab.medsavant.client.controller.ServerController;
import org.ut.biolab.medsavant.client.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;

/**
 *
 * @author mfiume
 */
public class ServerDetailedListEditor extends DetailedListEditor {

    private SplashServerManagementComponent serverManager;

    public ServerDetailedListEditor(SplashServerManagementComponent serverManager) {
        this.serverManager = serverManager;
    }

    @Override
    public boolean doesImplementAdding() {
        return true;
    }

    @Override
    public void addItems() {
        serverManager.addServerAndEdit();
    }

    @Override
    public boolean doesImplementDeleting() {
        return true;
    }

    public void deleteItems(List<Object[]> items) {

        for (Object[] o : items) {
            MedSavantServerInfo server = (MedSavantServerInfo) o[1];
            int result = DialogUtils.askYesNo("Remove Server", String.format("Really remove %s?", server.getNickname()));
            if (result == DialogUtils.YES) {

                ServerController.getInstance().removeServer(server);
            }
        }
    }
}
