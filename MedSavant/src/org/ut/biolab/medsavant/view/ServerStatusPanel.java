package org.ut.biolab.medsavant.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.sql.Date;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.db.util.query.ServerLogQueryUtil;
import org.ut.biolab.medsavant.log.ClientLogger;
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

    private ServerStateChecker serverStateChecker;
    private final JPanel lastServerCheckInLabel;
    private final JLabel warningLabel;

    public ServerStatusPanel() {
        ViewUtil.setBoxXLayout(this);

        lastServerCheckInLabel = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(lastServerCheckInLabel);
        lastServerCheckInLabel.setPreferredSize(new Dimension(15,15));
        this.add(lastServerCheckInLabel);

        this.add(ViewUtil.getSmallSeparator());

        warningLabel = new JLabel();
        this.add(warningLabel);

        LoginController.addLoginListener(this);

        setState(ServerState.NOT_CONNECTED);
    }

    public void loginEvent(LoginEvent evt) {
        if (evt.isLoggedIn()) {
            if (serverStateChecker != null) {
                serverStateChecker.cancel(true);
            }
            serverStateChecker = new ServerStateChecker(this,UPDATE_INTERVAL);
            serverStateChecker.execute();
        } else {
            setState(ServerState.NOT_CONNECTED);
            setLastServerCheckIn(null);
        }
    }

    private synchronized void setState(ServerState serverState) {

        ClientLogger.log(ServerStatusPanel.class, "Server state is " + serverState);

        if (lastServerCheckInLabel != null) {
            ImageIcon im = null;
            switch (serverState) {
                case NOT_CONNECTED:
                    im = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.WHITE);
                    warningLabel.setText("");
                    break;
                case SERVER_CONNECTED:
                    im = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.GREEN);
                    warningLabel.setText("");
                    break;
                case SERVER_IDLE:
                    im = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.ORANGE);
                    warningLabel.setText("Server Utility idle");
                    break;
                case SERVER_NEVER_CONNECTED:
                    im = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.RED);
                    warningLabel.setText("Server Utility not initialized");
                    break;
            }
            ImagePanel imagePanel = new ImagePanel(im.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH),15,15);
            lastServerCheckInLabel.removeAll();
            lastServerCheckInLabel.add(imagePanel);
        }

        this.updateUI();
    }

    private static int UPDATE_INTERVAL = 1000*60;

    private synchronized void setLastServerCheckIn(Date lastCheckIn) {
        if (lastServerCheckInLabel == null) { return; }
        String tip = "";
        if (lastCheckIn != null) {
            tip = "Last communication was at " + lastCheckIn.toLocaleString();
        }
        this.lastServerCheckInLabel.setToolTipText(tip);
        this.warningLabel.setToolTipText(tip);

        this.updateUI();
    }

    private enum ServerState { NOT_CONNECTED, SERVER_CONNECTED, SERVER_IDLE, SERVER_NEVER_CONNECTED };


    private static class ServerStateChecker extends SwingWorker
    {
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

                Date lastCheckIn = ServerLogQueryUtil.getDateOfLastServerLog();

                serverStatusPanel.setLastServerCheckIn(lastCheckIn);

                if (lastCheckIn == null) {
                    serverStatusPanel.setState(ServerState.SERVER_NEVER_CONNECTED);
                } else {

                    long elapsed = System.currentTimeMillis() - lastCheckIn.getTime();

                    if (elapsed > 1000*60*30) { //30 minutes
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
