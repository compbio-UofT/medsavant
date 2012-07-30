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

package org.ut.biolab.medsavant.view.manage;

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
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.AnnotationLog;
import org.ut.biolab.medsavant.model.GeneralLog;
import org.ut.biolab.medsavant.util.DataRetriever;
import org.ut.biolab.medsavant.util.ThreadController;
import org.ut.biolab.medsavant.view.component.SearchableTablePanel;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
public class ServerLogPage extends SubSectionView {
    private static final Log LOG = LogFactory.getLog(ServerLogPage.class);
    private static final String CARDNAME_WAIT = "0";
    private static final String CARDNAME_CLIENT = "1";
    private static final String CARDNAME_ANNOTATION = "2";
    private static final String CARDNAME_SERVER = "3";
    private static final String[] CLIENT_COLUMN_NAMES = new String[] { "User", "Type", "Description", "Time" };
    private static final Class[] CLIENT_COLUMN_CLASSES = new Class[] { String.class, String.class, String.class, String.class };
    private static final String[] ANNOTATIONS_COLUMN_NAMES = new String[] { "Project", "Reference", "Action", "Status", "Time", "User" };
    private static final Class[] ANNOTATIONS_COLUMN_CLASSES = new Class[] { String.class, String.class, String.class, String.class, String.class, String.class };

    private boolean clientTableRefreshed = false;
    private boolean serverTableRefreshed = false;
    private boolean annotationTableRefreshed = false;
    private String currentCard;

    private JPanel panel;
    private JPanel menuPanel;
    private JPanel listPanel;
    private SearchableTablePanel clientTable;
    private SearchableTablePanel serverTable;
    private SearchableTablePanel annotationTable;
    private WaitPanel waitPanel;

    public ServerLogPage(SectionView parent) {
        super(parent);
    }

    @Override
    public String getName() {
        return "Logs";
    }

    @Override
    public JPanel getView(boolean update) {
        if (panel == null) {
            setPanel();
        }
        return panel;
    }

