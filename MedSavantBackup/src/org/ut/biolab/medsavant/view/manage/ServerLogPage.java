/*
 *    Copyright 2011 University of Toronto
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

import com.jidesoft.grid.SortableTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import org.ut.biolab.medsavant.controller.ThreadController;

import org.ut.biolab.medsavant.db.api.MedSavantDatabase;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.ServerLogTableSchema;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.util.DBUtil;
import org.ut.biolab.medsavant.db.util.query.AnnotationLogQueryUtil;
import org.ut.biolab.medsavant.db.util.query.AnnotationLogQueryUtil.Status;
import org.ut.biolab.medsavant.db.util.query.LogQueryUtil;
import org.ut.biolab.medsavant.view.component.SearchableTablePanel;
import org.ut.biolab.medsavant.view.component.SearchableTablePanel.DataRetriever;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
public class ServerLogPage extends SubSectionView {

    private static String CARDNAME_WAIT = "0";
    private static String CARDNAME_CLIENT = "1";
    private static String CARDNAME_ANNOTATION = "2";
    private static String CARDNAME_SERVER = "3";
    private JPanel panel;
    private JPanel menuPanel;
    private JPanel listPanel;
    private boolean clientTableRefreshed = false;
    private boolean serverTableRefreshed = false;
    private boolean annotationTableRefreshed = false;
    private SearchableTablePanel clientTable;
    private SearchableTablePanel serverTable;
    private SearchableTablePanel annotationTable;
    private static final List<String> clientColumnNames = Arrays.asList(new String[]{"User", "Type", "Description", "Time"});
    private static final List<String> serverColumnNames = Arrays.asList(new String[]{"Type", "Description", "Time"});
    private static final List<String> annotationsColumnNames = Arrays.asList(new String[]{"Project", "Reference", "Action", "Status", "Time", "Restart"});
    private static final List<Class> clientColumnClasses = Arrays.asList(new Class[]{String.class, String.class, String.class, String.class});
    private static final List<Class> serverColumnClasses = Arrays.asList(new Class[]{String.class, String.class, String.class});
    private static final List<Class> annotationsColumnClasses = Arrays.asList(new Class[]{String.class, String.class, String.class, String.class, String.class, JButton.class});
    private String currentCard;
    private WaitPanel waitPanel;

    public ServerLogPage(SectionView parent) {
        super(parent);
    }

    public String getName() {
        return "Logs";
    }

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
        JRadioButton b2 = new JRadioButton("Server");
        JRadioButton b3 = new JRadioButton("Annotations");

        bg.add(b1);
        bg.add(b2);
        bg.add(b3);

        listPanel = new JPanel();
        listPanel.setLayout(new CardLayout());

        listPanel.add(getWaitPanel(), CARDNAME_WAIT);
        listPanel.add(getClientCard(), CARDNAME_CLIENT);
        listPanel.add(getServerCard(), CARDNAME_SERVER);
        listPanel.add(getAnnotationCard(), CARDNAME_ANNOTATION);

        panel.add(menuPanel, BorderLayout.NORTH);
        panel.add(listPanel, BorderLayout.CENTER);

        b1.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                changeToCard(CARDNAME_CLIENT);

            }
        });
        b2.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                changeToCard(CARDNAME_SERVER);
            }
        });
        b3.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                changeToCard(CARDNAME_ANNOTATION);
            }
        });


        b3.setSelected(true);
        this.changeToCard(CARDNAME_ANNOTATION);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                refreshCurrentCard();
            }
        });

        menuPanel.add(Box.createHorizontalGlue());
        menuPanel.add(b3);
        menuPanel.add(b1);
        menuPanel.add(b2);
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
            public List<Object[]> retrieve(int start, int limit) {
                return retrieveClientData(start, limit);
            }
            public int getTotalNum() {
                try {
                    return LogQueryUtil.getClientLogSize();
                } catch (SQLException ex) {
                    return 0;
                }
            }
            public void retrievalComplete() {}
        };
        clientTable = new SearchableTablePanel(getName(), clientColumnNames, clientColumnClasses, new ArrayList<Integer>(), limit, retriever);
        p.add(clientTable, BorderLayout.CENTER);
        return p;
    }

    private JPanel getServerCard() {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        DataRetriever retriever = new DataRetriever(){
            public List<Object[]> retrieve(int start, int limit) {
                return retrieveServerData(start, limit);
            }
            public int getTotalNum() {
                try {
                    return LogQueryUtil.getServerLogSize();
                } catch (SQLException ex) {
                    return 0;
                }
            }    
            public void retrievalComplete() {}
        };
        serverTable = new SearchableTablePanel(getName(), serverColumnNames, serverColumnClasses, new ArrayList<Integer>(), limit, retriever);
        p.add(serverTable, BorderLayout.CENTER);
        return p;
    }

    private JPanel getAnnotationCard() {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        DataRetriever retriever = new DataRetriever(){
            public List<Object[]> retrieve(int start, int limit) {
                return retrieveAnnotationData(start, limit);
            }
            public int getTotalNum() {
                try {
                    return LogQueryUtil.getAnnotationLogSize();
                } catch (SQLException ex) {
                    return 0;
                }
            }      
            public void retrievalComplete() {}
        };
        annotationTable = new SearchableTablePanel(getName(), annotationsColumnNames, annotationsColumnClasses, new ArrayList<Integer>(), limit, retriever);
        annotationTable.getTable().getColumn("Restart").setCellRenderer(new JTableButtonRenderer());
        annotationTable.getTable().addMouseListener(new JTableButtonMouseListener(annotationTable.getTable()));
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
    public Component[] getBanner() {
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
            ResultSet rs = LogQueryUtil.getAnnotationLog(start, limit);
            v = new ArrayList<Object[]>();
            while (rs.next()) {

                Status status = AnnotationLogQueryUtil.intToStatus(rs.getInt(4));

                final int updateId = rs.getInt("update_id");
                JButton button = new JButton("Retry");
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        try {
                            AnnotationLogQueryUtil.setAnnotationLogStatus(updateId, Status.PENDING, DBUtil.getCurrentTimestamp());
                        } catch (SQLException ex) {
                            Logger.getLogger(ServerLogPage.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        refreshCurrentCard();
                    }
                });

                Object[] r = new Object[6];
                r[0] = rs.getString(1);
                r[1] = rs.getString(2);
                r[2] = AnnotationLogQueryUtil.intToAction(rs.getInt(3));
                r[3] = status;

                try {
                    r[4] = rs.getTimestamp(5);
                } catch (Exception e) {
                }

                if (status != Status.ERROR) {
                    r[5] = new JPanel();
                } else {
                    r[5] = button;
                }

                v.add(r);
            }
        } catch (Exception e){
            waitPanel.setComplete();
            waitPanel.setStatus("Problem getting log");
            showWaitPanel();
            Logger.getLogger(ServerLogPage.class.getName()).log(Level.SEVERE, null, e);
        }
        hideWaitPanel();
        return v;
    }
    
    private List<Object[]> retrieveServerData(int start, int limit){
        if(!currentCard.equals(CARDNAME_SERVER)) return new ArrayList<Object[]>();
        List<Object[]> v = null;
        waitPanel.setIndeterminate();
        waitPanel.setStatus("");
        showWaitPanel();
        try {
            ResultSet rs = LogQueryUtil.getServerLog(start, limit);
            v = new ArrayList<Object[]>();
            while (rs.next()) {
                TableSchema table = MedSavantDatabase.ServerlogTableSchema;
                v.add(new Object[] {
                    rs.getString(table.getFieldAlias(ServerLogTableSchema.COLUMNNAME_OF_EVENT)),
                    rs.getString(table.getFieldAlias(ServerLogTableSchema.COLUMNNAME_OF_DESCRIPTION)),
                    rs.getTimestamp(table.getFieldAlias(ServerLogTableSchema.COLUMNNAME_OF_TIMESTAMP))
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
    }
    
    private List<Object[]> retrieveClientData(int start, int limit){
        if(!currentCard.equals(CARDNAME_CLIENT)) return new ArrayList<Object[]>();
        List<Object[]> v = null;
        waitPanel.setIndeterminate();
        waitPanel.setStatus("");
        showWaitPanel();
        try {
            ResultSet rs = LogQueryUtil.getClientLog(start, limit);
            v = new ArrayList<Object[]>();
            while (rs.next()) {
                TableSchema table = MedSavantDatabase.ServerlogTableSchema;
                v.add(new Object[] {
                    rs.getString(table.getFieldAlias(ServerLogTableSchema.COLUMNNAME_OF_USER)),
                    rs.getString(table.getFieldAlias(ServerLogTableSchema.COLUMNNAME_OF_EVENT)),
                    rs.getString(table.getFieldAlias(ServerLogTableSchema.COLUMNNAME_OF_DESCRIPTION)),
                    rs.getTimestamp(table.getFieldAlias(ServerLogTableSchema.COLUMNNAME_OF_TIMESTAMP))
                });
            }
            if (Thread.currentThread().isInterrupted()) {
                return null;
            }           
        } catch (Exception e){
            waitPanel.setComplete();
            waitPanel.setStatus("Problem getting log");
            showWaitPanel();
            Logger.getLogger(ServerLogPage.class.getName()).log(Level.SEVERE, null, e);
        }
        hideWaitPanel();
        return v;
    }

    public class JTableButtonRenderer implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            if (value instanceof JButton) {
                JButton button = (JButton) value;
                if (isSelected) {
                    button.setForeground(table.getForeground());
                    button.setBackground(table.getSelectionBackground());
                } else {
                    button.setForeground(table.getForeground());
                    button.setBackground(UIManager.getColor("Button.background"));
                }
                return button;
            }
            return (Component) value;
        }
    }

    public class JTableButtonMouseListener extends MouseAdapter {

        private final JTable table;

        public JTableButtonMouseListener(JTable table) {
            this.table = table;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            int column = table.getColumnModel().getColumnIndexAtX(e.getX());
            int row = e.getY() / table.getRowHeight();

            if (row < table.getRowCount() && row >= 0 && column < table.getColumnCount() && column >= 0) {
                Object value = table.getValueAt(row, column);
                if (value instanceof JButton) {
                    ((JButton) value).doClick();
                }
            }
        }
    }
}
