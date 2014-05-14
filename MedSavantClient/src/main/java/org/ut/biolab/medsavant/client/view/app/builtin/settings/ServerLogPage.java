/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.view.app.builtin.settings;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.model.GeneralLog;
import org.ut.biolab.medsavant.client.util.DataRetriever;
import org.ut.biolab.medsavant.client.view.component.SearchableTablePanel;
import org.ut.biolab.medsavant.client.view.app.MultiSectionApp;
import org.ut.biolab.medsavant.client.view.app.AppSubSection;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.client.view.component.WaitPanel;

/**
 *
 * @author mfiume
 */
public class ServerLogPage extends AppSubSection {

    private static final Log LOG = LogFactory.getLog(ServerLogPage.class);
    private static final String CARDNAME_WAIT = "0";
    private static final String CARDNAME_SERVER = "1";
    private static final String[] CLIENT_COLUMN_NAMES = new String[]{"User", "Type", "Description", "Time"};
    private static final Class[] CLIENT_COLUMN_CLASSES = new Class[]{String.class, String.class, String.class, String.class};
    private static final String[] ANNOTATIONS_COLUMN_NAMES = new String[]{"Project", "Reference", "Action", "Status", "Time", "User"};
    private static final Class[] ANNOTATIONS_COLUMN_CLASSES = new Class[]{String.class, String.class, String.class, String.class, String.class, String.class};

    private boolean clientTableRefreshed = false;
    private boolean serverTableRefreshed = false;
    private boolean annotationTableRefreshed = false;
    private String currentCard;

    private JPanel view;
    private JPanel menuPanel;
    private JPanel listPanel;
    private SearchableTablePanel clientTable;
    private SearchableTablePanel serverTable;
    private SearchableTablePanel annotationTable;
    private WaitPanel waitPanel;

    public ServerLogPage(MultiSectionApp parent) {
        super(parent, "Logs");
    }

    @Override
    public JPanel getView() {
        if (view == null) {
            view = new JPanel();
            view.setLayout(new BorderLayout());

            menuPanel = new JPanel();
            ViewUtil.applyHorizontalBoxLayout(menuPanel);

            listPanel = new JPanel();
            listPanel.setLayout(new CardLayout());

            listPanel.add(getWaitPanel(), CARDNAME_WAIT);
            listPanel.add(getClientCard(), CARDNAME_SERVER);

            view.add(menuPanel, BorderLayout.NORTH);
            view.add(listPanel, BorderLayout.CENTER);

            changeToCard(CARDNAME_SERVER);

            JButton refreshButton = new JButton("Refresh");
            refreshButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    refreshCurrentCard();
                }
            });

            menuPanel.add(Box.createHorizontalGlue());
            menuPanel.add(refreshButton);
            menuPanel.add(Box.createHorizontalGlue());
        }
        return view;
    }

    private void refreshCurrentCard() {
        refreshClientCard();
    }

    private synchronized void changeToCard(String cardname) {

        CardLayout cl = (CardLayout) (listPanel.getLayout());
        cl.show(listPanel, cardname);
        this.currentCard = cardname;

        if (cardname.equals(CARDNAME_SERVER)) {
            if (!clientTableRefreshed) {
                this.refreshClientCard();
                clientTableRefreshed = true;
            }
        }
    }

    private synchronized void showWaitPanel() {
        CardLayout cl = (CardLayout) (listPanel.getLayout());
        cl.show(listPanel, CARDNAME_WAIT);
    }

    private synchronized void hideWaitPanel() {
        CardLayout cl = (CardLayout) (listPanel.getLayout());
        cl.show(listPanel, currentCard);
    }

    private int limit = 1000;

    private JPanel getClientCard() {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        DataRetriever retriever = new DataRetriever() {
            @Override
            public List<Object[]> retrieve(int start, int limit) {
                return retrieveClientData(start, limit);
            }

            @Override
            public int getTotalNum() {
                try {
                    return MedSavantClient.LogManager.getServerLogSize(LoginController.getSessionID());
                } catch (Exception ex) {
                    return 0;
                }
            }

            @Override
            public void retrievalComplete() {
            }
        };
        clientTable = new SearchableTablePanel(pageName, CLIENT_COLUMN_NAMES, CLIENT_COLUMN_CLASSES, new int[0], limit, retriever);
        clientTable.setChooseColumnsButtonVisible(false);
        clientTable.setExportButtonVisible(false);
        
        p.add(clientTable, BorderLayout.CENTER);
        return p;
    }

    private void refreshClientCard() {
        clientTable.forceRefreshData();
    }

    @Override
    public Component[] getSubSectionMenuComponents() {
        return null;
    }

    private WaitPanel getWaitPanel() {
        waitPanel = new WaitPanel("Getting log...");
        return waitPanel;
    }

    private List<Object[]> retrieveClientData(int start, int limit) {
        List<Object[]> v = null;
        waitPanel.setIndeterminate();
        waitPanel.setStatus("");
        showWaitPanel();
        try {
            List<GeneralLog> logs = MedSavantClient.LogManager.getServerLog(LoginController.getSessionID(), start, limit);
            v = new ArrayList<Object[]>();
            for (GeneralLog log : logs) {
                v.add(new Object[]{
                    log.getUser(),
                    log.getEvent(),
                    log.getDescription(),
                    log.getTimestamp()
                });
            }
            if (Thread.currentThread().isInterrupted()) {
                return null;
            }
        } catch (Exception ex) {
            waitPanel.setComplete();
            waitPanel.setStatus("Problem getting log");
            showWaitPanel();
            LOG.error("Error retrieving client log.", ex);
        }
        hideWaitPanel();
        return v;
    }
}
