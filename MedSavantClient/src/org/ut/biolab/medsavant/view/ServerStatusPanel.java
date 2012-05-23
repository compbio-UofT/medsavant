/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.view;

import java.awt.Dimension;
import java.awt.Image;
import java.sql.Date;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.event.LoginEvent;
import org.ut.biolab.medsavant.model.event.LoginListener;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.images.ImagePanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
class ServerStatusPanel extends JPanel implements LoginListener {
    private static final Log LOG = LogFactory.getLog(ServerStatusPanel.class);

    private ServerStateChecker serverStateChecker;
    private final JPanel lastServerCheckInLabel;
    private final JLabel warningLabel;

    public ServerStatusPanel() {
        ViewUtil.setBoxXLayout(this);

        lastServerCheckInLabel = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(lastServerCheckInLabel);
        lastServerCheckInLabel.setPreferredSize(new Dimension(15, 15));
        this.add(lastServerCheckInLabel);

        this.add(ViewUtil.getSmallSeparator());

        warningLabel = new JLabel();
        this.add(warningLabel);

        LoginController.addLoginListener(this);

        setState(ServerState.NOT_CONNECTED);
    }

    @Override
    public void loginEvent(LoginEvent evt) {
        if (evt.isLoggedIn()) {
            if (serverStateChecker != null) {
                serverStateChecker.cancel(true);
            }
            serverStateChecker = new ServerStateChecker(this, UPDATE_INTERVAL);
            serverStateChecker.execute();
        } else {
            setState(ServerState.NOT_CONNECTED);
            setLastServerCheckIn(null);
        }
    }

    private synchronized void setState(ServerState serverState) {
        LOG.info("Server state is " + serverState);

        if (lastServerCheckInLabel != null) {
            ImageIcon im = null;
            switch (serverState) {
                case NOT_CONNECTED:
                    //im = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.WHITE);
                    warningLabel.setText("");
                    break;
                case SERVER_CONNECTED:
                    im = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.GREEN);
                    warningLabel.setText("Server Utility running");
                    break;
                case SERVER_IDLE:
                    im = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.ORANGE);
                    warningLabel.setText("Server Utility not running");
                    break;
                case SERVER_NEVER_CONNECTED:
                    im = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.RED);
                    warningLabel.setText("Server Utility not initialized");
                    break;
            }

            lastServerCheckInLabel.removeAll();

            if (im != null) {
                ImagePanel imagePanel = new ImagePanel(im.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH), 15, 15);
                lastServerCheckInLabel.add(imagePanel);
            }
        }

        this.updateUI();
    }
    private static int UPDATE_INTERVAL = 1000 * 60;

    private synchronized void setLastServerCheckIn(Date lastCheckIn) {
        if (lastServerCheckInLabel == null) {
            return;
        }
        String tip = "";
        if (lastCheckIn != null) {
            tip = "Last communication was: " + lastCheckIn.toLocaleString();
        }
        this.lastServerCheckInLabel.setToolTipText(tip);
        this.warningLabel.setToolTipText(tip);

        this.updateUI();
    }

    private enum ServerState {

        NOT_CONNECTED, SERVER_CONNECTED, SERVER_IDLE, SERVER_NEVER_CONNECTED
    };

    private static class ServerStateChecker extends SwingWorker {

        private final int interval;
        private final ServerStatusPanel serverStatusPanel;

        public ServerStateChecker(ServerStatusPanel panel, int UPDATE_INTERVAL) {
            this.interval = UPDATE_INTERVAL;
            this.serverStatusPanel = panel;
        }

        @Override
        protected Object doInBackground() throws Exception {

            boolean interrupted = false;
            while (!interrupted) {

                Date lastCheckIn = MedSavantClient.ServerLogQueryUtilAdapter.getDateOfLastServerLog(LoginController.sessionId);

                serverStatusPanel.setLastServerCheckIn(lastCheckIn);

                if (lastCheckIn == null) {
                    serverStatusPanel.setState(ServerState.SERVER_NEVER_CONNECTED);
                } else {

                    long elapsed = System.currentTimeMillis() - lastCheckIn.getTime();

                    if (elapsed > 1000 * 60 * 30) { //30 minutes
                        serverStatusPanel.setState(ServerState.SERVER_IDLE);
                    } else {
                        serverStatusPanel.setState(ServerState.SERVER_CONNECTED);
                    }
                }

                Thread.sleep(interval);

                if (Thread.interrupted()) {
                    interrupted = true;
                }
            }

            return null;
        }
    }
}
