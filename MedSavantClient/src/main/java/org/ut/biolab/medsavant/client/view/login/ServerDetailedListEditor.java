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