    public void setPanel() {
        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        menuPanel = new JPanel();
        ViewUtil.applyHorizontalBoxLayout(menuPanel);

        ButtonGroup bg = new ButtonGroup();

        JRadioButton b1 = new JRadioButton("Client");
        //JRadioButton b2 = new JRadioButton("Server");
        JRadioButton b3 = new JRadioButton("Annotations");

        bg.add(b1);
        //bg.add(b2);
        bg.add(b3);

        listPanel = new JPanel();
        listPanel.setLayout(new CardLayout());

        listPanel.add(getWaitPanel(), CARDNAME_WAIT);
        listPanel.add(getClientCard(), CARDNAME_CLIENT);
        //listPanel.add(getServerCard(), CARDNAME_SERVER);
        listPanel.add(getAnnotationCard(), CARDNAME_ANNOTATION);

        panel.add(menuPanel, BorderLayout.NORTH);
        panel.add(listPanel, BorderLayout.CENTER);

        b1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                changeToCard(CARDNAME_CLIENT);

            }
        });
        /*b2.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                changeToCard(CARDNAME_SERVER);
            }
        });*/
        b3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                changeToCard(CARDNAME_ANNOTATION);
            }
        });


        b3.setSelected(true);
        this.changeToCard(CARDNAME_ANNOTATION);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                refreshCurrentCard();
            }
        });

        menuPanel.add(Box.createHorizontalGlue());
        menuPanel.add(b3);
        menuPanel.add(b1);
        //menuPanel.add(b2);
        menuPanel.add(refreshButton);
        menuPanel.add(Box.createHorizontalGlue());

    }

    private void refreshCurrentCard() {
        if (currentCard.equals(CARDNAME_CLIENT)) {
            refreshClientCard();
        } else if (currentCard.equals(CARDNAME_SERVER)) {
            refreshServerCard();
        } else if (currentCard.equals(CARDNAME_ANNOTATION)) {
            refreshAnnotationCard();
        }
    }

    private synchronized void changeToCard(String cardname) {

        CardLayout cl = (CardLayout) (listPanel.getLayout());
        cl.show(listPanel, cardname);
        this.currentCard = cardname;

        if (cardname.equals(CARDNAME_CLIENT)) {
            if (!clientTableRefreshed) {
                this.refreshClientCard();
                clientTableRefreshed = true;
            }
        } else if (cardname.equals(CARDNAME_SERVER)) {
            if (!serverTableRefreshed) {
                this.refreshServerCard();
                serverTableRefreshed = true;
            }
        } else if (cardname.equals(CARDNAME_ANNOTATION)) {
            if (!annotationTableRefreshed) {
                this.refreshAnnotationCard();
                annotationTableRefreshed = true;
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
        DataRetriever retriever = new DataRetriever(){
            @Override
            public List<Object[]> retrieve(int start, int limit) {
                return retrieveClientData(start, limit);
            }

            @Override
            public int getTotalNum() {
                try {
                    return MedSavantClient.LogManager.getClientLogSize(LoginController.sessionId);
                } catch (Exception ex) {
                    return 0;
                }
            }
            @Override
            public void retrievalComplete() {
            }
        };
        clientTable = new SearchableTablePanel(getName(), CLIENT_COLUMN_NAMES, CLIENT_COLUMN_CLASSES, new int[0], limit, retriever);
        p.add(clientTable, BorderLayout.CENTER);
        return p;
    }

    /*private JPanel getServerCard() {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        DataRetriever retriever = new DataRetriever(){
            public List<Object[]> retrieve(int start, int limit) {
                return retrieveServerData(start, limit);
            }
            public int getTotalNum() {
                try {
                    return MedSavantClient.LogQueryUtilAdapter.getServerLogSize(LoginController.sessionId);
                } catch (Exception ex) {
                    return 0;
                }
            }
            public void retrievalComplete() {}
        };
        serverTable = new SearchableTablePanel(getName(), serverColumnNames, serverColumnClasses, new ArrayList<Integer>(), limit, retriever);
        p.add(serverTable, BorderLayout.CENTER);
        return p;
    }*/

    private JPanel getAnnotationCard() {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        DataRetriever retriever = new DataRetriever<Object[]>() {
            @Override
            public List<Object[]> retrieve(int start, int limit) {
                return retrieveAnnotationData(start, limit);
            }

            @Override
            public int getTotalNum() {
                try {
                    return MedSavantClient.LogManager.getAnnotationLogSize(LoginController.sessionId);
                } catch (Exception ex) {
                    return 0;
                }
            }

            @Override
            public void retrievalComplete() {
            }
        };
        annotationTable = new SearchableTablePanel(getName(), ANNOTATIONS_COLUMN_NAMES, ANNOTATIONS_COLUMN_CLASSES, new int[0], limit, retriever);
        p.add(annotationTable, BorderLayout.CENTER);
        return p;
    }

    private void refreshClientCard() {
        clientTable.forceRefreshData();
    }

    private void refreshServerCard() {
        serverTable.forceRefreshData();
    }

    private void refreshAnnotationCard() {
        annotationTable.forceRefreshData();
    }

    @Override
    public Component[] getSubSectionMenuComponents() {
        return null;
    }

    @Override
    public void viewDidLoad() {
    }

    @Override
    public void viewDidUnload() {
        ThreadController.getInstance().cancelWorkers(getName());
    }

    private WaitPanel getWaitPanel() {
        waitPanel = new WaitPanel("Getting log...");
        return waitPanel;
    }


    private List<Object[]> retrieveAnnotationData(int start, int limit){
        if(!currentCard.equals(CARDNAME_ANNOTATION)) return new ArrayList<Object[]>();
        List<Object[]> v = null;
        waitPanel.setIndeterminate();
        waitPanel.setStatus("");
        showWaitPanel();


        try {
            List<AnnotationLog> logs = MedSavantClient.LogManager.getAnnotationLog(LoginController.sessionId, start, limit);
            v = new ArrayList<Object[]>();
            for(final AnnotationLog log : logs){


                Object[] r = new Object[6];
                r[0] = log.getProjectName();
                r[1] = log.getReferenceName();
                r[2] = log.getAction();
                r[3] = log.getStatus();
                r[4] = log.getTimestamp();
                r[5] = log.getUser();

                v.add(r);
            }

        } catch (Exception ex) {
            waitPanel.setComplete();
            waitPanel.setStatus("Problem getting log");
            showWaitPanel();
            LOG.error("Error retrieving annotation log.", ex);
        }

        hideWaitPanel();
        return v;
    }

    /*private List<Object[]> retrieveServerData(int start, int limit){
        if(!currentCard.equals(CARDNAME_SERVER)) return new ArrayList<Object[]>();
        List<Object[]> v = null;
        waitPanel.setIndeterminate();
        waitPanel.setStatus("");
        showWaitPanel();
        try {
            List<GeneralLog> logs = MedSavantClient.LogQueryUtilAdapter.getServerLog(LoginController.sessionId, start, limit);
            v = new ArrayList<Object[]>();
            for(GeneralLog log : logs) {
                v.add(new Object[] {
                    log.getEvent(),
                    log.getDescription(),
                    log.getTimestamp()
                });
            }
        } catch (Exception e){
            waitPanel.setComplete();
            waitPanel.setStatus("Problem getting log");
            showWaitPanel();
            Logger.getLogger(ServerLogPage.class.getName()).log(Level.SEVERE, null, e);
        }
        hideWaitPanel();
        return v;
    }*/

    private List<Object[]> retrieveClientData(int start, int limit){
        if(!currentCard.equals(CARDNAME_CLIENT)) return new ArrayList<Object[]>();
        List<Object[]> v = null;
        waitPanel.setIndeterminate();
        waitPanel.setStatus("");
        showWaitPanel();
        try {
            List<GeneralLog> logs = MedSavantClient.LogManager.getClientLog(LoginController.sessionId, start, limit);
            v = new ArrayList<Object[]>();
            for(GeneralLog log : logs) {
                v.add(new Object[] {
                    log.getUser(),
                    log.getEvent(),
                    log.getDescription(),
                    log.getTimestamp()
                });
            }
            if (Thread.currentThread().isInterrupted()) {
                return null;
            }
        } catch (Exception ex){
            waitPanel.setComplete();
            waitPanel.setStatus("Problem getting log");
            showWaitPanel();
            LOG.error("Error retrieving client log.", ex);
        }
        hideWaitPanel();
        return v;
    }
}
